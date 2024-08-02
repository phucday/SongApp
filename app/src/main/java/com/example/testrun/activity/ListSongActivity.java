package com.example.testrun.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.testrun.R;
import com.example.testrun.listener.PlayOrPauseListener;
import com.example.testrun.model.Song;
import com.example.testrun.adapter.SongAdapter;
import com.example.testrun.listener.SongListener;
import com.example.testrun.repository.SongRepository;
import com.example.testrun.databinding.ActivityListSongBinding;
import com.example.testrun.service.MyService;
import com.example.testrun.utils.Constants;

import java.util.List;

public class ListSongActivity extends AppCompatActivity implements SongListener, PlayOrPauseListener {
    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 2;

    private int curPosition = -1;
    private int oldPosition = -1;

    ActivityListSongBinding binding;
    private SongAdapter songAdapter;
    private SongRepository songRepository;
    private List<Song> songs;
    private String pathSong;
    private MyService musicService;
    private boolean bound = false;
    private BroadcastReceiver playbackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.PLAYBACK_UPDATED)) {
                String status = intent.getStringExtra("status");
                Log.d("PhucN",status);
                updatePlaybackUI(status);
            }else if (intent.getAction().equals(Constants.SONG_CHANGED)) {
//                updateSongInfoUI(title, artist, image);
                pathSong = intent.getStringExtra("song_path");
                updatePlaybackUI("");
            }
        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.MusicBinder binder = (MyService.MusicBinder) iBinder;
            musicService = binder.getService();
            bound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityListSongBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setBindService();
        getPerMission();
        setReceiver();
    }

    private void setBindService() {
        Intent intent = new Intent(this, MyService.class);
        if(!bound){
            bindService(intent,mServiceConnection,Context.BIND_AUTO_CREATE);
        }
    }

    private void setViewModel(){
        Log.d("setViewModel: ","come");
        int temp = musicService.getCurIndex();
        if(oldPosition != curPosition ){
            if(oldPosition != -1){
                Log.d("oldPosition != -1","oldPosition: "+oldPosition);
                songAdapter.setIsPlaying(false, oldPosition);
            }
        }
        Log.d("oldPositionNN ","oldPosition: "+oldPosition);
        if(curPosition != temp){
            oldPosition = curPosition;
            curPosition = musicService.getCurIndex();
        }
//        oldPosition = curPosition;
//        curPosition = musicService.getCurIndex();

        Log.d("curSongPoAtService: ","come "+ musicService.getCurIndex());
        binding.rcvSong.scrollToPosition(curPosition);
        songAdapter.setIsPlaying(musicService.isPlaying(),curPosition);

        songAdapter.setIsPlaying(false,oldPosition);

    }

    private void updatePlaybackUI(String status) {
        setViewModel();
    }

    private void getPerMission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) và cao hơn
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_MANAGE_EXTERNAL_STORAGE);
            } else {
                loadSongs();
            }
        } else {
            // Android 10 (API 29) và thấp hơn
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
            } else {
                loadSongs();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSongs() {
        songRepository = new SongRepository(this);
        songs = songRepository.getAllSongs();
        Log.d("songssongs",songs.size()+"");
        songAdapter = new SongAdapter(songs,this,this);
        binding.rcvSong.setAdapter(songAdapter);

    }
    private void setReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("PLAYBACK_UPDATED");
        filter.addAction("SONG_CHANGED");

        //có thể dùng cách 1:
//        ContextCompat.registerReceiver(this,playbackReceiver,filter,ContextCompat.RECEIVER_EXPORTED);

        //hoặc cách 2:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playbackReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(playbackReceiver, filter);
        }
    }

    @Override
    public void onSongClicked(Song song,int position) {
//        if (curPosition != position) {
            Intent intentSong = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong("idSong", song.getId());
            bundle.putString("artistSong", song.getArtist());
            bundle.putString("pathSong", song.getPath());
            bundle.putString("imageUriSong", song.getAlbumArtUri().toString());
            bundle.putInt("positionSong", position);
            Log.d("onSongClickedPo", position + "");
            intentSong.putExtras(bundle);
            startActivity(intentSong);
//        }
    }

    @Override
    public void playOrPause(Song song,int position) {
        if(bound) {
            if (curPosition != position) {
                onSongClicked(song, position);
            } else {
                if (musicService.isPlaying()) {
                    musicService.pause();
                } else {
                    musicService.play();
                }
            }
        }else {
            onSongClicked(song,position);
        }
    }
}