package com.avito.android

abstract class ExternalLibrariesExtension {

    // todo move to separate extension
    val androidGradlePluginVersion = "4.1.2"
    val buildToolsVersion = "29.0.3"
    val compileSdkVersion = 29

    val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib"
    val androidGradlePlugin = "com.android.tools.build:gradle:$androidGradlePluginVersion"
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin"
    val bintrayPlugin = "com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5"
    val truth = "com.google.truth:truth:1.0"
    val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Dependencies.Versions.junit5}"
    val junitPlatformRunner = "org.junit.platform:junit-platform-runner:${Dependencies.Versions.junit5Platform}"
    val junitPlatformLauncher = "org.junit.platform:junit-platform-launcher:${Dependencies.Versions.junit5Platform}"
    val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Dependencies.Versions.junit5}"
}
