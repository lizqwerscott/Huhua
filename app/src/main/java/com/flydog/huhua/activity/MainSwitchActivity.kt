package com.flydog.huhua.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flydog.huhua.MainActivity

class MainSwitchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        date()
        finish()
    }

    private fun date() {
        val sharedPreferences = getSharedPreferences("is", MODE_PRIVATE)
        val setting = getSharedPreferences("settings", 0)
        val isfer = sharedPreferences.getBoolean("isfer", true)
        val age = setting.getInt("age", -1)
        val editor = sharedPreferences.edit()
        if (isfer || age == -1) {
            val intent = Intent(baseContext, LoginActvity::class.java)
            startActivity(intent)
            editor.putBoolean("isfer", false)
            editor.apply()
        } else {
            val intent = Intent(baseContext, MainActivity::class.java)
            startActivity(intent)
        }
    }
}