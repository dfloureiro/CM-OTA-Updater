package cm.otaupdater;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Timer;
import java.util.logging.Handler;

/**
 * Created by cmdesktop on 11-08-2014.
 */
public class AutoCheckService extends Service {


    private String SERVER_INFO_VERSION;
    private String DEVICE_ROM_NAME;
    private String DEVICE_ROM_VERSION;
    private boolean STOP_CHECK;
    private boolean SUPPORTED_DEVICE;
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    public void onDestroy() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            if (!STOP_CHECK) {
                start();
            }

        STOP_CHECK = false;
        return START_STICKY;
    }

    public void onCreate(){
        SharedPreferences preferences = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        int SWITCH_STARTUP_VALUE = preferences.getInt("CHECK_STARTUP",1);
        if(SWITCH_STARTUP_VALUE==1) {
            STOP_CHECK = false;
            SystemClock.sleep(1500);
            //start();
        }
        else{
            STOP_CHECK = true;
        }
    }


    private void start(){

        if(isOnline()) {
            String temp[] = Utils.readFromBuilprop();
            if(temp[0]!=null) {
                DEVICE_ROM_NAME = temp[0];
                DEVICE_ROM_VERSION = temp[1];
                temp = HandleJSON.checkForUpdates(getResources().getString(R.string.SERVER_URL), DEVICE_ROM_NAME);
                if(temp[0]!=null) {
                    SERVER_INFO_VERSION = temp[1];
                    if (Utils.compareVersions(DEVICE_ROM_VERSION,SERVER_INFO_VERSION)==-1) {
                        SUPPORTED_DEVICE = true;
                        showNotification();
                    }
                }
            }
            else{
                SUPPORTED_DEVICE = false;
                showNotification();
            }
        }
    }

    private void showNotification(){

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(SUPPORTED_DEVICE) {
            Intent notificationIntent = new Intent(this, Mainscreen.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notificationIntent.putExtra("notificationclick",true);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification n = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.notification_text_title))
                    .setContentText(getResources().getString(R.string.notification_text_subject))
                    .setSmallIcon(R.drawable.caixamagicalogo)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(0, n);
        }

        else{
            Notification n = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.notification_not_supported_text_title))
                    .setContentText(getResources().getString(R.string.notification_not_supported_text_subject))
                    .setSmallIcon(R.drawable.caixamagicalogo)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(0, n);
        }

        new JsonSender(this,"notification").start();

        if(readSoundSettings()>0) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(this, notification);
            r.play();
        }
    }


    //precisa de estar aqui devido ao contexto do serviço
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&  cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    //precisa de estar aqui devido ao contexto do serviço
    private int readSoundSettings() {
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        int PREDIFINED_SWITCH_SOUND_VALUE = 1;
        return preferences.getInt("NOT_SOUND", PREDIFINED_SWITCH_SOUND_VALUE);
    }
}