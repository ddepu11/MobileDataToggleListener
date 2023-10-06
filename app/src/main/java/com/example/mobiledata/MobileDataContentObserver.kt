package com.example.mobiledata

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log

class MobileDataContentObserver(context: Context, onChange: (mobileDataOn: Boolean) -> Unit) {
    private val mobileDataUri: Uri = Settings.Secure.getUriFor("mobile_data");
    val handleChange = onChange;
    val appContext = context;

    val mobileDataChangeObserver: ContentObserver =
      object : ContentObserver(Handler(Looper.myLooper()!!)) {
            override fun onChange(selfChange: Boolean) {
                Log.d("NEW_TAG", "Mobile Data changed getting new status");

                handleChange(getCurrentMobileStatus());
            }
        }

    fun getCurrentMobileStatus(): Boolean {
        val resolver = appContext.contentResolver;
        val mobileDataStatus = Settings.Secure.getInt(resolver, "mobile_data");

        return mobileDataStatus == 1;
    }

    fun register() {
        val contentResolver = appContext.contentResolver
        contentResolver.registerContentObserver(mobileDataUri, false, mobileDataChangeObserver)
    }

    fun unregister() {
        val contentResolver = appContext.contentResolver
        contentResolver.unregisterContentObserver(mobileDataChangeObserver)
    }
}