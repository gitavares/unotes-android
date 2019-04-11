package com.giselletavares.unotes.activities;

import android.app.SearchManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.adapters.NoteAdapter;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Category;
import com.giselletavares.unotes.models.Note;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    public static AppDatabase sAppDatabase;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<Note> notes;
    private List<Note> mNoteList;
    private String categoryId;
    private static String searchTerm;
    private Category category;

    NoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        mRecyclerView = findViewById(R.id.rvNotes);

        // DATABASE
        sAppDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "unotes")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        categoryId = getIntent().getStringExtra("categoryId");

        notes = sAppDatabase.mNoteDAO().getNotesByCategory(categoryId);
        category = sAppDatabase.mCategoryDAO().getCategoryById(categoryId);

        mNoteList = new ArrayList<>();

        for(Note note : notes){
            mNoteList.add(note);
        }

        // USE A LINEAR LAYOUT MANAGER
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // LOADING RECYCLERVIEW AND SPECIFY AN ADAPTER
        noteAdapter = new NoteAdapter(mNoteList, this);
        mRecyclerView.setAdapter(noteAdapter);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(category.getName());

        // FOR SEARCH
        searchTerm = "";
        handleIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.search:
                return true;

            case R.id.btnMenuAddNote:
                Intent intent = new Intent(NotesActivity.this, NoteActivity.class);
                intent.putExtra("categoryId", categoryId);
                startActivity(intent);
                break;

            case R.id.btnOrderTitleAsc:
                notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesOrderByTitleAsc(categoryId, "%"+searchTerm+"%");
                getUpdatedNotesAfterResultQuery();
                break;

            case R.id.btnOrderTitleDesc:
                notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesOrderByTitleDesc(categoryId, "%"+searchTerm+"%");
                getUpdatedNotesAfterResultQuery();
                break;

            case R.id.btnOrderDateDesc:
                notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesOrderByDateDesc(categoryId, "%"+searchTerm+"%");
                getUpdatedNotesAfterResultQuery();
                break;

            case R.id.btnOrderDateAsc:
                notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesOrderByDateAsc(categoryId, "%"+searchTerm+"%");
                getUpdatedNotesAfterResultQuery();
                break;

        }

        return super.onOptionsItemSelected(item);

    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchTerm = intent.getStringExtra(SearchManager.QUERY);
            notes = NotesActivity.sAppDatabase.mNoteDAO().getNotesBySearch(categoryId, "%"+searchTerm+"%");
            getUpdatedNotesAfterResultQuery();
        }

    }

    private void getUpdatedNotesAfterResultQuery(){
        mNoteList = new ArrayList<>();
        for(Note note : notes){
            mNoteList.add(note);
        }
        noteAdapter = new NoteAdapter(mNoteList, this);
        mRecyclerView.setAdapter(noteAdapter);
        noteAdapter.notifyDataSetChanged();
    }
}
