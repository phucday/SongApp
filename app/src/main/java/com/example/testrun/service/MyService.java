package com.example.testrun.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import com.example.testrun.listener.OnSongCompleteListener;
import com.example.testrun.manager.PlayManager;
import com.example.testrun.R;
import com.example.testrun.model.Song;
import com.example.testrun.utils.Constants;
import com.example.testrun.utils.Utils;
import com.example.testrun.activity.ListSongActivity;
import com.example.testrun.application.MyApplication;


public class MyService extends Service implements OnSongCompleteListener {
    private final IBinder binder = new MusicBinder();
    private PlayManager playManager;
    private Song songg;
    private String pathSong = "";

    @Override
    public void onCreate() {
        super.onCreate();
        playManager = PlayManager.getInstance(this);
        playManager.setOnSongCompleteListener(this);
    }


    public class MusicBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() == null) {
            // lần đầu start
            if (pathSong.isEmpty()) {
                pathSong = intent.getStringExtra("pathS");
                playSongByPath(pathSong);
                // vẫn là bài đó
            } else if (pathSong.equals(intent.getStringExtra("pathS"))) {
                playUI();
                // chọn bài khác
            } else if (!pathSong.equals(intent.getStringExtra("pathS"))) {
                // nếu Song tự hoàn thành khi đang ở ListSongAc
                if (getCurSong().getPath().equals(intent.getStringExtra("pathS"))) {
                    pathSong = getCurSong().getPath();
                    songChanged();
                    playUI();
                } else {
                    pathSong = intent.getStringExtra("pathS");
                    playSongByPath(pathSong);
                }

            }
        }

        // xử lí click Notification
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Constants.PAUSE)) {
                Log.d("clickNotification", intent.getAction());
                pause();
            } else if (intent.getAction().equals(Constants.PLAY)) {
                play();
            } else if (intent.getAction().equals(Constants.NEXT)) {
                next();
            } else if (intent.getAction().equals(Constants.PREVIOUS)) {
                previous();
            }
        }
        showNotification();
        return START_NOT_STICKY;
    }

    public void play() {
        playManager.play();
        Intent intent = new Intent(Constants.PLAYBACK_UPDATED);
        intent.putExtra("status", "playing");
        sendBroadcast(intent);
        showNotification();
    }

    public void pause() {
        playManager.pause();
        pauseUI();
        showNotification();
    }

    public void next() {
        playManager.next();
        songChanged();
        pathSong = songg.getPath();
    }

    public void previous() {
        playManager.previous();
        songChanged();
    }

    public void seekTo(int position) {
        playManager.seekTo(position);
    }

    public void playSongByPath(String path) {
        playManager.playSongByPath(path);
        songChanged();
    }

    private void songChanged() {
        songg = getCurSong();
        Intent intent = new Intent(Constants.SONG_CHANGED);
        intent.putExtra("song_title", songg.getTitle());
        intent.putExtra("song_artist", songg.getArtist());
        intent.putExtra("song_path", songg.getPath());
        intent.putExtra("song_image", songg.getAlbumArtUri().toString());
        sendBroadcast(intent);
        showNotification();
    }

    private void pauseUI() {
        Intent intentNotPlay = new Intent(Constants.PLAYBACK_UPDATED);
        intentNotPlay.putExtra("status", "paused");
        sendBroadcast(intentNotPlay);
    }

    private void playUI() {
        playManager.continueSong();
        Intent intentContinueSong = new Intent(Constants.PLAYBACK_UPDATED);
        intentContinueSong.putExtra("status", "playing");
        sendBroadcast(intentContinueSong);
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, ListSongActivity.class);
        Intent notificationIntentTest = this.getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntentTest, PendingIntent.FLAG_MUTABLE);

        Bitmap bitmap = Utils.getBitmapFromUri(this, songg.getAlbumArtUri());

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_custom_notification);
        remoteViews.setImageViewBitmap(R.id.img_notification, bitmap);
        remoteViews.setTextViewText(R.id.tv_title_notification, songg.getTitle());
        remoteViews.setTextViewText(R.id.tv_body_notification, "Name Singer, feat");
        remoteViews.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_play);
        remoteViews.setImageViewResource(R.id.btn_previous_notification, R.drawable.icon_previous);
        remoteViews.setImageViewResource(R.id.btn_next_notification, R.drawable.icon_next);
        if (isPlaying()) {
            remoteViews.setOnClickPendingIntent(R.id.btn_play_pause_notification, getPendingIntent(this, "PAUSE"));
            remoteViews.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_pause);
        } else {
            remoteViews.setOnClickPendingIntent(R.id.btn_play_pause_notification, getPendingIntent(this, "PLAY"));
            remoteViews.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_play);
        }
        remoteViews.setOnClickPendingIntent(R.id.btn_next_notification, getPendingIntent(this, "NEXT"));
        remoteViews.setOnClickPendingIntent(R.id.btn_previous_notification, getPendingIntent(this, "PREVIOUS"));

        RemoteViews remoteViewsBig = new RemoteViews(getPackageName(), R.layout.layout_custom_big_notification);
        remoteViewsBig.setImageViewBitmap(R.id.img_notification, bitmap);
        remoteViewsBig.setTextViewText(R.id.tv_title_notification, songg.getTitle());
        remoteViewsBig.setTextViewText(R.id.tv_body_notification, "Name Singer, feat");
        remoteViewsBig.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_play);
        remoteViewsBig.setImageViewResource(R.id.btn_previous_notification, R.drawable.icon_previous);
        remoteViewsBig.setImageViewResource(R.id.btn_next_notification, R.drawable.icon_next);
        if (isPlaying()) {
            remoteViewsBig.setOnClickPendingIntent(R.id.btn_play_pause_notification, getPendingIntent(this, "PAUSE"));
            remoteViewsBig.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_pause);
        } else {
            remoteViewsBig.setOnClickPendingIntent(R.id.btn_play_pause_notification, getPendingIntent(this, "PLAY"));
            remoteViewsBig.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_play);
        }
        remoteViewsBig.setOnClickPendingIntent(R.id.btn_next_notification, getPendingIntent(this, "NEXT"));
        remoteViewsBig.setOnClickPendingIntent(R.id.btn_previous_notification, getPendingIntent(this, "PREVIOUS"));

        Notification notification = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.music_note)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViewsBig)
                .setSound(null)
                .setOngoing(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        String actionIcon = isPlaying() ? "PAUSE" : "PLAY";
        int iconPlayOrPause = isPlaying() ? R.drawable.icon_pause : R.drawable.icon_play;
        Notification notificationTest = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.music_note)
                .setLargeIcon(bitmap)
                .setContentText(songg.getArtist())
                .setContentIntent(pendingIntent)
                .setSound(null)
                .setOngoing(false)
                .addAction(R.drawable.icon_previous, "Previous", getPendingIntent(this, "PREVIOUS"))
                .addAction(iconPlayOrPause, actionIcon, getPendingIntent(this, actionIcon))
                .addAction(R.drawable.icon_next, "Next", getPendingIntent(this, "NEXT"))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        startForeground(3443, notificationTest);
        Log.d("PHUCVP ", "showNotification");
    }

    private void showNotificationOld() {
        Intent notificationIntent = new Intent(this, ListSongActivity.class);
        Intent notificationIntentTest = this.getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntentTest, PendingIntent.FLAG_MUTABLE);

        Bitmap bitmap = Utils.getBitmapFromUri(this, songg.getAlbumArtUri());

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_custom_notification);
        remoteViews.setImageViewBitmap(R.id.img_notification, bitmap);
        remoteViews.setTextViewText(R.id.tv_title_notification, songg.getTitle());
        remoteViews.setTextViewText(R.id.tv_body_notification, "Name Singer, feat");
        remoteViews.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_play);
        remoteViews.setImageViewResource(R.id.btn_previous_notification, R.drawable.icon_previous);
        remoteViews.setImageViewResource(R.id.btn_next_notification, R.drawable.icon_next);
        if (isPlaying()) {
            remoteViews.setOnClickPendingIntent(R.id.btn_play_pause_notification, getPendingIntent(this, "PAUSE"));
            remoteViews.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_pause);
        } else {
            remoteViews.setOnClickPendingIntent(R.id.btn_play_pause_notification, getPendingIntent(this, "PLAY"));
            remoteViews.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_play);
        }
        remoteViews.setOnClickPendingIntent(R.id.btn_next_notification, getPendingIntent(this, "NEXT"));
        remoteViews.setOnClickPendingIntent(R.id.btn_previous_notification, getPendingIntent(this, "PREVIOUS"));

        RemoteViews remoteViewsBig = new RemoteViews(getPackageName(), R.layout.layout_custom_big_notification);
        remoteViewsBig.setImageViewBitmap(R.id.img_notification, bitmap);
        remoteViewsBig.setTextViewText(R.id.tv_title_notification, songg.getTitle());
        remoteViewsBig.setTextViewText(R.id.tv_body_notification, "Name Singer, feat");
        remoteViewsBig.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_play);
        remoteViewsBig.setImageViewResource(R.id.btn_previous_notification, R.drawable.icon_previous);
        remoteViewsBig.setImageViewResource(R.id.btn_next_notification, R.drawable.icon_next);
        if (isPlaying()) {
            remoteViewsBig.setOnClickPendingIntent(R.id.btn_play_pause_notification, getPendingIntent(this, "PAUSE"));
            remoteViewsBig.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_pause);
        } else {
            remoteViewsBig.setOnClickPendingIntent(R.id.btn_play_pause_notification, getPendingIntent(this, "PLAY"));
            remoteViewsBig.setImageViewResource(R.id.btn_play_pause_notification, R.drawable.icon_play);
        }
        remoteViewsBig.setOnClickPendingIntent(R.id.btn_next_notification, getPendingIntent(this, "NEXT"));
        remoteViewsBig.setOnClickPendingIntent(R.id.btn_previous_notification, getPendingIntent(this, "PREVIOUS"));

        Notification notification = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.music_note)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViewsBig)
                .setSound(null)
                .setOngoing(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        startForeground(3443, notification);
        Log.d("PHUCVP ", "showNotification");
    }

    private PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(context, MyService.class);
        intent.setAction(action);
        Log.d("PVPLOG", "getPendingIntent " + action);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_MUTABLE);
    }

    public boolean isPlaying() {
        return playManager.isPlaying();
    }

    public int getDuration() {
        return playManager.getDuration();
    }

    public int getCurPosition() {
        return playManager.getCurPosition();
    }

    public void next10s() {
        playManager.next10s();
    }

    public void previous10s() {
        playManager.previous10s();
    }

    public void resetSong(boolean yOn) {
        playManager.resetSong(yOn);
    }

    public Song getCurSong() {
        return playManager.getCurSong();
    }

    public int getCurIndex() {
        return playManager.getCurIndex();
    }

    @Override
    public void onSongComplete() {
        next();
    }
}
