package com.avito.android.test.report.troubleshooting.dump

// TODO: merge with MainLooperQueueDumper
interface MainLooperHistoryDumper {
    fun getLastIdleMs(): Long
    fun start()
    fun stop()
}
