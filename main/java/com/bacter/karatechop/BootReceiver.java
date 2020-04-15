package com.bacter.karatechop;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MyService.class));
        Toast.makeText(context, "Shake ativado", Toast.LENGTH_SHORT).show();
        Log.e("service", "Service iniciado");
    }
}
