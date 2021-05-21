package com.avito.android.rule

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.annotation.CallSuper
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry

/**
 * This rule provides functional testing of a single [Activity] similar to deprecated
 * [androidx.test.rule.ActivityTestRule].
 * @param activityClass The Activity class under test
 * @param launchActivity true if the Activity should be launched before each test
 */
open class ActivityScenarioRule<A : Activity>(
    private val activityClass: Class<A>,
    private val launchActivity: Boolean
) : SimpleRule() {

    val activityResult: Instrumentation.ActivityResult
        get() = scenario.result

    private val activityIsNotLaunchedMessage = "Activity $activityClass is not launched"

    lateinit var scenario: ActivityScenario<A>
        private set

    override fun before() {
        super.before()
        if (launchActivity) {
            launchActivity(null)
        }
    }

    override fun after() {
        super.after()
        scenario.close()
        afterActivityFinished()
    }

    fun launchActivity(intent: Intent? = null): ActivityScenario<A> {
        scenario = when (intent) {
            null -> ActivityScenario.launch(activityClass)
            else -> ActivityScenario.launch(
                intent.setClass(InstrumentationRegistry.getInstrumentation().targetContext, activityClass)
            )
        }
        afterActivityLaunched()
        return scenario
    }

    fun runOnUiThread(runnable: Runnable) {
        scenario.onActivity { runnable.run() }
    }

    private fun throwActivityIsNotLaunchedException(): Nothing = error(activityIsNotLaunchedMessage)

    @CallSuper
    protected open fun afterActivityLaunched() {
        // empty
    }

    @CallSuper
    protected open fun afterActivityFinished() {
        // empty
    }
}
