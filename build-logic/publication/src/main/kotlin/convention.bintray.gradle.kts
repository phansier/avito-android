@file:Suppress("UnstableApiUsage")

import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig

plugins {
    id("com.jfrog.bintray")
    id("convention.publish")
}

val projectVersion: Provider<String> =
    providers.systemProperty("avito.project.version")
        .forUseAtConfigurationTime()
        .orElse(
            providers.systemProperty("projectVersion")
                .forUseAtConfigurationTime()
        )

group = "com.avito.android"
version = projectVersion.get()

val publishReleaseTaskName = "publishRelease"

/**
 * https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html#BuildScriptInteractionwithTeamCity-ReportingBuildNumber
 */
val teamcityPrintVersionTask: TaskProvider<Task> = tasks.register("teamcityPrintReleasedVersion") {
    group = "publication"
    description = "Prints teamcity service message to display released version as build number"

    doLast {
        logger.lifecycle("##teamcity[buildNumber '${projectVersion.get()}']")
    }
}

tasks.register(publishReleaseTaskName) {
    group = "publication"
    finalizedBy(teamcityPrintVersionTask)
}

val bomBuild = gradle.includedBuild("platforms")

afterEvaluate {
    val registeredPublications = publishing.publications.names

    tasks.named(publishReleaseTaskName).configure {
        dependsOn(tasks.named("bintrayUpload"))
        dependsOn(bomBuild.task(":bintrayUpload"))
    }

    bintray {
        // todo fail fast with meaningful error message
        user = getOptionalExtra("avito.bintray.user")
        key = getOptionalExtra("avito.bintray.key")

        setPublications(*registeredPublications.toTypedArray())

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
            closureOf<PackageConfig> {
                repo = "maven"
                userOrg = "avito"
                name = "avito-android"
                setLicenses("mit")
                vcsUrl = "https://github.com/avito-tech/avito-android.git"

                version(
                    closureOf<VersionConfig> {
                        name = projectVersion.get()
                    }
                )
            }
        )
    }
}

fun Project.getOptionalExtra(key: String): String? {
    return if (extra.has(key)) {
        (extra[key] as? String)?.let { if (it.isBlank()) null else it }
    } else {
        null
    }
}
