package com.equadriga.mapcomponent.Utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import com.equadriga.mapcomponent.R

object Utilities {

    /*  for progress bar*/
    private var isAlertShowing: Boolean = false
    private var animation: Animation? = null
    private var dialog: Dialog? = null

    /* show progress*/
    fun showProgress(context: Activity) {
        if (!isAlertShowing && !context.isFinishing) {
            animation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation)
            dialog = Dialog(context)
            dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog!!.setTitle("")
            dialog!!.setContentView(R.layout.dialog_layout)
            dialog!!.setCancelable(false)
            val progress = dialog!!.findViewById(R.id.login_complete_progress) as ProgressBar
            progress.startAnimation(animation)
            dialog!!.show()
            isAlertShowing = true

        }
    }

    /* dismiss progress*/
    fun dismissProgress(context: Activity) {
        if (dialog != null && context != null) {
            if (dialog!!.isShowing)
                dialog!!.dismiss()
            isAlertShowing = false
        }
    }

    fun isConnectivity(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val nw = cm.activeNetwork ?: return false
            val actNw = cm.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val nwInfo = cm.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }
}