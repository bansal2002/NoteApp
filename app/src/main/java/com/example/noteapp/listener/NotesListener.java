package com.example.noteapp.listener;

import com.example.noteapp.entities.Note;

public interface NotesListener {

    void onNoteClicked(Note note,int position);
}
