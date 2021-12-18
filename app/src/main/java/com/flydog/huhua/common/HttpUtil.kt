package com.flydog.huhua.common

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class HttpUtil {
     fun httpGet1(url : String): String? {
        val client = OkHttpClient.Builder().readTimeout(5000, TimeUnit.MILLISECONDS).build()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        return body
    }
}