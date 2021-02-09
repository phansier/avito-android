plugins {
    `java-platform`
    id("com.example.libraries")
}

// Allow dependencies for dependencies to other platforms (BOMs)
javaPlatform.allowDependencies()

group = "com.example.idiomatic.gradle"

@Suppress("UnstableApiUsage")
val kotlinVersion: Provider<String> = providers
    .systemProperty("kotlinVersion")
    .forUseAtConfigurationTime()

dependencies {
    constraints {
        api(libs.kotlinStdlib) { version { strictly(kotlinVersion.get()) } }
        api(libs.kotlinPlugin) { version { strictly(kotlinVersion.get()) } }
    }
}
