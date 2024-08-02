package com.example.testrun.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.example.testrun.databinding.LayoutNoticeBinding;
import com.example.testrun.listener.PlayOrPauseListener;
import com.example.testrun.service.MyService;
import com.example.testrun.manager.PlayManager;
import com.example.testrun.R;
import com.example.testrun.model.Song;
import com.example.testrun.adapter.SongAdapter;
import com.example.testrun.listener.SongListener;
import com.example.testrun.repository.SongRepository;
import com.example.testrun.databinding.ActivityMainBinding;
import com.example.testrun.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SongListener, PlayOrPauseListener {
    private static final int RC_NOTIFICATION = 33;
    private ActivityMainBinding binding;
    private LayoutNoticeBinding noticeBinding;
    private PlayManager playManager;
    private List<Song> playlist;
    private boolean nav_down = true;
    private MyService musicService;
    private boolean bound = false;
    //    private Song songMain;
    private Animation animation;
    private String pathSong;
    private SongAdapter songAdapter;
    private SongRepository songRepository;
    private List<Song> songs;
    private boolean resetSong = false;
    private SimpleDateFormat spf = new SimpleDateFormat("mm:ss");
    private ObjectAnimator rotationAnimator;
    private float curRotation;

    private int curPosition ;
    private int oldPosition = -1;

    private float[] waveform;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyService.MusicBinder binder = (MyService.MusicBinder) service;
            musicService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private BroadcastReceiver playbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.PLAYBACK_UPDATED)) {
                String status = intent.getStringExtra("status");
                Log.d("PhucPLAYBACK_UPDATED", status);
                updatePlaybackUI(status);
            } else if (intent.getAction().equals(Constants.SONG_CHANGED)) {
                String title = intent.getStringExtra("song_title");
                String artist = intent.getStringExtra("song_artist");
                pathSong = intent.getStringExtra("song_path");
                String image = intent.getStringExtra("song_image");
                updateSongInfoUI(title, artist, image);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        permission_post_notification();
        checkList();

        setUp();
        getDataSong();
        setBindService();
        setReceiver();

        loadSongs();
        userClick();
    }

    private void setWaveformSeekBar(){
        Random rd = new Random();
        waveform = new float[28];
        float min = 0.2f;
        float max = 1f;
        for(int i =0; i < waveform.length ;i++){
            waveform[i] = min + (max - min) * rd.nextFloat();
        }
        binding.processSong.setWaveform(waveform);
    }

    private void checkList() {
        playManager = PlayManager.getInstance(this);
        playlist = playManager.getPlaylist();
        Log.d("PHUCLOG", playlist.size() + "");
    }

    private void userClick() {
        onPlayOrPauseClick();
        onNextClick();
        onPreviousClick();
        next10s();
        previous10s();
        shareSong();
        notice();
        resetSong();
        testVisibleGone();
//        testNav();
    }

    private void updateSongTime() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            binding.tvStartSong.setText(spf.format(musicService.getCurPosition()) + "");
            binding.processSong.setProgress(musicService.getCurPosition());
            updateSongTime();
        },200);
    }

    private void setUp() {
        initRotationAnimator();
        binding.imgBack.setOnClickListener(v -> onBackPressed());
    }

    private void getDataSong() {

        pathSong = (String) getIntent().getExtras().get("pathSong");
        String imageUriSong = (String) getIntent().getExtras().get("imageUriSong");
        String artistSong = (String) getIntent().getExtras().get("artistSong");
        String nameSong = (String) getIntent().getExtras().get("nameSong");
        curPosition = (int) getIntent().getExtras().get("positionSong");
        Log.d("curPositiongetDataSong"," "+curPosition);
        Glide.with(this).load(Uri.parse(imageUriSong))
                .into(binding.imgCd);
        startRotation();
        binding.nameSong.setText(nameSong);
        binding.tvDepeche.setText(artistSong);

        setWaveformSeekBar();
    }

    private void loadSongs() {
        songRepository = new SongRepository(this);
        songs = songRepository.getAllSongs();
        Log.d("songssongs", songs.size() + "");
        songAdapter = new SongAdapter(songs, this,this);
        binding.rcvSongPlay.setAdapter(songAdapter);
    }

    private void setViewModel() {
        if(oldPosition != curPosition ){
            if(oldPosition != -1){
                Log.d("oldPosition != -1","oldPosition: "+oldPosition);
                songAdapter.setIsPlaying(false, oldPosition);
            }
        }
        Log.d("oldPositionNN ","oldPosition: "+oldPosition);
        oldPosition = curPosition;
        curPosition = musicService.getCurIndex();
        songAdapter.setIsPlaying(musicService.isPlaying(), curPosition);
        Log.d("curPositionsetViewModel", " " + curPosition);
    }

    private void setBindService() {
        Intent intent = new Intent(this, MyService.class);
        if (!bound) {
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
        intent.putExtra("pathS", pathSong);
        startService(intent);
    }

    private void setReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.PLAYBACK_UPDATED);
        filter.addAction(Constants.SONG_CHANGED);

        //có thể dùng cách 1:
