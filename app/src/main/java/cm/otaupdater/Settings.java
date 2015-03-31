package cm.otaupdater;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.AdapterView.*;

/**
 * Created by cmdesktop on 04-08-2014.
 */
public class Settings extends Activity {

    private RadioButton RADIOBUTTON_DAY;
    private RadioButton RADIOBUTTON_WEEK;
    private RadioButton RADIOBUTTON_HOUR;
    private RadioButton RADIOBUTTON_NEVER;
    private Switch SWITCH_STARTUP;
    private Switch SWITCH_SOUND;
    private int SWITCH_STARTUP_VALUE;
    private int SWITCH_SOUND_VALUE;
    private int RADIOGROUP_VALUE;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsettings);

        //iniciar variaveis
        TextView TEXTVIEW_CHECKEVERY = (TextView) findViewById(R.id.textViewCheckEvery);
        RADIOBUTTON_DAY = (RadioButton) findViewById(R.id.radioButtonDay);
        RADIOBUTTON_WEEK = (RadioButton) findViewById(R.id.radioButtonWeek);
        RADIOBUTTON_HOUR = (RadioButton) findViewById(R.id.radioButtonHour);
        RADIOBUTTON_NEVER = (RadioButton) findViewById(R.id.radioButtonNever);
        Button BUTTON_DELETE = (Button) findViewById(R.id.buttonDelete);
        SWITCH_STARTUP = (Switch) findViewById(R.id.switchStartUp);
        SWITCH_SOUND = (Switch) findViewById(R.id.switchSound);

        //texto e traducoes
        SWITCH_STARTUP.setText(getResources().getString(R.string.settings_switch));
        SWITCH_SOUND.setText(getResources().getString(R.string.settings_switch_sound));
        TEXTVIEW_CHECKEVERY.setText(getResources().getString(R.string.settings_checkEvery));
        RADIOBUTTON_DAY.setText(getResources().getString(R.string.settings_checkDay));
        RADIOBUTTON_WEEK.setText(getResources().getString(R.string.settings_checkWeek));
        RADIOBUTTON_HOUR.setText(getResources().getString(R.string.settings_checkHour));
        RADIOBUTTON_NEVER.setText(getResources().getString(R.string.settings_checkNever));
        BUTTON_DELETE.setText(getResources().getString(R.string.settings_delete));

        readSettings();
        checkOptions();

        SWITCH_STARTUP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SWITCH_STARTUP_VALUE = 1;
                } else {
                    SWITCH_STARTUP_VALUE = 0;
                }
                saveSettings();
            }
        });

        SWITCH_SOUND.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SWITCH_SOUND_VALUE = 1;
                } else {
                    SWITCH_SOUND_VALUE = 0;
                }
                saveSettings();
            }
        });


    }

    /**
     * button delete old rom files method
     * @param view
     */
    public void deleteOldDownloadedFiles(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        AlertDialogFragment newFragment = new AlertDialogFragment();
        newFragment.show(fragmentManager, "Dialog");
    }

    /**
     * radiobutton click method
     * @param view
     */
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radioButtonHour:
                if (checked)
                    RADIOGROUP_VALUE = 1;
                    RADIOBUTTON_DAY.setChecked(false);
                    RADIOBUTTON_WEEK.setChecked(false);
                    RADIOBUTTON_NEVER.setChecked(false);
                break;
            case R.id.radioButtonDay:
                if (checked)
                    RADIOGROUP_VALUE = 2;
                    RADIOBUTTON_HOUR.setChecked(false);
                    RADIOBUTTON_WEEK.setChecked(false);
                    RADIOBUTTON_NEVER.setChecked(false);
                break;
            case R.id.radioButtonWeek:
                if (checked)
                    RADIOGROUP_VALUE = 3;
                    RADIOBUTTON_HOUR.setChecked(false);
                    RADIOBUTTON_DAY.setChecked(false);
                    RADIOBUTTON_NEVER.setChecked(false);
                break;
            case R.id.radioButtonNever:
                if (checked)
                    RADIOGROUP_VALUE = 0;
                    RADIOBUTTON_DAY.setChecked(false);
                    RADIOBUTTON_WEEK.setChecked(false);
                    RADIOBUTTON_HOUR.setChecked(false);
                CheckServiceStartup.stopAutoCheckService(this);
                break;
        }
        Log.d("radio", RADIOGROUP_VALUE+"");
        saveSettings();
        CheckServiceStartup.setAutoCheckService(this, RADIOGROUP_VALUE, false);
    }


    /**
     * no inicio da actividade coloca os switchs e radiobuttons aos valores das definições
     */
    @SuppressLint("NewApi")
    private void checkOptions() {
        if (SWITCH_STARTUP_VALUE == 1)
            SWITCH_STARTUP.setChecked(true);
        if (SWITCH_SOUND_VALUE == 1)
            SWITCH_SOUND.setChecked(true);

        switch (RADIOGROUP_VALUE) {
            case 0:
                RADIOBUTTON_NEVER.setChecked(true);
                break;
            case 1:
                RADIOBUTTON_HOUR.setChecked(true);
                break;
            case 2:
                RADIOBUTTON_DAY.setChecked(true);
                break;
            case 3:
                RADIOBUTTON_WEEK.setChecked(true);
                break;
        }


    }


    /**
     * grava os settings selecionados
     */
    private void saveSettings() {
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();

        edit.putInt("CHECK_STARTUP", SWITCH_STARTUP_VALUE);
        edit.putInt("CHECK_EVERY", RADIOGROUP_VALUE);
        edit.putInt("NOT_SOUND", SWITCH_SOUND_VALUE);
        edit.commit();
    }

    /**
     * le os settings gravados anteriormente ou por omissao caso inexistentes
     */
    private void readSettings() {
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);

        int PREDIFINED_SWITCH_STARTUP_VALUE = 1;
        SWITCH_STARTUP_VALUE = preferences.getInt("CHECK_STARTUP", PREDIFINED_SWITCH_STARTUP_VALUE);
        int PREDIFINED_RADIOGROUP_VALUE = 2;
        RADIOGROUP_VALUE = preferences.getInt("CHECK_EVERY", PREDIFINED_RADIOGROUP_VALUE);
        int PREDIFINED_SWITCH_SOUND_VALUE = 1;
        SWITCH_SOUND_VALUE = preferences.getInt("NOT_SOUND", PREDIFINED_SWITCH_SOUND_VALUE);

    }


    public static class AlertDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.alertdialog_warning_title))
                    .setMessage(getResources().getString(R.string.settings_warning_message_delete))
                    .setPositiveButton(getResources().getString(R.string.alertdialog_yes_option), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String[] directories = Utils.getStorageDirectories();
                            Utils.deleteDownloadDir(new File(directories[directories.length-1]+"/.CM_OTA_updater/"));
                            Log.d("delete dir",new File(directories[directories.length-1]+"/.CM_OTA_updater/")+"");
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.alertdialog_no_option), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create();
        }


    }
}
