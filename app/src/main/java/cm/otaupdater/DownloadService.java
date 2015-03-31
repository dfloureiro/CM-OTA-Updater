package cm.otaupdater;

import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by cmdesktop on 19-08-2014.
 */
public class DownloadService extends Service {

    private Timer MY_TIMER;
    private DownloadManager DOWNLOAD_MANAGER;
    private BroadcastReceiver BROADCAST_RECEIVER;
    private int dl_progress;
    private Long DOWNLOAD_NUMBER;
    private boolean IN_PROGRESS;


    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    public void onDestroy() {
    }

    public void onCreate() {
        //iniciar variaveis
        DOWNLOAD_MANAGER = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DOWNLOAD_NUMBER = Long.valueOf(0);
        IN_PROGRESS = false;


        BROADCAST_RECEIVER = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("broadcast",action+"");

                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(DOWNLOAD_NUMBER);
                    final Cursor c = DOWNLOAD_MANAGER.query(query);

                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            Log.d("Download sucessful","DM status sucess!");
                        }
                        if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {
                            stopDownload();
                        }
                    }

                    c.close();

                }
            }
        };
        useProgressBar();

    }


    private void initDownload() {
        if (Utils.isOnline()) {
            HandleJSON.postData(getApplicationContext(),"Download");
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(Mainscreen.getServerInfoRom_file()));

            String[] a = Utils.getStorageDirectories();
            File root = new File(a[a.length-1] + getResources().getString(R.string.DOWNLOAD_FOLDER_NAME));
            if (!root.exists())
                root.mkdir();

            Log.d("root",root+"");
            Uri path = Uri.withAppendedPath(Uri.fromFile(root), HandleDownloadProgress.getDOWNLOAD_FILE_NAME());
            Log.d("path",path+"");
            request.setDestinationUri(path);

            Long enqueue = DOWNLOAD_MANAGER.enqueue(request);
            HandleDownloadProgress.setCURRENT_DOWNLOAD_IDNUMBER(enqueue);
            DOWNLOAD_NUMBER = enqueue;
            IN_PROGRESS = true;
        }
    }

    public void stopDownload() {
        Log.d("service","stopdownload");
        MY_TIMER.cancel();
        DOWNLOAD_MANAGER.remove(DOWNLOAD_NUMBER);
        HandleDownloadProgress.setCURRENT_DOWNLOAD_IDNUMBER(Long.valueOf(0));
        HandleDownloadProgress.setSTART_DOWNLOADSERVICE_NOW(false);
        IN_PROGRESS = false;
        stopSelf();
    }



    private void useProgressBar() {
        MY_TIMER = new Timer();
        MY_TIMER.schedule(new TimerTask() {

            @Override
            public void run() {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(DOWNLOAD_NUMBER);
                Cursor cursor = DOWNLOAD_MANAGER.query(q);
                cursor.moveToFirst();

                try {
                    if (HandleDownloadProgress.getSTART_DOWNLOADSERVICE_NOW()) {
                        if (!IN_PROGRESS) {
                            initDownload();
                        } else {

                            int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                            if ((int) ((bytes_downloaded * 100l) / bytes_total) != 0)
                                dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if(dl_progress<100)
                                    Mainscreen.setProgressbar(dl_progress);

                                    else{
                                        MY_TIMER.cancel();
                                        HandleDownloadProgress.setSTART_DOWNLOADSERVICE_NOW(false);
                                        IN_PROGRESS = false;
                                        Mainscreen.setProgressbar(dl_progress);
                                        stopSelf();
                                    }
                                }
                            }).start();

                            if (HandleDownloadProgress.getCURRENT_DOWNLOAD_IDNUMBER() == 0 || !Utils.isOnline()) {
                                stopDownload();
                            }

                        }
                    }


                } catch (Exception e) {
                    Log.d("timer",e+"");
                }
                cursor.close();
            }
        }, 0, 1000);

    }
}
