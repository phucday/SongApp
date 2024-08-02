package com.example.testrun.manager;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.testrun.listener.OnSongCompleteListener;
import com.example.testrun.model.Song;
import com.example.testrun.repository.SongRepository;

import java.io.IOException;
import java.util.List;

public class PlayManager {
    private static PlayManager instance;
    private MediaPlayer mediaPlayer;
    private List<Song> playlist;
    private int currentIndex;
    private SongRepository songRepository;
    private boolean isPrepared = false;
    private int countPath = 0;
    private Song song;
    private OnSongCompleteListener onSongCompleteListener;

    private PlayManager(Context context) {
        mediaPlayer = new MediaPlayer();
        currentIndex = 0;
        songRepository = new SongRepository(context);
        loadPlaylist();
        mediaPlayer.setOnCompletionListener(mp -> {
            Log.d("PlayManagerNext: ", "come");
            if (onSongCompleteListener != null) {
                onSongCompleteListener.onSongComplete();
            }
        });
    }

    // vì dùng cách đo time bị sai số lúc đưọc lúc không -> dùng interface
    public void setOnSongCompleteListener(OnSongCompleteListener listener) {
        this.onSongCompleteListener = listener;
    }

    public static synchronized PlayManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlayManager(context);
        }
        return instance;
    }

    private void loadPlaylist() {
        playlist = songRepository.getAllSongs();
    }

    public List<Song> getPlaylist() {
        return playlist;
    }

    public void refreshPlaylist() {
        loadPlaylist();
    }

    public void setPlaylist(List<Song> songs) {
        playlist = songs;
        currentIndex = 0;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void continueSong() {
        mediaPlayer.start();

    }

    public void play() {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                if (!isPrepared) {
                    prepareCurrentSong();
                }
                mediaPlayer.start();

            }
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();

        }
    }

    public void next() {
        if (currentIndex <= playlist.size() - 1) {
            currentIndex++;
            if (currentIndex > playlist.size() - 1) {
                currentIndex = 0;
            }
            prepareCurrentSong();
            play();
        }
    }

    public void previous() {
        if (currentIndex >= 0) {
            currentIndex--;
            if (currentIndex < 0) {
                currentIndex = playlist.size() - 1;
            }
            prepareCurrentSong();
            play();
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public void next10s() {
        int timeNext10s = mediaPlayer.getCurrentPosition() + 10000;
        if (timeNext10s <= mediaPlayer.getDuration()) {
            mediaPlayer.seekTo(timeNext10s);
        } else {
            next();
        }
    }

    public void previous10s() {
        int timePre10s = mediaPlayer.getCurrentPosition() - 10000;
        if (timePre10s >= 0) {
            mediaPlayer.seekTo(timePre10s);
        } else {
            previous();
        }
    }

    private void prepareCurrentSong() {
        if (currentIndex >= 0 && currentIndex < playlist.size()) {
            song = playlist.get(currentIndex);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song.getPath());
                mediaPlayer.prepare();
                isPrepared = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void playSongByPath(String path) {
        try {
            countPath = 0;
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPrepared = true;
            song = getCurrentSongByPath(path);
            currentIndex = countPath;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Song getCurrentSongByPath(String path) {
        for (Song song : playlist) {
            if (song.getPath().equals(path)) {
                return song;
            }
            countPath++;
        }
        return null;
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void resetSong(boolean yOn) {
        mediaPlayer.setOnCompletionListener(m -> {
            if (yOn) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            } else {
                if (onSongCompleteListener != null) {
                    onSongCompleteListener.onSongComplete();
                }
            }
        });
    }

    public Song getCurSong() {
        return song;
    }

    public int getCurIndex() {
        return currentIndex;
    }

    public boolean autoCompleteSong() {
        return mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration();
    }
}
