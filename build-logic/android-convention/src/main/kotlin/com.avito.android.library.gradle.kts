plugins {
//    id("com.avito.versioning")
    id("com.example.libraries")
    id("com.android.library")
    id("com.avito.kotlin")
    kotlin("android")
}

dependencies {
    implementation(platform("com.example.idiomatic.gradle:platform"))
}
