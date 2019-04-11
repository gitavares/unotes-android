package com.giselletavares.unotes.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "attachments",
        foreignKeys = @ForeignKey(entity = Note.class,
        parentColumns = "id",
        childColumns = "note_id",
        onDelete = ForeignKey.CASCADE))
public class Attachment {

//    @PrimaryKey(autoGenerate = true)
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    private String _id;
    @ColumnInfo(name = "note_id")
    private String noteId;
    @NonNull
    private String filename;
    @NonNull
    private String type;
    @TypeConverters(DateTypeConverter.class)
    private Date createdDate;


    // getters and setters
    @NonNull
    public String get_id() {
        return _id;
    }

    public void set_id(@NonNull String _id) {
        this._id = _id;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
