package com.giselletavares.unotes.models;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface NoteDAO {

    @Insert
    void addNote(Note note);

    @Query("SELECT * FROM notes WHERE category_id = :categoryId ORDER BY updatedDate DESC")
    List<Note> getNotesByCategory(String categoryId);

    @Query("SELECT * FROM notes WHERE id = :noteId ")
    Note getNoteById(String noteId);

    @Query("SELECT COUNT(*) FROM notes WHERE category_id = :categoryId")
    int getNumOfNotesByCategory(String categoryId);

    @Query("SELECT * FROM notes WHERE category_id = :categoryId AND (title LIKE :search OR note LIKE :search) ORDER BY title ASC")
    List<Note> getNotesOrderByTitleAsc(String categoryId, String search);

    @Query("SELECT * FROM notes WHERE category_id = :categoryId AND (title LIKE :search OR note LIKE :search) ORDER BY title DESC")
    List<Note> getNotesOrderByTitleDesc(String categoryId, String search);

    @Query("SELECT * FROM notes WHERE category_id = :categoryId AND (title LIKE :search OR note LIKE :search) ORDER BY createdDate DESC")
    List<Note> getNotesOrderByDateDesc(String categoryId, String search);

    @Query("SELECT * FROM notes WHERE category_id = :categoryId AND (title LIKE :search OR note LIKE :search) ORDER BY createdDate ASC")
    List<Note> getNotesOrderByDateAsc(String categoryId, String search);

    @Query("SELECT * FROM notes WHERE category_id = :categoryId AND (title LIKE :search OR note LIKE :search)")
    List<Note> getNotesBySearch(String categoryId, String search);

    @Update
    void updateNote(Note note);

    @Delete
    void deleteNote(Note note);

}
