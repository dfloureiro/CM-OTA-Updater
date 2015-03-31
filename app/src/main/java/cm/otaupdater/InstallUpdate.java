package cm.otaupdater;

import android.app.Activity;
import android.app.FragmentManager;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * código da instalação do update
 * Created by Diogo Loureiro on 19-08-2014.
 */
public class InstallUpdate {

    public static void installUpdate(){
    try {
            String temp[] = Utils.readFromBuilprop();
            Runtime r = Runtime.getRuntime();
            Process process = r.exec("voldemort");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            os.writeBytes("rm -f /cache/recovery/command\n");
            if(Integer.parseInt(temp[2])>0)
                os.writeBytes("echo '--wipe_data' >> /cache/recovery/command\n");
            if(Integer.parseInt(temp[3])>0)
            os.writeBytes("echo '--wipe_cache' >> /cache/recovery/command\n");
            if(temp[5]!=null)
                os.writeBytes("echo '--update_package=/" + temp[5] +"/.CM_OTA_updater/"+ HandleDownloadProgress.getDOWNLOAD_FILE_NAME() + "' >> /cache/recovery/command\n");

            else
                os.writeBytes("echo '--update_package=" + "/sdcard" +"/.CM_OTA_updater/"+ HandleDownloadProgress.getDOWNLOAD_FILE_NAME() + "' >> /cache/recovery/command\n");

            os.writeBytes("sync\n");
            os.writeBytes("reboot recovery\n");

            os.flush();
            os.close();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
