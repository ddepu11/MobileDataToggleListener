package com.example.mobiledata

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectivityReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {

    if (connectivityReceiverListener != null) {
      connectivityReceiverListener!!.onNetworkConnectionChanged(isConnectedOrConnecting(context!!))
    }
  }

  @SuppressLint("ServiceCast")
  private fun isConnectedOrConnecting(context: Context): NetworkInfo? {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
//    return networkInfo != null && networkInfo.isConnectedOrConnecting

    return networkInfo
  }

  interface ConnectivityReceiverListener {
    fun onNetworkConnectionChanged(isConnected: NetworkInfo?)
  }

  companion object {
    var connectivityReceiverListener: ConnectivityReceiverListener? = null
  }
}
