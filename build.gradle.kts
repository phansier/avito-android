@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    /**
     * https://docs.gradle.org/current/userguide/base_plugin.html
     * base plugin added to add wiring on check->build tasks for detekt
     */
    base
    id("org.jetbrains.kotlin.jvm") apply false
    id("com.android.application") apply false
    id("io.gitlab.arturbosch.detekt")
    id("com.autonomousapps.dependency-analysis") apply false
    id("com.jfrog.bintray") version "1.8.4" apply false
}

/**
 * We use exact version to provide consistent environment and avoid build cache issues
 * (AGP tasks has artifacts from build tools)
 */
val buildTools = "29.0.3"
val javaVersion = JavaVersion.VERSION_1_8
val compileSdk = 29
val infraVersion: Provider<String> = providers.gradleProperty("infraVersion").forUseAtConfigurationTime()
val detektVersion: Provider<String> = providers.systemProperty("detektVersion").forUseAtConfigurationTime()
val artifactoryUrl = providers.gradleProperty("artifactoryUrl").forUseAtConfigurationTime()
val projectVersion = providers.gradleProperty("projectVersion").forUseAtConfigurationTime()
val kotlinVersion = providers.systemProperty("kotlinVersion").forUseAtConfigurationTime()
val androidGradlePluginVersion = providers.systemProperty("androidGradlePluginVersion").forUseAtConfigurationTime()
val publishToArtifactoryTask = tasks.register<Task>("publishToArtifactory") {
    group = "publication"
    doFirst {
        requireNotNull(artifactoryUrl.orNull) {
            "Property artifactoryUrl is required for publishing"
        }
    }
}

val publishReleaseTaskName = "publishRelease"

if (gradle.startParameter.taskNames.contains("buildHealth")) {
    // Reasons to disabling by default:
    // The plugin schedules heavy LocateDependenciesTask tasks even without analysis
    apply(plugin = "com.autonomousapps.dependency-analysis")
}

dependencies {
    add("detektPlugins", "io.gitlab.arturbosch.detekt:detekt-formatting:${detektVersion.get()}")
}

subprojects {

    plugins.withType<AppPlugin> {
        configure<BaseExtension> {
            packagingOptions {
                pickFirst("META-INF/*.kotlin_module")
            }
        }
    }

    plugins.withType<LibraryPlugin> {
        val libraryExtension = this@subprojects.extensions.getByType<LibraryExtension>()

        val sourcesTask = tasks.register<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(libraryExtension.sourceSets["main"].java.srcDirs)
        }

        val publishingVariant = "release"

        plugins.withType<MavenPublishPlugin> {
            libraryExtension.libraryVariants
                .matching { it.name == publishingVariant }
                .whenObjectAdded {
                    extensions.getByType<PublishingExtension>().apply {
                        publications {
                            register<MavenPublication>(publishingVariant) {
                                from(components.getAt(publishingVariant))
                                artifact(sourcesTask.get())
                            }
                        }
                    }
//                    configureBintray(publishingVariant)
                }
        }
    }

    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
            sourceSets {
                named("main").configure { java.srcDir("src/main/kotlin") }
                named("androidTest").configure { java.srcDir("src/androidTest/kotlin") }
                named("test").configure { java.srcDir("src/test/kotlin") }
            }

            buildToolsVersion(buildTools)
            compileSdkVersion(compileSdk)

            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }

            defaultConfig {
                minSdkVersion(21)
                targetSdkVersion(28)
            }

            lintOptions {
                isAbortOnError = false
                isWarningsAsErrors = true
                textReport = true
                isQuiet = true
            }
        }
    }

    // todo more precise configuration for gradle plugins, no need for gradle testing in common kotlin modules
    plugins.withId("kotlin") {

        extensions.getByType<JavaPluginExtension>().run {
            withSourcesJar()
        }

        dependencies {
            add("testImplementation", gradleTestKit())
        }
    }

    plugins.withId("java-test-fixtures") {
        dependencies {
//            add("testFixturesImplementation", Dependencies.Test.junitJupiterApi)
//            add("testFixturesImplementation", Dependencies.Test.truth)
        }
    }

    plugins.withType<JavaGradlePluginPlugin> {
        extensions.getByType<GradlePluginDevelopmentExtension>().run {
            isAutomatedPublishing = false
        }
    }

