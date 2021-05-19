package com.damon.videocompress.activitys;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.damon.compressvideo.VideoCompress;
import com.damon.videocompress.R;
import com.damon.videocompress.utils.Util;
import com.damon.videocompress.utils.videoeditor.FillMode;
import com.damon.videocompress.utils.videoeditor.Mp4Composer;
import com.damon.videocompress.utils.videoeditor.VideoUtil;
import com.damon.videocompress.view.cutvideo.NormalProgressDialog;
import com.damon.videocompress.view.cutvideo.RangeSeekBar2;
import com.damon.videocompress.view.cutvideo.VideoEditor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.damon.videocompress.activitys.CompressVideoActivity.getSystemLocale;
import static com.damon.videocompress.activitys.CompressVideoActivity.getSystemLocaleLegacy;

public class NewCutVideo extends BaseActivityDos {

    private String mVideoPath;
    private VideoView mVideoView;
    private int duration;
//    private RangeSeekBar rangeSeekBar;
    private Runnable r;
    private TextView tvLeft, tvRight;
    private TextView cortatr;
    private static final String TAG = CortarVideoActivity.class.getSimpleName();
    private ProgressBar progressBar_cut;
    private Mp4Composer mMp4Composer;
    private Dialog dialogCalidad;
    private RadioGroup radioGroup;
    private Button comprimir_btn;
    private byte select_calidad = 1;
    private Dialog dialog;

    private TextView tv_indicator, tv_progress,mTvShootTip;

    private ProgressBar pb_compress;

    private VideoEditor videoEditor;
    private Button btn_save;

    private String path;
    private ImageView btn_back;
    private boolean isSeeking;
    private ImageView play_btn;
    private boolean isPlaying;


    private Timer timer;
    private long leftProgress, rightProgress; //Recorta la posición de tiempo del área a la izquierda del vídeo., La posición de tiempo correcta.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cut_video);


        mVideoPath = getIntent().getStringExtra("videoPath");
        mVideoView = findViewById(R.id.video_view);
//        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        cortatr = findViewById(R.id.toolbar_menu_title);

        dialog = new Dialog(this);
        dialogCalidad = new Dialog(this);
        progressBar_cut = findViewById(R.id.progrss_cut);
        btn_back = findViewById(R.id.tolbar_dos);
        progressBar_cut.setProgress(0);
        progressBar_cut.setMax(100);

        dialog.setContentView(R.layout.dialogo_video_progress);
        tv_indicator = (TextView) dialog.findViewById(R.id.tv_indicator);
        pb_compress = (ProgressBar) dialog.findViewById(R.id.pb_compress);
        tv_progress = (TextView) dialog.findViewById(R.id.tv_progress);
        pb_compress.setMax(100);

        mTvShootTip = findViewById(R.id.video_shoot_tip);
        tvLeft = (TextView) findViewById(R.id.tvLeft);
        tvRight = (TextView) findViewById(R.id.tvRight);
        play_btn = findViewById(R.id.btn_play_cut);

        videoEditor = (VideoEditor) findViewById(R.id.videoeditor);
        videoEditor.setFilePath(mVideoPath);
        videoEditor.setSeekListener(mOnRangeSeekBarChangeListener);

        try {
            mVideoView.setVideoURI(Uri.parse(mVideoPath));
        }catch (Exception e){
            e.printStackTrace();
        }

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //设置MediaPlayer的OnSeekComplete监听
                Log.e(TAG, "prepared");
                tvLeft.setText("00:00:00");
                tvRight.setText(getTime(mp.getDuration() / 1000));
                mTvShootTip.setText(String.format("Tiempo %d s", mp.getDuration() / 1000));

                leftProgress = 0;
                rightProgress = mp.getDuration()/1000;


                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(!isSeeking){
//                           restartVideo();
                        }
                    }
                }, 0L, 1000L);
                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        if (!isSeeking) {
                            videoStart();
                        }
                    }
                });
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlaying = false;
                play_btn.setImageResource(R.drawable.player_play_btn);
            }
        });

        //first
