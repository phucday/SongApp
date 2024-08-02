package com.example.testrun.repository;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.testrun.model.Song;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SongRepository {
    private ContentResolver contentResolver;

    public SongRepository(Context context) {
        this.contentResolver = context.getContentResolver();
    }

    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        try (Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                Log.d("cursorcursor", "Cursor count: " + cursor.getCount());
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String path = cursor.getString(pathColumn);
                    long albumId = cursor.getLong(albumIdColumn);
                    Uri albumArtUri = getAlbumArtUri(albumId);
                    Log.d("PHUCVPLOG", "title: " + title);
                    Log.d("PHUCVPLOG", "albumArtUri: " + albumArtUri);
                    songs.add(new Song(id, title, artist, path, albumArtUri));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    private Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }

}
