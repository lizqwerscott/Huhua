package com.flydog.huhua.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

class ItemViewTouchListener(val wl: WindowManager.LayoutParams, val windowManager: WindowManager) :
    View.OnTouchListener {
    private var x = 0
    private var y = 0
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                x = motionEvent.rawX.toInt()
                y = motionEvent.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val nowX = motionEvent.rawX.toInt()
                val nowY = motionEvent.rawY.toInt()
                val movedX = nowX - x
                val movedY = nowY - y
                x = nowX
                y = nowY
                wl.apply {
                    x += movedX
                    y += movedY
                }
                windowManager.updateViewLayout(p0, wl)
            } else -> {

            }
        }
        return false
    }
}