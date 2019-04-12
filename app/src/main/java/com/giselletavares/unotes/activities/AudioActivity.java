package com.giselletavares.unotes.activities;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.adapters.AudioAdapter;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Attachment;

import java.io.File;

public class AudioActivity extends AppCompatActivity {

    public static AppDatabase sAppDatabase;
    private String audioId;
    private Attachment audio;
    private ToggleButton tbPlayStop;
    private String outputFile;
    private MediaPlayer mediaPlayer;

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

        outputFile = audio.getFilename();

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Note audio record");

        tbPlayStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked) {
                if (isChecked) {
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(outputFile);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                            // @Override
                            public void onCompletion(MediaPlayer arg0) {
                                tbPlayStop.setChecked(false);
                                Toast.makeText(AudioActivity.this, "Audio Finished", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.d("TEST", "Error: " + e);
                    }
                } else {
                    if(mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        Toast.makeText(getApplicationContext(), "Audio stopped", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(AudioActivity.this, NoteActivity.class);
                intent.putExtra("noteId", audio.getNoteId());
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
