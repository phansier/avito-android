package com.avito.android.ui.test

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.app.core.screenRule
import com.avito.android.test.util.toBitmap
import com.avito.android.ui.BitmapActivity
import com.avito.android.ui.R
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test

class BitmapMatcherTest {

    @get:Rule
    val rule = screenRule<BitmapActivity>(launchActivity = true)

    @Test
    fun matches_sameBitmap() {
        val image = getDrawable(R.drawable.red_bitmap).toBitmap()
        Screen.bitmapScreen.imageViewBitmap.checks.withImage(image)
    }

    @Test
    fun matches_sameVector() {
        val image = getDrawable(R.drawable.red).toBitmap()
        Screen.bitmapScreen.imageViewVector.checks.withImage(image)
    }

    @Test
    fun fails_differentImageSize() {
        val image = getDrawable(R.drawable.ic_check_black_24dp).toBitmap()
        assertThat(true).isFalse()

        val error = assertThrows(AssertionError::class.java) {
            Screen.bitmapScreen.imageViewBitmap.checks.withImage(image)
        }
        assertThat(error).hasMessageThat().containsMatch(
            "Bitmaps has different sizes: actual \\[\\d+x\\d+\\], compared \\[\\d+x\\d+\\]"
        )
    }

    @Test
    fun fails_differentImage() {
        val image = getDrawable(R.drawable.blue).toBitmap()

        val error = assertThrows(AssertionError::class.java) {
            Screen.bitmapScreen.imageViewVector.checks.withImage(image)
        }
        assertThat(error).hasMessageThat().contains("Bitmaps are different")
        assertThat(error).hasMessageThat().contains("Pixel at [0,0]")
        assertThat(error).hasMessageThat().contains("actual=#FFFF0000") // R.drawable.red
        assertThat(error).hasMessageThat().contains("expected=#FF0000FF")
    }

    private fun getDrawable(@DrawableRes id: Int): Drawable =
        InstrumentationRegistry.getInstrumentation().targetContext.getDrawable(id)
            ?: throw Resources.NotFoundException("Can't find drawable by id = $id")
}
