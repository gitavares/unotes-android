package com.giselletavares.unotes.activities;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Attachment;

import java.io.File;

public class ImageActivity extends AppCompatActivity {

    public static AppDatabase sAppDatabase;
    private ImageView ivNote;
    private String imageId;
    private Attachment image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ivNote = findViewById(R.id.ivNote);

        image = new Attachment();
        imageId = getIntent().getStringExtra("imageId");

        // DATABASE
        sAppDatabase = Room.databaseBuilder(ImageActivity.this, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        image = sAppDatabase.mAttachmentDAO().getAttachmentById(imageId);

        File imgFile = new  File(image.getFilename());
        ivNote.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Note image");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(ImageActivity.this, NoteActivity.class);
                intent.putExtra("noteId", image.getNoteId());
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
