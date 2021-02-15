@file:Suppress("UnstableApiUsage")

plugins {
    `java-platform`
    id("convention.libraries")
    `maven-publish`
    id("com.jfrog.bintray")
}

// Allow dependencies for dependencies to other platforms (BOMs)
javaPlatform.allowDependencies()

group = "com.avito.android"

// todo reuse with `convention.publish`
val projectVersion: Provider<String> =
    providers.systemProperty("avito.project.version")
        .forUseAtConfigurationTime()
        .orElse(
            providers.systemProperty("projectVersion")
                .forUseAtConfigurationTime()
        )

// todo find a way to get it from publication info
val publishedModules = listOf(
    "android",
    "android-log",
    "artifactory-app-backup",
    "bitbucket",
    "build-checks",
    "build-environment",
    "build-failer",
    "build-metrics",
    "build-on-target",
    "build-properties",
    "build-trace",
    "build-verdict",
    "build-verdict-tasks-api",
    "buildchecks",
    "cd",
    "ci-logger",
    "code-ownership",
    "composite-exception",
    "coroutines-extension",
    "dependencies-lint",
    "design-screenshots",
    "docker",
    "elastic",
    "elastic-logger",
    "feature-toggles",
    "file-storage",
    "files",
    "git",
    "gradle-extensions",
    "gradle-logger",
    "gradle-profile",
    "graphite",
    "graphite-config",
    "impact",
    "impact-shared",
    "impact-shared-test-fixtures",
    "instrumentation-changed-tests-finder",
    "instrumentation-test-impact-analysis",
    "instrumentation-tests",
    "instrumentation-tests-default-config",
    "instrumentation-tests-dex-loader",
    "junit-utils",
    "keep-for-testing",
    "kotlin-ast-parser",
    "kotlin-dsl-support",
    "kotlin-root",
    "kubernetes",
    "lint-report",
    "logger",
    "logging",
    "math",
    "module-types",
    "namespaced-resources-fixer",
    "okhttp",
    "percent",
    "performance",
    "pre-build",
    "process",
    "prosector",
    "proxy-toast",
    "qapps",
    "random-utils",
    "report-viewer",
    "resource-manager-exceptions",
    "resources",
    "retrace",
    "robolectric",
    "room-config",
    "runner-client",
    "runner-device-provider",
    "runner-fake",
    "runner-report",
    "runner-service",
    "runner-shared",
    "runner-shared-test",
    "runner-stub",
    "rx3-idler",
    "sentry",
    "sentry-config",
    "sentry-logger",
    "signer",
    "slack",
    "slf4j-logger",
    "snackbar-proxy",
    "snackbar-rule",
    "statsd",
    "statsd-config",
    "teamcity",
    "teamcity-common",
    "test-annotations",
    "test-inhouse-runner",
    "test-instrumentation-runner",
    "test-okhttp",
    "test-project",
    "test-report",
    "test-summary",
    "throwable-utils",
    "time",
    "tms",
    "toast-rule",
    "trace-event",
    "truth-extensions",
    "ui-test-bytecode-analyzer",
    "ui-testing-core",
    "ui-testing-maps",
    "upload-cd-build-result",
    "upload-to-googleplay",
    "utils",
    "waiter",
    "websocket-reporter",
    "worker"
)

dependencies {
    api(platform(libs.kotlinBom)) { (this as ExternalDependency).version { prefer(libs.kotlinVersion) } }
    api(platform(libs.okhttpBom)) { (this as ExternalDependency).version { prefer(libs.okhttpVersion) } }

    constraints {
        publishedModules.forEach { module ->
            api("com.avito.android:$module:${projectVersion.get()}")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven-bom") {
            from(components["javaPlatform"])
            artifactId = "infra-bom"
            version = projectVersion.get()
        }
    }

    val artifactoryUrl: Provider<String> = providers.gradleProperty("artifactoryUrl")
        .forUseAtConfigurationTime()

    repositories {
        if (!artifactoryUrl.orNull.isNullOrBlank()) {
            maven {
                name = "artifactory"
                setUrl("${artifactoryUrl.orNull}/libs-release-local")
                credentials {
                    username = project.getOptionalExtra("avito.artifactory.user")
                    password = project.getOptionalExtra("avito.artifactory.password")
                }
            }
        }
    }
}

// todo find a way to get it from publication info
bintray {
    // todo fail fast with meaningful error message
    user = getOptionalExtra("avito.bintray.user")
    key = getOptionalExtra("avito.bintray.key")

    setPublications("maven-bom")

    dryRun = false
    publish = true
    // You can use override for inconsistently uploaded artifacts
    // Examples of issues:
    // - NoHttpResponseException: api.bintray.com:443 failed to respond
    //   (https://github.com/bintray/gradle-bintray-plugin/issues/325)
    // - Could not upload to 'https://api.bintray.com/...':
    //   HTTP/1.1 405 Not Allowed 405 Not Allowed405 Not Allowednginx
    override = false

    pkg(
        closureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
            repo = "maven"
            userOrg = "avito"
            name = "avito-android"
            setLicenses("mit")
            vcsUrl = "https://github.com/avito-tech/avito-android.git"

            version(
                closureOf<com.jfrog.bintray.gradle.BintrayExtension.VersionConfig> {
                    name = projectVersion.get()
                }
            )
        }
    )
}

fun Project.getOptionalExtra(key: String): String? {
    return if (extra.has(key)) {
        (extra[key] as? String)?.let { if (it.isBlank()) null else it }
    } else {
        null
    }
}
