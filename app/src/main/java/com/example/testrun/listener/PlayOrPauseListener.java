package com.example.testrun.listener;

import com.example.testrun.model.Song;

public interface PlayOrPauseListener {
    void playOrPause(Song song,int position);
}
