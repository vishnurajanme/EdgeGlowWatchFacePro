package com.vishnu.edgeglowwatchfacepro

import android.content.*
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.*
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.text.TextPaint
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BlurMaskFilter


/**
 * Updates rate in milliseconds for interactive mode. We update once a second to advance the
 * second hand.
 */
private const val INTERACTIVE_UPDATE_RATE_MS = 1000

/**
 * Handler message id for updating the time periodically in interactive mode.
 */
private const val MSG_UPDATE_TIME = 0

class MyWatchFace : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {

        return Engine()
    }

    private class EngineHandler(reference: MyWatchFace.Engine) : Handler() {
        private val mWeakReference: WeakReference<MyWatchFace.Engine> = WeakReference(reference)

        override fun handleMessage(msg: Message) {

            val engine = mWeakReference.get()
            if (engine != null) {
                when (msg.what) {
                    MSG_UPDATE_TIME -> engine.handleUpdateTimeMessage()
                }
            }
        }
    }

    inner class Engine : CanvasWatchFaceService.Engine() {

        var spacing = 20f
        private var bgcolor = Color.BLACK
        private var topcolor = Color.GREEN
        private var bottomcolor = Color.BLUE
        private var numTap = 0

        lateinit var sharedPreferences: SharedPreferences
        private lateinit var mCalendar: Calendar
        val hours = SimpleDateFormat("hh")
        val minutes = SimpleDateFormat("mm")
        val day = SimpleDateFormat("EEEE, dd LLL yyyy")
        var dateflag = 1
        var edgeglowflag = 1
        var digitglowflag = 1
        var mainFontSize:Float = 100f
        var timeSize = 100f
        var dateSize = 10f
        var spacingSize = 20f
        var myfont = R.font.big

        private lateinit var hournow: String
        private lateinit var minutenow: String
        private lateinit var datenow: String

        private var mRegisteredTimeZoneReceiver = false
        private var mMuteMode: Boolean = false
        private var mCenterX: Float = 0F
        private var mCenterY: Float = 0F
        private var mwidthX: Float = 0F
        private var mheightX: Float = 0F


        private lateinit var mDigitalPaint: TextPaint
        private lateinit var mDigitalPaint1: TextPaint
        private lateinit var mCirclePaint: Paint
        private lateinit var mBlurPaint: TextPaint
        private lateinit var mBlurPaint1: TextPaint
        private lateinit var mDayPaint: TextPaint

        private lateinit var mBackgroundPaint: Paint

        private var mAmbient: Boolean = false
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false
        private val mUpdateTimeHandler = EngineHandler(this)

        private fun getBatteryPercentage(context: Context): Int {
            return if (Build.VERSION.SDK_INT >= 21) {
                val bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
                bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            } else {
                val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, iFilter)
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val batteryPct = level / scale.toDouble()
                (batteryPct * 100).toInt()
            }
        }

        private val mTimeZoneReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@MyWatchFace)
                    .setAcceptsTapEvents(true)
                    .build()
            )
            mCalendar = Calendar.getInstance()

            initializeBackground()
            initializeWatchFace()
        }

        private fun initializeBackground() {
            mBackgroundPaint = Paint().apply {
                color = bgcolor
            }
        }

        private fun initializeWatchFace() {

            mCirclePaint = Paint().apply {
                color = topcolor
                // Smooths out edges of what is drawn without affecting shape.
                isAntiAlias = true
                // Dithering affects how colors with higher-precision than the device are down-sampled.
                isDither = true
                style = Paint.Style.FILL // default: FILL
                maskFilter = setMaskFilter(BlurMaskFilter(50F, BlurMaskFilter.Blur.NORMAL))
            }


            mDayPaint = TextPaint().apply {
                textAlign = Paint.Align.CENTER
                textSize = mainFontSize/8
                color = Color.WHITE
                isAntiAlias = false
                style = Paint.Style.FILL
                typeface =
                    (ResourcesCompat.getFont(this@MyWatchFace, R.font.wide))
            }


            mDigitalPaint = TextPaint().apply {
                textAlign = Paint.Align.CENTER
                textSize = mainFontSize
                color = topcolor
                isAntiAlias = false
                style = Paint.Style.FILL
                typeface =
                    (ResourcesCompat.getFont(this@MyWatchFace, myfont))
            }


            mDigitalPaint1 = TextPaint().apply {
                textAlign = Paint.Align.CENTER
                textSize = mainFontSize
                color = bottomcolor
                isAntiAlias = false
                style = Paint.Style.FILL
                typeface =
                    (ResourcesCompat.getFont(this@MyWatchFace, myfont))
            }

            mBlurPaint = TextPaint().apply {
                textAlign = Paint.Align.CENTER
                textSize = mainFontSize
                color = topcolor
                isAntiAlias = false
                style = Paint.Style.FILL
                typeface =
                    (ResourcesCompat.getFont(this@MyWatchFace, myfont))
                maskFilter = setMaskFilter(BlurMaskFilter(20F, BlurMaskFilter.Blur.NORMAL))
                textSize = mainFontSize
            }

            mBlurPaint1 = TextPaint().apply {
                textAlign = Paint.Align.CENTER
                textSize = mainFontSize
                color = bottomcolor
                isAntiAlias = false
                style = Paint.Style.FILL
                typeface =
                    (ResourcesCompat.getFont(this@MyWatchFace, myfont))
                maskFilter = setMaskFilter(BlurMaskFilter(20F, BlurMaskFilter.Blur.NORMAL))
                textSize = mainFontSize
            }


        }

        override fun onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
            mLowBitAmbient = properties.getBoolean(
                WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false
            )
            mBurnInProtection = properties.getBoolean(
                WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false
            )
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            mAmbient = inAmbientMode
            // Check and trigger whether or not timer should be running (only
            // in active mode).
            updateTimer()
        }

        override fun onInterruptionFilterChanged(interruptionFilter: Int) {
            super.onInterruptionFilterChanged(interruptionFilter)
            val inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE
            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode
                mDigitalPaint.alpha = if (inMuteMode) 80 else 255
                mDayPaint.alpha = if (inMuteMode) 80 else 255
                mBlurPaint1.alpha = if (inMuteMode) 80 else 255
                mBlurPaint.alpha = if (inMuteMode) 80 else 255
                mDigitalPaint1.alpha = if (inMuteMode) 80 else 255
                mCirclePaint.alpha = if (inMuteMode) 80 else 255
                invalidate()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            mCenterX = width / 2f
            mCenterY = height / 2f
            mwidthX = width.toFloat()
            mheightX = height.toFloat()

        }

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                WatchFaceService.TAP_TYPE_TAP -> {
                    when (numTap) {
                        0 -> {
                            Toast.makeText(this@MyWatchFace, "Tap again to remix", Toast.LENGTH_SHORT).show()
                        }
                        1 -> {
                            //rose green
                            bgcolor = Color.parseColor("#000000")
                            mDigitalPaint.color = Color.parseColor("#ecc19c")
                            mBlurPaint.color = Color.parseColor("#ecc19c")
                            mCirclePaint.color = Color.parseColor("#ecc19c")
                            mDigitalPaint1.color = Color.parseColor("#1e847f")
                            mBlurPaint1.color = Color.parseColor("#1e847f")
                        }
                        2 -> {
                            //orange grey
                            mDigitalPaint.color = Color.parseColor("#ef9d10")
                            mBlurPaint.color = Color.parseColor("#ef9d10")
                            mCirclePaint.color = Color.parseColor("#ef9d10")
                            mDigitalPaint1.color = Color.parseColor("#6b7b8c")
                            mBlurPaint1.color = Color.parseColor("#6b7b8c")
                        }
                        3 -> {
                            //green colors
                            mDigitalPaint.color = Color.parseColor("#7e9636")
                            mBlurPaint.color = Color.parseColor("#7e9636")
                            mCirclePaint.color = Color.parseColor("#7e9636")
                            mDigitalPaint1.color = Color.parseColor("#7eb33d")
                            mBlurPaint1.color = Color.parseColor("#7eb33d")
                        }
                        4 -> {
                            //white red
                            mDigitalPaint.color = Color.parseColor("#f5f0e1")
                            mBlurPaint.color = Color.parseColor("#f5f0e1")
                            mCirclePaint.color = Color.parseColor("#f5f0e1")
                            mDigitalPaint1.color = Color.parseColor("#ff6e40")
                            mBlurPaint1.color = Color.parseColor("#ff6e40")

                        }
                        5 -> {
                            //whiteish
                            mDigitalPaint.color = Color.parseColor("#d4ffef")
                            mBlurPaint.color = Color.parseColor("#d4ffef")
                            mCirclePaint.color = Color.parseColor("#d4ffef")
                            mDigitalPaint1.color = Color.parseColor("#ffd2d2")
                            mBlurPaint1.color = Color.parseColor("#ffd2d2")
                        }
                        6 -> {
                            //yellow orange
                            mDigitalPaint.color = Color.parseColor("#ffc13b")
                            mBlurPaint.color = Color.parseColor("#ffc13b")
                            mCirclePaint.color = Color.parseColor("#ffc13b")
                            mDigitalPaint1.color = Color.parseColor("#ff6e40")
                            mBlurPaint1.color = Color.parseColor("#ff6e40")
                        }
                        7 -> {
                            //whiish orange
                            mDigitalPaint.color = Color.parseColor("#f7a07c")
                            mBlurPaint.color = Color.parseColor("#f7a07c")
                            mCirclePaint.color = Color.parseColor("#f7a07c")
                            mDigitalPaint1.color = Color.parseColor("#ffaf80")
                            mBlurPaint1.color = Color.parseColor("#ffaf80")
                        }
                        8 -> {
                            //blueish white
                            mDigitalPaint.color = Color.parseColor("#c5dff0")
                            mBlurPaint.color = Color.parseColor("#c5dff0")
                            mCirclePaint.color = Color.parseColor("#c5dff0")
                            mDigitalPaint1.color = Color.parseColor("#c6e6fb")
                            mBlurPaint1.color = Color.parseColor("#c6e6fb")
                        }
                        9 -> {
                            //white rose
                            mDigitalPaint.color = Color.parseColor("#c6d7eb")
                            mBlurPaint.color = Color.parseColor("#c6d7eb")
                            mCirclePaint.color = Color.parseColor("#c6d7eb")
                            mDigitalPaint1.color = Color.parseColor("#d9a5b3")
                            mBlurPaint1.color = Color.parseColor("#d9a5b3")
                        }
                        10 -> {
                            //red blue
                            mDigitalPaint.color = Color.parseColor("#7a2048")
                            mBlurPaint.color = Color.parseColor("#7a2048")
                            mCirclePaint.color = Color.parseColor("#7a2048")
                            mDigitalPaint1.color = Color.parseColor("#408ec6")
                            mBlurPaint1.color = Color.parseColor("#408ec6")
                        }
                        11 -> {
                            //blue rose
                            mDigitalPaint.color = Color.parseColor("#79a7d3")
                            mBlurPaint.color = Color.parseColor("#79a7d3")
                            mCirclePaint.color = Color.parseColor("#79a7d3")
                            mDigitalPaint1.color = Color.parseColor("#8a307f")
                            mBlurPaint1.color = Color.parseColor("#8a307f")
                        }
                        12 -> {
                            //grey white
                            mDigitalPaint.color = Color.parseColor("#65728d")
                            mBlurPaint.color = Color.parseColor("#65728d")
                            mCirclePaint.color = Color.parseColor("#65728d")
                            mDigitalPaint1.color = Color.parseColor("#cdccd1")
                            mBlurPaint1.color = Color.parseColor("#cdccd1")
                        }
                        13 -> {
                            //orange white
                            mDigitalPaint.color = Color.parseColor("#d2601a")
                            mBlurPaint.color = Color.parseColor("#d2601a")
                            mCirclePaint.color = Color.parseColor("#d2601a")
                            mDigitalPaint1.color = Color.parseColor("#fff1e1")
                            mBlurPaint1.color = Color.parseColor("#fff1e1")
                        }
                        14 -> {
                            //change red blue white
                            mDigitalPaint.color = Color.parseColor("#e52165")
                            mBlurPaint.color = Color.parseColor("#e52165")
                            mCirclePaint.color = Color.parseColor("#e52165")
                            mDigitalPaint1.color = Color.parseColor("#aed6dc")
                            mBlurPaint1.color = Color.parseColor("#aed6dc")
                        }
                        15 -> {
                            //red white
                            mDigitalPaint.color = Color.parseColor("#d72631")
                            mBlurPaint.color = Color.parseColor("#d72631")
                            mCirclePaint.color = Color.parseColor("#d72631")
                            mDigitalPaint1.color = Color.parseColor("#a2d5c6")
                            mBlurPaint1.color = Color.parseColor("#a2d5c6")
                        }
                        16 -> {
                            //blue purple
                            mDigitalPaint.color = Color.parseColor("#077b8a")
                            mBlurPaint.color = Color.parseColor("#077b8a")
                            mCirclePaint.color = Color.parseColor("#077b8a")
                            mDigitalPaint1.color = Color.parseColor("#5c3c92")
                            mBlurPaint1.color = Color.parseColor("#5c3c92")
                        }
                        17 -> {
                            //yellow rose
                            mDigitalPaint.color = Color.parseColor("#e2d810")
                            mBlurPaint.color = Color.parseColor("#e2d810")
                            mCirclePaint.color = Color.parseColor("#e2d810")
                            mDigitalPaint1.color = Color.parseColor("#d9138a")
                            mBlurPaint1.color = Color.parseColor("#d9138a")
                        }
                        18 -> {
                            //rose yellow
                            mDigitalPaint.color = Color.parseColor("#cf1578")
                            mBlurPaint.color = Color.parseColor("#cf1578")
                            mCirclePaint.color = Color.parseColor("#cf1578")
                            mDigitalPaint1.color = Color.parseColor("#e8d21d")
                            mBlurPaint1.color = Color.parseColor("#e8d21d")
                        }
                        19 -> {
                            //blue red
                            mDigitalPaint.color = Color.parseColor("#039fbe")
                            mBlurPaint.color = Color.parseColor("#039fbe")
                            mCirclePaint.color = Color.parseColor("#039fbe")
                            mDigitalPaint1.color = Color.parseColor("#b20238")
                            mBlurPaint1.color = Color.parseColor("#b20238")
                        }
                        20 -> {
                            //roseish
                            mDigitalPaint.color = Color.parseColor("#e75874")
                            mBlurPaint.color = Color.parseColor("#e75874")
                            mCirclePaint.color = Color.parseColor("#e75874")
                            mDigitalPaint1.color = Color.parseColor("#be1558")
                            mBlurPaint1.color = Color.parseColor("#be1558")
                        }

                        21 -> {
                            //roseish
                            mDigitalPaint.color = Color.parseColor("#77c593")
                            mBlurPaint.color = Color.parseColor("#77c593")
                            mCirclePaint.color = Color.parseColor("#77c593")
                            mDigitalPaint1.color = Color.parseColor("#ed3572")
                            mBlurPaint1.color = Color.parseColor("#ed3572")
                        }

                        22 -> {
                            //rose green
                            mDigitalPaint.color = Color.parseColor("#da68a0")
                            mBlurPaint.color = Color.parseColor("#da68a0")
                            mCirclePaint.color = Color.parseColor("#da68a0")
                            mDigitalPaint1.color = Color.parseColor("#77c593")
                            mBlurPaint1.color = Color.parseColor("#77c593")
                        }

                        23 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#7fe7dc")
                            mBlurPaint.color = Color.parseColor("#7fe7dc")
                            mCirclePaint.color = Color.parseColor("#7fe7dc")
                            mDigitalPaint1.color = Color.parseColor("#f47a60")
                            mBlurPaint1.color = Color.parseColor("#f47a60")
                        }

                        24 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#d902ee")
                            mBlurPaint.color = Color.parseColor("#d902ee")
                            mCirclePaint.color = Color.parseColor("#d902ee")
                            mDigitalPaint1.color = Color.parseColor("#ffd79d")
                            mBlurPaint1.color = Color.parseColor("#ffd79d")
                        }

                        25 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#daf2dc")
                            mBlurPaint.color = Color.parseColor("#daf2dc")
                            mCirclePaint.color = Color.parseColor("#daf2dc")
                            mDigitalPaint1.color = Color.parseColor("#81b7d2")
                            mBlurPaint1.color = Color.parseColor("#81b7d2")
                        }

                        26 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#ffcce7")
                            mBlurPaint.color = Color.parseColor("#ffcce7")
                            mCirclePaint.color = Color.parseColor("#ffcce7")
                            mDigitalPaint1.color = Color.parseColor("#4d5198")
                            mBlurPaint1.color = Color.parseColor("#4d5198")
                        }

                        27 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#e1dd72")
                            mBlurPaint.color = Color.parseColor("#e1dd72")
                            mCirclePaint.color = Color.parseColor("#e1dd72")
                            mDigitalPaint1.color = Color.parseColor("#1b6535")
                            mBlurPaint1.color = Color.parseColor("#1b6535")
                        }

                        28 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#d13ca4")
                            mBlurPaint.color = Color.parseColor("#d13ca4")
                            mCirclePaint.color = Color.parseColor("#d13ca4")
                            mDigitalPaint1.color = Color.parseColor("#ffea04")
                            mBlurPaint1.color = Color.parseColor("#ffea04")
                        }

                        29 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#e3b448")
                            mBlurPaint.color = Color.parseColor("#e3b448")
                            mCirclePaint.color = Color.parseColor("#e3b448")
                            mDigitalPaint1.color = Color.parseColor("#3a6b35")
                            mBlurPaint1.color = Color.parseColor("#3a6b35")
                        }

                        30 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#79cbb8")
                            mBlurPaint.color = Color.parseColor("#79cbb8")
                            mCirclePaint.color = Color.parseColor("#79cbb8")
                            mDigitalPaint1.color = Color.parseColor("#500472")
                            mBlurPaint1.color = Color.parseColor("#500472")
                        }

                        31 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#9bc472")
                            mBlurPaint.color = Color.parseColor("#9bc472")
                            mCirclePaint.color = Color.parseColor("#9bc472")
                            mDigitalPaint1.color = Color.parseColor("#cbf6db")
                            mBlurPaint1.color = Color.parseColor("#cbf6db")
                        }
                        32 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#16acea")
                            mBlurPaint.color = Color.parseColor("#16acea")
                            mCirclePaint.color = Color.parseColor("#16acea")
                            mDigitalPaint1.color = Color.parseColor("#4203c9")
                            mBlurPaint1.color = Color.parseColor("#4203c9")
                        }
                        33 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#d71b3b")
                            mBlurPaint.color = Color.parseColor("#d71b3b")
                            mCirclePaint.color = Color.parseColor("#d71b3b")
                            mDigitalPaint1.color = Color.parseColor("#e8d71e")
                            mBlurPaint1.color = Color.parseColor("#e8d71e")
                        }

                        34 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#16acea")
                            mBlurPaint.color = Color.parseColor("#16acea")
                            mCirclePaint.color = Color.parseColor("#16acea")
                            mDigitalPaint1.color = Color.parseColor("#4203c9")
                            mBlurPaint1.color = Color.parseColor("#4203c9")
                        }

                        35 -> {
                            //blue orange
                            mDigitalPaint.color = Color.parseColor("#16acea")
                            mBlurPaint.color = Color.parseColor("#16acea")
                            mCirclePaint.color = Color.parseColor("#16acea")
                            mDigitalPaint1.color = Color.parseColor("#4203c9")
                            mBlurPaint1.color = Color.parseColor("#4203c9")
                        }

                        36 -> {
                            //blue orange
                            mDigitalPaint.color = topcolor
                            mBlurPaint.color = topcolor
                            mCirclePaint.color = topcolor
                            mDigitalPaint1.color = bottomcolor
                            mBlurPaint1.color = bottomcolor
                            numTap = 0
                        }



                    }
                    numTap += 1
                }
            }
            invalidate()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            val now = System.currentTimeMillis()

            mCalendar.timeInMillis = now
            sharedPreferences = getSharedPreferences("edgeglowwatchface", Context.MODE_PRIVATE)
            dateflag = sharedPreferences.getInt("dateflag", 1)
            edgeglowflag = sharedPreferences.getInt("edgeglowflag", 1)
            digitglowflag = sharedPreferences.getInt("digitglowflag", 1)
            timeSize = sharedPreferences.getFloat("timeSize", mwidthX/1.5f)
            dateSize = sharedPreferences.getFloat("dateSize", mainFontSize/8)
            spacingSize = sharedPreferences.getFloat("spacingSize", 20f)
            mDigitalPaint.textSize = timeSize
            mDigitalPaint1.textSize = timeSize
            mBlurPaint.textSize = timeSize
            mBlurPaint1.textSize = timeSize
            spacing = spacingSize
            mDayPaint.textSize = dateSize
            drawBackground(canvas)
            drawWatchFace(canvas)
        }


        private fun drawBackground(canvas: Canvas) {


            myfont = R.font.big
            mDigitalPaint.typeface = (ResourcesCompat.getFont(this@MyWatchFace, myfont))

            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK)
            } else if (mAmbient) {
                canvas.drawColor(Color.BLACK)
                //canvas.drawBitmap(mGrayBackgroundBitmap, 0f, 0f, mBackgroundPaint)
            } else {
                canvas.drawColor(bgcolor)

            }

            //canvas.drawRect(frame, border)
        }

        private fun drawWatchFace(canvas: Canvas) {
            //mDigitalPaint.typeface = (ResourcesCompat.getFont(this@MyWatchFace, R.font.monospacebold))
            hournow = hours.format(Date())
            minutenow = minutes.format(Date())
            datenow = day.format(Date())

            //mDigitalPaint.alpha = if (mAmbient) 50 else 255
            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                displayAll(canvas, hournow, minutenow, datenow)
            } else if (mAmbient) {
                displayAll(canvas, hournow, minutenow, datenow)
            } else {
                displayAll(canvas, hournow, minutenow, datenow)
            }
        }

        private fun displayAll(canvas: Canvas, hour: String, minute:String, date: String) {
            //canvas.drawText("C:\\>" + saveBtnTimeValue, (xData), (yData), mDigitalPaint)

            if (edgeglowflag == 1) {
                canvas.drawCircle(0f, (mCenterY - ((mDayPaint.descent() + mDayPaint.ascent()) / 2) ), 35f, mCirclePaint)
                canvas.drawCircle(mCenterX*2, (mCenterY - ((mDayPaint.descent() + mDayPaint.ascent()) / 2) ), 35f, mCirclePaint)
            }

            if (digitglowflag == 1) {
                canvas.drawText(hour, mCenterX, (mCenterY - spacing - mCenterY/2 - ((mDigitalPaint.descent() + mDigitalPaint.ascent()) / 1.5f) ), mBlurPaint)
                canvas.drawText(minute, mCenterX, (mCenterY + spacing + 10 + mCenterY/2 - ((mDigitalPaint1.descent() + mDigitalPaint1.ascent()) / 2.5f) ), mBlurPaint1)
            }

            canvas.drawText(hour, mCenterX, (mCenterY - spacing - mCenterY/2 - ((mDigitalPaint.descent() + mDigitalPaint.ascent()) / 1.5f) ), mDigitalPaint)
            canvas.drawText(minute, mCenterX, (mCenterY + spacing + 10 + mCenterY/2 - ((mDigitalPaint1.descent() + mDigitalPaint1.ascent()) / 2.5f) ), mDigitalPaint1)

            if (dateflag == 1) {
                canvas.drawText(date, mCenterX, (mCenterY - ((mDayPaint.descent() + mDayPaint.ascent()) / 2) ), mDayPaint)
            }

        }




        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            } else {
                unregisterReceiver()
            }

            updateTimer()
        }

        private fun registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            this@MyWatchFace.registerReceiver(mTimeZoneReceiver, filter)
        }

        private fun unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = false
            this@MyWatchFace.unregisterReceiver(mTimeZoneReceiver)
        }

        /**
         * Starts/stops the [.mUpdateTimeHandler] timer based on the state of the watch face.
         */
        private fun updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            }
        }

        /**
         * Returns whether the [.mUpdateTimeHandler] timer should be running. The timer
         * should only run in active mode.
         */
        private fun shouldTimerBeRunning(): Boolean {
            return isVisible && !mAmbient
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        fun handleUpdateTimeMessage() {
            invalidate()
            if (shouldTimerBeRunning()) {
                val timeMs = System.currentTimeMillis()
                val delayMs = INTERACTIVE_UPDATE_RATE_MS - timeMs % INTERACTIVE_UPDATE_RATE_MS
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
            }
        }
    }
}