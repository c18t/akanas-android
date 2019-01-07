package org.chimata.u.akanas

import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_fullscreen.*
import java.util.*
import kotlin.concurrent.schedule

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
//        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    private var mTimer = Timer()
    private var mRemainingTime: Long = 25 * 60 * 1000 + 900
    private var mExitTime: Long = 0
    private var mCounting: Boolean = false
    private var mInterval: Boolean = false
    private var mSoundPool: SoundPool = SoundPool(0,0,0)
    private var mSoundCursor: Int = 0
    private var mSoundDecosion: Int = 0
    private var mSoundWarning: Int = 0

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreen_content.setOnClickListener {
            mSoundPool.play(mSoundCursor, 1.0f, 1.0f, 0, 0, 1.0f)
            doAkanas()
            toggle()
        }

//        // Upon interacting with UI controls, delay any scheduled hide()
//        // operations to prevent the jarring behavior of controls going away
//        // while interacting with the UI.
//        dummy_button.setOnTouchListener(mDelayHideTouchListener)


        val audioAttributes = AudioAttributes.Builder()
                // USAGE_MEDIA
                // USAGE_GAME
                .setUsage(AudioAttributes.USAGE_GAME)
                // CONTENT_TYPE_MUSIC
                // CONTENT_TYPE_SPEECH, etc.
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

        mSoundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                // ストリーム数に応じて
                .setMaxStreams(3)
                .build()

        mSoundCursor = mSoundPool.load(this, R.raw.cursor1, 1)
        mSoundDecosion = mSoundPool.load(this, R.raw.decision1, 1)
        mSoundWarning = mSoundPool.load(this, R.raw.warning1, 1)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
//        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())

        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private fun doAkanas() {
        if (!mCounting) {
            mExitTime = mRemainingTime + Date().time
            mTimer = Timer()
            mTimer.schedule(0, 100, { countDown() })
        } else {
            mTimer.cancel()
        }

        mCounting = !mCounting
    }

    private fun countDown() {
        mRemainingTime = mExitTime - Date().time

        if (mRemainingTime < 1000) {
            switchAkanas()

            if (mInterval) {
                mRemainingTime = 5 * 60 * 1000 + 900
            } else {
                mRemainingTime = 25 * 60 * 1000 + 900

                if (mCounting) {
                    doAkanas()
                }
            }

            mExitTime = mRemainingTime + Date().time
        }

        updateText()
    }

    private fun switchAkanas() {
        if (!mInterval) {
            mSoundPool.play(mSoundWarning, 1.0f, 1.0f, 0, 0, 1.0f)
            setThemeToNas()
        } else {
            mSoundPool.play(mSoundDecosion, 1.0f, 1.0f, 0, 0, 1.0f)
            setThemeToAkanas()
        }

        mInterval = !mInterval
    }

    private fun updateText() {
        val min = mRemainingTime / (60 * 1000)
        val sec = (mRemainingTime / 1000) % 60

        mHideHandler.post({
            fullscreen_content.text = "%02d:%02d".format(min, sec)
        })
    }

    private fun setThemeToNas() {
        mHideHandler.post({
            setTitle("nas")
            supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.colorNas)))
            fullscreen_frame.setBackgroundColor(ContextCompat.getColor(this, R.color.colorNas))
        })
    }

    private fun setThemeToAkanas() {
        mHideHandler.post({
            setTitle("Akanas")
            supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.colorAkanas)))
            fullscreen_frame.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAkanas))
        })
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
