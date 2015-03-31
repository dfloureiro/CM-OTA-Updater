package cm.otaupdater;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 *
 * Trata de iniciar, parar e todas as outras acções que envolvem o serviço de notificações da existência de novos updates.
 *
 * Created by Diogo Loureiro on 11-08-2014.
 */
public class CheckServiceStartup{

    public static void setAutoCheckService(Context context, int RADIOGROUP_VALUE, boolean BOOTTING){

        int Time_seconds = 0;
        //dependendo da valor selecionado nos settings, o alarm vai ter comportamentos diferentes
        switch(RADIOGROUP_VALUE){
            case 1:
                Time_seconds = 3600;
                break;
            case 2:
                Time_seconds = 86400;
                break;
            case 3:
                Time_seconds = 604800;
                break;
        }

        if(Time_seconds!=0 || (Time_seconds==0 && BOOTTING)) {
            Calendar cal = Calendar.getInstance();

            Intent intent = new Intent(context, AutoCheckService.class);
            PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);

            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if(!BOOTTING)
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()+Time_seconds * 1000, Time_seconds * 1000, pintent);
            else
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), Time_seconds * 1000, pintent);

            if(Time_seconds==0 && BOOTTING){
                stopAutoCheckService(context);
            }
        }
    }

    //parar o alarm, usado quando o utilizador tem a opção "nunca" nos settings
    public static void stopAutoCheckService(Context context){

        Intent intent = new Intent(context, AutoCheckService.class);
        PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
    }


}
