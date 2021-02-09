val finalProjectVersion: String = System.getProperty("avito.project.version").let { env ->
    if (env.isNullOrBlank()) "2021.5" else env
}

group = "com.avito.android"
version = rootProject.layout.projectDirectory.file("version.txt").asFile.let {
    // Note: it is important to handle the file-not-exists case as this code is executed during script compilation (on an artificial project)
    if (it.exists()) it.readLines().first().trim() else "unknown"
}

/**
 * https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html#BuildScriptInteractionwithTeamCity-ReportingBuildNumber
 */
val teamcityPrintVersionTask = tasks.register("teamcityPrintReleasedVersion") {
    group = "publication"
    description = "Prints teamcity service message to display released version as build number"

    doLast {
        logger.lifecycle("##teamcity[buildNumber '$finalProjectVersion']")
    }
}

//tasks.register(publishReleaseTaskName) {
//    group = "publication"
//    finalizedBy(teamcityPrintVersionTask)
//}
