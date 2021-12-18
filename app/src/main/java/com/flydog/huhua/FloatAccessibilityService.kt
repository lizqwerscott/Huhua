package com.flydog.huhua

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.flydog.huhua.utils.ItemViewClickListener
import com.flydog.huhua.utils.ItemViewTouchListener
import com.flydog.huhua.utils.ViewModleMain
import com.flydog.huhua.utils.Utils.isNull
import java.text.SimpleDateFormat
import java.util.*

class FloatAccessibilityService : AccessibilityService(), LifecycleOwner{
    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null
    private val mLifecycleRegistry = LifecycleRegistry(this)
    private val short_video: List<String> = listOf("com.ss.android.ugc.aweme", "com.ss.android.ugc.aweme.lite", "com.smile.gifmaker", "com.kuaishou.nebula")

    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val simpleDateFormatDate: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var douDate: String = simpleDateFormatDate.format(System.currentTimeMillis())
    private var douTime: Long = 0
    private var lastJTime: Long = 0


    private var isClosep: Boolean = false

    private var changeTimeHandle: Handler? = null
    private val changeTimeCallBack: Handler.Callback = Handler.Callback { msg ->
        if (msg.what == 0) {
            val lastTime = getLastTime()
            val textView = floatRootView?.findViewById(R.id.time) as TextView
            //Log.w("Info", "LastTime: " + simpleDateFormat.format(lastTime))
            if (!isClosep) {
                textView.text = simpleDateFormat.format(lastTime)
            } else {
                textView.text = "时间到"
            }
            changeTimeHandle?.sendEmptyMessageDelayed(0, 1000)
        }
        false
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT+0")
        initObserve()
        changeTimeHandle = Handler(this.mainLooper, changeTimeCallBack)
        ViewModleMain.isShowWindow.postValue(true)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun initObserve() {
        ViewModleMain.isShowWindow.observe(this) {
            if (it) {
                showWindow()
            } else {
                if (!isNull(floatRootView)) {
                    if (!isNull(floatRootView?.windowToken)) {
                        if (!isNull(windowManager)) {
                            windowManager.removeView(floatRootView)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        //val outMetrics = DisplayMetrics()
        //windowManager.defaultDisplay.getMetrics(outMetrics)
        val layoutParam = WindowManager.LayoutParams()
        layoutParam.apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            format = PixelFormat.TRANSPARENT

            floatRootView = LayoutInflater.from(baseContext).inflate(R.layout.activity_float, null)
            floatRootView?.setOnClickListener(ItemViewClickListener(layoutParam, windowManager))
            floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
            windowManager.addView(floatRootView, layoutParam)
            changeTimeHandle?.sendEmptyMessageDelayed(0, 1000)
        }
    }

    override fun onServiceConnected() {
        Log.w("Info", "accessibility servcice connected")
        super.onServiceConnected()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun getLifecycle(): Lifecycle = mLifecycleRegistry
    override fun onStart(intent: Intent?, startId: Int) {
        //super.onStart(intent, startId)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName.toString()
        //val componentName = ComponentName(packageName, event?.className.toString())
        Log.w("Info", "Start$packageName")
        //var activityInfo = tryGetActivity(componentName)
        val nowDate = simpleDateFormatDate.format(System.currentTimeMillis())
        if (douDate != nowDate) {
            douDate = nowDate
            douTime = 0
            isClosep = false
        }
        if (short_video.contains(packageName)) {
            Log.w("Info", "--------------------------")
            if (lastJTime == 0.toLong()) {
                lastJTime = System.currentTimeMillis()
                Log.w("Info", "Event:Set:LastJTime$lastJTime")
            } else {
                val nowTime: Long = System.currentTimeMillis()
                douTime += (nowTime - lastJTime)
                lastJTime = nowTime
                Log.w("Info", "Event:LastJTime$lastJTime")
                Log.w("Info", "Event:douTime$douTime")
            }
            Log.w("Info", "--------------------------")
            if (isClose()) {
                if (!isClosep) {
                    ViewModleMain.isShowWindow.postValue(false)
                    Toast.makeText(baseContext, "时间到了,请不要再看了", Toast.LENGTH_SHORT).show()
                }
                isClosep = true
                //Log.w("Info", "Close" + packageName)
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        } else {
            lastJTime = 0
        }
    }

    override fun onInterrupt() {
    }

    private fun getLastTime(): Long {
        val setting = getSharedPreferences("settings", 0)
        val minute = setting.getInt("time", -1)
        if (minute == -1) {
            Toast.makeText(baseContext, "Please setting usage time", Toast.LENGTH_LONG).show()
            return -999999
        }
        return minute * 60 * 1000 - douTime
    }

    private fun isClose(): Boolean {
        return getLastTime() <= 0
    }

}