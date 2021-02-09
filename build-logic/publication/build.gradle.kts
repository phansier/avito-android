plugins {
    `kotlin-dsl`
    id("com.example.libraries")
}

group = "com.example.buildlogic"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(libs.kotlinPlugin)
    implementation(libs.bintrayPlugin)
    implementation(project(":versioning"))
}
