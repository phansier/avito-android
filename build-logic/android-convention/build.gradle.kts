plugins {
    `kotlin-dsl`
    id("com.example.libraries")
}

group = "com.example.buildlogic"

dependencies {
    implementation(project(":versioning"))
    implementation(project(":kotlin-convention"))
    implementation("com.example.buildlogic:libraries")
    implementation(platform("com.example.idiomatic.gradle:platform"))
    implementation(libs.kotlinPlugin)
    implementation(libs.androidGradlePlugin)
}

repositories {
    mavenCentral()
    google()
    jcenter()
}
