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
import kotlinx.coroutines.GlobalScope
import java.util.concurrent.Executors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
  private val tag = "NEW_TAG"

  @RequiresApi(Build.VERSION_CODES.R)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    GlobalScope.launch {
      getMobileDataToggleUpdatesFlow()
          .onCompletion { Log.d(tag, "OnCompletion: $it") }
          .catch { Log.d(tag, "OnCompletion: $it") }
          .onEmpty { Log.d(tag, "OnEmpty:") }
          .collect { Log.d(tag, "MDATA: $it") }
    }
  }

  private fun getMobileDataToggleUpdatesFlow(): Flow<Boolean> {
    return callbackFlow<Boolean> {
      val telephonyManager =
          application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
      val exec = Executors.newSingleThreadExecutor()

      if (Build.VERSION.SDK_INT >= 31) {

        val callback =
            object : TelephonyCallback(), UserMobileDataStateListener {

              override fun onUserMobileDataStateChanged(enabled: Boolean) {
                trySend(enabled)
              }
            }

        telephonyManager.registerTelephonyCallback(exec, callback)

        awaitClose {
          Log.d(tag, "CLOSE!!!")

          telephonyManager.unregisterTelephonyCallback(callback)
          exec.shutdown()
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
          Log.d(tag, "CLOSE!!!")

          getContentResolver().unregisterContentObserver(mObserver)
          exec.shutdown()
        }
      }
    }
  }
}
