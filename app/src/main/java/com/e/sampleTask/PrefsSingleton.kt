package com.e.sampleTask

import android.content.Context

/*
 * Created by Shripal Jain
 * on 04/01/2022
 */

/**
 * Helper singleton class for reading and writing from/to SharedPreferences
 */
class PrefsSingleton constructor(context: Context) {

  private val prefs by lazy {
    context.getSharedPreferences("e", Context.MODE_PRIVATE)
  }
  private val editor by lazy {
    prefs.edit()
  }

  companion object {
    @Volatile
    private var INSTANCE: PrefsSingleton? = null
    fun getInstance(context: Context) =
      INSTANCE ?: synchronized(this) {
        INSTANCE ?: PrefsSingleton(context).also {
          INSTANCE = it
        }
      }
  }

  fun getString(key: String, defaultValue: String? = null): String? = prefs.getString(key, defaultValue)

  fun putString(key: String, value: String): Boolean = with(editor) {
    putString(key, value)
    return commit()
  }
}