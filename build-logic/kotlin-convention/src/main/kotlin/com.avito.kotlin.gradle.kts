import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
//    id("com.avito.versioning")
    id("com.example.libraries")
    id("com.avito.testing")
    kotlin("jvm")
}

dependencies {
    implementation(platform("com.example.idiomatic.gradle:platform"))
    implementation(libs.kotlinStdlib)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        allWarningsAsErrors = true
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}
