package com.example.testrun.model;

import android.net.Uri;

import java.io.Serializable;

public class Song implements Serializable {
    private long id;
    private String title;
    private String artist;
    private String path;
    private Uri albumArtUri;

    public Song(long id, String title, String artist, String path, Uri albumArtUri) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.albumArtUri = albumArtUri;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Uri getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(Uri albumArtUri) {
        this.albumArtUri = albumArtUri;
    }
}
