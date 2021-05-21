package com.avito.instrumentation.internal.report.listener

internal class FailingLogcatProcessor : LogcatProcessor {

    override suspend fun process(logcat: List<String>?, isUploadNeeded: Boolean): String {
        return "logcat not available"
    }
}