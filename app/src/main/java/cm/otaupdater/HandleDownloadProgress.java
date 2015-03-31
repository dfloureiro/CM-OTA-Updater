package cm.otaupdater;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cmdesktop on 20-08-2014.
 */
public class HandleDownloadProgress {

    private static Long CURRENT_DOWNLOAD_IDNUMBER;
    private static boolean START_DOWNLOADSERVICE_NOW;
    private static int ALERTDIALOG_SELECTED;
    private static String DOWNLOAD_FILE_NAME;
    private static boolean INSTALL_AFTER_DOWNLOAD;
    private static Mainscreen m;
    private static String FILE_PATH;
    private static String SERVER_MD5;
    private static boolean ACTIVITY_OPEN_BY_NOTIFICATION;

    public static Long getCURRENT_DOWNLOAD_IDNUMBER() {
        return CURRENT_DOWNLOAD_IDNUMBER;
    }

    public static void setCURRENT_DOWNLOAD_IDNUMBER(Long e) {
        CURRENT_DOWNLOAD_IDNUMBER = e;
    }

    public static boolean getSTART_DOWNLOADSERVICE_NOW() {return START_DOWNLOADSERVICE_NOW; }

    public static void setSTART_DOWNLOADSERVICE_NOW(boolean START_DOWNLOADSERVICE_NOW) {HandleDownloadProgress.START_DOWNLOADSERVICE_NOW = START_DOWNLOADSERVICE_NOW; }

    public static int getALERTDIALOG_SELECTED() {return ALERTDIALOG_SELECTED; }

    public static void setALERTDIALOG_SELECTED(int ALERTDIALOG_SELECTED) {HandleDownloadProgress.ALERTDIALOG_SELECTED = ALERTDIALOG_SELECTED;}

    public static String getDOWNLOAD_FILE_NAME() {return DOWNLOAD_FILE_NAME; }

    public static void setDOWNLOAD_FILE_NAME(String DOWNLOAD_FILE_NAME) {HandleDownloadProgress.DOWNLOAD_FILE_NAME = DOWNLOAD_FILE_NAME;}

    public static boolean getINSTALL_AFTER_DOWNLOAD() {
        return INSTALL_AFTER_DOWNLOAD;
    }

    public static void setINSTALL_AFTER_DOWNLOAD(boolean INSTALL_AFTER_DOWNLOAD) {
        HandleDownloadProgress.INSTALL_AFTER_DOWNLOAD = INSTALL_AFTER_DOWNLOAD;
    }

    public static Mainscreen getM() {
        return m;
    }

    public static void setM(Mainscreen m) {
        HandleDownloadProgress.m = m;
    }


    public static String getSERVER_MD5() {
        return SERVER_MD5;
    }

    public static void setSERVER_MD5(String SERVER_MD55) {
        SERVER_MD5 = SERVER_MD55;
    }

    public static String getFILE_PATH() {
        return FILE_PATH;
    }

    public static void setFILE_PATH(String FILE_PATHH) {
        FILE_PATH = FILE_PATHH;
    }


    public static boolean getACTIVITY_OPEN_BY_NOTIFICATION() {
        return ACTIVITY_OPEN_BY_NOTIFICATION;
    }

    public static void setACTIVITY_OPEN_BY_NOTIFICATION(boolean ACTIVITY_OPEN_BY_NOTIFICATION) {
        HandleDownloadProgress.ACTIVITY_OPEN_BY_NOTIFICATION = ACTIVITY_OPEN_BY_NOTIFICATION;
    }
}
