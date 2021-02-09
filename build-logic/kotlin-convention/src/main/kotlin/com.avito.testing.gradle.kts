import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    id("com.example.libraries")
    kotlin("jvm")
}

dependencies {
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.truth)

    testRuntimeOnly(libs.junitPlatformRunner)
    testRuntimeOnly(libs.junitPlatformLauncher)
    testRuntimeOnly(libs.junitJupiterEngine)

    if (project.name != "truth-extensions") {
        testImplementation(project(":subprojects:common:truth-extensions"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = 8
    failFast = true

    /**
     * fix for retrofit `WARNING: Illegal reflective access by retrofit2.Platform`
     * see square/retrofit/issues/3341
     */
    jvmArgs = listOf("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED")

    systemProperty("rootDir", "${project.rootDir}")
    systemProperty(
        "kotlinVersion",
        plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion
    )
    systemProperty("compileSdkVersion", libs.compileSdkVersion)
    systemProperty("buildToolsVersion", libs.buildToolsVersion)
    systemProperty("androidGradlePluginVersion", libs.androidGradlePluginVersion)

    /**
     * IDEA добавляет специальный init script, по нему понимаем что запустили в IDE
     * используется в `:test-project`
     */
    systemProperty(
        "isInvokedFromIde",
        gradle.startParameter.allInitScripts.find { it.name.contains("ijtestinit") } != null
    )

    systemProperty("isTest", true)

    systemProperty(
        "junit.jupiter.execution.timeout.default",
        TimeUnit.MINUTES.toSeconds(10)
    )

    val testProperties = listOf(
        "avito.kubernetes.url",
        "avito.kubernetes.token",
        "avito.kubernetes.cert",
        "avito.kubernetes.namespace",
        "avito.slack.test.channel",
        "avito.slack.test.token",
        "avito.slack.test.workspace",
        "avito.elastic.endpoint",
        "avito.elastic.indexpattern",
        "teamcityBuildId"
    )
    testProperties.forEach { key ->
        val property = if (project.hasProperty(key)) {
            project.property(key)!!.toString()
        } else {
            ""
        }
        systemProperty(key, property)
    }
}
