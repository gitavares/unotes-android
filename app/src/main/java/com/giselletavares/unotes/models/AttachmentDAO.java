package com.giselletavares.unotes.models;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.giselletavares.unotes.models.Attachment;

import java.util.List;

@Dao
public interface AttachmentDAO {

    @Insert
    void addAttachment(Attachment attachment);

    @Query("SELECT * FROM attachments WHERE note_id = :noteId AND type = :type ORDER BY createdDate DESC")
    List<Attachment> getAttachmentsByNote(String noteId, String type);

    @Query("SELECT filename FROM attachments WHERE note_id = :noteId AND type = 'image' ORDER BY id DESC LIMIT 1")
    String getLastImageByNoteId(String noteId);

    @Query("SELECT * " +
            "FROM attachments " +
            "WHERE id = :attachmentId ")
    Attachment getAttachmentById(String attachmentId);

    @Update
    void updateAttachment(Attachment attachment);

    @Delete
    void deleteAttachment(Attachment attachment);
    
}