//        ContextCompat.registerReceiver(this,playbackReceiver,filter,ContextCompat.RECEIVER_EXPORTED);

        //hoặc cách 2:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playbackReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(playbackReceiver, filter);
        }
    }

    private void updatePlaybackUI(String status) {
        if (status.equals("playing")) {
            binding.btnPlayPause.setImageResource(R.drawable.icon_pause);
            startRotation();
        } else if (status.equals("paused")) {
            binding.btnPlayPause.setImageResource(R.drawable.icon_play);
            pauseRotation();
        }
        binding.rcvSongPlay.scrollToPosition(curPosition);
        setViewModel();

        updateProcess();
    }

    private void updateProcess() {
        binding.tvEndSong.setText(spf.format(musicService.getDuration()) + "");
        binding.processSong.setMax(musicService.getDuration());
        binding.processSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    musicService.seekTo(binding.processSong.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                musicService.seekTo(binding.processSong.getProgress());
            }
        });
        updateSongTime();
    }

    private void updateSongInfoUI(String title, String artist, String image) {
        // Update UI with new song information
        if (musicService.isPlaying()) {
            binding.btnPlayPause.setImageResource(R.drawable.icon_pause);
            binding.nameSong.setText(title);
            binding.tvDepeche.setText(artist);
            Glide.with(this).load(Uri.parse(image))
                    .into(binding.imgCd);
        }

        curPosition = musicService.getCurIndex();
        binding.rcvSongPlay.scrollToPosition(curPosition);

        Log.d("curPosiSongInfoUI"," "+curPosition);
        setViewModel();

        setWaveformSeekBar();
        updateProcess();
    }

    private void onPlayOrPauseClick() {
        binding.btnPlayPause.setOnClickListener(v -> {
            if (musicService.isPlaying()) {
                if (bound) musicService.pause();
                songAdapter.setIsPlaying(false,curPosition);
            } else {
                if (bound) musicService.play();
                songAdapter.setIsPlaying(true,curPosition);
            }
        });
    }

    private void onNextClick() {
        binding.btnNext.setOnClickListener(v -> {
            if (bound) musicService.next();
        });
    }

    private void onPreviousClick() {
        binding.btnPrevious.setOnClickListener(v -> {
            if (bound) musicService.previous();
        });
    }

    private void next10s() {
        binding.btnNext10s.setOnClickListener(v -> {
            musicService.next10s();
        });
    }

    private void previous10s() {
        binding.btnBack10s.setOnClickListener(v -> {
            musicService.previous10s();
        });
    }

    private void shareSong() {
        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("audio/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(String.valueOf(pathSong)));
            startActivity(Intent.createChooser(shareIntent, "Sharing File Song"));
        });
    }

    private void notice() {
        binding.btnNotice.setOnClickListener(v -> {
            Toast.makeText(this, "Nothing happen ! Keep enjoy", Toast.LENGTH_SHORT).show();
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            noticeBinding = LayoutNoticeBinding.inflate(getLayoutInflater());
            dialog.setContentView(noticeBinding.getRoot());
            Window window = dialog.getWindow();
            if(window == null){
                return;
            }
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams wdAttributes = window.getAttributes();
            wdAttributes.gravity = Gravity.BOTTOM;
            window.setAttributes(wdAttributes);
            dialog.setCancelable(true);

            noticeBinding.btnCloseFb.setOnClickListener(vi -> dialog.dismiss());
            noticeBinding.btnSendFb.setOnClickListener(vi -> {
                Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            dialog.show();
        });
    }

    private void resetSong() {
        binding.btnRenew.setOnClickListener(v -> {
            resetSong = !resetSong;
            Log.d("PhucTAG", "resetSong: "+ resetSong);
            if (resetSong) {
                musicService.resetSong(true);
                binding.btnRenew.setImageResource(R.drawable.icon_change_finish);
            } else {
                musicService.resetSong(false);
                binding.btnRenew.setImageResource(R.drawable.icon_renew);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
        unregisterReceiver(playbackReceiver);
    }

    private void testVisibleGone() {
        binding.navUpDown.setOnClickListener(v -> {
            pauseRotation();
            if (nav_down) {
                binding.navUpDown.setImageResource(R.drawable.icon_down);
                binding.imgCd.setVisibility(View.GONE);
                binding.btnPlayPause.setVisibility(View.GONE);
                binding.btnNext.setVisibility(View.GONE);
                binding.btnPrevious.setVisibility(View.GONE);
                binding.btnNext10s.setVisibility(View.GONE);
                binding.btnBack10s.setVisibility(View.GONE);
                binding.btnNotice.setVisibility(View.GONE);
                binding.btnShare.setVisibility(View.GONE);
                binding.btnRenew.setVisibility(View.GONE);
                nav_down = false;
            } else {
                binding.navUpDown.setImageResource(R.drawable.icon_up);
                binding.imgCd.setVisibility(View.VISIBLE);
                binding.btnPlayPause.setVisibility(View.VISIBLE);
                binding.btnNext.setVisibility(View.VISIBLE);
                binding.btnPrevious.setVisibility(View.VISIBLE);
                binding.btnNext10s.setVisibility(View.VISIBLE);
                binding.btnBack10s.setVisibility(View.VISIBLE);
                binding.btnNotice.setVisibility(View.VISIBLE);
                binding.btnShare.setVisibility(View.VISIBLE);
                binding.btnRenew.setVisibility(View.VISIBLE);
                if (musicService.isPlaying()) {
                    resumeRotation();
                }
                nav_down = true;
            }
        });
    }

    private void permission_post_notification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, RC_NOTIFICATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_NOTIFICATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    private void initRotationAnimator() {
        rotationAnimator = ObjectAnimator.ofFloat(binding.imgCd, "rotation", 0f, 360f);
        rotationAnimator.setDuration(10000);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
    }

    private void startRotation() {
        rotationAnimator.setFloatValues(curRotation, curRotation + 360f);
        rotationAnimator.start();
    }

    private void pauseRotation() {
        curRotation = binding.imgCd.getRotation();
        rotationAnimator.cancel();
    }

    private void resumeRotation() {
        startRotation();
    }

    private void testNav() {
        binding.navUpDown.setOnClickListener(v -> {
            collapseView(binding.ctnHeader);
            Log.d("testNavtestNav: ",nav_down+"");
        });
    }

    private void collapseView(View view) {
        final int initialHeight = view.getMeasuredHeight();

        ObjectAnimator animator = ObjectAnimator.ofInt(view, "navUp", initialHeight, 0);
        animator.setDuration(1000); // Thời gian thu gọn

        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
        });
        animator.start();
    }

    private void expandView(View view) {
    }

    @Override
    public void onSongClicked(Song song, int position) {
//        if(!pathSong.equals(song.getPath())){
        if (curPosition != position) {
            pathSong = song.getPath();
            Glide.with(this).load(song.getAlbumArtUri())
                    .into(binding.imgCd);
            binding.imgCd.setAnimation(animation);
            setBindService();
            Log.d("ssssafter",curPosition+"");
        }
    }

    @Override
    public void playOrPause(Song song,int position) {
//        if(!pathSong.equals(song.getPath())){
            if (curPosition != position) {
            onSongClicked(song,position);
        }else {
            if(musicService.isPlaying()){
                musicService.pause();
            }else {
                musicService.play();
            }
        }

    }
}