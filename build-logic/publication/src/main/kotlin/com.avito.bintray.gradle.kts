import com.jfrog.bintray.gradle.BintrayExtension

plugins {
//    id("com.avito.versioning")
    id("com.jfrog.bintray")
}

//val publishReleaseTaskName = "publishRelease"
//
//tasks.named(publishReleaseTaskName).configure {
//    dependsOn(tasks.named("bintrayUpload"))
//}

bintray {
    // todo fail fast with meaningful error message
    user = getOptionalExtra("avito.bintray.user")
    key = getOptionalExtra("avito.bintray.key")

    dryRun = false
    publish = true
    // You can use override for inconsistently uploaded artifacts
    // Examples of issues:
    // - NoHttpResponseException: api.bintray.com:443 failed to respond
    //   (https://github.com/bintray/gradle-bintray-plugin/issues/325)
    // - Could not upload to 'https://api.bintray.com/...':
    //   HTTP/1.1 405 Not Allowed 405 Not Allowed405 Not Allowednginx
    override = false
    pkg(
        closureOf<BintrayExtension.PackageConfig> {
            repo = "maven"
            userOrg = "avito"
            name = "avito-android"
            setLicenses("mit")
            vcsUrl = "https://github.com/avito-tech/avito-android.git"

            version(
                closureOf<BintrayExtension.VersionConfig> {
                    name = project.version.toString()
                }
            )
        }
    )
}

fun Project.getOptionalExtra(key: String): String? {
    return if (extra.has(key)) {
        (extra[key] as? String)?.let { if (it.isBlank()) null else it }
    } else {
        null
    }
}