//    plugins.withType<MavenPublishPlugin> {
//        extensions.getByType<PublishingExtension>().run {
//
//            publications {
//                // todo should not depend on ordering
//                if (plugins.hasPlugin("kotlin")) {
//                    val publicationName = "maven"
//
//                    register<MavenPublication>(publicationName) {
//                        from(components["java"])
//                        afterEvaluate {
//                            artifactId = this@subprojects.getOptionalExtra("artifact-id") ?: this@subprojects.name
//                        }
//                    }
//
//                    afterEvaluate {
//                        configureBintray(publicationName)
//                    }
//                }
//            }
//
//            repositories {
//                if (!artifactoryUrl.orNull.isNullOrBlank()) {
//                    maven {
//                        name = "artifactory"
//                        setUrl("${artifactoryUrl.orNull}/libs-release-local")
//                        credentials {
//                            username = project.getOptionalExtra("avito.artifactory.user")
//                            password = project.getOptionalExtra("avito.artifactory.password")
//                        }
//                    }
//                }
//            }
//        }
//
//        if (!artifactoryUrl.orNull.isNullOrBlank()) {
//            publishToArtifactoryTask.configure {
//                dependsOn(tasks.named("publishAllPublicationsToArtifactoryRepository"))
//            }
//        }
//    }
}

val Project.sourceSets: SourceSetContainer
    get() = (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

val SourceSetContainer.main: NamedDomainObjectProvider<SourceSet>
    get() = named<SourceSet>("main")

fun Project.getOptionalExtra(key: String): String? {
    return if (extra.has(key)) {
        (extra[key] as? String)?.let { if (it.isBlank()) null else it }
    } else {
        null
    }
}

//fun Project.configureBintray(vararg publications: String) {
//    extensions.findByType<BintrayExtension>()?.run {
//
//        // todo fail fast with meaningful error message
//        user = getOptionalExtra("avito.bintray.user")
//        key = getOptionalExtra("avito.bintray.key")
//
//        setPublications(*publications)
//
//        dryRun = false
//        publish = true
//        // You can use override for inconsistently uploaded artifacts
//        // Examples of issues:
//        // - NoHttpResponseException: api.bintray.com:443 failed to respond
//        //   (https://github.com/bintray/gradle-bintray-plugin/issues/325)
//        // - Could not upload to 'https://api.bintray.com/...':
//        //   HTTP/1.1 405 Not Allowed 405 Not Allowed405 Not Allowednginx
//        override = false
//        pkg(
//            closureOf<PackageConfig> {
//                repo = "maven"
//                userOrg = "avito"
//                name = "avito-android"
//                setLicenses("mit")
//                vcsUrl = "https://github.com/avito-tech/avito-android.git"
//
//                version(
//                    closureOf<VersionConfig> {
//                        name = finalProjectVersion
//                    }
//                )
//            }
//        )
//    }
//
//    tasks.named(publishReleaseTaskName).configure {
//        dependsOn(tasks.named("bintrayUpload"))
//    }
//}

tasks.withType<Wrapper> {
    // sources unavailable with BIN until https://youtrack.jetbrains.com/issue/IDEA-231667 resolved
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "6.8.2"
}

val detektAll = tasks.register<Detekt>("detektAll") {
    description = "Runs over whole code base without the starting overhead for each module."
    parallel = true
    setSource(files(projectDir))

    /**
     * About config:
     * yaml is a copy of https://github.com/detekt/detekt/blob/master/detekt-core/src/main/resources/default-detekt-config.yml
     * all rules are disabled by default, enabled one by one
     */
    config.setFrom(files(project.rootDir.resolve("detekt.yml")))
    buildUponDefaultConfig = false

    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    reports {
        xml.enabled = false
        html.enabled = false
    }
}

tasks.named("check").dependsOn(detektAll)
