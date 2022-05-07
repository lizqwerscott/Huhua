package com.flydog.huhua.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast

object Utils {
    const val REQUEST_FLOAT_CODE=1001
   private fun accessibilityToSettingPage(context: Context) {
        //开启辅助功能页面
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            e.printStackTrace()
        }
    }

   private fun commonROMPermissionCheck(context: Context?): Boolean {
        var result = true
       try {
           val clazz: Class<*> = Settings::class.java
           val canDrawOverlays =
               clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
           result = canDrawOverlays.invoke(null, context) as Boolean
       } catch (e: Exception) {
           Log.e("ServiceUtils", Log.getStackTraceString(e))
       }
       return result
    }

    fun checkSuspendedWindowPermission(context: Activity, block: () -> Unit) {
        if (commonROMPermissionCheck(context)) {
            block()
        } else {
            Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            context.startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }, REQUEST_FLOAT_CODE)
        }
    }

    fun isNull(any: Any?): Boolean = any == null

}