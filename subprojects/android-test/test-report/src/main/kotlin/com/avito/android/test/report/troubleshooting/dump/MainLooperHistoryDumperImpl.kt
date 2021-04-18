package com.avito.android.test.report.troubleshooting.dump

import android.os.Build
import android.os.Looper
import android.os.MessageQueue
import com.avito.android.util.executeMethod
import com.avito.time.TimeProvider

class MainLooperHistoryDumperImpl(
    private val timeProvider: TimeProvider
) : MainLooperHistoryDumper {

    @Volatile
    private var lastIdleMs: Long = -1

    private val idleHandler = MessageQueue.IdleHandler {
        lastIdleMs = timeProvider.nowInMillis()
        true
    }

    override fun getLastIdleMs(): Long {
        return lastIdleMs
    }

    override fun start() {
        getMessageQueue().addIdleHandler(idleHandler)
    }

    override fun stop() {
        getMessageQueue().removeIdleHandler(idleHandler)
    }

    private fun getMessageQueue(): MessageQueue {
        return if (Build.VERSION.SDK_INT >= 23) {
            Looper.getMainLooper().queue
        } else {
            Looper.getMainLooper().executeMethod("getQueue") as MessageQueue
        }
    }
}
