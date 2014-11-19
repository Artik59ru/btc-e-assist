package com.btc_e_assist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutostartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AssistSynchronize.synchronizedWithDB();
        PrefControl.getInstance().setAllPreferences();
    }
}
