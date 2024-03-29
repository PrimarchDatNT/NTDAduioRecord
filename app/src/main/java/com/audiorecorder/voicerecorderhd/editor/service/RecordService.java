package com.audiorecorder.voicerecorderhd.editor.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.audiorecorder.voicerecorderhd.editor.MainActivity;
import com.audiorecorder.voicerecorderhd.editor.R;
import com.audiorecorder.voicerecorderhd.editor.data.DBQuerys;
import com.audiorecorder.voicerecorderhd.editor.utils.Constants;

import java.io.File;
import java.io.IOException;

public class RecordService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static int pauseStatus;
    public static int recordingStatus;
    public static boolean isRunning ;
    private MediaRecorder mAudioRecorder;
    private String outputFile;
    private NotificationManager mNotificationManager;
    private Notification mBuilder;
    private long startTime = 0;
    private long millis = 0;
    private long countTimeRecord = 0;
    private DBQuerys dbQuerys;
    private String pathFile;
    private long dateTime;
    private long fileSize;
    private String audioName;
    private static long extraCurrentTime;
    private NotificationManager notificationManager;

    private NotificationReceiver notificationReceiver = new NotificationReceiver();
    private Handler handler = new Handler();
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;
            countTimeRecord += 1000;
            sendTimeToReceiver();
            handler.postDelayed(this, 1000);
        }
    };

    public RecordService() {

    }

    public static int getPauseStatus() {
        return pauseStatus;
    }

    public static void setPauseStatus(int pauseStatus) {
        RecordService.pauseStatus = pauseStatus;
    }

    public static int getRecordingStatus() {
        return recordingStatus;
    }

    public static void setRecordingStatus(int recordingStatus) {
        RecordService.recordingStatus = recordingStatus;
    }

    public static long getExtraCurrentTime() {
        return extraCurrentTime;
    }

    public static void setExtraCurrentTime(long extraCurrentTime) {
        RecordService.extraCurrentTime = extraCurrentTime;
    }

    private void insertSQL(){
        dbQuerys = new DBQuerys(getApplicationContext());
        dbQuerys.insertAudioString(audioName,outputFile,fileSize,dateTime,countTimeRecord - 200);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        createNotificationChannel();
        createNotification();
        startRecording();
        startCounter();
        setRecordingStatus(1);
        initReceiver();
        return START_STICKY;
    }

    private void initReceiver() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.START_ACTION);
            filter.addAction(Constants.RESUME_ACTION);
            filter.addAction(Constants.PAUSE_ACTION);
            filter.addAction(Constants.STOP_ACTION);
            registerReceiver(notificationReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendTimeToReceiver() {
        Intent intentTimer = new Intent(Constants.SEND_TIME);
        intentTimer.putExtra(Constants.TIME_COUNT, millis);
        sendBroadcast(intentTimer);
    }

    public void startCounter() {
        startTime = System.currentTimeMillis();
        countTimeRecord = 0;
        handler.postDelayed(serviceRunnable, 0);

    }

    public void continueCouter() {
        startTime = System.currentTimeMillis() - countTimeRecord;
        handler.postDelayed(serviceRunnable, 0);
    }

    public void stopCounter() {
        handler.removeCallbacksAndMessages(null);
    }

    public void createFile() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.K_AUDIO_SETTING, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            int checkStatus = sharedPreferences.getInt(Constants.K_FORMAT_TYPE, 0);
            String pathDirector = sharedPreferences.getString(Constants.K_DIRECTION_CHOOSER_PATH, Environment.getExternalStorageDirectory() + File.separator + "Recorder");
            pathFile = pathDirector;
            dateTime = System.currentTimeMillis();
            File file = new File(pathDirector);
            if (checkStatus == 0) {
                outputFile =  file.getAbsolutePath() + "/RecordFile" + System.currentTimeMillis() + ".mp3";
                audioName = "RecordFile" + System.currentTimeMillis() + ".mp3";
            } else if (checkStatus == 1) {
                outputFile =  file.getAbsolutePath() + "/RecordFile" + System.currentTimeMillis() + ".wav";
                audioName = "RecordFile" + System.currentTimeMillis() + ".wav";
            }
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    public void setupMediaRecorder() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.K_AUDIO_SETTING, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            int checkStatus = sharedPreferences.getInt(Constants.K_FORMAT_TYPE, 0);
            mAudioRecorder = new MediaRecorder();
            mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            if (checkStatus == 0) {
                mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.MPEG_4);

            } else if (checkStatus == 1) {
                mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.MPEG_4);

            }
            int checkQuality = sharedPreferences.getInt(Constants.K_FORMAT_QUALITY, 16);
            if (checkQuality == 16) {
                mAudioRecorder.setAudioEncodingBitRate(16);
                mAudioRecorder.setAudioSamplingRate(16 * Constants.K_SAMPLE_RATE_QUALITY);

            } else if (checkQuality == 22) {
                mAudioRecorder.setAudioEncodingBitRate(22);
                mAudioRecorder.setAudioSamplingRate(22 * Constants.K_SAMPLE_RATE_QUALITY);

            } else if (checkQuality == 32) {
                mAudioRecorder.setAudioEncodingBitRate(32);
                mAudioRecorder.setAudioSamplingRate(32 * Constants.K_SAMPLE_RATE_QUALITY);

            } else if (checkQuality == 44) {
                mAudioRecorder.setAudioEncodingBitRate(44);
                mAudioRecorder.setAudioSamplingRate(44100);

            }
        }
        mAudioRecorder.setOutputFile(outputFile);
    }

    public void startRecording() {
        createFile();
        setupMediaRecorder();
        try {
            mAudioRecorder.prepare();
            mAudioRecorder.start();
        } catch (IllegalStateException ise) {
            // make something ...
        } catch (IOException ioe) {
            // make something
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void pauseRecording() {
        if (mAudioRecorder != null) {
            mAudioRecorder.pause();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resumeRecording() {
        if (mAudioRecorder != null) {
            mAudioRecorder.resume();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void stopRecording() {
        try {
            if (mAudioRecorder != null) {
                mAudioRecorder.stop();
                File file = new File(outputFile);
                fileSize = file.length();
                mAudioRecorder.release();
                mAudioRecorder = null;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Null Media File", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        stopRecording();
        try {
            unregisterReceiver(notificationReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            mNotificationManager = getSystemService(NotificationManager.class);
            mNotificationManager.createNotificationChannel(serviceChannel);
        }

    }

    private void createNotification() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2019, notificationIntent, 0);

        Intent pauseReceive = new Intent(Constants.PAUSE_ACTION);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, 2019, pauseReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent resumeReceive = new Intent(Constants.RESUME_ACTION);
        PendingIntent pendingIntentResume = PendingIntent.getBroadcast(this, 2019, resumeReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopReceive = new Intent(Constants.STOP_ACTION);
        PendingIntent pendingIntentStop = PendingIntent.getBroadcast(this, 2019, stopReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Recording")
                .setLocalOnly(true)
                .addAction(

                        isRunning ? R.drawable.ic_play_pause : R.drawable.ic_play_play,
                        isRunning ? "Pause" : "Resume",
                        isRunning ? pendingIntentPause : pendingIntentResume
                )
                .addAction(R.drawable.ic_play_record_pr, "stop", pendingIntentStop)
                .setSmallIcon(R.drawable.ic_record, 4)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, mBuilder);

    }

    public class NotificationReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("Test", "onReadyStart: " + action);
            if (Constants.PAUSE_ACTION.equals(action)) {
                isRunning = false;
                pauseRecording();

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    stopForeground(true);
                createNotification();

                setPauseStatus(1);
                stopCounter();
                setExtraCurrentTime(countTimeRecord - 1000);


            } else if (Constants.STOP_ACTION.equals(action)) {
                isRunning = false;
                setRecordingStatus(0);
                stopRecording();
                insertSQL();
                setExtraCurrentTime(0);
                stopCounter();
                try {
                    unregisterReceiver(notificationReceiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopSelf();

            } else if (Constants.RESUME_ACTION.equals(action)) {
                isRunning = true;

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    stopForeground(true);
                createNotification();

                resumeRecording();
                setPauseStatus(0);
                continueCouter();

            } else if (Constants.START_ACTION.equals(action)) {

                //Do something here

            }
        }
    }


}
