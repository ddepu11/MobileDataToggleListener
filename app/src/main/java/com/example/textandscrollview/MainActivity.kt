package com.example.textandscrollview

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.telephony.TelephonyCallback
import android.telephony.TelephonyCallback.UserMobileDataStateListener
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

    Log.d(tag, Build.VERSION.SDK_INT.toString())

    if (Build.VERSION.SDK_INT >= 31) {

      val callback =
          object : TelephonyCallback(), UserMobileDataStateListener {

            override fun onUserMobileDataStateChanged(enabled: Boolean) {
              Log.d(tag, enabled.toString())
            }
          }

      telephonyManager.registerTelephonyCallback(exec, callback)
    } else {
      Log.d(tag, "State: UNDER SDK 30")

      val mObserver: ContentObserver =
          object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
              // Retrieve mobile data value here and perform necessary actions
              Log.d(tag, "State: ${selfChange.toString()} | URI: $uri")
            }
          }

      val mobileDataSettingUri: Uri = Settings.Secure.getUriFor("mobile_data")

      getApplicationContext()
          .getContentResolver()
          .registerContentObserver(mobileDataSettingUri, true, mObserver)
    }
  }
}
