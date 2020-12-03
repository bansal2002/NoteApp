package com.example.noteapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noteapp.R;
import com.example.noteapp.database.NotesDatabase;
import com.example.noteapp.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteCreation extends AppCompatActivity {

    ImageView back,done,imageNote;
    EditText noteTitle,noteSubTitle,noteText;
    TextView dateTime;
    private String selectSubtitleColor;
    private View subtitleIndicater;
    TextView textWebUrl;
    LinearLayout webLayout;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private String selectedImagePath;

    private AlertDialog alertDialog;
    private AlertDialog alertDelete;

    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_creation);

        noteTitle = findViewById(R.id.noteTitle);
        noteSubTitle = findViewById(R.id.noteSubtitle);
        noteText = findViewById(R.id.inputNote);
        dateTime = findViewById(R.id.date);
        subtitleIndicater = findViewById(R.id.viewSubtitleIndicater);
        imageNote = findViewById(R.id.imageNote);
        textWebUrl = findViewById(R.id.textWebUrl);
        webLayout = findViewById(R.id.layoutWebUrl);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        dateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date())
        );

        done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        selectSubtitleColor = "#4A3C3C";
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.deleteWebUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebUrl.setText(null);
                webLayout.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.deleteImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.deleteImage).setVisibility(View.GONE);
                selectedImagePath = "";
            }
        });

        if (getIntent().getBooleanExtra("isFromQuickAction",false)){
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null){
                if (type.equals("image")){
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.deleteImage).setVisibility(View.VISIBLE);
                }else if (type.equals("url")){
                    textWebUrl.setText(getIntent().getStringExtra("url"));
                    webLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        setSubtitleIndicater();
        initmicellous();
    }

    private void setViewOrUpdateNote(){
        noteTitle.setText(alreadyAvailableNote.getTitle());
        noteSubTitle.setText(alreadyAvailableNote.getSubtitle());
        noteText.setText(alreadyAvailableNote.getNoteText());
        dateTime.setText(alreadyAvailableNote.getDate_time());

        if (alreadyAvailableNote.getImage_path() != null && !alreadyAvailableNote.getImage_path().trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImage_path()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.deleteImage).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImage_path();
        }

        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
            textWebUrl.setText(alreadyAvailableNote.getWebLink());
            webLayout.setVisibility(View.VISIBLE);
        }
    }

    public void saveNote(){
        if (noteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note can't be empty",Toast.LENGTH_SHORT).show();
            return;
        }else if (noteSubTitle.getText().toString().trim().isEmpty()||noteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"can't be empty",Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(noteTitle.getText().toString());
        note.setSubtitle(noteSubTitle.getText().toString());
        note.setNoteText(noteText.getText().toString());
        note.setDate_time(dateTime.getText().toString());
        note.setColor(selectSubtitleColor);
        note.setImage_path(selectedImagePath);

        if (webLayout.getVisibility() == View.VISIBLE){
            note.setWebLink(textWebUrl.getText().toString());
        }

        if (alreadyAvailableNote != null){
            note.setId(alreadyAvailableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class saveNoteText extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getNotesDatabase(getApplicationContext()).daoNote().insert(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new saveNoteText().execute();
    }

    private void initmicellous(){
        final LinearLayout layoutMiscellous = findViewById(R.id.layoutMiscellousll);
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellous);
        layoutMiscellous.findViewById(R.id.textMiscellous).setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView imageView1 = layoutMiscellous.findViewById(R.id.image_color_1);
        final ImageView imageView2 = layoutMiscellous.findViewById(R.id.image_color_2);
        final ImageView imageView3 = layoutMiscellous.findViewById(R.id.image_color_3);
        final ImageView imageView4 = layoutMiscellous.findViewById(R.id.image_color_4);
        final ImageView imageView5 = layoutMiscellous.findViewById(R.id.image_color_5);

        layoutMiscellous.findViewById(R.id.view_color_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSubtitleColor = "#4A3C3C";
                imageView1.setImageResource(R.drawable.icon_done);
                imageView2.setImageResource(0);
                imageView3.setImageResource(0);
                imageView4.setImageResource(0);
                imageView5.setImageResource(0);
                setSubtitleIndicater();
            }
        });
        layoutMiscellous.findViewById(R.id.view_color_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSubtitleColor = "#F9A825";
                imageView1.setImageResource(0);
                imageView2.setImageResource(R.drawable.icon_done);
                imageView3.setImageResource(0);
                imageView4.setImageResource(0);
                imageView5.setImageResource(0);
                setSubtitleIndicater();
            }
        });
        layoutMiscellous.findViewById(R.id.view_color_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSubtitleColor = "#E9F3062A";
                imageView1.setImageResource(0);
                imageView2.setImageResource(0);
                imageView3.setImageResource(R.drawable.icon_done);
                imageView4.setImageResource(0);
                imageView5.setImageResource(0);
                setSubtitleIndicater();
            }
        });
        layoutMiscellous.findViewById(R.id.view_color_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSubtitleColor = "#283593";
                imageView1.setImageResource(0);
                imageView2.setImageResource(0);
                imageView3.setImageResource(0);
                imageView4.setImageResource(R.drawable.icon_done);
                imageView5.setImageResource(0);
                setSubtitleIndicater();
            }
        });
        layoutMiscellous.findViewById(R.id.view_color_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSubtitleColor = "#000000";
                imageView1.setImageResource(0);
                imageView2.setImageResource(0);
                imageView3.setImageResource(0);
                imageView4.setImageResource(0);
                imageView5.setImageResource(R.drawable.icon_done);
                setSubtitleIndicater();
            }
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case "#F9A825":
                    layoutMiscellous.findViewById(R.id.view_color_2).performClick();
                    break;
                case "#E9F3062A":
                    layoutMiscellous.findViewById(R.id.view_color_3).performClick();
                    break;
                case "#283593":
                    layoutMiscellous.findViewById(R.id.view_color_4).performClick();
                    break;
                case "#000000":
                    layoutMiscellous.findViewById(R.id.view_color_4).performClick();
                    break;
            }
        }

        layoutMiscellous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(bottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                )!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            NoteCreation.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else {
                    selectImage();
                }
            }
        });
        layoutMiscellous.findViewById(R.id.textAddUrlMis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddDialogUrl();
            }
        });

        if (alreadyAvailableNote != null){
            layoutMiscellous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellous.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }
    }
    private void showDeleteNoteDialog(){
        if (alertDelete == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteCreation.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteContainer)
            );
            builder.setView(view);
            alertDelete = builder.create();
            if (alertDelete.getWindow() != null){
                alertDelete.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteYas).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getNotesDatabase(getApplicationContext()).daoNote()
                                    .deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });
            view.findViewById(R.id.textCancleDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDelete.dismiss();
                }
            });
        }
        alertDelete.show();
    }

    public void setSubtitleIndicater(){
        GradientDrawable gradientDrawable = (GradientDrawable) subtitleIndicater.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectSubtitleColor));
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else {
                Toast.makeText(this,"premission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectNoteUri = data.getData();
                if (selectNoteUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectNoteUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.deleteImage).setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectNoteUri);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri){
        String filepath;
        Cursor cursor = getContentResolver().
                query(contentUri,null,null,null,null);
        if (contentUri == null){
            filepath = contentUri.getPath();
        }else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filepath = cursor.getString(index);
            cursor.close();
        }
        return filepath;
    }

    private void showAddDialogUrl(){
        if (alertDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(NoteCreation.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_dialog,
                    (ViewGroup) findViewById(R.id.layoutWebUrlDialog)
            );
            builder.setView(view);
            alertDialog = builder.create();
            if (alertDialog.getWindow() != null){
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText editWebUrl = view.findViewById(R.id.webUrl);
            editWebUrl.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editWebUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(NoteCreation.this,"Please Enter a url",Toast.LENGTH_SHORT).show();
                    }else if (!Patterns.WEB_URL.matcher(editWebUrl.getText().toString()).matches()){
                        Toast.makeText(NoteCreation.this,"Url is not found",Toast.LENGTH_SHORT).show();
                    }else {
                        textWebUrl.setText(editWebUrl.getText().toString());
                        webLayout.setVisibility(View.VISIBLE);
                        alertDialog.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textCancle).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }
        alertDialog.show();
    }
}