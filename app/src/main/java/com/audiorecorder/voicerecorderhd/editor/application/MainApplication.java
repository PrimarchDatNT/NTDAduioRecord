package com.audiorecorder.voicerecorderhd.editor.application;

import android.app.Application;
import android.os.StrictMode;


public class MainApplication extends Application {
    @Override
    public void onCreate() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        super.onCreate();
    }
}
