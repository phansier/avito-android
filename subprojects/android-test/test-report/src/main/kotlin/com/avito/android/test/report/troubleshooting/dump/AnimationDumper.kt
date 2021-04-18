package com.avito.android.test.report.troubleshooting.dump

import android.animation.ValueAnimator
import android.os.Build
import android.os.Looper
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.Result
import com.avito.android.util.getFieldValue
import com.avito.android.util.getStaticFieldValue

object AnimationDumper {

    fun getDump(): String {
        return try {
            when (Build.VERSION.SDK_INT) {
                in 22..23 -> dumpAnimationHandlerPre24()
                    .recover { "error while reading animations state: $it" }
                    .getOrElse { "TODO" }

                else -> "not supported Android version ${Build.VERSION.SDK_INT}"
            }
        } catch (e: Exception) {
            "error: $e"
        }
    }

    private fun dumpAnimationHandlerPre24(): Result<String> {
        return Result.tryCatch {
            var dump: String? = null
            val readDump = {
                dump = internalDumpAnimationHandlerPre24()
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                readDump()
            } else {
                InstrumentationRegistry.getInstrumentation().runOnMainSync(readDump)
            }
            dump ?: "not initialized"
        }
    }

    private fun internalDumpAnimationHandlerPre24(): String {
        require(Looper.myLooper() == Looper.getMainLooper()) {
            "reads from ThreadLocal"
        }
        // https://cs.android.com/android/platform/superproject/+/android-6.0.1_r67:frameworks/base/core/java/android/animation/ValueAnimator.java;l=671
        val animationHandlerRef = ValueAnimator::class.java.getStaticFieldValue<ThreadLocal<Any>>("sAnimationHandler")
        val animationHandler = animationHandlerRef.get() as Any

        val animations = animationHandler.getFieldValue<ArrayList<ValueAnimator>>("mAnimations")
        val pendingAnimations = animationHandler.getFieldValue<ArrayList<ValueAnimator>>("mPendingAnimations")

        return "animations: " + animations.joinToString { it.getDump() } +
            ";\n pending: " + pendingAnimations.joinToString { it.getDump() }
    }

    private fun ValueAnimator.getDump(): String {
        val type = this::class.java.simpleName
        return buildString {
            append("$type(")
            append("values=${values.joinToString()}, ")
            append("duration=$duration, ")
            append("playTime=$currentPlayTime, ")
            append("repeatCount=$repeatCount, ")
            append("repeatMode=$repeatMode)")
        }
    }
}
