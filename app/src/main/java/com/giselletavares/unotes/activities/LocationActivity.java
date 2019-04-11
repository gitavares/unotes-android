package com.giselletavares.unotes.activities;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Note;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static AppDatabase sAppDatabase;
    private GoogleMap mMap;
    private Double latitude;
    private Double longitude;
    private String noteId;
    private Note note;
    private Toolbar toolbar;
    private ImageButton btnBack;
    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        noteId = getIntent().getStringExtra("noteId");

        sAppDatabase = Room.databaseBuilder(LocationActivity.this, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        note = sAppDatabase.mNoteDAO().getNoteById(noteId);
        latitude = note.getLatitude();
        longitude = note.getLongitude();

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Note location");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(LocationActivity.this, NoteActivity.class);
                intent.putExtra("noteId", note.get_id());
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng noteLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(noteLocation).title("Note location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noteLocation, 17));
    }
}
