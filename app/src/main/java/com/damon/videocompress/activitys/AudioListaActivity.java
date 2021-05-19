package com.damon.videocompress.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.damon.videocompress.R;
import com.damon.videocompress.adapters.AudioListAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AudioListaActivity extends AppCompatActivity implements AudioListAdapter.onItemListClick {


    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private RecyclerView audioList;
    private File[] allFile;
    private ArrayList<File> arrayList = new ArrayList<>();
    private AudioListAdapter audioListAdapter;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlay  =false;
    private File filePlay;

    //ui elemens
    private ImageButton playBtn,nextBtn,previewBtn;
    private TextView playerHeader,playerFilename;
    private SeekBar playerSkeebar;
    private Handler seekBarHandler;
    private Runnable updateSeekbar;
    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_lista);

        playerSheet = findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        audioList = findViewById(R.id.audio_list_view);

        playBtn = findViewById(R.id.player_play_btn);
        nextBtn = findViewById(R.id.btn_next);
        previewBtn = findViewById(R.id.btn_preview);
        playerHeader = findViewById(R.id.player_header_title);
        playerFilename = findViewById(R.id.player_file_name);

        playerSkeebar = findViewById(R.id.player_seekbar);

//        String path =  getExternalFilesDir("/").getAbsolutePath();
        File directory;

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            directory = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC)+File.separator+getString(R.string.app_name));
            // folder = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());

        }else {
            directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+File.separator+getString(R.string.app_name));
            // folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        }


        File[] files = directory.listFiles();

        if (files !=null){
            for (File file: files){
                arrayList.add(file);
            }
        }



        audioListAdapter = new AudioListAdapter(arrayList,this,this);

        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(AudioListaActivity.this,LinearLayoutManager.VERTICAL,false));
        audioList.setAdapter(audioListAdapter);


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState  == BottomSheetBehavior.STATE_HIDDEN){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // we cant do anything here for this app
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlay){
                    try {
                        pauseAudio();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    try {
                        if (arrayList.get(position) != null){
                            resumeAudio();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                // play();
            }
        });

        playerSkeebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (filePlay!=null){
                    pauseAudio();
                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (filePlay!=null){
                    mediaPlayer.seekTo(progress);
                    resumeAudio();
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < arrayList.size()-1){
                    position++;
                }else {
                    position =0;
                }
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                playAudio(arrayList.get(position),position);
            }
        });

        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position <=0){
                    position = arrayList.size()-1;
                }else {
                    position--;
                }
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                playAudio(arrayList.get(position),position);
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
        this.position = position;
        filePlay = file;
        //initPlayer(position);
        if (isPlay){
            stopAudio();
            playAudio(filePlay,position);

        }else {
            playAudio(filePlay,position);
        }
    }

    private void pauseAudio(){
        mediaPlayer.pause();
        try {
            // playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn,null));
            playBtn.setImageResource(R.drawable.player_play_btn);
        }catch (Exception e){
            e.printStackTrace();
        }
        isPlay = false;
        seekBarHandler.removeCallbacks(updateSeekbar);
    }

    private void resumeAudio(){
        mediaPlayer.start();
        try {
            // playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn,null));
            playBtn.setImageResource(R.drawable.player_pause_btn);
        }catch (Exception e){
            e.printStackTrace();
        }
        isPlay = true;

        updateRunnable();
        seekBarHandler.postDelayed(updateSeekbar,0);
    }

    private void stopAudio() {
        //stop audio
        try {
            //  playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn,null));
            playBtn.setImageResource(R.drawable.player_play_btn);
        }catch (Exception e){
            e.printStackTrace();
        }
        playerHeader.setText("Detenido");
        isPlay = false;
        mediaPlayer.stop();
        seekBarHandler.removeCallbacks(updateSeekbar);
    }

    private void playAudio(File filePlay,int position) {

        Uri songUri = Uri.parse(arrayList.get(position).toString());

        mediaPlayer = new MediaPlayer();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        try {
            mediaPlayer.setDataSource(songUri.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn,null));
        playBtn.setImageResource(R.drawable.player_pause_btn);
        playerFilename.setText(filePlay.getName());
        playerHeader.setText("Reproduciendo");
        //play the audio
        isPlay = true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                pause();
                playerHeader.setText("Finalizado");
            }
        });
        playerSkeebar.setMax(mediaPlayer.getDuration());

        seekBarHandler = new Handler();
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekbar,0);
    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                playerSkeebar.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this,500);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isPlay){
            stopAudio();
        }
    }

    private void initPlayer(final  int position){
        if (mediaPlayer !=null && mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }
        Uri songUri = Uri.parse(arrayList.get(position).toString());

        mediaPlayer = MediaPlayer.create(getApplicationContext(),songUri);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                playerSkeebar.setMax(mp.getDuration());
                mediaPlayer.start();
                try {
                    // playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn,null));
                    playBtn.setImageResource(R.drawable.player_pause_btn);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int curSongPosition = position;
                if (curSongPosition < arrayList.size()-1){
                    curSongPosition++;
                    initPlayer(position);
                }else{
                    curSongPosition =0;
                    initPlayer(curSongPosition);
                }
            }
        });

        playerSkeebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mediaPlayer.seekTo(progress);
                    playerSkeebar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer !=null){
                    try {
                        if (mediaPlayer.isPlaying()){
                            Message msg = new Message();
                            msg.what = mediaPlayer.getCurrentPosition();
                            handler.sendMessage(msg);
                            Thread.sleep(1000);
                        }
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int current_position = msg.what;
            playerSkeebar.setProgress(current_position);
            String cTime = createTimeLabel(current_position);

        }
    };

    private void play(){
        if (mediaPlayer!=null && !mediaPlayer.isPlaying()){
            mediaPlayer.start();
            try {
                playBtn.setImageResource(R.drawable.player_pause_btn);
                // playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn,null));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private void pause(){
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            try {
                playBtn.setImageResource(R.drawable.player_play_btn);
                isPlay = false;
                // playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn,null));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public  String createTimeLabel(int duration){
        String timeLabel = "";
        int min = duration /1000/ 60;
        int sec = duration /1000 %60;
        timeLabel += min + ":" ;
        if (sec < 10) timeLabel +="0";
        timeLabel += sec;
        return  timeLabel;
    }

    @Override
    public void onBackPressed() {

        if (mediaPlayer !=null){
            mediaPlayer.stop();
            finish();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(getApplicationContext()).clearMemory();
//        Glide.get(getApplicationContext()).clearDiskCache();
        clearApplicationData();

    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("EEEEEERRRRRROOOOOOORRRR", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            int i = 0;
            while (i < children.length) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
                i++;
            }
        }

        assert dir != null;
        return dir.delete();
    }
}