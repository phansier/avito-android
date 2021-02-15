@file:Suppress("UnstableApiUsage")

rootProject.name = "infra-bom"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.jfrog.bintray") {
                useVersion(providers.systemProperty("bintrayVersion").forUseAtConfigurationTime().get())
            }
        }
    }
}
