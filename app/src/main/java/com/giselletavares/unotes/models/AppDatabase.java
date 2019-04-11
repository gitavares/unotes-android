package com.giselletavares.unotes.models;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {Category.class, Note.class, Attachment.class}, version = 10)
@TypeConverters({DateTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract CategoryDAO mCategoryDAO();
    public abstract NoteDAO mNoteDAO();
    public abstract AttachmentDAO mAttachmentDAO();

}
