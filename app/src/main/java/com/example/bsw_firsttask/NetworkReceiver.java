package com.example.bsw_firsttask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.bsw_firsttask.Activity.MainActivity;


public class NetworkReceiver extends BroadcastReceiver {

    private ConnectivityReceiverListener connectivityReceiverListener;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnected();

        if (connectivityReceiverListener != null) {
                connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }
    }

    public boolean isConnected(Context context) {
        ConnectivityManager
                cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnected();
    }

    public void setConnectivityReceiverListener(ConnectivityReceiverListener connectivityReceiverListener) {
        this.connectivityReceiverListener = connectivityReceiverListener;
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
