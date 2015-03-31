package cm.otaupdater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;


public class Mainscreen extends Activity {

    private static String SERVER_INFO_NAME;
    private static String SERVER_INFO_VERSION;
    private static String SERVER_INFO_ROM_FILE;
    private static String SERVER_INFO_ROM_MD5;
    private static String SERVER_INFO_CHANGELOG;
    private static String DEVICE_ROM_NAME;
    private static String DEVICE_ROM_VERSION;
    private static String UPDATE_FILE_NAME;
    private static String UPDATE_FILE_PATH;
    private static ProgressDialog progressDialog;
    private static File DOWNLOADS_FOLDER;



    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainscreen);

        //iniciar variaveis
        TextView TEXTVIEW_WELCOME_MESSAGE = (TextView) findViewById(R.id.WelcomeMsg);
        TextView TEXTVIEW_DESCRIPTION = (TextView) findViewById(R.id.description);
        TextView TEXTVIEW_DEVICE_INFO = (TextView) findViewById(R.id.device_info);
        TextView TEXTVIEW_INFO_DEVICE = (TextView) findViewById(R.id.info_device);
        TextView TEXTVIEW_INFO_DEVICE_NAME = (TextView) findViewById(R.id.info_device_name);
        TextView TEXTVIEW_ROM = (TextView) findViewById(R.id.info_rom);
        TextView TEXTVIEW_ROM_NAME = (TextView) findViewById(R.id.info_rom_name);
        TextView TEXTVIEW_VERSION = (TextView) findViewById(R.id.info_version);
        TextView TEXTVIEW_VERSION_NAME = (TextView) findViewById(R.id.info_version_name);
        TextView TEXTVIEW_ANDROID_VERSION = (TextView) findViewById(R.id.info_android_version);
        TextView TEXTVIEW_ANDROID_VERSION_NAME = (TextView) findViewById(R.id.info_android_version_name);
        Button BUTTON_CHECK_NOW = (Button) findViewById(R.id.ButtonCheckNow);
        //texto e traducoes
        TEXTVIEW_WELCOME_MESSAGE.setText(getResources().getString(R.string.mainscreen_welcomeMessage));
        TEXTVIEW_DESCRIPTION.setText(getResources().getString(R.string.mainscreen_description));
        TEXTVIEW_DEVICE_INFO.setText(getResources().getString(R.string.mainscreen_device_title));
        TEXTVIEW_INFO_DEVICE.setText(getResources().getString(R.string.mainscreen_info_device));
        TEXTVIEW_INFO_DEVICE_NAME.setText(Build.DEVICE);
        TEXTVIEW_ROM.setText(getResources().getString(R.string.mainscreen_info_rom));

        TEXTVIEW_VERSION.setText(getResources().getString(R.string.mainscreen_info_version));
        TEXTVIEW_ANDROID_VERSION.setText(getResources().getString(R.string.mainscreen_info_android_version));
        TEXTVIEW_ANDROID_VERSION_NAME.setText(Build.VERSION.RELEASE);
        BUTTON_CHECK_NOW.setText(getResources().getString(R.string.mainscreen_checknow));


        String[] directories = Utils.getStorageDirectories();
        DOWNLOADS_FOLDER = new File(directories[directories.length-1] + getResources().getString(R.string.DOWNLOAD_FOLDER_NAME)+ "/");
        DEVICE_ROM_VERSION = "0";
        DEVICE_ROM_NAME = null;
        UPDATE_FILE_NAME = null;
        UPDATE_FILE_PATH = null;
        progressDialog = new ProgressDialog(this);
        HandleDownloadProgress.setM(this);

        String temp[] = Utils.readFromBuilprop();
        if (temp[0] != null) {
            DEVICE_ROM_NAME = temp[0];
            DEVICE_ROM_VERSION = temp[1];
        }

        if(DEVICE_ROM_NAME!=null) {
            TEXTVIEW_ROM_NAME.setText(DEVICE_ROM_NAME);
            TEXTVIEW_VERSION_NAME.setText(DEVICE_ROM_VERSION);}
        else {
            TEXTVIEW_ROM_NAME.setText(getResources().getString(R.string.mainscreen_info_notfound));
            TEXTVIEW_VERSION_NAME.setText(getResources().getString(R.string.mainscreen_info_notfound));}

        //notificacao normal de existencia de update faz isto
        if(getIntent().getBooleanExtra("notificationclick",false)){
            HandleDownloadProgress.setACTIVITY_OPEN_BY_NOTIFICATION(true);
            checkProcess();
            getIntent().putExtra("notificationclick",false);
        }
        else{
            HandleDownloadProgress.setACTIVITY_OPEN_BY_NOTIFICATION(false);
        }

        //notificacao de download completo faz isto (apenas aparece quando o utilizador escolheu install after update e tem a app em background
        if(getIntent().getBooleanExtra("notificationDownloadCompleteClick",false)){
            HandleDownloadProgress.setALERTDIALOG_SELECTED(8);
            FragmentManager fragmentManager = getFragmentManager();
            AlertDialogFragment newFragment = new AlertDialogFragment();
            newFragment.show(fragmentManager, "Dialog");
            getIntent().putExtra("notificationDownloadCompleteClick",false);
        }

        //nao aparece o lock screen ao fazer lock na app, deixando aparecer os alertdialogs mesmo com o ecra desligado
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainscreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent openMainActivity = new Intent(Mainscreen.this, Settings.class);
            startActivityForResult(openMainActivity, 0);
            return true;
        }
        if (id == R.id.action_about) {
            HandleDownloadProgress.setALERTDIALOG_SELECTED(6);
            FragmentManager fragmentManager = getFragmentManager();
            AlertDialogFragment newFragment = new AlertDialogFragment();
            newFragment.show(fragmentManager, "Dialog");
            return true;
        }
        if (id == R.id.action_instructions) {
            HandleDownloadProgress.setALERTDIALOG_SELECTED(7);
            FragmentManager fragmentManager = getFragmentManager();
            AlertDialogFragment newFragment = new AlertDialogFragment();
            newFragment.show(fragmentManager, "Dialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method executed by button checknow o click
     * @param view
     *
     */
    public void checkNow(View view) {
        checkProcess();
        new JsonSender(this,"manual").start();
    }


    /**
     * Contacta o servidor para confirmar a mais recente atualização
     * Verifica os updates e a situação atual (update tranferido, por tranferir...) e trata o diferente alertdialog dependendo da situação.
     */
    public void checkProcess(){

        Utils.createDownloadFolder();

        if (Utils.isOnline()) {
            //ler build.prop
            String infoDevice[] = Utils.readFromBuilprop();
            if (infoDevice[0] != null) {
                DEVICE_ROM_NAME = infoDevice[0];
                DEVICE_ROM_VERSION = infoDevice[1];
                //ler info servidor
                infoDevice = HandleJSON.checkForUpdates(getResources().getString(R.string.SERVER_URL), DEVICE_ROM_NAME);
                if (infoDevice[0] != null) {
                    SERVER_INFO_NAME = infoDevice[0];
                    SERVER_INFO_VERSION = infoDevice[1];
                    SERVER_INFO_ROM_FILE = infoDevice[2];
                    SERVER_INFO_ROM_MD5 = infoDevice[3];
                    SERVER_INFO_CHANGELOG = infoDevice[4];
                    UPDATE_FILE_PATH = infoDevice[5];
                    UPDATE_FILE_NAME = infoDevice[6];
                    HandleDownloadProgress.setDOWNLOAD_FILE_NAME(infoDevice[6]);
                    HandleDownloadProgress.setFILE_PATH(infoDevice[5]);
                    HandleDownloadProgress.setSERVER_MD5(infoDevice[3]);


                    //compara versoes
                    if (Utils.compareVersions(DEVICE_ROM_VERSION, SERVER_INFO_VERSION)==-1) {
                        //se o ficheiro de update ja exista
                        Log.d("update_file_path", Utils.checkFileExists(UPDATE_FILE_PATH)+"");
                        if (Utils.checkFileExists(UPDATE_FILE_PATH)) {
                            HandleDownloadProgress.setALERTDIALOG_SELECTED(3);
                            showAlertDialog();
                        } else {
                            //caso contrario notifica update
                            HandleDownloadProgress.setALERTDIALOG_SELECTED(0);
                            showAlertDialog();
                        }

                    } else {
                        //se nao encontrou updates
                        if (!SERVER_INFO_NAME.equals("null")) {
                            HandleDownloadProgress.setALERTDIALOG_SELECTED(1);
                            FragmentManager fragmentManager = getFragmentManager();
                            AlertDialogFragment newFragment = new AlertDialogFragment();
                            newFragment.show(fragmentManager, "Dialog");
                        }
                    }
                } else {
                    //falhou a ligar ao server
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_connection_server_failed), Toast.LENGTH_LONG).show();
                }
            } else {
                //o dispositivo nao tem info no build.prop
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_device_not_supported), Toast.LENGTH_LONG).show();
            }

        } else {
            //nao tem ligação à internet
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_no_internet_connection), Toast.LENGTH_LONG).show();
        }
    }


    public static String getServerInfoRom_file() {
        return SERVER_INFO_ROM_FILE;
    }

   public static void setProgressbar(int progress) {
        progressDialog.setProgress(progress);

       //se o disposotivo ficar sem ligação à internet o download é parado de imediato e surge a notificação
        if(!Utils.isOnline()){
            progressDialog.dismiss();

            NotificationManager notificationManager = (NotificationManager) HandleDownloadProgress.getM().getSystemService(NOTIFICATION_SERVICE);
                Intent notificationIntent = new Intent(HandleDownloadProgress.getM().getApplicationContext(), Mainscreen.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pIntent = PendingIntent.getActivity(HandleDownloadProgress.getM().getApplicationContext(), 0, notificationIntent, 0);

                Notification n = new Notification.Builder(HandleDownloadProgress.getM().getApplicationContext())
                        .setContentTitle(HandleDownloadProgress.getM().getResources().getString(R.string.notification_download_failed_text_title))
                        .setContentText(HandleDownloadProgress.getM().getResources().getString(R.string.notification_download_failed_text_subject))
                        .setSmallIcon(R.drawable.caixamagicalogo)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .build();

                notificationManager.notify(0, n);
            if(readSoundSettings()>0) {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(HandleDownloadProgress.getM().getApplicationContext(), notification);
                r.play();
            }
        }
       //se o download chegou ao fim
        if(progressDialog.getProgress()==progressDialog.getMax()) {
            progressDialog.dismiss();


            //se o utilizador escolheu instalar depois de tranferir
            if (HandleDownloadProgress.getINSTALL_AFTER_DOWNLOAD()) {
                Log.d("download finished", "done");
                PowerManager.WakeLock WakeLock1 = ((PowerManager) HandleDownloadProgress.getM().getApplicationContext().getSystemService(HandleDownloadProgress.getM().getApplicationContext().POWER_SERVICE)).newWakeLock(268435482, "WAKEUP");
                WakeLock1.acquire();
                SystemClock.sleep(1000);

                //se a atividade estiver on screen mostra o dialog e não manda notificação
                if(Utils.isActivityVisible()) {

                    HandleDownloadProgress.setALERTDIALOG_SELECTED(8);
                    FragmentManager fragmentManager = HandleDownloadProgress.getM().getFragmentManager();
                    AlertDialogFragment newFragment = new AlertDialogFragment();
                    newFragment.show(fragmentManager, "Dialog");
                }
                //se a atividade nao estiver on screen, envia a notificação
                else{
                    NotificationManager notificationManager = (NotificationManager) HandleDownloadProgress.getM().getSystemService(NOTIFICATION_SERVICE);
                    Intent notificationIntent = new Intent(HandleDownloadProgress.getM().getApplicationContext(), Mainscreen.class);
                    notificationIntent.putExtra("notificationDownloadCompleteClick",true);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                    PendingIntent pIntent = PendingIntent.getActivity(HandleDownloadProgress.getM().getApplicationContext(), 0, notificationIntent, 0);

                    Notification n = new Notification.Builder(HandleDownloadProgress.getM().getApplicationContext())
                            .setContentTitle(HandleDownloadProgress.getM().getResources().getString(R.string.notification_ready_to_install_text_title))
                            .setContentText(HandleDownloadProgress.getM().getResources().getString(R.string.notification_ready_to_install_text_subject))
                            .setSmallIcon(R.drawable.caixamagicalogo)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .build();

                    notificationManager.notify(0, n);
                    HandleDownloadProgress.getM().finish();
                }
                WakeLock1.release();
            }

            //se escolher tranferir apenas é notficado no fim do download que a tranferencia foi concluida com sucesso
            else{
                NotificationManager notificationManager = (NotificationManager) HandleDownloadProgress.getM().getSystemService(NOTIFICATION_SERVICE);

                Notification n = new Notification.Builder(HandleDownloadProgress.getM().getApplicationContext())
                        .setContentTitle(HandleDownloadProgress.getM().getResources().getString(R.string.notification_download_completed_text_title))
                        .setSmallIcon(R.drawable.caixamagicalogo)
                        .setAutoCancel(true)
                        .build();

                notificationManager.notify(0, n);
            }

            if(readSoundSettings()>0) {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(HandleDownloadProgress.getM().getApplicationContext(), notification);
                r.play();
            }
        }
    }

    private static int readSoundSettings() {
        SharedPreferences preferences = HandleDownloadProgress.getM().getSharedPreferences("SETTINGS", MODE_PRIVATE);
        int PREDIFINED_SWITCH_SOUND_VALUE = 1;
        return preferences.getInt("NOT_SOUND", PREDIFINED_SWITCH_SOUND_VALUE);
    }

    //mostra o alertdialog
    public void showAlertDialog(){

        FragmentManager fragmentManager = getFragmentManager();
        AlertDialogFragment newFragment = new AlertDialogFragment();
        newFragment.show(fragmentManager, "Dialog");
        startService(new Intent(getApplicationContext(), DownloadService.class));
    }

    public static File getDOWNLOADS_FOLDER() {
        return DOWNLOADS_FOLDER;
    }



    //mostra o progressdialog com a barra horizontal aquando da tranferencia
    public static class ProgressDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle(getResources().getString(R.string.progressdialog_title));
            progressDialog.setMessage(getResources().getString(R.string.progressdialog_text));
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.downloadmanager_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    HandleDownloadProgress.setCURRENT_DOWNLOAD_IDNUMBER(Long.valueOf(0));
                    dialog.dismiss();
                }
            });
            HandleDownloadProgress.setSTART_DOWNLOADSERVICE_NOW(true);
            return progressDialog;
        }

    }

    public static class ProgressDialogInstallingFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
            progressDialog.setTitle("installing");
            progressDialog.setMessage("Checking md5 and installing");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            return progressDialog;
        }

    }

    //mostra os diferentes alertdialogs possiveis
    public static class AlertDialogFragment extends DialogFragment {

        /**
         * verifica se o utilizador esta ligado por mobile, se sim, avisa com um alertdialog
         */
        private void checkConnectionType() {
            ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (mMobile!= null && mMobile.isConnectedOrConnecting()){
                HandleDownloadProgress.setALERTDIALOG_SELECTED(4);
                FragmentManager fragmentManager = getFragmentManager();
                AlertDialogFragment newFragment = new AlertDialogFragment();
                newFragment.show(fragmentManager, "Dialog");
            }
            else{
                showProgressDialog();
            }
        }

        private void showProgressDialogInstall(){
            FragmentManager fragmentManager = getFragmentManager();
            ProgressDialogInstallingFragment newFragment = new ProgressDialogInstallingFragment();
            newFragment.setCancelable(false);
            newFragment.show(fragmentManager, "Dialog");
        }

        private void showProgressDialog(){
            FragmentManager fragmentManager = getFragmentManager();
            ProgressDialogFragment newFragment = new ProgressDialogFragment();
            newFragment.setCancelable(false);
            newFragment.show(fragmentManager, "Dialog");
        }


        /**
         * mostra os diferentes alertdialogs dependendo do valor atribuido a variavel "alertdialog_selected"
         *
         * case 0: new update found alertdialog
         * case 1: no new updates found alertdialog
         * case 2: downloaded update with error alertdialog
         * case 3: downloaded update found alertdialog
         * case 4: using mobile internet connection alertdialog
         * case 6: about CM OTA Updater alertdialog
         * case 7: instructions alertdialog
         * case 8: download completed and ready to install alertdialog
         *
         * @param savedInstanceState
         * @return
         */
    @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            switch (HandleDownloadProgress.getALERTDIALOG_SELECTED()) {
                case 0:
                TextView textView = new TextView(getActivity());
                textView.setText(Html.fromHtml("<h3>\n" +
                        getResources().getString(R.string.webview_text_title_1) +
                        SERVER_INFO_VERSION +
                        getResources().getString(R.string.webview_text_title_2) +
                        "</h3>\n<h3>\n" +
                        getResources().getString(R.string.webview_text_title_3) +
                        "</h3>" + SERVER_INFO_CHANGELOG));
                textView.setPadding(20,20,20,20);
                textView.setTextSize(15);

                ScrollView scrollView = new ScrollView(getActivity());
                scrollView.addView(textView);

                   AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog
                        .setView(scrollView)
                        .setTitle(getResources().getString(R.string.alertdialog_new_update_found_title))
                        .setPositiveButton(getResources().getString(R.string.alertdialog_download_and_install_option), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                HandleDownloadProgress.setINSTALL_AFTER_DOWNLOAD(true);
                                checkConnectionType();
                            }
                        })
                            .setNegativeButton(getResources().getString(R.string.alertdialog_download_only_option), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    HandleDownloadProgress.setINSTALL_AFTER_DOWNLOAD(false);
                                    checkConnectionType();
                                }
                            });

                 if(HandleDownloadProgress.getACTIVITY_OPEN_BY_NOTIFICATION()) {
                     dialog.setOnKeyListener(new Dialog.OnKeyListener() {

                         @Override
                         public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                             // TODO Auto-generated method stub
                             if (keyCode == KeyEvent.KEYCODE_BACK) {
                                 arg0.cancel();
                                 HandleDownloadProgress.setACTIVITY_OPEN_BY_NOTIFICATION(false);
                                 HandleDownloadProgress.getM().finish();
                             }
                             return true;
                         }
                     });
                 }

                return dialog.create();

                case 1:
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.alertdialog_no_update_found_title))
                            .setMessage(getResources().getString(R.string.alertdialog_no_update_found_text))
                            .setPositiveButton(getResources().getString(R.string.alertdialog_ok_option), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            HandleDownloadProgress.setCURRENT_DOWNLOAD_IDNUMBER(Long.valueOf(0));
                            dialog.cancel();
                        }
                    }).create();

                case 2:
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.alertdialog_downloaded_update_error_found_title))
                            .setMessage(getResources().getString(R.string.alertdialog_downloaded_update_error_found_text))
                            .setPositiveButton(getResources().getString(R.string.alertdialog_download_and_install_option), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    HandleDownloadProgress.setINSTALL_AFTER_DOWNLOAD(true);
                                    Utils.deleteDownloadDir(Mainscreen.getDOWNLOADS_FOLDER());
                                    checkConnectionType();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.alertdialog_download_only_option), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    HandleDownloadProgress.setINSTALL_AFTER_DOWNLOAD(false);
                                    Utils.deleteDownloadDir(Mainscreen.getDOWNLOADS_FOLDER());
                                    checkConnectionType();
                                }
                            }).create();

                case 3:

                    dialog = new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.alertdialog_downloaded_update_found_title))
                            .setMessage(getResources().getString(R.string.alertdialog_downloaded_update_found_text))
                            .setPositiveButton(getResources().getString(R.string.alertdialog_yes_option), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            showProgressDialogInstall();
                            if (Utils.checkMd5OfFile(HandleDownloadProgress.getFILE_PATH(), HandleDownloadProgress.getSERVER_MD5())) {
                                InstallUpdate.installUpdate();
                            } else {
                                HandleDownloadProgress.setALERTDIALOG_SELECTED(2);
                                FragmentManager fragmentManager2 = getFragmentManager();
                                AlertDialogFragment newFragment2 = new AlertDialogFragment();
                                newFragment2.show(fragmentManager2, "Dialog");

                            }
                        }
                    })
                            .setNegativeButton(getResources().getString(R.string.alertdialog_no_option), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if(HandleDownloadProgress.getACTIVITY_OPEN_BY_NOTIFICATION()) {
                                HandleDownloadProgress.setACTIVITY_OPEN_BY_NOTIFICATION(false);
                                HandleDownloadProgress.getM().finish();
                            }
                        }
                    });

                if(HandleDownloadProgress.getACTIVITY_OPEN_BY_NOTIFICATION()) {
                    dialog.setOnKeyListener(new Dialog.OnKeyListener() {

                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                            // TODO Auto-generated method stub
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                arg0.cancel();
                                HandleDownloadProgress.setACTIVITY_OPEN_BY_NOTIFICATION(false);
                                HandleDownloadProgress.getM().finish();
                            }
                            return true;
                        }
                    });

                }

                    return dialog.create();

                case 4:
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.alertdialog_using_mobile_internet_connection_title))
                            .setMessage(getResources().getString(R.string.alertdialog_using_mobile_internet_connection_text))
                            .setPositiveButton(getResources().getString(R.string.alertdialog_yes_option), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    showProgressDialog();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.alertdialog_no_option), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    HandleDownloadProgress.setCURRENT_DOWNLOAD_IDNUMBER(Long.valueOf(0));
                                    dialog.cancel();
                                }
                            }).create();

                case 6:

                    final TextView message = new TextView(getActivity());
                    final SpannableString s = new SpannableString(getActivity().getText(R.string.alertdialog_about_CMOTAupdater_text));
                    Linkify.addLinks(s, Linkify.WEB_URLS);
                    message.setText(s);
                    message.setMovementMethod(LinkMovementMethod.getInstance());

                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.alertdialog_about_CMOTAupdater_title))
                            .setView(message)
                            .setNegativeButton(getResources().getString(R.string.alertdialog_ok_option), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).create();

                case 7:

                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.alertdialog_instructions_title))
                            .setMessage(getResources().getString(R.string.alertdialog_instructions_text))
                            .setNegativeButton(getResources().getString(R.string.alertdialog_ok_option), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).create();



                case 8:

                    dialog = new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.alertdialog_download_completed_title))
                            .setMessage(getResources().getString(R.string.alertdialog_download_completed_text))
                            .setNegativeButton(getResources().getString(R.string.alertdialog_no_option), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }

                            })
                            .setPositiveButton((getResources().getString(R.string.alertdialog_yes_option)), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    showProgressDialogInstall();
                                    if (Utils.checkMd5OfFile(HandleDownloadProgress.getFILE_PATH(), HandleDownloadProgress.getSERVER_MD5())) {
                                        InstallUpdate.installUpdate();
                                    } else {
                                        HandleDownloadProgress.setALERTDIALOG_SELECTED(2);
                                        FragmentManager fragmentManager2 = getFragmentManager();
                                        AlertDialogFragment newFragment2 = new AlertDialogFragment();
                                        newFragment2.show(fragmentManager2, "Dialog");

                                    }
                                }

                            });
                            return dialog.create();


            }
            return null;
        }


    }
}

