package com.giselletavares.unotes.activities;

import android.arch.persistence.room.Room;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Attachment;

import java.io.File;

public class AudioActivity extends AppCompatActivity {

    public static AppDatabase sAppDatabase;
    private String audioId;
    private Attachment audio;
    private ToggleButton tbPlayStop;
    private String outputFile;
    private MediaRecorder audioRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        tbPlayStop = findViewById(R.id.tbPlayStop);

        audio = new Attachment();
        audioId = getIntent().getStringExtra("audioId");

        // DATABASE
        sAppDatabase = Room.databaseBuilder(AudioActivity.this, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        audio = sAppDatabase.mAttachmentDAO().getAttachmentById(audioId);

//        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
        outputFile = audio.getFilename();

        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        audioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        audioRecorder.setOutputFile(outputFile);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Note audio record");

        tbPlayStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(outputFile);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        // make something
                    }
                } else {
                    audioRecorder.stop();
                    audioRecorder.release();
                    Toast.makeText(getApplicationContext(), "Audio Recorder stopped", Toast.LENGTH_LONG).show();
                }
            }
        });


    }
}
