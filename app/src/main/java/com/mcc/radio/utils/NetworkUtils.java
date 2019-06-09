package com.mcc.radio.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mcc.radio.data.constant.AppConstants;

public class NetworkUtils {


    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return AppConstants.TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return AppConstants.TYPE_MOBILE;
        }
        return AppConstants.TYPE_NOT_CONNECTED;
    }

    public static int getConnectivityStatusInt(Context context) {
        int status = AppConstants.VALUE_ZERO;
        int conn = NetworkUtils.getConnectivityStatus(context);
        if (conn == AppConstants.TYPE_WIFI) {
            status = AppConstants.TYPE_WIFI;
        } else if (conn == AppConstants.TYPE_MOBILE) {
            status = AppConstants.TYPE_MOBILE;
        } else if (conn == AppConstants.TYPE_NOT_CONNECTED) {
            status = AppConstants.TYPE_NOT_CONNECTED;
        }
        return status;
    }

}
