package com.example.mobiledata

import android.content.Context
import android.content.Intent
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
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
  private val tag = "NEW_TAG"

  private lateinit var callback: TelephonyCallback
  private lateinit var mObserver: ContentObserver
  private lateinit var telephonyManager: TelephonyManager

  private lateinit var exec: ExecutorService

  @RequiresApi(Build.VERSION_CODES.R)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val testActivityBtn = findViewById<Button>(R.id.testActivityBtn)

    testActivityBtn.setOnClickListener {
      intent = Intent(applicationContext, TestActivity::class.java)
      startActivity(intent)
    }
  }

  @OptIn(DelicateCoroutinesApi::class)
  override fun onResume() {
    super.onResume()

    Log.d(tag, "!!!!_RESUME_!!!!")
    GlobalScope.launch(Dispatchers.Main) {
      getMobileDataToggleUpdatesFlow()
          .onCompletion { Log.d(tag, "OnCompletion: $it") }
          .catch { Log.d(tag, "OnCompletion: $it") }
          .onEmpty { Log.d(tag, "OnEmpty:") }
          .collect { Log.d(tag, "Mobile_Data: ${if(it)"On" else "off"}") }
    }
  }

  override fun onPause() {
    super.onPause()
    Log.d(tag, "!!!!_PAUSE_!!!!:")

    if (Build.VERSION.SDK_INT >= 31) {
      telephonyManager.unregisterTelephonyCallback(callback)
    } else {
      getContentResolver().unregisterContentObserver(mObserver)
    }

    exec.shutdown()
  }

  private fun getMobileDataToggleUpdatesFlow(): Flow<Boolean> {
    return callbackFlow<Boolean> {
      telephonyManager = application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
      exec = Executors.newSingleThreadExecutor()



      if (Build.VERSION.SDK_INT >= 31) {

        callback =
            object : TelephonyCallback(), UserMobileDataStateListener {

              override fun onUserMobileDataStateChanged(enabled: Boolean) {
                trySend(enabled)
              }
            }

        telephonyManager.registerTelephonyCallback(exec, callback)
      } else {
        Log.d(tag, "State: UNDER SDK 30")

        mObserver =
            object : ContentObserver(Handler()) {
              override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(selfChange)
              }
            }

        val mobileDataSettingUri: Uri = Settings.Secure.getUriFor("mobile_data")

        getApplicationContext()
            .getContentResolver()
            .registerContentObserver(mobileDataSettingUri, true, mObserver)
      }

      awaitClose {
        Log.d(tag, "!!!!CLEAR!!!!")

        if (Build.VERSION.SDK_INT >= 31) {
          telephonyManager.unregisterTelephonyCallback(callback)
        } else {
          getContentResolver().unregisterContentObserver(mObserver)
        }

        exec.shutdown()
      }
    }
  }
}
