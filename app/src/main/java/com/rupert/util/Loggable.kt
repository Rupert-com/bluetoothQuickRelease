package com.rupert.util

import android.util.Log
import java.lang.Exception

interface Loggable {
    val tag: String
        get() = getTag(javaClass)
}

private fun getTag(clazz: Class<*>): String {
    val tag = clazz.simpleName
    return if (tag.length <= 23) {
        tag
    } else {
        tag.substring(0, 23)
    }
}

fun Loggable.info(message: Any) {
    if (Log.isLoggable(tag, Log.INFO)) {
        Log.i(tag, message.toString())
    }
}

fun Loggable.error(message: Any?, ex: Throwable?) {
    if (Log.isLoggable(tag, Log.ERROR)) {
        Log.e(tag, message.toString(), ex)
    }
}

