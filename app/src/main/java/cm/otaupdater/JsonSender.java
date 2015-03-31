package cm.otaupdater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
/**
 * Created by cmdesktop on 02-09-2014.
 */
public class JsonSender extends Thread {

    private int sizeX, sizeY, dpiBucket;
    private double screenInches;
    private String version;
    private String ROM_NAME;
    private String ROM_VERSION;
    private String ROM_WIPEDATA;
    private String ROM_WIPECACHE;
    private Context ctx;
    private String release_type;
    private String post_type;
    //private Handler handler = new Handler();

    public JsonSender(Context ctx, String post_type) {
        this.ctx = ctx;
        this.version = ctx.getResources().getString(R.string.OTA_VERSION);
        this.release_type = "FINAL";
        String tempbuild[]= Utils.readFromBuilprop();
        ROM_NAME = tempbuild[0];
        ROM_VERSION = tempbuild[1];
        ROM_WIPEDATA = tempbuild[2];
        ROM_WIPECACHE = tempbuild[3];
        if(tempbuild[4]!=null && Integer.parseInt(tempbuild[4])==1)
            this.release_type = "DEV";
        this.post_type = post_type;
        screenInches = 0;
        dpiBucket = 0;
        sizeX = 0;
        sizeY = 0;
    }

    @Override
    public void run() {
        JSONObject json = new JSONObject();
        Calendar calendar = Calendar.getInstance();
        getScreenProperties();

        Message msg = Message.obtain();

        DecimalFormat twoDigitFormatter = new DecimalFormat("00");

        try {
            json.put(
                    "date",
                    calendar.get(Calendar.YEAR)
                            + "-" +
                            twoDigitFormatter.format(calendar
                                    .get(Calendar.MONTH)+1)
                            + '-' +
                            (twoDigitFormatter.format(calendar
                                    .get(Calendar.DAY_OF_MONTH)))
                            + " " +
                            twoDigitFormatter.format(calendar
                                    .get(Calendar.HOUR_OF_DAY))
                            + ":" +
                            twoDigitFormatter.format(calendar
                                    .get(Calendar.MINUTE)));
            json.put("OTA_version", version);
            json.put("post_type", post_type);
            json.put("rom_name", ROM_NAME);
            json.put("rom_version", ROM_VERSION);
            json.put("rom_wipedata", ROM_WIPEDATA);
            json.put("rom_wipecache", ROM_WIPECACHE);
            json.put("release_type", release_type);  //DEV = desenvolvimento, pode ser alterado no build.prop
            json.put("manufacturer", Build.MANUFACTURER);
            json.put("model", Build.MODEL);
            json.put("imei", getDeviceId());
            json.put("sn", (Build.SERIAL == "unknown") ? "null" : Build.SERIAL);
            json.put("mac", getWifiMac());
            json.put("language", Locale.getDefault().toString());
            json.put("osversion", android.os.Build.VERSION.SDK_INT);
            json.put("oskernel", System.getProperty("os.version"));
            json.put("resolution", sizeX + "x" + sizeY);
            json.put("inches", screenInches);
            json.put("screentype", getScreenType());
            json.put("dpibucket", dpiBucket);

            String output = SendHttpPost("http://ws.safeexplorers.com/checkrom/stats", json);

            //Log.d("RESPONSE", output);
            try{
                if (output.equals("")) {
                    msg.what = 1;
                } else {
                    msg.what = 0;
                }
            }catch(NullPointerException e){
                msg.what = -1;
            }
            ///handler.sendMessage(msg);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private String getScreenType(){
        if ((ctx.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_SMALL) {
            return "small";
        }
        else if ((ctx.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            return "normal";
        }
        else if ((ctx.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE) {
            return "large";
        }
        else if ((ctx.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
            return "xlarge";
        }
        return "undefined";
    }

    public static String getScreenType(Context ctx){
        if ((ctx.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_SMALL) {
            return "small";
        }
        else if ((ctx.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            return "normal";
        }
        else if ((ctx.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE) {
            return "large";
        }
        else if ((ctx.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
            return "xlarge";
        }
        return "undefined";
    }

    /**
     * Private method that gets the screen resolution, approximate screen size
     * and dpi bucket
     */
    private void getScreenProperties() {
        WindowManager windowManager = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
            try {
                sizeX = (Integer) Display.class.getMethod("getRawWidth")
                        .invoke(display);
                sizeY = (Integer) Display.class.getMethod("getRawHeight")
                        .invoke(display);
            } catch (Exception ignored) {
            }
        if (Build.VERSION.SDK_INT >= 17)
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(
                        display, realSize);
                sizeX = realSize.x;
                sizeY = realSize.y;
            } catch (Exception ignored) {
            }

        // GET APROX SCREEN INCH AND DPI BUCKET
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        dpiBucket = dm.densityDpi;
        double x = Math.pow(sizeX / dm.xdpi, 2);
        double y = Math.pow(sizeY / dm.ydpi, 2);
        screenInches = Math.sqrt(x + y);
        screenInches = (double) Math.round(screenInches * 10) / 10;
    }

    /**
     * Private method that returns the MAC address of the wireless card
     *
     * @return MAC address
     */
    private String getWifiMac() {
        WifiManager manager = (WifiManager) ctx
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        return info.getMacAddress();
    }

    private String getDeviceId() {
        final String deviceId = ((TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (deviceId != null) {
            return deviceId;
        } else {
            return "null";
        }
    }

    private String SendHttpPost(String URL, JSONObject jsonData) {

        HttpPost request = new HttpPost(URL);
        JSONObject returnedJObject;
        try {
            returnedJObject = new JSONObject(jsonData.toString());
            JSONStringer json = new JSONStringer();
            StringBuilder sb = new StringBuilder();

            if (returnedJObject != null) {
                Iterator<String> itKeys = returnedJObject.keys();
                if (itKeys.hasNext())
                    json.object();
                while (itKeys.hasNext()) {
                    String k = itKeys.next();
                    json.key(k).value(returnedJObject.get(k));
                    // Log.e("keys "+k,"value "+returnedJObject.get(k).toString());
                }
            }
            json.endObject();

            StringEntity entity;

            entity = new StringEntity(json.toString());

            entity.setContentType("application/json;charset=UTF-8");
            request.setHeader("Accept", "application/json");
            request.setEntity(entity);

            HttpResponse response = null;
            DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpConnectionParams.setSoTimeout(httpClient.getParams(), 30000);
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
                    50000);

            response = httpClient.execute(request);

            InputStream in;
            in = response.getEntity().getContent();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static boolean hasInternetConnection(Context ctx) {
        ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(ctx.CONNECTIVITY_SERVICE);

        boolean is3g = false;
        if (manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
            is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    .isConnectedOrConnecting();
        }

        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();

        return is3g || isWifi;
    }

}