package com.example.mobiledata

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
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.concurrent.Executors
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  @OptIn(DelicateCoroutinesApi::class)
  @RequiresApi(Build.VERSION_CODES.R)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val tag = "NEW_TAG"

    //   #######################_________@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //   Try with Kotlin flow

    GlobalScope.launch { getMobileDataToggleUpdatesFlow().collect { Log.d(tag, "MDATA: $it") } }
  }

  private fun getMobileDataToggleUpdatesFlow(): Flow<Any> {
    return callbackFlow<Any> {
      val tag = "NEW_TAG"

      val telephonyManager =
          application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
      val exec = Executors.newCachedThreadPool()

      if (Build.VERSION.SDK_INT >= 31) {

        val callback =
            object : TelephonyCallback(), UserMobileDataStateListener {

              override fun onUserMobileDataStateChanged(enabled: Boolean) {
                trySend(enabled)
              }
            }

        telephonyManager.registerTelephonyCallback(exec, callback)

        awaitClose {
          telephonyManager.unregisterTelephonyCallback(callback)
          exec.shutdown()
          Log.d(tag, "CLOSE!!!")
        }
      } else {
        Log.d(tag, "State: UNDER SDK 30")

        val mObserver: ContentObserver =
            object : ContentObserver(Handler()) {
              override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(selfChange)
              }
            }

        val mobileDataSettingUri: Uri = Settings.Secure.getUriFor("mobile_data")

        getApplicationContext()
            .getContentResolver()
            .registerContentObserver(mobileDataSettingUri, true, mObserver)

        awaitClose {
          getContentResolver().unregisterContentObserver(mObserver)
          exec.shutdown()
        }
      }
    }
  }
}
