package com.flydog.huhua.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.eclipsesource.json.Json
import com.flydog.huhua.MainActivity
import com.flydog.huhua.R
import com.flydog.huhua.common.HttpUtil
import kotlinx.coroutines.*

import java.text.SimpleDateFormat
import java.util.*

class LoginActvity : AppCompatActivity() {
    private var URL = "https://api.jisuapi.com/idcard/query?appkey=1ef04c8d15e5d03f&idcard="
    private var isVerifying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_login)
        val idCard = findViewById<EditText>(R.id.idcard)
        val loading = findViewById<TextView>(R.id.loading)
        val button = findViewById<Button>(R.id.login)

        button.setOnClickListener {
            val str = idCard.text.toString()
            str.replace("\\s".toRegex(), "")
            if (str == "") {
                Toast.makeText(baseContext, "输入为空", Toast.LENGTH_SHORT).show()
            } else {
                if (str.length != 18) {
                    Toast.makeText(baseContext, "输入身份证号码错误", Toast.LENGTH_SHORT).show()
                } else {
                    if (isNetworkConntected()) {
                        loading.setText(R.string.loading)
                        onVerify()
                    } else {
                        Toast.makeText(baseContext, "连接不到网络", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun isNetworkConntected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork != null) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }
        return false
    }

    private fun getAge(birth: String): Int {
        val now = Calendar.getInstance()
        val bir = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        bir.time = simpleDateFormat.parse(birth)
        require(!now.before(birth)) { "The birthday is before Now!!" }
        val yearNow = now[Calendar.YEAR]
        val monthNow = now[Calendar.MONTH] + 1
        val dayNow = now[Calendar.DAY_OF_MONTH]

        val yearBirth = bir[Calendar.YEAR]
        val monthBirth = bir[Calendar.MONTH] + 1
        val dayBirth = bir[Calendar.DAY_OF_MONTH] + 1

        var age = yearNow - yearBirth - 1
        if (monthNow > monthBirth) {
            age++
        } else if (monthNow == monthBirth){
            if (dayNow > dayBirth) {
                age++
            }
        }
        return age
    }

    private fun onVerify() = GlobalScope.launch(Dispatchers.Main) {
        isVerifying = true
        val http = HttpUtil()
        val idCard = findViewById<EditText>(R.id.idcard)
        val loading = findViewById<TextView>(R.id.loading)
        val url = URL + idCard.text
        async(Dispatchers.Default) { http.httpGet1(url) }.await()
            .let {
                val result = Json.parse(it).asObject()
                val textView = findViewById(R.id.result) as TextView
                textView.setText(result.toString())
                loading.text = ""
                Log.w("Info", result.toString())
                if (result.get("status").isNumber) {
                    if (result.getInt("status", -1) == 0) {
                        Log.w("Info", "result get good")
                        val lastflag = result["result"].asObject().getInt("lastflag", -1)
                        if (lastflag != -1 && lastflag == 0) {
                            val birth = result["result"].asObject().getString("birth", "error")
                            val age = getAge(birth)
                            val intent = Intent(baseContext, MainActivity::class.java)
                            val setting = getSharedPreferences("settings", 0)
                            val editor = setting.edit()
                            editor.putInt("age", age)
                            editor.apply()
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(baseContext, "身份证号码错误", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(baseContext, "身份证号码错误", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(baseContext, "身份证号码错误", Toast.LENGTH_SHORT).show()
//                    textView.setText("")
                }
                isVerifying = false
            }
    }
}