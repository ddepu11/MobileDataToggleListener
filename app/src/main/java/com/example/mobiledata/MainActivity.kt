package com.example.mobiledata

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

  lateinit var wifiSwitch: Switch
  lateinit var wifiManager: WifiManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val textView1 = findViewById<TextView>(R.id.textView1)

    title = "KotlinApp"
    wifiSwitch = findViewById(R.id.wifiSwitch)
    wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    wifiSwitch.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        wifiManager.isWifiEnabled = true
        wifiSwitch.text = "WiFi is ON"
      } else {
        wifiManager.isWifiEnabled = false
        wifiSwitch.text = "WiFi is OFF"
      }
    }

    //  Network
    registerReceiver(ConnectivityReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
  }

  override fun onStart() {
    super.onStart()
    val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
    registerReceiver(wifiStateReceiver, intentFilter)
  }

  override fun onStop() {
    super.onStop()
    unregisterReceiver(wifiStateReceiver)
  }

  private val wifiStateReceiver: BroadcastReceiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
            WifiManager.WIFI_STATE_ENABLED -> {
              wifiSwitch.isChecked = true
              wifiSwitch.text = "WiFi is ON"
              Toast.makeText(this@MainActivity, "Wifi is On", Toast.LENGTH_SHORT).show()
            }
            WifiManager.WIFI_STATE_DISABLED -> {
              wifiSwitch.isChecked = false
              wifiSwitch.text = "WiFi is OFF"
              Toast.makeText(this@MainActivity, "Wifi is Off", Toast.LENGTH_SHORT).show()
            }
          }
        }
      }

  override fun onNetworkConnectionChanged(info: NetworkInfo?) {
    //    showNetworkMessage(isConnected)
    Log.d("MESSAGE", info.toString())
  }

  override fun onResume() {
    super.onResume()
    ConnectivityReceiver.connectivityReceiverListener = this
  }

  private fun showNetworkMessage(isConnected: Boolean) {
    if (!isConnected) {
      Toast.makeText(applicationContext, "You are offline!", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(applicationContext, "You are ONLINE!", Toast.LENGTH_LONG).show()
    }
  }
}
