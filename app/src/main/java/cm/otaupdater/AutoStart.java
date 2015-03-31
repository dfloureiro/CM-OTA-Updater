package cm.otaupdater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Código onboot
 *
 * Created by Diogo Loureiro on 11-08-2014.
 */

public class AutoStart extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            SharedPreferences preferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
            int RADIOGROUP_VALUE = preferences.getInt("CHECK_EVERY",2);
            CheckServiceStartup.setAutoCheckService(context, RADIOGROUP_VALUE, true);
        }
    }



}