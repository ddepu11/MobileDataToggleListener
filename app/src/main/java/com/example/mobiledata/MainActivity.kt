package com.example.mobiledata

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

typealias HandleChangeFunction = (Boolean) -> Unit

class MainActivity : AppCompatActivity() {

  private val handleChange: HandleChangeFunction = { mobileDataOn -> Log.d("NEW_TAG", "Mobile Data is: $mobileDataOn")}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val mobileDataObserver = MobileDataContentObserver(applicationContext, handleChange)

    mobileDataObserver.register();
  }
}
