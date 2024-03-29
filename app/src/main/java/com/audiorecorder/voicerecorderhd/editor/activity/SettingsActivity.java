package com.audiorecorder.voicerecorderhd.editor.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.audiorecorder.voicerecorderhd.editor.MainActivity;
import com.audiorecorder.voicerecorderhd.editor.R;
import com.audiorecorder.voicerecorderhd.editor.utils.Constants;
import com.codekidlabs.storagechooser.StorageChooser;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String STATIC_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private RadioGroup rgFormatType, rgSetQuality;
    private TextView tvChooseFolder;
    private TextView locationFileSetting;
    private ImageView ivBottomLibrary;
    private ImageView ivBottomRecoder;
    private ImageView ivBottomSettings;
    private TextView fileTypeSetting;
    private TextView recoderQualitySetting;
    private TextView tvResponQualitySetting;
    private AlertDialog dialog;
    private SharedPreferences sharedPreferences;
    private TextView tvResponFileTypeSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(getResources().getString(R.string.label_setting));
        mapping();

        sharedPreferences = this.getSharedPreferences(Constants.K_AUDIO_SETTING, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            getKeyQuality(sharedPreferences);
            getKeyPath(sharedPreferences);
            getKeyFileType(sharedPreferences);
            return;
        }

    }



    private void getKeyFileType(SharedPreferences s) {
        int checkFormatType = s.getInt(Constants.K_FORMAT_TYPE, 0);
        if (checkFormatType == 0) {
            tvResponFileTypeSetting.setText(Constants.K_FORMAT_TYPE_MP3);
            return;
        }
        tvResponFileTypeSetting.setText(Constants.K_FORMAT_TYPE_WAV);
    }

    private void getKeyPath(SharedPreferences s) {
        if (s!=null){
            String path = s.getString(Constants.K_DIRECTION_CHOOSER_PATH, Constants.K_DEFAULT_PATH);
            locationFileSetting.setText(path);
            return;
        }
        locationFileSetting.setText(Constants.K_DEFAULT_PATH);

    }

    private void getKeyQuality( SharedPreferences s) {
        int checkQuality = s.getInt(Constants.K_FORMAT_QUALITY, 16);
        Log.e("quality",checkQuality+" kHz");
        tvResponQualitySetting.setText(String.valueOf(checkQuality)+" kHz");
    }

    private void mapping() {

        ivBottomLibrary = findViewById(R.id.iv_bottom_library);
        ivBottomRecoder = findViewById(R.id.iv_bottom_recoder);
        ivBottomSettings = findViewById(R.id.iv_bottom_settings);

        tvChooseFolder = findViewById(R.id.tv_Choose_Folder);
        locationFileSetting = findViewById(R.id.tv_location_file_setting);

        fileTypeSetting = findViewById(R.id.tv_choose_file_type_setting);
        recoderQualitySetting = findViewById(R.id.tv_choose_quality_setting);
        tvResponQualitySetting = findViewById(R.id.tv_respon_quality_setting);
        tvResponFileTypeSetting = findViewById(R.id.tv_respon_file_type_setting);

        ivBottomSettings.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings_pr));
        ivBottomLibrary.setOnClickListener(this);
        ivBottomRecoder.setOnClickListener(this);
        tvChooseFolder.setOnClickListener(this);
        fileTypeSetting.setOnClickListener(this);
        recoderQualitySetting.setOnClickListener(this);

    }

      @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_bottom_recoder:
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                break;
            case R.id.iv_bottom_library:
                startActivity(new Intent(SettingsActivity.this, LibraryActivity.class));
                break;
            case R.id.tv_Choose_Folder:
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.K_AUDIO_SETTING, Context.MODE_PRIVATE);
                StorageChooser chooser = new StorageChooser.Builder()
                        .withActivity(SettingsActivity.this)
                        .withPredefinedPath(STATIC_PATH)
                        .actionSave(true)
                        .withFragmentManager(getFragmentManager())
                        .allowCustomPath(true)
                        .allowAddFolder(true)
                        .withPreference(sharedPreferences)
                        .setType(StorageChooser.DIRECTORY_CHOOSER)
                        .withMemoryBar(true)
                        .skipOverview(true)
                        .build();
                chooser.show();
                chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                    @Override
                    public void onSelect(String path) {
                        Log.e("SELECTED_PATH", path);
                        SharedPreferences sharedPreferences = getSharedPreferences(Constants.K_AUDIO_SETTING, Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.K_DIRECTION_CHOOSER_PATH, path);
                        editor.apply();
                        locationFileSetting.setText(path);

                    }
                });
                break;
            case R.id.tv_choose_quality_setting:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View viewDialog = LayoutInflater.from(this).inflate(R.layout.dialog_setting_quality, null);
                builder.setView(viewDialog);

                RadioButton rb16kHz, rb22kHz, rb32kHz, rb44kHz;

                rgSetQuality = viewDialog.findViewById(R.id.rg_SetQuality);
                rb16kHz = viewDialog.findViewById(R.id.rb_16kHz);
                rb22kHz = viewDialog.findViewById(R.id.rb_22kHz);
                rb32kHz = viewDialog.findViewById(R.id.rb_32kHz);
                rb44kHz = viewDialog.findViewById(R.id.rb_44kHz);


                if (this.sharedPreferences != null) {

                    int checkQuality = this.sharedPreferences.getInt(Constants.K_FORMAT_QUALITY, 16);

                    if (checkQuality == 16) {
                        rb16kHz.setChecked(true);

                    } else if (checkQuality == 22) {
                        rb22kHz.setChecked(true);

                    } else if (checkQuality == 32) {
                        rb32kHz.setChecked(true);

                    } else if (checkQuality == 44) {
                        rb44kHz.setChecked(true);

                    }
                }


                final SharedPreferences.Editor editor = this.sharedPreferences.edit();
                rb16kHz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor.putInt(Constants.K_FORMAT_QUALITY, 16);
                        editor.apply();
                        dialog.dismiss();

                        tvResponQualitySetting.setText(Constants.K_QUALITY_16);

                    }

                });

                rb22kHz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor.putInt(Constants.K_FORMAT_QUALITY, 22);
                        editor.apply();
                        dialog.dismiss();

                        tvResponQualitySetting.setText(Constants.K_QUALITY_22);


                    }
                });

                rb32kHz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor.putInt(Constants.K_FORMAT_QUALITY, 32);
                        editor.apply();
                        dialog.dismiss();

                        tvResponQualitySetting.setText(Constants.K_QUALITY_32);

                    }
                });

                rb44kHz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor.putInt(Constants.K_FORMAT_QUALITY, 44);
                        editor.apply();
                        dialog.dismiss();

                        tvResponQualitySetting.setText(Constants.K_QUALITY_44);

                    }
                });

                builder.create();
                dialog = builder.show();
                break;
            case R.id.tv_choose_file_type_setting:

                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                final View viewDialog2 = LayoutInflater.from(this).inflate(R.layout.dialog_setting_file_type, null);
                builder2.setView(viewDialog2);

                RadioButton rbMp3, rbWav;

                rgFormatType = findViewById(R.id.rg_FormatType);
                rbMp3 = viewDialog2.findViewById(R.id.rb_Mp3);
                rbWav = viewDialog2.findViewById(R.id.rb_Wav);


                if (this.sharedPreferences != null) {
                    int checkFormatType = this.sharedPreferences.getInt(Constants.K_FORMAT_TYPE, 0);
                    if (checkFormatType == 0) {
                        rbMp3.setChecked(true);
                        rbWav.setChecked(false);
                    } else if (checkFormatType == 1) {
                        rbMp3.setChecked(false);
                        rbWav.setChecked(true);
                    }

                }
                final SharedPreferences.Editor editor2 = this.sharedPreferences.edit();

                rbWav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor2.putInt(Constants.K_FORMAT_TYPE, 1);
                        editor2.apply();
                        dialog.dismiss();
                        tvResponFileTypeSetting.setText(Constants.K_FORMAT_TYPE_WAV);

                    }
                });

                rbMp3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor2.putInt(Constants.K_FORMAT_TYPE, 0);
                        editor2.apply();
                        dialog.dismiss();
                        tvResponFileTypeSetting.setText(Constants.K_FORMAT_TYPE_MP3);

                    }
                });
                builder2.create();
                dialog = builder2.show();
                break;
        }
    }
    @SuppressLint("WrongConstant")
    public void setTitle(String title) {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView textView = new TextView(this);
        textView.setText(title);
        textView.setTextSize(25);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.all_color_black));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(textView);
    }



}