package com.flydog.huhua

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.widget.TextView
import android.widget.NumberPicker
import android.content.IntentFilter
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.flydog.huhua.activity.LoginActvity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(baseContext, "没有悬浮窗权限", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
        val settings = getSharedPreferences("settings", 0)
        val age = settings.getInt("age", -1)
        val textView = findViewById<TextView>(R.id.ageshow)
        if (age < 18) {
            textView.text = "您可使用的时间为1小时"
        } else {
            textView.text = "您可使用的时间为2小时"
        }

        val hourPicker = findViewById<NumberPicker>(R.id.hour)
        val minutePicker = findViewById<NumberPicker>(R.id.minute)
        hourPicker.minValue = 0
        hourPicker.maxValue = getMaxHour(age)
        setPickerValue(hourPicker, "hour")

        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        setPickerValue(minutePicker, "minute")


        val accessibility = findViewById<Button>(R.id.accessibility)
        if (isAccessibilituSerivceOpen) {
            accessibility.setText(R.string.acc_start)
        } else {
            accessibility.setText(R.string.acc_open)
        }
        accessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            accessibility.setText(R.string.acc_start)
        }
        val intentFilter = IntentFilter(Intent.ACTION_TIME_TICK)
        val receiver = MyBroadCastReceiver()
        registerReceiver(receiver, intentFilter)
        val start = findViewById<Button>(R.id.start)
        start.setOnClickListener {
            val editor = settings.edit()
            val times = hourPicker.value * 60 + minutePicker.value
            editor.putInt("time", times)
            editor.putInt("hour", hourPicker.value)
            editor.putInt("minute", minutePicker.value)
            editor.apply()
            Toast.makeText(baseContext, "Time:$times", Toast.LENGTH_LONG).show()
            moveTaskToBack(true)
        }
    }

    private fun getMaxHour(age: Int): Int {
        return if (age >= 18) {
            1
        } else {
            0
        }
    }

    private fun setPickerValue(picker: NumberPicker, name: String) {
        val settings = getSharedPreferences("settings", 0)
        val value = settings.getInt(name, -1)
        if (value == -1) {
            picker.value = 0
        } else {
            picker.value = value
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.re_login) {
            startActivity(Intent(baseContext, LoginActvity::class.java))
            finish()
        }
        if (id == R.id.help) {
            AlertDialog.Builder(this)
                .setMessage(R.string.help)
                .setTitle(R.string.menu_help)
                .setIcon(R.drawable.icon_huahau_hdip)
                .setCancelable(true)
                .create()
                .show()
        }
        return super.onOptionsItemSelected(item)
    }


    private val isAccessibilituSerivceOpen: Boolean
        get() {
            var enabled = 0
            val service = packageName + "/" + FloatAccessibilityService::class.java.canonicalName
            Log.w("Info", "service:$service")
            try {
                enabled = Settings.Secure.getInt(
                    baseContext.applicationContext.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                )
            } catch (e: SettingNotFoundException) {
                e.printStackTrace()
            }
            val simpleStringSplitter = SimpleStringSplitter(':')
            if (enabled == 1) {
                val settingValue = Settings.Secure.getString(
                    baseContext.applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                if (settingValue != null) {
                    simpleStringSplitter.setString(settingValue)
                    while (simpleStringSplitter.hasNext()) {
                        val accessibilituService = simpleStringSplitter.next()
                        if (accessibilituService.equals(service, ignoreCase = true)) {
                            return true
                        }
                    }
                } else {
                    return false
                }
            }
            return false
        }

}