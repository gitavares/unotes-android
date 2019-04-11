package com.giselletavares.unotes.activities;

import android.Manifest;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.adapters.AudioAdapter;
import com.giselletavares.unotes.adapters.ImageAdapter;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Attachment;
import com.giselletavares.unotes.models.Category;
import com.giselletavares.unotes.models.Note;
import com.giselletavares.unotes.utils.CheckPermission;
import com.giselletavares.unotes.utils.Formatting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class NoteActivity extends AppCompatActivity {

    public static AppDatabase sAppDatabase;
    private Note note;
    private String categoryId;
    private String noteId;
    private Date currentDateTime;
    private Formatting formatting;

    private TextView txtNoteTitle;
    private TextView txtNoteDescription;
    private RecyclerView rvImages;
    private RecyclerView rvAudioRecords;
    private RecyclerView.LayoutManager mLayoutImageManager;
    private RecyclerView.LayoutManager mLayoutAudioManager;
    private TextView lblAttachmentTitle;

    // for location
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    double latitude;
    double longitude;

    // for images
    private int REQUEST_CAMERA = 0;
    private int SELECT_FILE = 1;
    private String userChosenTask;
    ImageAdapter mImageAdapter;
    AudioAdapter mAudioAdapter;
    private List<Attachment> mImageList;
    private List<Attachment> mImages;
    private List<Attachment> mAudioList;
    private List<Attachment> mAudios;
    private Category category;
    private static Boolean isAudio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        checkPermissions();

        txtNoteTitle = findViewById(R.id.txtNoteTitle);
        txtNoteDescription = findViewById(R.id.txtNoteDescription);
        rvImages = findViewById(R.id.rvImages);
        rvAudioRecords = findViewById(R.id.rvAudioRecords);
        lblAttachmentTitle = findViewById(R.id.lblAttachmentTitle);

        categoryId = getIntent().getStringExtra("categoryId");
        Log.d("TEST", "category ID: " + categoryId);

        if(getIntent().getStringExtra("noteId") == null){
            noteId = "";
        } else {
            noteId = getIntent().getStringExtra("noteId");
        }

        note = new Note();
        mImageList = new ArrayList<>();
        mAudioList = new ArrayList<>();

        // LOCATION
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);

        // DATABASE
        sAppDatabase = Room.databaseBuilder(NoteActivity.this, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        if(!noteId.isEmpty()) {
            note = sAppDatabase.mNoteDAO().getNoteById(noteId);

            if(getIntent().getStringExtra("categoryId") == null){
                categoryId = note.getCategoryId();
            }

            txtNoteTitle.setText(note.getTitle());
            txtNoteDescription.setText(note.getNote());
            // implement audio record recycler view

            // IMAGES
            mImages = sAppDatabase.mAttachmentDAO().getAttachmentsByNote(noteId, "image");
            for(Attachment image : mImages){
                mImageList.add(image);
            }

            // AUDIOS
            mAudios = sAppDatabase.mAttachmentDAO().getAttachmentsByNote(noteId, "audio");
            for(Attachment audio : mAudios){
                mImageList.add(audio);
            }
        }

        // IMAGES
        mLayoutImageManager = new LinearLayoutManager(this);
        rvImages.setLayoutManager(mLayoutImageManager);
        mImageAdapter = new ImageAdapter(mImageList, this);
        rvImages.setAdapter(mImageAdapter);

        // AUDIOS
        mLayoutAudioManager = new LinearLayoutManager(this);
        rvAudioRecords.setLayoutManager(mLayoutAudioManager);
        mAudioAdapter = new AudioAdapter(mAudioList, this);
        rvAudioRecords.setAdapter(mAudioAdapter);

        category = sAppDatabase.mCategoryDAO().getCategoryById(categoryId);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(category.getName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mImageAdapter.notifyDataSetChanged();
        mAudioAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(NoteActivity.this, NotesActivity.class);
                intent.putExtra("categoryId", note.getCategoryId());
                startActivity(intent);
                return true;

            case R.id.btnSaveNote:
                saveNote();
                break;

            case R.id.btnLocation:
                if(note.get_id() != null) {
                    intent = new Intent(NoteActivity.this, LocationActivity.class);
                    intent.putExtra("latitude", note.getLatitude());
                    intent.putExtra("longitude", note.getLongitude());
                    intent.putExtra("noteId", note.get_id());
                    startActivity(intent);
                    return true;
                } else {
                    Toast.makeText(this, "You need first to SAVE THE NOTE to see the NOTE LOCATION", Toast.LENGTH_LONG).show();
                    break;
                }

            case R.id.btnImage:
                if(note.get_id() != null) {
                    selectImage();
                } else {
                    Toast.makeText(this, "You need first to SAVE THE NOTE to ADD IMAGE", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnAudioRecord:
                if(note.get_id() != null) {
                    selectAudio();
                } else {
                    Toast.makeText(this, "You need first to SAVE THE NOTE to ADD AUDIO RECORD", Toast.LENGTH_LONG).show();
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    // SAVE NOTE
    private void saveNote(){
        currentDateTime = new Date();

        if(!txtNoteTitle.getText().toString().isEmpty()){

            note.setCategoryId(categoryId);
            note.setTitle(txtNoteTitle.getText().toString());
            note.setNote(txtNoteDescription.getText().toString());
            note.setUpdatedDate(currentDateTime);

            if(noteId.isEmpty()) {

                // get the current lat and long to set
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    getLocation();
                    note.setLatitude(latitude);
                    note.setLongitude(longitude);

                    Log.d("TEST", "latitude: " + latitude);
                    Log.d("TEST", "longitude: " + longitude);
                }

                note.setCreatedDate(currentDateTime);
                formatting = new Formatting();
                String newNoteId = formatting.getDateTimeForIdFormatter(currentDateTime);
                note.set_id(newNoteId);
                HomeActivity.sAppDatabase.mNoteDAO().addNote(note);
            } else {
                Log.d("TEST", "Note id is NOT empty: " + noteId);
                HomeActivity.sAppDatabase.mNoteDAO().updateNote(note);
            }

            Toast.makeText(NoteActivity.this, getString(R.string.msg_note_saved), Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(NoteActivity.this, getString(R.string.error_save_note), Toast.LENGTH_LONG).show();
        }
    }


    // FOR IMAGES
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
        builder.setTitle("Add an Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = CheckPermission.checkPermission(NoteActivity.this);
                if (items[item].equals("Take Photo")) {
                    userChosenTask = "Take Photo";
                    if(result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChosenTask = "Choose from Library";
                    if(result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent()
    {
        isAudio = false;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CheckPermission.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChosenTask.equals("Choose from Library")) {
                        galleryIntent();
                    } else if(userChosenTask.equals("Choose Audio from Library")) {
                        audioLibraryIntent();
                    } else if(userChosenTask.equals("Record an Audio"))
                        recordAudioIntent();
                } else {
                    //code for deny
                }
                break;

            default:
                if (requestCode == 100) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do something
                    }
                    return;
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE && !isAudio)
                onSelectFromGalleryResult(data);
            else if (requestCode == SELECT_FILE && isAudio)
                onSelectFromAudioLibraryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Uri selectedImageURI = data.getData();
        Log.d("TEST", "selectedImageURI: " + selectedImageURI);

        if(data != null){

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageURI);
                addImageDataBaseAndList(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        Log.d("TEST", "thumbnail: " + thumbnail);

        if(data != null){
            addImageDataBaseAndList(thumbnail);
        }
    }

    private void addImageDataBaseAndList(Bitmap bitmap){
        String path = saveImage(bitmap);

        Log.d("TEST", "bitmap: " + bitmap);
        Log.d("TEST", "path: " + path);

        Attachment attachment = new Attachment();

        formatting = new Formatting();
        String newAttachmentId = formatting.getDateTimeForIdFormatter(new Date());
        attachment.set_id(newAttachmentId);

        attachment.setNoteId(note.get_id());
        attachment.setFilename(path);
        attachment.setType("image");
        attachment.setCreatedDate(new Date());

        HomeActivity.sAppDatabase.mAttachmentDAO().addAttachment(attachment);
        mImageList.add(attachment);
        mImageAdapter.notifyDataSetChanged();
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + "/unotes");
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TEST", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }


    // FOR AUDIO
    private void selectAudio() {
        final CharSequence[] items = { "Record an Audio", "Choose Audio from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
        builder.setTitle("Add an Audio Record");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = CheckPermission.checkPermission(NoteActivity.this);
                if (items[item].equals("Record an Audio")) {
                    userChosenTask = "Record an Audio";
                    if(result)
                        recordAudioIntent();
                } else if (items[item].equals("Choose Audio from Library")) {
                    userChosenTask = "Choose Audio from Library";
                    if(result)
                        audioLibraryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    private void recordAudioIntent()
    {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void audioLibraryIntent()
    {
        isAudio = true;
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void onSelectFromAudioLibraryResult(Intent data) {

        Uri selectedAudioURI = data.getData();
        Log.d("TEST", "selectedAudioURI: " + selectedAudioURI);

        if(data != null){

//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageURI);
//                addImageDataBaseAndList(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }
    }

    public String saveAudio(MediaRecorder mediaRecorder) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + "/unotes");
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }
//
        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".wav");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"audio/wav"}, null);
            fo.close();
            Log.d("TEST", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }



    // FOR LOCATION
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(NoteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (NoteActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(NoteActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else if (location1 != null) {
                latitude = location1.getLatitude();
                longitude = location1.getLongitude();
            } else if (location2 != null) {
                latitude = location2.getLatitude();
                longitude = location2.getLongitude();
            } else {
                Toast.makeText(this,"Unable to trace your location",Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    // GENERAL PERMISSIONS
    public static String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECORD_AUDIO,
    };

    public boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(NoteActivity.this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(NoteActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

}
