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
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
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
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;
import android.widget.ToggleButton;

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


public class NoteActivity extends AppCompatActivity implements LocationListener {

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

    // for locationNetwork
    private static final int REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private static double latitude;
    private static double longitude;
    private static double updatedLatitude;
    private static double updatedLongitude;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 30; // 30 minute
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private Location locationNetwork;
    private Location locationGPS;
    private Location locationPassive;

    // for images
    private int REQUEST_CAMERA = 0;
    private int SELECT_FILE = 1;
    private String userChosenTask;
    private ImageAdapter mImageAdapter;
    private List<Attachment> mImageList;
    private List<Attachment> mImages;
    private Category category;

    // for audio
    private AudioAdapter mAudioAdapter;
    private List<Attachment> mAudioList;
    private List<Attachment> mAudios;
    private static Boolean isAudio;
    private MediaRecorder mediaRecorder;
    private String AudioSavePathInDevice = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        txtNoteTitle = findViewById(R.id.txtNoteTitle);
        txtNoteDescription = findViewById(R.id.txtNoteDescription);
        rvImages = findViewById(R.id.rvImages);
        rvAudioRecords = findViewById(R.id.rvAudioRecords);
        lblAttachmentTitle = findViewById(R.id.lblAttachmentTitle);

        // CHECK GENERAL PERMISSIONS
        checkPermissions();


