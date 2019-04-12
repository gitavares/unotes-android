package com.giselletavares.unotes.adapters;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.giselletavares.unotes.R;
import com.giselletavares.unotes.activities.NoteActivity;
import com.giselletavares.unotes.models.AppDatabase;
import com.giselletavares.unotes.models.Note;
import com.giselletavares.unotes.utils.Formatting;

import java.io.File;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NotesViewHolder> {

    public static AppDatabase sAppDatabase;
    private List<Note> mNoteList;
    private Context mContext;

    Formatting formatting;

    public NoteAdapter(List<Note> noteList, Context context) {
        this.mNoteList = noteList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final Context mContext = viewGroup.getContext();

        View mView;
        mView = LayoutInflater.from(mContext).inflate(R.layout.item_note_list, viewGroup, false);
        final NotesViewHolder notesViewHolder = new NotesViewHolder(mView);

        notesViewHolder.mLinearLayout_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NoteActivity.class);
                intent.putExtra("noteId", mNoteList.get(notesViewHolder.getAdapterPosition()).get_id());
                intent.putExtra("categoryId", mNoteList.get(notesViewHolder.getAdapterPosition()).getCategoryId());
                mContext.startActivity(intent);
            }
        });

        return notesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NotesViewHolder notesViewHolder, int position) {
        // DATABASE
        sAppDatabase = Room.databaseBuilder(mContext, AppDatabase.class, "unotes")
                .allowMainThreadQueries() // it will allow the database works on the main thread
                .fallbackToDestructiveMigration() // because i wont implement now migrations
                .build();

        Note currentNote = mNoteList.get(position);
        String lastImage = sAppDatabase.mAttachmentDAO().getLastImageByNoteId(currentNote.get_id());

        formatting = new Formatting();

        notesViewHolder.lblNoteTitle.setText(currentNote.getTitle());
        notesViewHolder.lblCreatedDate.setText(formatting.getDateMediumFormatter(currentNote.getCreatedDate()) + " - Last update: " + formatting.getDateLongFormatter(currentNote.getUpdatedDate()));

        if(lastImage != null){
            File imgFile = new  File(lastImage);
            notesViewHolder.imgNote.setVisibility(View.VISIBLE);
            notesViewHolder.imgNote.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        } else {
            notesViewHolder.imgNote.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLinearLayout_note;
        private TextView lblNoteTitle;
        private TextView lblCreatedDate;
        private ImageView imgNote;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);

            this.mLinearLayout_note = itemView.findViewById(R.id.noteId);
            this.lblNoteTitle = itemView.findViewById(R.id.lblNoteTitle);
            this.lblCreatedDate = itemView.findViewById(R.id.lblCreatedDate);
            this.imgNote = itemView.findViewById(R.id.imgNote);
        }

    }

}
