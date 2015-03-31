package cm.otaupdater;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by cmdesktop on 09-09-2014.
 */
public class Utils {

    public static int compareVersions(String v1, String v2) {
        v1 = v1.replaceAll("\\s", "");
        v2 = v2.replaceAll("\\s", "");
        String[] a1 = v1.split("\\.");
        String[] a2 = v2.split("\\.");
        List<String> l1 = Arrays.asList(a1);
        List<String> l2 = Arrays.asList(a2);


        int i = 0;
        while (true) {
            Double d1 = null;
            Double d2 = null;

            try {
                d1 = Double.parseDouble(l1.get(i));
            } catch (IndexOutOfBoundsException e) {
            }

            try {
                d2 = Double.parseDouble(l2.get(i));
            } catch (IndexOutOfBoundsException e) {
            }

            if (d1 != null && d2 != null) {
                if (d1.doubleValue() > d2.doubleValue()) {
                    return 1;
                } else if (d1.doubleValue() < d2.doubleValue()) {
                    return -1;
                }
            } else if (d2 == null && d1 != null) {
                if (d1.doubleValue() > 0) {
                    return 1;
                }
            } else if (d1 == null && d2 != null) {
                if (d2.doubleValue() > 0) {
                    return -1;
                }
            } else {
                break;
            }
            i++;
        }
        return 0;
    }

    public static String[] readFromBuilprop() {

        String[] temp_final = {null, null, null, null, null, null};
        temp_final[0] = null;
        try {
            FileInputStream inputStream = new FileInputStream(new File("/system/build.prop"));

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                String[] temp;

                while ((receiveString = bufferedReader.readLine()) != null) {
                    if (receiveString.contains("cm.ROM.name")) {
                        temp = receiveString.split("=");
                        String DEVICE_ROM_NAME = temp[temp.length - 1];
                        temp_final[0] = DEVICE_ROM_NAME;
                    }
                    if (receiveString.contains("cm.ROM.version")) {
                        temp = receiveString.split("=");
                        String DEVICE_ROM_VERSION = temp[temp.length - 1];
                        temp_final[1] = DEVICE_ROM_VERSION;
                    }
                    if (receiveString.contains("cm.ROM.wipedata")) {
                        temp = receiveString.split("=");
                        String DEVICE_ROM_WIPE_DATA = temp[temp.length - 1];
                        temp_final[2] = DEVICE_ROM_WIPE_DATA;
                    }
                    if (receiveString.contains("cm.ROM.wipecache")) {
                        temp = receiveString.split("=");
                        String DEVICE_ROM_WIPE_CACHE = temp[temp.length - 1];
                        temp_final[3] = DEVICE_ROM_WIPE_CACHE;
                    }
                    if (receiveString.contains("cm.ROM.type")) {
                        temp = receiveString.split("=");
                        String DEVICE_ROM_TYPE = temp[temp.length - 1];
                        temp_final[4] = DEVICE_ROM_TYPE;
                    }
                    if (receiveString.contains("cm.ROM.recoverysdcard")) {
                        temp = receiveString.split("=");
                        String DEVICE_ROM_RECOVERYSDCARD = temp[temp.length - 1];
                        temp_final[5] = DEVICE_ROM_RECOVERYSDCARD;
                    }
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return temp_final;
    }

    public static void createDownloadFolder() {
        String[] directories = getStorageDirectories();
        File direct = new File(directories[directories.length - 1] + "/.CM_OTA_updater/");
        if (!direct.exists()) {
            direct.mkdirs();
        }
    }

    public static boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) HandleDownloadProgress.getM().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /**
     * verifica se a atividade esta aberta e a ser vista pelo utilizador
     *
     * @return true se estiver com o ecra ligado e com a atividade on screen, false para todos os restantes casos
     */
    public static boolean isActivityVisible() {
        if (HandleDownloadProgress.getM().getApplicationContext() != null) {
            Class klass = HandleDownloadProgress.getM().getClass();
            while (klass != null) {
                try {
                    Field field = klass.getDeclaredField("mResumed");
                    field.setAccessible(true);
                    Object obj = field.get(HandleDownloadProgress.getM());
                    return (Boolean) obj;

                } catch (NoSuchFieldException exception1) {
//                Log.e(TAG, exception1.toString());
                } catch (IllegalAccessException exception2) {
//                Log.e(TAG, exception2.toString());
                }
                klass = klass.getSuperclass();
            }
        }
        return false;
    }

    public static void deleteDownloadDir(File file) {
        if (file.isDirectory())
            for (String child : file.list())
                deleteDownloadDir(new File(file, child));
        file.delete();
    }

    public static boolean checkMd5OfFile(final String filePath, String server_md5) {
        final String[] returnVal = {""};
        Thread mythread = new Thread() {
            @Override
            public void run() {
                try {
                    InputStream input = new FileInputStream(filePath);
                    byte[] buffer = new byte[1024];
                    MessageDigest md5Hash = MessageDigest.getInstance("MD5");
                    int numRead = 0;
                    while (numRead != -1) {
                        numRead = input.read(buffer);
                        if (numRead > 0) {
                            md5Hash.update(buffer, 0, numRead);
                        }
                    }
                    input.close();

                    byte[] md5Bytes = md5Hash.digest();
                    for (int i = 0; i < md5Bytes.length; i++) {
                        returnVal[0] += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        mythread.start();
        try {
            mythread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return returnVal[0].equals(server_md5);

    }


    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");

    public static String[] getStorageDirectories() {
        // Final set of paths
        final Set<String> rv = new HashSet<String>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPORATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        return rv.toArray(new String[rv.size()]);
    }

    public static boolean checkFileExists(String filePathString) {
        File f = new File(filePathString);
        f.exists();
        return f.exists();
    }
}