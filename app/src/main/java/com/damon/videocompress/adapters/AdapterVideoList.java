package com.damon.videocompress.adapters;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.damon.videocompress.activitys.AudioListaActivity;
import com.damon.videocompress.models.ModelVideo;
import com.damon.videocompress.R;
import com.damon.videocompress.utils.Util;
import com.damon.videocompress.activitys.CompressVideoActivity;
import com.damon.videocompress.activitys.VideoCompartir;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.io.File;
import java.util.ArrayList;

public class AdapterVideoList extends RecyclerView.Adapter<AdapterVideoList.MyViewHolder> {

    ArrayList<ModelVideo> videosList = new ArrayList<ModelVideo>();
    Context context;
    private Dialog dialog;
    MediaController controller;

    public AdapterVideoList(ArrayList<ModelVideo> videosList, Context context) {
        this.videosList = videosList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false);
        dialog = new Dialog(context);
        controller = new MediaController(context);
        int heigh = parent.getMeasuredHeight() / 2;
        itemView.setMinimumHeight(heigh);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final ModelVideo item = videosList.get(position);
        holder.tv_title.setText(item.getTitle());
        int valor = Integer.parseInt(item.getDuration());
        int mg = valor / (1024 * 1024);
        int kb = valor / 1024;
        if (kb > 1000) {
            holder.tv_duration.setText(mg + " MB");
        } else {
            holder.tv_duration.setText(kb + " KB");
        }
//        Glide.with(context).load(item.getData())
//                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true))
//                .into(holder.imgView_thumbnail);

        Glide.with(context).load(item.getData()).into(holder.imgView_thumbnail);

        holder.tv_duracion_video.setVisibility(View.VISIBLE);
        holder.tv_duracion_video.setText(item.getDuracion_video());

        holder.tv_display.setVisibility(View.VISIBLE);
        holder.tv_display.setText(item.getResolucion());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), CompressVideoActivity.class);
//                intent.putExtra("videoId", item.getId());
//                v.getContext().startActivity(intent);
//                System.out.println(item.getId());
                try {
                    mostarDialogo(item.getId(), item.getTitle(), position, item.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    SimpleExoPlayer exoPlayer;
    PlayerView playerView;
    ProgressBar progressVideo;
    private PlaybackStateListener playbackStateListener;

    private void mostarDialogo(final long id, String title, final int position, final Uri data)throws Exception{
        final Uri videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
        final String  s = Util.getFilePath(context,videoUri);
        dialog.setContentView(R.layout.dialog_video);
        playerView = dialog.findViewById(R.id.video_dialog);

        TextView titulo = dialog.findViewById(R.id.titulo_dialog);
        LinearLayout shared = dialog.findViewById(R.id.linear_shared);
        final LinearLayout compres = dialog.findViewById(R.id.linear_compress);
        final LinearLayout delete = dialog.findViewById(R.id.linear_delete);
        final ImageView expanVideo = dialog.findViewById(R.id.expan_video);
        final ImageView close_vide = dialog.findViewById(R.id.close_vide);
        progressVideo = dialog.findViewById(R.id.progress_video);

        playbackStateListener = new PlaybackStateListener();
        titulo.setText(title);

        try{
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer = (SimpleExoPlayer) ExoPlayerFactory.newSimpleInstance(context);
            playerView.setPlayer(exoPlayer);
            ExtractorMediaSource audioSource = new ExtractorMediaSource(
                    data,
                    new DefaultDataSourceFactory(context.getApplicationContext(),"MyExoplayer"),
                    new DefaultExtractorsFactory(),
                    null,
                    null
            );
            exoPlayer.prepare(audioSource);

            exoPlayer.addListener(playbackStateListener);
            exoPlayer.setPlayWhenReady(true);
//            exoPlayer.setVolume(0);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        }catch (Exception e){
            e.printStackTrace();
        }





        shared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pararVideo();
                sharedVide(s);
            }
        });

        compres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CompressVideoActivity.class);
                intent.putExtra("videoId",id);
                intent.putExtra("caso","no");
                pararVideo();
                context.startActivity(intent);
            }
        });

        expanVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    videoView.stopPlayback();
                    sendVideoView(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                deleteVideo(videoUri,position);
//                dialog.dismiss();
                Intent intent = new Intent(context, AudioListaActivity.class);
                pararVideo();
                context.startActivity(intent);
            }
        });


        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                 pararVideo();
                dialog.dismiss();
            }
        });

        close_vide.setOnClickListener(v -> {
           pararVideo();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteVideoDos(long id, int position, Uri data) {

      ContentResolver resolver = context.getContentResolver();
      resolver.delete(data,null,null);
        Toast.makeText(context, "gola", Toast.LENGTH_SHORT).show();
    }

    private void sendVideoView(long path)throws  Exception{
        final Uri videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, path);
        final String  s = Util.getFilePath(context,videoUri);
        Intent intent  =new Intent(context, VideoCompartir.class);
        boolean playWhenReady = true;
        int currentWindow = 0;
        long playbackPosition = 0;
        playWhenReady = exoPlayer.getPlayWhenReady();
        playbackPosition = exoPlayer.getCurrentPosition();
        currentWindow = exoPlayer.getCurrentWindowIndex();
        intent.putExtra("playWhenReady",playWhenReady);
        intent.putExtra("playbackPosition",playbackPosition);
        intent.putExtra("currentWindow",currentWindow);
        intent.putExtra("id",s);
        pararVideo();
        context.startActivity(intent);
    }
    public void pararVideo(){
        if (exoPlayer != null){
            if (exoPlayer.isPlaying()){
                exoPlayer.setPlayWhenReady(!exoPlayer.getPlayWhenReady());
            }
        }

    }
    private void deleteVideo(Uri s, int position) {
        boolean file = new File(s.getLastPathSegment()).getAbsoluteFile().delete();
        if (file){
            videosList.remove(position);
            notifyDataSetChanged();
        }

    }

    private void sharedVide(String s) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(s));
        context.startActivity(Intent.createChooser(intent,"Compartir via"));
    }


    private class  PlaybackStateListener implements  Player.EventListener{
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    progressVideo.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    progressVideo.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    progressVideo.setVisibility(View.GONE);

                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    progressVideo.setVisibility(View.GONE);
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
        }
    }
    @Override
    public int getItemCount() {
        return videosList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgView_thumbnail;
        TextView tv_title, tv_duration,tv_duracion_video,tv_display;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_duration = itemView.findViewById(R.id.tv_duration);
            imgView_thumbnail = itemView.findViewById(R.id.imageView_thumbnail);
            tv_duracion_video = itemView.findViewById(R.id.duracion_video);
            tv_display = itemView.findViewById(R.id.display);
        }
    }



}
