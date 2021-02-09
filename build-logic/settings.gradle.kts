rootProject.name = "build-logic"

include("kotlin-convention")
include("android-convention")
include("publication")
include("versioning")

pluginManagement {
    repositories {
        jcenter()
        gradlePluginPortal()
    }
}
