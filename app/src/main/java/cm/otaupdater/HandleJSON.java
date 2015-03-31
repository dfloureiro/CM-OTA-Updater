package cm.otaupdater;

/**
 * Created by cmdesktop on 22-07-2014.
 */
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class HandleJSON {
    private String name = "name";
    private String versionS = "version";
    private String rom_file = "rom_file";
    private String rom_md5 = "rom_md5";
    private String changelog = "changelog";
    private static String urlString = null;

    private static int sizeX, sizeY, dpiBucket;
    private static double screenInches;
    private static String screenType;
    private static String getWifiMac;
    private String version;
    private Context ctx;
    private Handler handler;

    public volatile boolean parsingComplete = true;
    public boolean connStatusFailed = false;
    final Timer myTimer = new Timer();

    public HandleJSON(String url) {
        this.urlString = url;
    }

    public String getname() {
        return name;
    }

    public String getversion() {
        return versionS;
    }

    public String getrom_file() {
        return rom_file;
    }

    public String getrom_md5() {
        return rom_md5;
    }

    public String getchangelog() {
        return changelog;
    }

    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            JSONObject reader = new JSONObject(in);

            name = reader.getString("name");
            if (!name.equals("null")) {
                versionS = reader.getString("version");
                rom_file = reader.getString("rom_file");
                rom_md5 = reader.getString("rom_md5");
                changelog = reader.getString("changelog");
            }
            myTimer.cancel();
            parsingComplete = false;


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void fetchJSON() {

        myTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(5000 /* milliseconds */);
                    conn.setRequestMethod("GET");

                    //conn.setDoInput(true);
                    //conn.connect();
                    int length = conn.getContentLength();
                    InputStream stream = conn.getInputStream();
                    String data = convertStreamToString(stream);

                    //OutputStream out = conn.getOutputStream();
                    //out.write(Integer.parseInt("hi"));

                    readAndParseJSON(data);
                    stream.close();



                } catch (Exception e) {
                    myTimer.cancel();
                    parsingComplete = false;
                    connStatusFailed = true;
                }
            }
        }, 0, 10);

    }


    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void postData(Context context, String post_type) {

        JsonSender js = new JsonSender(context, post_type);
        js.run();
    }

    public static String[] checkForUpdates(String SERVER_URL, String DEVICE_ROM_NAME){
        String tempbuild[]= Utils.readFromBuilprop();
        String url1 = SERVER_URL+DEVICE_ROM_NAME;
        if(tempbuild[4]!=null && Integer.parseInt(tempbuild[4])==1)
            url1 = SERVER_URL+DEVICE_ROM_NAME+"?dev=1";

        HandleJSON obj;
        obj = new HandleJSON(url1);
        obj.fetchJSON();
        String [] temp_final = new String[7];
        temp_final[0]=null;

        while (obj.parsingComplete) ;
        if (!obj.connStatusFailed) {
            if(!obj.getname().equals("null")) {
                String SERVER_INFO_NAME = obj.getname();
                temp_final [0]= SERVER_INFO_NAME;
                String SERVER_INFO_VERSION = obj.getversion();
                temp_final [1]= SERVER_INFO_VERSION;
                String SERVER_INFO_ROM_FILE = obj.getrom_file();
                temp_final [2]= SERVER_INFO_ROM_FILE;
                String SERVER_INFO_ROM_MD5 = obj.getrom_md5();
                temp_final [3] = SERVER_INFO_ROM_MD5;
                String SERVER_INFO_CHANGELOG = obj.getchangelog();
                temp_final [4]= SERVER_INFO_CHANGELOG;

                //get file name
                String[] temp;
                temp = obj.getrom_file().split("//");
                temp = temp[temp.length-1].split("/");
                String UPDATE_FILE_NAME = temp[temp.length-1];
                String[] directories = Utils.getStorageDirectories();
                String UPDATE_FILE_PATH = directories[directories.length-1]+ "/.CM_OTA_updater"+"/"+UPDATE_FILE_NAME;
                temp_final [5]= UPDATE_FILE_PATH;
                temp_final [6] = UPDATE_FILE_NAME;
            }
        }
        return temp_final;
    }
}