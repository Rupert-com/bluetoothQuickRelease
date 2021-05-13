package com.rupert.bluetoothquickrelease

import android.content.SharedPreferences

class Storage {

    val sharedPref: SharedPreferences

    constructor(sh: SharedPreferences) {
        sharedPref = sh
    }

     inline fun <reified T> store(KEY: String, Val: T) {
        when (T::class.java) {
            String::class.java -> sharedPref.edit().putString(KEY, Val as String).apply()
            Set::class.java -> { // TODO auch Set<T> checken... nicht nur T<>
                sharedPref.edit().putStringSet(KEY, Val as Set<String>).apply()
            }
            else -> throw Exception("Unhandled return type")
        }
    }

     inline fun <reified T> reStore(KEY: String, defaultVal: T? = null): T {
        return when (T::class.java) {
            String::class.java -> sharedPref.getString(KEY, defaultVal?.toString()) as T
            Set::class.java -> sharedPref.getStringSet(
                KEY,
                defaultVal as Set<String>
            ) as T
            else -> throw Exception("Unhandled return type")
        }
    }

}