        // LOCATION
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NoteActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);


        // DATABASE
        sAppDatabase = Room.databaseBuilder(NoteActivity.this, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        note = new Note();
        mImageList = new ArrayList<>();
        mAudioList = new ArrayList<>();
        latitude = 0.0;
        longitude = 0.0;
        updatedLatitude = 0.0;
        updatedLongitude = 0.0;

        // GET INTENT VALUES
        categoryId = getIntent().getStringExtra("categoryId");
        if(getIntent().getStringExtra("noteId") == null){
            noteId = "";
        } else {
            noteId = getIntent().getStringExtra("noteId");
        }

        if(!noteId.isEmpty()) {
            note = NoteActivity.sAppDatabase.mNoteDAO().getNoteById(noteId);

            if(categoryId == null){
                categoryId = note.getCategoryId();
            }

            txtNoteTitle.setText(note.getTitle());
            txtNoteDescription.setText(note.getNote());

            // IMAGES
            mImages = NoteActivity.sAppDatabase.mAttachmentDAO().getAttachmentsByNote(noteId, "image");
            for(Attachment image : mImages){
                mImageList.add(image);
            }

            // AUDIOS
            mAudios = NoteActivity.sAppDatabase.mAttachmentDAO().getAttachmentsByNote(noteId, "audio");
            for(Attachment audio : mAudios){
                mAudioList.add(audio);
            }
        }

        // TO UPDATE TOOLBAR INFO
        category = NoteActivity.sAppDatabase.mCategoryDAO().getCategoryById(categoryId);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(category.getName());

        // RECYCLER VIEWS
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

    }

    @Override
    protected void onStart() {
        super.onStart();
        mImageAdapter.notifyDataSetChanged();
        mAudioAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

            case R.id.btnDelete:
                deleteNote();
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

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else {
                    getLocation();
                    if(updatedLatitude != 0.0 && updatedLatitude != latitude){
                        latitude = updatedLatitude;
                        longitude = updatedLongitude;
                    }
                    note.setLatitude(latitude);
                    note.setLongitude(longitude);
                }

                note.setCreatedDate(currentDateTime);
                formatting = new Formatting();
                String newNoteId = formatting.getDateTimeForIdFormatter(currentDateTime);
                note.set_id(newNoteId);
                NoteActivity.sAppDatabase.mNoteDAO().addNote(note);

            } else {
                Log.d("TEST", "Note id is NOT empty: " + noteId);
                NoteActivity.sAppDatabase.mNoteDAO().updateNote(note);
            }

            Toast.makeText(NoteActivity.this, getString(R.string.msg_note_saved), Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(NoteActivity.this, getString(R.string.error_save_note), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteNote(){
        final AlertDialog alertDialog =new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Are you want to delete this");
        alertDialog.setCancelable(false);
        alertDialog.setMessage("By deleting this, item will permanently be deleted. Are you still want to delete this?");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // DATABASE
                sAppDatabase = Room.databaseBuilder(NoteActivity.this, AppDatabase.class, "unotes")
                        .allowMainThreadQueries() // it will allow the database works on the main thread
                        .fallbackToDestructiveMigration() // because i wont implement now migrations
                        .build();

                sAppDatabase.mNoteDAO().deleteNote(note);
                alertDialog.dismiss();

                sAppDatabase.close();

                Intent intent = new Intent(NoteActivity.this, NotesActivity.class);
                intent.putExtra("categoryId", note.getCategoryId());
                startActivity(intent);
            }
        });
        alertDialog.show();
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

        if(data != null){
            addImageDataBaseAndList(thumbnail);
        }
    }

    private void addImageDataBaseAndList(Bitmap bitmap){
        String path = saveImage(bitmap);

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
        saveNote();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_audio_record, null);
        final ToggleButton tbRecordAudio = view.findViewById(R.id.tbRecordAudio);
        final Button btnAddAudioRecord_dialog = view.findViewById(R.id.btnAddAudioRecord_dialog);

        btnAddAudioRecord_dialog.setEnabled(false);

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        tbRecordAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    AudioSavePathInDevice = Environment.getExternalStorageDirectory() + "/unotes/" +
                            Calendar.getInstance().getTimeInMillis() + ".3gp";

                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    mediaRecorder.setOutputFile(AudioSavePathInDevice);

                    try {
                        mediaRecorder.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaRecorder.start();

                    Toast.makeText(NoteActivity.this, "Recording audio...", Toast.LENGTH_LONG).show();

                } else {
                    mediaRecorder.stop();

                    btnAddAudioRecord_dialog.setEnabled(true);

                    Toast.makeText(NoteActivity.this, "Recording Completed", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnAddAudioRecord_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AudioSavePathInDevice != ""){
                    addAudioDataBaseAndList(AudioSavePathInDevice);
                    dialog.cancel();
                } else {
                    Toast.makeText(NoteActivity.this, "Error saving audio recording", Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();
    }

    private void audioLibraryIntent()
    {
        isAudio = true;
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void onSelectFromAudioLibraryResult(Intent data) {
        String absFileName = data.getData().getLastPathSegment();
        String fileName = absFileName.substring(15);
        String path = Environment.getExternalStorageDirectory() + "/unotes/" + fileName;

        if(data != null){
            addAudioDataBaseAndList(path);
        }
    }

    private void addAudioDataBaseAndList(String path){
        Attachment attachment = new Attachment();

        formatting = new Formatting();
        String newAttachmentId = formatting.getDateTimeForIdFormatter(new Date());
        attachment.set_id(newAttachmentId);

        attachment.setNoteId(note.get_id());
        attachment.setFilename(path);
        attachment.setType("audio");
        attachment.setCreatedDate(new Date());

        NoteActivity.sAppDatabase.mAttachmentDAO().addAttachment(attachment);
        mAudioList.add(attachment);
        mAudioAdapter.notifyDataSetChanged();
        saveNote();
    }


    // FOR LOCATION
    private void getLocation() {
        if (locationGPS != null) {
            latitude = locationGPS.getLatitude();
            longitude = locationGPS.getLongitude();
        } else if (locationNetwork != null) {
            latitude = locationNetwork.getLatitude();
            longitude = locationNetwork.getLongitude();
        } else if (locationPassive != null) {
            latitude = locationPassive.getLatitude();
            longitude = locationPassive.getLongitude();
        } else {
            Toast.makeText(this,"Unable to trace your locationNetwork",Toast.LENGTH_LONG).show();
        }

        Log.d("TEST", "getLocation() - Latitude: " + latitude);
        Log.d("TEST", "getLocation() - Longitude: " + longitude);
    }

    @Override
    public void onLocationChanged(Location location) {
        updatedLatitude = location.getLatitude();
        updatedLongitude = location.getLongitude();
        Log.d("TEST", "onLocationChanged - Latitude: " + updatedLatitude);
        Log.d("TEST", "onLocationChanged - Longitude: " + updatedLongitude);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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
