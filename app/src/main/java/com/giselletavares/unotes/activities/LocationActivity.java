package com.giselletavares.unotes.activities;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.location.LocationListener;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Attachment;
import com.giselletavares.unotes.models.Note;
import com.giselletavares.unotes.utils.Formatting;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

import butterknife.BindView;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static AppDatabase sAppDatabase;
    private GoogleMap mMap;
    private Double latitude;
    private Double longitude;
    private String noteId;
    private Note note;
    private Formatting formatting;

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

        note = LocationActivity.sAppDatabase.mNoteDAO().getNoteById(noteId);
        latitude = note.getLatitude();
        longitude = note.getLongitude();

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Note locationNetwork");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(LocationActivity.this, NoteActivity.class);
                intent.putExtra("noteId", note.get_id());
                startActivity(intent);
                return true;

            case R.id.btnSaveLocation:

                formatting = new Formatting();
                note.setLatitude(latitude);
                note.setLongitude(longitude);
                note.setUpdatedDate(new Date());

                LocationActivity.sAppDatabase.mNoteDAO().updateNote(note);

                Toast.makeText(LocationActivity.this, "New location saved", Toast.LENGTH_LONG).show();
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

        LatLng noteLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(noteLocation).title("Note location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noteLocation, 17));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("New Note location: " + latLng.latitude + " : " + latLng.longitude);

                // Clears the previously touched position
                mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

                latitude = latLng.latitude;
                longitude = latLng.longitude;
            }
        });
    }
}
