package com.damon.videocompress.activitys;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.damon.videocompress.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.initialization.InitializationStatus;
//import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class VideoCompartir extends AppCompatActivity {

    private  String uri ;

    private TextView textView;
    private ImageView shared;
    PlayListener playListener;
//    private InterstitialAd mInterstitialAd;

    private ProgressBar progressVideo;
    private PlayerView playerView;

    private SimpleExoPlayer exoPlayer;
    private boolean play;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_compartir);
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {}
//        });
//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstal));
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        playListener = new PlayListener();
        progressVideo = findViewById(R.id.progress_video);
        Intent intent = getIntent();
        if (intent !=null){
            playWhenReady = intent.getBooleanExtra("playWhenReady",true);
            currentWindow = intent.getIntExtra("currentWindow",0);
            playbackPosition = intent.getLongExtra("playbackPosition",0);
        }
        uri  = getIntent().getExtras().getString("id");

        playerView = findViewById(R.id.playerViewActivity);
        shared = findViewById(R.id.sharedVideo);
        textView = findViewById(R.id.directory);
        MediaController controller = new MediaController(this);

        try {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(getApplicationContext()).build();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer = (SimpleExoPlayer) ExoPlayerFactory.newSimpleInstance(getApplicationContext());
            playerView.setPlayer(exoPlayer);
            ExtractorMediaSource audioSource = new ExtractorMediaSource(
                    Uri.parse(uri),
                    new DefaultDataSourceFactory(getApplicationContext(),"MyExoplayer"),
                    new DefaultExtractorsFactory(),
                    null,
                    null
            );
            exoPlayer.prepare(audioSource);
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.setPlayWhenReady(playWhenReady);
            exoPlayer.seekTo(currentWindow, playbackPosition);
            exoPlayer.addListener(playListener);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            textView.setText(uri);
        }catch (Exception e){
            e.printStackTrace();
        }








        shared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedVideo(uri);
            }
        });


//        mInterstitialAd.setAdListener(new AdListener(){
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                if (mInterstitialAd.isLoaded()) {
//                    mInterstitialAd.show();
//                } else {
//                    Log.d("TAG", "The interstitial wasn't loaded yet.");
//                }
//            }
//        });

    }



    private void sharedVideo(String uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
        startActivity(Intent.createChooser(intent,"Compartir via"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (exoPlayer != null){
            exoPlayer.release();
            exoPlayer = null;
        }
        finish();

    }
    private class PlayListener implements Player.EventListener{
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
}