//        videoStart();

        cortatr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trimmerVideo();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying){
                    try {
                        videoPause();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else {
                    try {
                        videoStart();
//                        videoResum();
//                        restartVideo();

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private Handler handler = new Handler();
    private Runnable run = new Runnable() {

        @Override
        public void run() {
            if (!isSeeking){
                restartVideo();
                handler.postDelayed(run, 1000);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_cut_video;
    }

    private void trimmerVideo() {
        NormalProgressDialog
                .showLoading(this, getResources().getString(R.string.in_process), false);
        progressBar_cut.setVisibility(View.VISIBLE);
        mVideoView.pause();
        VideoUtil
                .cutVideo(mVideoPath, VideoUtil.getTrimmedVideoPath(this, "small_video/trimmedVideo",
                        "trimmedVideo_"), leftProgress / 1000,
                        rightProgress / 1000)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        subscribe(d);
                    }

                    @Override
                    public void onNext(String outputPath) {
                        Log.e(TAG, "cutVideo---onSuccess");
                        try {
                            startMediaCodec(outputPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.e(TAG, "cutVideo---onError:" + e.toString());
                        NormalProgressDialog.stopLoading();
                        Toast.makeText(NewCutVideo.this, "Error en el recorte de vídeo.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private final RangeSeekBar2.OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener = new RangeSeekBar2.OnRangeSeekBarChangeListener() {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar2 bar, long minValue, long maxValue, int action, boolean isMin, int pressedThumb) {
            leftProgress = minValue ;
            rightProgress = maxValue ;

            tvLeft.setText(getTime((int) (leftProgress/1000)));

            tvRight.setText(getTime((int) (rightProgress/1000)));

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isSeeking = false;
                    videoPause();
                    break;
                case MotionEvent.ACTION_MOVE:
                    isSeeking = true;
                    mVideoView.seekTo((int) (pressedThumb == RangeSeekBar2.MIN ?
                            leftProgress : rightProgress));
                    break;
                case MotionEvent.ACTION_UP:
                    isSeeking = false;
                    //从minValue开始播
                    mVideoView.seekTo((int) leftProgress);
//                    videoStart();
                    mTvShootTip
                            .setText(String.format("Tiempo %d s", (rightProgress - leftProgress) / 1000));
                    break;
                default:
                    break;
            }
        }
    };



    private void videoStart() {
        mVideoView.start();
        videoEditor.startVideo();
        isPlaying = true;
        play_btn.setImageResource(R.drawable.player_pause_btn);
        handler.removeCallbacks(run);
        handler.post(run);
    }

    private void restartVideo() {
        isPlaying = true;
        long currentPosition = mVideoView.getCurrentPosition();
        if (currentPosition >= (videoEditor.getRightProgress()) || (!mVideoView.isPlaying() && !isSeeking)) {
            NewCutVideo.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVideoView.seekTo((int) videoEditor.getLeftProgress());
                    videoEditor.restartVideo();
                }
            });
        }

    }

    private void videoPause() {
        isSeeking = false;
        isPlaying = false;
        play_btn.setImageResource(R.drawable.player_play_btn);
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        videoEditor.pauseVideo();
        handler.removeCallbacks(run);

    }


    private void videoResum(){
        isSeeking = true;
        if (mVideoView !=null) {
            System.out.println("resumen----");
            mVideoView.start();
            videoEditor.startVideo();
            play_btn.setImageResource(R.drawable.player_pause_btn);
//           videoView.seekTo((int) leftProgress);
            isPlaying = true;
            handler.removeCallbacks(run);
            handler.post(run);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.seekTo((int) videoEditor.getLeftProgress());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null && mVideoView.isPlaying()) {
            videoPause();
        }
    }

    private void startMediaCodec(String srcPath) {
        final String outputPath = VideoUtil.getTrimmedVideoPath(this, "small_video/trimmedVideo",
                "filterVideo_");

        mMp4Composer = new Mp4Composer(srcPath, outputPath)
                // .rotation(Rotation.ROTATION_270)
                //.size(720, 1280)
                .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                // .filter(MagicFilterFactory.getFilter())
                .mute(false)
                .flipHorizontal(false)
                .flipVertical(false)
                .listener(new Mp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {
                        Log.d(TAG, "filterVideo---onProgress: " + (int) (progress * 100));
                        runOnUiThread(() -> {
                            //show progress
                            progressBar_cut.setProgress((int) (progress * 100));
                        });
                    }

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "filterVideo---onCompleted");
                        runOnUiThread(() -> {
                            // compressVideo(outputPath);
                            try {
                                NormalProgressDialog.stopLoading();
                                mostrarDialogoCalidad(outputPath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("finalizado");
                        });
                    }

                    @Override
                    public void onCanceled() {
                        NormalProgressDialog.stopLoading();
                    }

                    @Override
                    public void onFailed(Exception exception) {
                        Log.e(TAG, "filterVideo---onFailed()");
                        NormalProgressDialog.stopLoading();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(NewCutVideo.this, "Error en el procesamiento de vídeo.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                })
                .start();
    }


    private void mostrarDialogoCalidad(String outputPath) {
        dialogCalidad.setContentView(R.layout.dialog_custom_compress);
        dialogCalidad.setCancelable(false);

        radioGroup = dialogCalidad.findViewById(R.id.grupo_radio);
        comprimir_btn = dialogCalidad.findViewById(R.id.coprimir_btn);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rd_baja:
                        select_calidad = 1;
                        break;
                    case  R.id.rd_media:
                        select_calidad = 2;
                        break;
                    case  R.id.rd_alta:
                        select_calidad = 3;
                        break;
                    default:
                        select_calidad =1;
                        break;
                }
            }
        });

        comprimir_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (select_calidad){
                    case 1:
                        try {
                            compresionBaja(outputPath);
                            dialogCalidad.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        try {
                            compresionMeida(outputPath);
                            dialogCalidad.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        try {
                            compresionAlta(outputPath);
                            dialogCalidad.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        try {
                            compresionBaja(outputPath);
                            dialogCalidad.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });





        dialogCalidad.show();

    }

    private void compresionAlta(final String tv_input) throws Exception{
        dialog.setCancelable(false);
        dialog.show();
        String file_name = UUID.randomUUID() + ".mp4";
        String destPath  ;
        File folder;
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            folder = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+File.separator+getString(R.string.app_name));
            // folder = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());

        }else {
            folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+File.separator+getString(R.string.app_name));
            // folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        }


        if (!folder.exists()) {
            folder.mkdirs();
        }
        File image = new File(folder+File.separator+file_name);
        Uri imageUri = Uri.fromFile(image);
        destPath = String.valueOf(imageUri.getPath());
        VideoCompress.compressVideoHigh(tv_input, destPath, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                tv_indicator.setText("Comprimiendo..." + "\n"
                        + "Inicio alas: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.VISIBLE);
                //startTime = System.currentTimeMillis();
                Util.writeFile(NewCutVideo.this, "Comienzo at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
                dialog.dismiss();
                Intent intent = new Intent(NewCutVideo.this,VideoCompartir.class);
                intent.putExtra("id",destPath);
                startActivity(intent);
            }

            @Override
            public void onFail() {
                dialog.dismiss();
            }

            @Override
            public void onProgress(float percent) {
                int p = (int) percent;
                pb_compress.setProgress(p);
                tv_progress.setText(String.valueOf(p) + "%");
            }
        });
    }

    private void compresionMeida(final String  tv_input) throws Exception{
        dialog.setCancelable(false);
        dialog.show();
        String file_name = UUID.randomUUID() + ".mp4";
        String destPath  ;
        File folder;
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            folder = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+File.separator+getString(R.string.app_name));
            // folder = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());

        }else {
            folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+File.separator+getString(R.string.app_name));
            // folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        }


        if (!folder.exists()) {
            folder.mkdirs();
        }
        File image = new File(folder+File.separator+file_name);
        Uri imageUri = Uri.fromFile(image);
        destPath = String.valueOf(imageUri.getPath());
        VideoCompress.compressVideoMedium(tv_input, destPath, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                tv_indicator.setText("Comprimiendo..." + "\n"
                        + "Inicio alas: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.VISIBLE);
                //startTime = System.currentTimeMillis();
                Util.writeFile(NewCutVideo.this, "Comienzo at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
                dialog.dismiss();
                Intent intent = new Intent(NewCutVideo.this,VideoCompartir.class);
                intent.putExtra("id",destPath);
                startActivity(intent);
            }

            @Override
            public void onFail() {
                dialog.dismiss();
            }

            @Override
            public void onProgress(float percent) {
                int p = (int) percent;
                pb_compress.setProgress(p);
                tv_progress.setText(String.valueOf(p) + "%");
            }
        });
    }

    private void compresionBaja(final String tv_input) throws  Exception{
        dialog.setCancelable(false);
        dialog.show();
        String file_name = UUID.randomUUID() + ".mp4";
        String destPath  ;
        File folder;
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            folder = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+File.separator+getString(R.string.app_name));
            // folder = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());

        }else {
            folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+File.separator+getString(R.string.app_name));
            // folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        }


        if (!folder.exists()) {
            folder.mkdirs();
        }
        File image = new File(folder+File.separator+file_name);
        Uri imageUri = Uri.fromFile(image);
        destPath = String.valueOf(imageUri.getPath());
        VideoCompress.compressVideoLow(tv_input, destPath, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                tv_indicator.setText("Comprimiendo..." + "\n"
                        + "Inicio alas: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.VISIBLE);
                //startTime = System.currentTimeMillis();
                Util.writeFile(NewCutVideo.this, "Comienzo at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
                dialog.dismiss();
                Intent intent = new Intent(NewCutVideo.this,VideoCompartir.class);
                intent.putExtra("id",destPath);
                startActivity(intent);
            }

            @Override
            public void onFail() {
                dialog.dismiss();
            }

            @Override
            public void onProgress(float percent) {
                int p = (int) percent;
                pb_compress.setProgress(p);
                tv_progress.setText(String.valueOf(p) + "%");
            }
        });
    }

    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }

    private Locale getLocale() {
        Configuration config = getResources().getConfiguration();
        Locale sysLocale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sysLocale = getSystemLocale(config);
        } else {
            sysLocale = getSystemLocaleLegacy(config);
        }

        return sysLocale;
    }


    @Override
    protected void onDestroy() {
        if(videoEditor != null) {
            try {
                videoEditor.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }

        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        //Retire el vídeo recortado, filtre el vídeo.
        String trimmedDirPath = VideoUtil.getTrimmedVideoDir(this, "small_video/trimmedVideo");
        if (!TextUtils.isEmpty(trimmedDirPath)) {
            VideoUtil.deleteFile(new File(trimmedDirPath));
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}