package com.flydog.huhua.common

import java.time.LocalTime
import kotlin.math.min

class TimeUtil {
    fun timeToSecond(time: LocalTime): Int {
        return time.hour * 3600 + time.minute * 60 + time.second;
    }

    fun generateTimeSecond(hour: Int, minute: Int = 0, second: Int = 0): Int {
        return hour * 3600 + minute * 60 + second;
    }
}