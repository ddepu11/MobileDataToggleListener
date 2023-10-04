package com.example.textandscrollview

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

  @RequiresApi(Build.VERSION_CODES.R)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val tag = "NEW_TAG"
    val exec = Executors.newSingleThreadExecutor()

    val telephonyManager =
        application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    if (Build.VERSION.SDK_INT >= 31) {

      val callback =
          object : TelephonyCallback(), TelephonyCallback.UserMobileDataStateListener {

            override fun onUserMobileDataStateChanged(enabled: Boolean) {
              Log.d(tag, enabled.toString())
            }
          }

      telephonyManager.registerTelephonyCallback(exec, callback)
    } else {

      @Suppress("OVERRIDE_DEPRECATION")
      val callback =
          @RequiresApi(Build.VERSION_CODES.Q)
          object : PhoneStateListener(exec) {
            override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
              Log.d(tag, telephonyDisplayInfo.toString())
            }
          }
      telephonyManager.listen(callback, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED)
    }
  }
}
