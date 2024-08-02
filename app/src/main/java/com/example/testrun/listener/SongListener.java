package com.example.testrun.listener;

import android.view.View;

import com.example.testrun.model.Song;

public interface SongListener {
    void onSongClicked(Song song, int position);
}
