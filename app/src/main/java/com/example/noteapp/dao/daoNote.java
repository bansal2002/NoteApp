package com.example.noteapp.dao;

import com.example.noteapp.entities.Note;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface daoNote {

    @Query("Select * From notes order by id desc")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Delete()
    void deleteNote(Note note);
}
