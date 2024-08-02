package com.example.testrun.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.testrun.R;
import com.example.testrun.databinding.ItemSongBinding;
import com.example.testrun.listener.PlayOrPauseListener;
import com.example.testrun.listener.SongListener;
import com.example.testrun.model.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songs;
    private LayoutInflater layoutInflater;
    private SongListener songListener;
    private PlayOrPauseListener playOrPauseListener;


    private int currentPlayingPosition = -1;
    private boolean isPlaying = false;

    public SongAdapter(List<Song> songs, SongListener songListener,PlayOrPauseListener playOrPauseListener) {
        this.songs = songs;
        this.songListener = songListener;
        this.playOrPauseListener = playOrPauseListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        ItemSongBinding itemSongBinding = ItemSongBinding.inflate(layoutInflater, parent, false);
        return new SongViewHolder(itemSongBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.bindData(songs.get(position), position);

    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position, @NonNull List<Object> payloads) {
        Log.e("PNLogBe", "COME HERE " + payloads);
        Log.e("PNLogBe", "COME HERE " + position);
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                Boolean isPayling = (Boolean) payload;
                Log.e("PNLog", "COME HERE " + position);
                if (isPayling) {
                    holder.changeState(true);
                    Log.e("PNLog", "isPayling: true " + position);
                }else {
                    holder.changeState(false);
                    Log.e("PNLog", "isPayling: false " + position);
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
            Log.e("PNLogElse", "COME ");
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setIsPlaying(boolean playing, int position) {
        currentPlayingPosition = position;
//        this.isPlaying = playing;
        if (position != -1) {
            this.isPlaying = playing;
            Log.d("SPLNotifyItemChanged","before noti: "+position+"");
            notifyItemChanged(currentPlayingPosition, playing);
            Log.d("SPLNotifyItemChanged","after noti: "+position+"");
        }
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {
        ItemSongBinding itemSongBinding;

        public SongViewHolder(ItemSongBinding itemSongBinding) {
            super(itemSongBinding.getRoot());
            this.itemSongBinding = itemSongBinding;
        }

        private void bindData(Song song, int position) {
            itemSongBinding.nameSong.setText(song.getTitle());
            itemSongBinding.nameDepeche.setText(song.getArtist());

            Glide.with(itemSongBinding.imageSong.getContext())
                    .load(song.getAlbumArtUri())
                    .placeholder(R.drawable.bg_dark)
                    .into(itemSongBinding.imageSong);
            itemSongBinding.imagePlay.setImageResource((currentPlayingPosition == position && isPlaying) ? R.drawable.icon_pause : R.drawable.logo_play);
            itemSongBinding.layoutNotContainBtnPlay.setOnClickListener(v -> songListener.onSongClicked(song, position));
            itemSongBinding.imagePlay.setOnClickListener(v -> playOrPauseListener.playOrPause(song,position) );
        }

        private void changeState(boolean play) {
            itemSongBinding.imagePlay.setImageResource(play ? R.drawable.icon_pause : R.drawable.logo_play);
        }
    }
}
