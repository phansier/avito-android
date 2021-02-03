import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.KubernetesCredentials.Service
import com.avito.utils.gradle.kubernetesCredentials

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.avito.android.instrumentation-tests")
}

android {

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
        testInstrumentationRunner = "com.avito.android.test.app.core.TestAppRunner"

        // TODO: describe in docs that they are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments(
            mapOf(
                "planSlug" to "AndroidTestApp"
            )
        )
    }

    testBuildType = "debug"

    /**
     * Disable all buildTypes except testing
     * to avoid confusing errors in IDE if wrong build variant is selected
     */
    variantFilter {
        if (name != testBuildType) {
            ignore = true
            logger.debug("Build variant $name is omitted for module: $path")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

/**
 * delegateClosureOf used because kotlin dsl accessor
 * `fun Project.dependencies(configuration: DependencyHandlerScope.() -> Unit)`
 * is somehow unavailable for this and only this module.
 * It probably related to our custom plugins applied, but don't know how to debug this issue right now
 */
dependencies(
    delegateClosureOf<DependencyHandler> {

        implementation(Dependencies.appcompat)
        implementation(Dependencies.material)
        implementation(Dependencies.playServicesMaps)
        implementation(Dependencies.recyclerView)

        implementation(project(":subprojects:android-lib:proxy-toast"))

        androidTestImplementation(Dependencies.AndroidTest.rules)
        androidTestImplementation(Dependencies.AndroidTest.runner)
        androidTestImplementation(Dependencies.funktionaleTry)
        androidTestImplementation(Dependencies.gson)
        androidTestImplementation(Dependencies.kotson)
        androidTestImplementation(Dependencies.okhttp)
        androidTestImplementation(Dependencies.okhttpLogging)
        androidTestImplementation(Dependencies.sentry)
        androidTestImplementation(Dependencies.Test.junit)
        androidTestImplementation(Dependencies.Test.mockitoCore)
        androidTestImplementation(Dependencies.Test.okhttpMockWebServer)
        androidTestImplementation(Dependencies.Test.truth)

        androidTestImplementation(project(":subprojects:android-test:test-inhouse-runner"))
        androidTestImplementation(project(":subprojects:android-test:test-report"))
        androidTestImplementation(project(":subprojects:android-test:toast-rule"))
        androidTestImplementation(project(":subprojects:android-test:ui-testing-core"))
        androidTestImplementation(project(":subprojects:common:file-storage"))
        androidTestImplementation(project(":subprojects:common:junit-utils"))
        androidTestImplementation(project(":subprojects:common:okhttp"))
        androidTestImplementation(project(":subprojects:common:report-viewer"))
        androidTestImplementation(project(":subprojects:common:test-annotations"))
        androidTestImplementation(project(":subprojects:common:time"))

        androidTestUtil(Dependencies.AndroidTest.orchestrator)
    }
)

extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {

    // todo make these params optional features in plugin
    reportApiUrl = project.getOptionalStringProperty("avito.report.url") ?: "http://stub"
    reportApiFallbackUrl = project.getOptionalStringProperty("avito.report.fallbackUrl") ?: "http://stub"
    reportViewerUrl = project.getOptionalStringProperty("avito.report.viewerUrl") ?: "http://stub"
    registry = project.getOptionalStringProperty("avito.registry", "registry") ?: "registry"
    sentryDsn = project.getOptionalStringProperty("avito.instrumentaion.sentry.dsn")
        ?: "http://stub-project@stub-host/0"
    slackToken = project.getOptionalStringProperty("avito.slack.token") ?: "stub"
    fileStorageUrl = project.getOptionalStringProperty("avito.fileStorage.url") ?: "http://stub"

    logcatTags = setOf(
        "UITestRunner:*",
        "ActivityManager:*",
        "ReportTestListener:*",
        "StorageJsonTransport:*",
        "TestReport:*",
        "VideoCaptureListener:*",
        "TestRunner:*",
        "SystemDialogsManager:*",
        "AndroidJUnitRunner:*",
        "ito.android.de:*", // по этому тэгу система пишет логи об использовании hidden/restricted api https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
        "*:E"
    )

    instrumentationParams = mapOf(
        "videoRecording" to "failed",
        "jobSlug" to "FunctionalTests"
    )

    val runAllFilterName = "runAll"
    filters.register(runAllFilterName)

    filters.register("dynamicFilter") {
        val includeAnnotation: String? = project.getOptionalStringProperty("includeAnnotation", true)
        if (includeAnnotation != null) {
            fromSource.includeByAnnotations(setOf(includeAnnotation))
        }
        val includePrefix: String? = project.getOptionalStringProperty("includePrefix", true)
        if (includePrefix != null) {
            fromSource.includeByPrefixes(setOf(includePrefix))
        }
    }

    filters.register("ci") {
        fromSource.excludeFlaky = true
    }

    val defaultFilter = "default"
    val customFilter: String = project.getOptionalStringProperty("customFilter", defaultFilter)

    configurationsContainer.register("Local") {
        reportSkippedTests = true
        filter = customFilter

        targetsContainer.register("api28") {
            deviceName = "API28"

            scheduling {
                quota {
                    retryCount = 1
                    minimumSuccessCount = 1
                }

                testsCountBasedReservation {
                    device = Device.LocalEmulator.device(28, "Android_SDK_built_for_x86_64")
                    maximum = 1
                    minimum = 1
                    testsPerEmulator = 1
                }
            }
        }
    }

    val credentials = project.kubernetesCredentials
    if (credentials is Service || credentials is KubernetesCredentials.Config) {

        afterEvaluate {
            tasks.named("check").dependsOn(tasks.named("instrumentationUi"))
        }

        val registry = project.providers.gradleProperty("avito.registry")
            .forUseAtConfigurationTime()
            .orNull

        val emulator22 = CloudEmulator(
            name = "api22",
            api = 22,
            model = "Android_SDK_built_for_x86",
            image = "${emulatorImage(registry, 22)}:740eb9a948",
            cpuCoresRequest = "1",
            cpuCoresLimit = "1.3",
            memoryLimit = "4Gi"
        )

        val emulator29 = CloudEmulator(
            name = "api29",
            api = 29,
            model = "Android_SDK_built_for_x86_64",
            image = "${emulatorImage(registry, 29)}:915c1f20be",
            cpuCoresRequest = "1",
            cpuCoresLimit = "1.3",
            memoryLimit = "4Gi"
        )

        configurationsContainer.register("ui") {
            reportSkippedTests = true
            filter = customFilter

            targetsContainer.register("api22") {
                deviceName = "API22"

                scheduling {
                    quota {
                        retryCount = 1
                        minimumSuccessCount = 1
                    }

                    testsCountBasedReservation {
                        device = emulator22
                        maximum = 50
                        minimum = 2
                        testsPerEmulator = 3
                    }
                }
            }

            targetsContainer.register("api29") {
                deviceName = "API29"

                scheduling {
                    quota {
                        retryCount = 1
                        minimumSuccessCount = 1
                    }

                    testsCountBasedReservation {
                        device = emulator29
                        maximum = 50
                        minimum = 2
                        testsPerEmulator = 3
                    }
                }
            }
        }

        configurationsContainer.register("uiDebug") {
            reportSkippedTests = false
            enableDeviceDebug = true
            filter = customFilter

            targetsContainer.register("api29") {
                deviceName = "API29"

                scheduling {
                    quota {
                        retryCount = 1
                        minimumSuccessCount = 1
                    }

                    testsCountBasedReservation {
                        device = emulator29
                        maximum = 1
                        minimum = 1
                        testsPerEmulator = 1
                    }
                }
            }
        }
    }
}

// todo registry value not respected here, it's unclear how its used (in fact concatenated in runner)
// todo pass whole image, and not registry
fun emulatorImage(registry: String?, api: Int): String {
    return if (registry.isNullOrBlank()) {
        "avitotech/android-emulator-$api"
    } else {
        "android/emulator-$api"
    }
}
