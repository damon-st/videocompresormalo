package com.damon.videocompress.activitys;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.compressvideo.VideoCompress;
import com.damon.videocompress.R;
import com.damon.videocompress.adapters.TrimVideoAdapter;
import com.damon.videocompress.models.FilterModel;
import com.damon.videocompress.utils.Util;
import com.damon.videocompress.utils.videoeditor.ExtractFrameWorkThread;
import com.damon.videocompress.utils.videoeditor.ExtractVideoInfoUtil;
import com.damon.videocompress.utils.videoeditor.FillMode;
import com.damon.videocompress.utils.videoeditor.Mp4Composer;
import com.damon.videocompress.utils.videoeditor.VideoEditInfo;
import com.damon.videocompress.utils.videoeditor.VideoUtil;
import com.damon.videocompress.utils.videoeffect.ConfigUtils;
import com.damon.videocompress.utils.videoeffect.MagicFilterType;
import com.damon.videocompress.utils.videoeffect.UIUtils;
import com.damon.videocompress.view.cutvideo.NormalProgressDialog;
import com.damon.videocompress.view.cutvideo.RangeSeekBar;
import com.damon.videocompress.view.cutvideo.VideoThumbSpacingItemDecoration;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.damon.videocompress.activitys.CompressVideoActivity.getSystemLocale;
import static com.damon.videocompress.activitys.CompressVideoActivity.getSystemLocaleLegacy;

public class CortarVideoActivity extends BaseActivityDos {

    //    @BindView(R.id.glsurfaceview)
//    GlVideoView mSurfaceView;
    @BindView(R.id.video_shoot_tip)
    TextView mTvShootTip;
    @BindView(R.id.video_thumb_listview)
    RecyclerView mRecyclerView;
    @BindView(R.id.positionIcon)
    ImageView mIvPosition;
    @BindView(R.id.id_seekBarLayout)
    LinearLayout seekBarLayout;
    @BindView(R.id.layout_surface_view)
    RelativeLayout mRlVideo;
    @BindView(R.id.view_trim_indicator)
    View mViewTrimIndicator;
    @BindView(R.id.view_effect_indicator)
    View mViewEffectIndicator;
    @BindView(R.id.ll_trim_container)
    LinearLayout mLlTrimContainer;
    @BindView(R.id.hsv_effect)
    HorizontalScrollView mHsvEffect;
    @BindView(R.id.ll_effect_container)
    LinearLayout mLlEffectContainer;
    private RangeSeekBar seekBar;



    private int aumento = 60;
    private static final String TAG = CortarVideoActivity.class.getSimpleName();
    private static final long MIN_CUT_DURATION = 3 * 1000L;// Tiempo mínimo de clip.3s
    private long MAX_CUT_DURATION = aumento * 1000L;//Cuánto tiempo se corta el vídeo como máximo.
    private static final int MAX_COUNT_RANGE = 10;//seekBarCuántas fotos hay en la zona.
    private static final int MARGIN = UIUtils.dp2Px(56); //El espaciado entre la izquierda y la derecha.
    private ExtractVideoInfoUtil mExtractVideoInfoUtil;
    private int mMaxWidth; //La anchura máxima del área que se puede recortar.
    private long duration; //La duración total del vídeo.
    private TrimVideoAdapter videoEditAdapter;
    private float averageMsPx;//Px por milisegundo.
    private float averagePxMs;//px ocupado ms milisegundos.
    private String OutPutFileDirPath;
    private ExtractFrameWorkThread mExtractFrameWorkThread;
    private long leftProgress, rightProgress; //Recorta la posición de tiempo del área a la izquierda del vídeo., La posición de tiempo correcta.
    private long scrollPos = 0;
    private int mScaledTouchSlop;
    private int lastScrollX;
    private boolean isSeeking;
    private String mVideoPath;
    private int mOriginalWidth; //El ancho original del vídeo.
    private int mOriginalHeight; //La altura original del vídeo.
    private List<FilterModel> mVideoEffects = new ArrayList<>(); //视频滤镜效果
    private MagicFilterType[] mMagicFilterTypes;
    private ValueAnimator mEffectAnimator;
    private SurfaceTexture mSurfaceTexture;
    private MediaPlayer mMediaPlayer;
    private Mp4Composer mMp4Composer;
    private FrameLayout frameLayout;
    private TextView cortatr;
    private TextView tv_input, tv_output, tv_indicator, tv_progress;
    private Dialog dialog;
    private ProgressBar pb_compress;
    private ImageView toolbar;
    private VideoView videoView;
    private ProgressBar progressBar_cut;


    private Button comprimir_btn;
    private RadioGroup radioGroup;
    private Dialog dialogCalidad;
    private ImageView play_btn;
    private boolean isPlaying;


    public static void startActivity(Context context, String videoPath) {
        Intent intent = new Intent(context, CortarVideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_cortar_video;
    }

    @Override
    protected void init() {
        dialog = new Dialog(this);
        mVideoPath = getIntent().getStringExtra("videoPath");

        mExtractVideoInfoUtil = new ExtractVideoInfoUtil(mVideoPath);
        mMaxWidth = UIUtils.getScreenWidth() - MARGIN * 2;
        mScaledTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) {
                e.onNext(mExtractVideoInfoUtil.getVideoLength());
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        subscribe(d);
                    }

                    @Override
                    public void onNext(String s) {
                        duration = Long.valueOf(mExtractVideoInfoUtil.getVideoLength());
                        //矫正获取到的视频时长不是整数问题
                        float tempDuration = duration / 1000f;
                        duration = new BigDecimal(tempDuration).setScale(0, BigDecimal.ROUND_HALF_UP).intValue() * 1000;
                        Log.e(TAG, "La duración total del vídeo.：" + duration);
                        initEditVideo();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

//    @Override
//    protected void initToolbar(ToolbarHelper toolbarHelper) {
//        super.initToolbar(toolbarHelper);
//        toolbarHelper.setTitle("CortarVideo");
//        toolbarHelper.setMenuTitle("Cortar",v -> {
//            trimmerVideo();
//        });
//    }

    @Override
    protected void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        play_btn = findViewById(R.id.btn_play_cut);
        videoView = findViewById(R.id.video_view);
        dialogCalidad = new Dialog(this);
        progressBar_cut = findViewById(R.id.progrss_cut);
        progressBar_cut.setProgress(0);
        progressBar_cut.setMax(100);
        toolbar = findViewById(R.id.tolbar_dos);
        dialog.setContentView(R.layout.dialogo_video_progress);
        tv_indicator = (TextView) dialog.findViewById(R.id.tv_indicator);
        pb_compress = (ProgressBar) dialog.findViewById(R.id.pb_compress);
        tv_progress = (TextView) dialog.findViewById(R.id.tv_progress);
        pb_compress.setMax(100);

        cortatr = findViewById(R.id.toolbar_menu_title);
        mRecyclerView = findViewById(R.id.video_thumb_listview);
//        mSurfaceView  = findViewById(R.id.glsurfaceview);
        mTvShootTip = findViewById(R.id.video_shoot_tip);
        mIvPosition = findViewById(R.id.positionIcon);
        seekBarLayout = findViewById(R.id.id_seekBarLayout);
        mRlVideo = findViewById(R.id.layout_surface_view);
        mViewTrimIndicator = findViewById(R.id.view_trim_indicator);
        mViewEffectIndicator = findViewById(R.id.view_effect_indicator);
        mLlTrimContainer = findViewById(R.id.ll_trim_container);
        mHsvEffect = findViewById(R.id.hsv_effect);
        mLlEffectContainer = findViewById(R.id.ll_effect_container);

        mRecyclerView
                .setLayoutManager(new LinearLayoutManager(CortarVideoActivity.this, LinearLayoutManager.HORIZONTAL, false));
        videoEditAdapter = new TrimVideoAdapter(this, mMaxWidth / 10);
        mRecyclerView.setAdapter(videoEditAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

//        mSurfaceView.init(surfaceTexture -> {
//            mSurfaceTexture = surfaceTexture;
//
//        });
        try {
            initMediaPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            Iniciar();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //Una colección de efectos de filtro.
        mMagicFilterTypes = new MagicFilterType[]{
                MagicFilterType.NONE, MagicFilterType.INVERT,
                MagicFilterType.SEPIA, MagicFilterType.BLACKANDWHITE,
                MagicFilterType.TEMPERATURE, MagicFilterType.OVERLAY,
                MagicFilterType.BARRELBLUR, MagicFilterType.POSTERIZE,
                MagicFilterType.CONTRAST, MagicFilterType.GAMMA,
                MagicFilterType.HUE, MagicFilterType.CROSSPROCESS,
                MagicFilterType.GRAYSCALE, MagicFilterType.CGACOLORSPACE,
        };

//        for (int i = 0; i < mMagicFilterTypes.length; i++) {
//            FilterModel model = new FilterModel();
//            model.setName(
//                    UIUtils.getString(MagicFilterFactory.filterType2Name(mMagicFilterTypes[i])));
//            mVideoEffects.add(model);
//        }

        cortatr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trimmerVideo();
            }
        });
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addEffectView();

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
//                        videoStart();
                        videoResum();

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @OnClick({R.id.ll_trim_tab})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_trim_tab: //Ficha Cortar.
                mViewTrimIndicator.setVisibility(View.VISIBLE);
                mViewEffectIndicator.setVisibility(View.GONE);
                mLlTrimContainer.setVisibility(View.VISIBLE);
                mHsvEffect.setVisibility(View.GONE);
                break;
//            case R.id.ll_effect_tab: //Ficha Filtro.
//                mViewTrimIndicator.setVisibility(View.GONE);
//                mViewEffectIndicator.setVisibility(View.VISIBLE);
//                mLlTrimContainer.setVisibility(View.GONE);
//                mHsvEffect.setVisibility(View.VISIBLE);
//                break;
        }
    }

    /**
     * Agregue dinámicamente un efecto de filtro Vista.
     */
    private void addEffectView() {
        mLlEffectContainer.removeAllViews();
//        for (int i = 0; i < mVideoEffects.size(); i++) {
//            View itemView = LayoutInflater.from(this)
//                    .inflate(R.layout.item_video_effect, mLlEffectContainer, false);
//            TextView tv = itemView.findViewById(R.id.tv);
//            ImageView iv = itemView.findViewById(R.id.iv);
//            FilterModel model = mVideoEffects.get(i);
//            int thumbId = MagicFilterFactory.filterType2Thumb(mMagicFilterTypes[i]);
//            Glide.with(App.sApplication)
//                    .load(thumbId)
//                    .into(iv);
//            tv.setText(model.getName());
//            int index = i;
//            itemView.setOnClickListener(v -> {
//                for (int j = 0; j < mLlEffectContainer.getChildCount(); j++) {
//                    View tempItemView = mLlEffectContainer.getChildAt(j);
//                    TextView tempTv = tempItemView.findViewById(R.id.tv);
//                    FilterModel tempModel = mVideoEffects.get(j);
//                    if (j == index) {
//                        //选中的滤镜效果
//                        if (!tempModel.isChecked()) {
//                            openEffectAnimation(tempTv, tempModel, true);
//                        }
//                        ConfigUtils.getInstance().setMagicFilterType(mMagicFilterTypes[j]);
//                        mSurfaceView.setFilter(MagicFilterFactory.getFilter());
//                    } else {
//                        //未选中的滤镜效果
//                        if (tempModel.isChecked()) {
//                            openEffectAnimation(tempTv, tempModel, false);
//                        }
//                    }
//                }
//            });
//            mLlEffectContainer.addView(itemView);
//        }
    }

    private void openEffectAnimation(TextView tv, FilterModel model, boolean isExpand) {
        model.setChecked(isExpand);
        int startValue = UIUtils.dp2Px(30);
        int endValue = UIUtils.dp2Px(100);
        if (!isExpand) {
            startValue = UIUtils.dp2Px(100);
            endValue = UIUtils.dp2Px(30);
        }
        mEffectAnimator = ValueAnimator.ofInt(startValue, endValue);
        mEffectAnimator.setDuration(300);
        mEffectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, value, Gravity.BOTTOM);
                tv.setLayoutParams(params);
                tv.requestLayout();
            }
        });
        mEffectAnimator.start();
    }

    private void initEditVideo() {
        //for video edit
        long startPosition = 0;
        long endPosition = duration;
        int thumbnailsCount;
        int rangeWidth;
        boolean isOver_10_s;
        if (endPosition <= MAX_CUT_DURATION) {
            isOver_10_s = false;
            thumbnailsCount = MAX_COUNT_RANGE;
            rangeWidth = mMaxWidth;
        } else {
            isOver_10_s = true;
            thumbnailsCount = (int) (endPosition * 1.0f / (MAX_CUT_DURATION * 1.0f)
                    * MAX_COUNT_RANGE);
            rangeWidth = mMaxWidth / MAX_COUNT_RANGE * thumbnailsCount;
        }
        mRecyclerView
                .addItemDecoration(new VideoThumbSpacingItemDecoration(MARGIN, thumbnailsCount));

        //init seekBar
        if (isOver_10_s) {
            seekBar = new RangeSeekBar(this, 0L, MAX_CUT_DURATION);
            seekBar.setSelectedMinValue(0L);
            seekBar.setSelectedMaxValue(MAX_CUT_DURATION);
        } else {
            seekBar = new RangeSeekBar(this, 0L, endPosition);
            seekBar.setSelectedMinValue(0L);
            seekBar.setSelectedMaxValue(endPosition);
        }
        seekBar.setMin_cut_time(MIN_CUT_DURATION);//Establecer tiempo mínimo de corte
        seekBar.setNotifyWhileDragging(true);
        seekBar.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener);
        seekBarLayout.addView(seekBar);

        Log.d(TAG, "-------thumbnailsCount--->>>>" + thumbnailsCount);
        averageMsPx = duration * 1.0f / rangeWidth * 1.0f;
        Log.d(TAG, "-------rangeWidth--->>>>" + rangeWidth);
        Log.d(TAG, "-------localMedia.getDuration()--->>>>" + duration);
        Log.d(TAG, "-------averageMsPx--->>>>" + averageMsPx);
        OutPutFileDirPath = VideoUtil.getSaveEditThumbnailDir(this);
        int extractW = mMaxWidth / MAX_COUNT_RANGE;
        int extractH = UIUtils.dp2Px(62);
        mExtractFrameWorkThread = new ExtractFrameWorkThread(extractW, extractH, mUIHandler,
                mVideoPath,
                OutPutFileDirPath, startPosition, endPosition, thumbnailsCount);
        mExtractFrameWorkThread.start();

        //init pos icon start
        leftProgress = 0;
        if (isOver_10_s) {
            rightProgress = MAX_CUT_DURATION;
        } else {
            rightProgress = endPosition;
        }
        mTvShootTip.setText(String.format("Tiempo %d s", rightProgress / 1000));
        averagePxMs = (mMaxWidth * 1.0f / (rightProgress - leftProgress));
        Log.d(TAG, "------averagePxMs----:>>>>>" + averagePxMs);
    }


    private void Iniciar()throws Exception{
        videoView.setVideoURI(Uri.parse(mVideoPath));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0,0);
                mp.start();
            }
        });
    }
    /**
     * Inicializa MediaPlayer.
     */
    private void initMediaPlayer() throws Exception{
        videoView.setVideoURI(Uri.parse(mVideoPath));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
//                mp.setLooping(true);
                ViewGroup.LayoutParams lp = videoView.getLayoutParams();
                int videoWidth = mp.getVideoWidth();
                int videoHeight = mp.getVideoHeight();
                float videoProportion = (float) videoWidth / (float) videoHeight;
                int screenWidth = mRlVideo.getWidth();
                int screenHeight = mRlVideo.getHeight();
                float screenProportion = (float) screenWidth / (float) screenHeight;
                if (videoProportion > screenProportion) {
                    lp.width = screenWidth;
                    lp.height = (int) ((float) screenWidth / videoProportion);
                } else {
                    lp.width = (int) (videoProportion * (float) screenHeight);
                    lp.height = screenHeight;
                }
//                mSurfaceView.setLayoutParams(lp);

                mOriginalWidth = videoWidth;
                mOriginalHeight = videoHeight;
                Log.e("videoView", "videoWidth:" + videoWidth + ", videoHeight:" + videoHeight);

                //Establezca en OnSeekComplete listen en MediaPlayer.
                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        Log.d(TAG, "------ok----real---start-----");
                        Log.d(TAG, "------isSeeking-----" + isSeeking);
                        if (!isSeeking) {
                            videoStart();

                        }
                    }
                });
//                mp.setLooping(true);
            }
        });

//        try {
//            videoStart();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlaying = false;
                play_btn.setImageResource(R.drawable.player_play_btn);
            }
        });

//        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                if (mp.isPlaying())videoPause();
//                System.out.println(what);
//                System.out.println(extra);
//                return true;
//            }
//        });
    }

    /**
     * Recorte de vídeo.
     */
    private void trimmerVideo() {
        NormalProgressDialog
                .showLoading(this, getResources().getString(R.string.in_process), false);
        progressBar_cut.setVisibility(View.VISIBLE);
        videoPause();
        videoView.pause();
        Log.e(TAG, "trimVideo...startSecond:" + leftProgress + ", endSecond:"
                + rightProgress); //start:44228, end:48217
        //裁剪后的小视频第一帧图片
        // /storage/emulated/0/haodiaoyu/small_video/picture_1524055390067.jpg
//        Bitmap bitmap = mExtractVideoInfoUtil.extractFrame(leftProgress);
//        String firstFrame = FileUtil.saveBitmap("small_video", bitmap);
//        if (bitmap != null && !bitmap.isRecycled()) {
//            bitmap.recycle();
//            bitmap = null;
//        }
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
                        // /storage/emulated/0/Android/data/com.kangoo.diaoyur/files/small_video/trimmedVideo_20180416_153217.mp4
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
                        Toast.makeText(CortarVideoActivity.this, "Error en el recorte de vídeo.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * Añade un efecto de filtro al vídeo.
     */
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
                        Toast.makeText(CortarVideoActivity.this, "Error en el procesamiento de vídeo.", Toast.LENGTH_SHORT).show();
                    }
                })
                .start();
    }

    private byte select_calidad = 1;

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

    /**
     * VideCOmpresion
     */
//    private void compressVideo(String srcPath) {
//        String destDirPath = VideoUtil.getTrimmedVideoDir(this, "small_video");
//        Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(ObservableEmitter<String> emitter) {
//                try {
//                    int outWidth = 0;
//                    int outHeight = 0;
//                    if (mOriginalWidth > mOriginalHeight) {
//                        //横屏
//                        outWidth = 720;
//                        outHeight = 480;
//                    } else {
//                        //竖屏
//                        outWidth = 480;
//                        outHeight = 720;
//                    }
//                    String compressedFilePath = SiliCompressor.with(TrimVideoActivity.this)
//                            .compressVideo(srcPath, destDirPath, outWidth, outHeight, 900000);
//                    emitter.onNext(compressedFilePath);
//                } catch (Exception e) {
//                    emitter.onError(e);
//                }
//                emitter.onComplete();
//            }
//        })
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<String>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        subscribe(d);
//                    }
//
//                    @Override
//                    public void onNext(String outputPath) {
//                        //源路径: /storage/emulated/0/Android/data/com.kangoo.diaoyur/cache/small_video/trimmedVideo_20180514_163858.mp4
//                        //压缩路径: /storage/emulated/0/Android/data/com.kangoo.diaoyur/cache/small_video/VIDEO_20180514_163859.mp4
//                        Log.e(TAG, "compressVideo---onSuccess");
//                        //获取视频第一帧图片
//                        mExtractVideoInfoUtil = new ExtractVideoInfoUtil(outputPath);
//                        Bitmap bitmap = mExtractVideoInfoUtil.extractFrame();
//                        String firstFrame = FileUtil.saveBitmap("small_video", bitmap);
//                        if (bitmap != null && !bitmap.isRecycled()) {
//                            bitmap.recycle();
//                            bitmap = null;
//                        }
//                        NormalProgressDialog.stopLoading();
//
//                        //VideoPreviewActivity.startActivity(TrimVideoActivity.this, outputPath, firstFrame);
//                        finish();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.e(TAG, "compressVideo---onError:" + e.toString());
//                        NormalProgressDialog.stopLoading();
//                        Toast.makeText(CortarVideoActivity.this, "视频压缩失败", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });
//    }

    private boolean isOverScaledTouchSlop;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Log.d(TAG, "-------newState:>>>>>" + newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                isSeeking = false;
//                videoStart();
            } else {
                isSeeking = true;
                if (isOverScaledTouchSlop) {
                    videoPause();
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            isSeeking = false;
            int scrollX = getScrollXDistance();
            //No se puede alcanzar la distancia deslizante.
            if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                isOverScaledTouchSlop = false;
                return;
            }
            isOverScaledTouchSlop = true;
            Log.d(TAG, "-------scrollX:>>>>>" + scrollX);
            //El estado inicial.,por qué ? Porque por defecto hay un 56dp en blanco!
            if (scrollX == -MARGIN) {
                scrollPos = 0;
            } else {
                // ¿Por qué tratar con él aquí, porque el onScrollState cambiado es anterior que la devolución de llamada onScrolled.
                videoPause();
                isSeeking = true;
                scrollPos = (long) (averageMsPx * (MARGIN + scrollX));
                Log.d(TAG, "-------scrollPos:>>>>>" + scrollPos);
                leftProgress = seekBar.getSelectedMinValue() + scrollPos;
                rightProgress = seekBar.getSelectedMaxValue() + scrollPos;
                Log.d(TAG, "-------leftProgress:>>>>>" + leftProgress);
//                mMediaPlayer.seekTo((int) leftProgress);
                videoView.seekTo((int)leftProgress);
            }
            lastScrollX = scrollX;
        }
    };

    /**
     * Cuánto px se desliza horizontalmente.
     *
     * @return int px
     */
    private int getScrollXDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisibleChildView.getWidth();
        return (position) * itemWidth - firstVisibleChildView.getLeft();
    }

    private ValueAnimator animator;

    private void anim() {
//        Log.d(TAG, "--anim--onProgressUpdate---->>>>>>>" + mMediaPlayer.getCurrentPosition());
        Log.d(TAG, "--anim--onProgressUpdate---->>>>>>>" + videoView.getCurrentPosition());
        if (mIvPosition.getVisibility() == View.GONE) {
            mIvPosition.setVisibility(View.VISIBLE);
        }
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mIvPosition
                .getLayoutParams();
        int start = (int) (MARGIN
                + (leftProgress/*mVideoView.getCurrentPosition()*/ - scrollPos) * averagePxMs);
        int end = (int) (MARGIN + (rightProgress - scrollPos) * averagePxMs);
        animator = ValueAnimator
                .ofInt(start, end)
                .setDuration(
                        (rightProgress - scrollPos) - (leftProgress/*mVideoView.getCurrentPosition()*/
                                - scrollPos));
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.leftMargin = (int) animation.getAnimatedValue();
                mIvPosition.setLayoutParams(params);
            }
        });
        animator.start();
    }

    private final MainHandler mUIHandler = new MainHandler(this);

    private static class MainHandler extends Handler {

        private final WeakReference<CortarVideoActivity> mActivity;

        MainHandler(CortarVideoActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CortarVideoActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == ExtractFrameWorkThread.MSG_SAVE_SUCCESS) {
                    if (activity.videoEditAdapter != null) {
                        VideoEditInfo info = (VideoEditInfo) msg.obj;
                        activity.videoEditAdapter.addItemVideoInfo(info);
                    }
                }
            }
        }
    }

    private final RangeSeekBar.OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener = new RangeSeekBar.OnRangeSeekBarChangeListener() {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar bar, long minValue, long maxValue,
                                                int action, boolean isMin, RangeSeekBar.Thumb pressedThumb) {
            Log.d(TAG, "-----minValue----->>>>>>" + minValue);
            Log.d(TAG, "-----maxValue----->>>>>>" + maxValue);
            leftProgress = minValue + scrollPos;
            rightProgress = maxValue + scrollPos;
            Log.d(TAG, "-----leftProgress----->>>>>>" + leftProgress);
            Log.d(TAG, "-----rightProgress----->>>>>>" + rightProgress);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "-----ACTION_DOWN---->>>>>>");
                    isSeeking = false;
                    videoPause();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "-----ACTION_MOVE---->>>>>>");
                    isSeeking = true;
//                    mMediaPlayer.seekTo((int) (pressedThumb == RangeSeekBar.Thumb.MIN ?
//                            leftProgress : rightProgress));



                    videoView.seekTo((int) (pressedThumb == RangeSeekBar.Thumb.MIN ?
                            leftProgress : rightProgress));
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "-----ACTION_UP--leftProgress--->>>>>>" + leftProgress);
                    isSeeking = false;
                    //Comience con minValue.
//                    mMediaPlayer.seekTo((int) leftProgress);
//                    videoStart();
                    videoView.seekTo((int) leftProgress);
                    mTvShootTip
                            .setText(String.format("Tiempo %d s", (rightProgress - leftProgress) / 1000));
                    break;
                default:
                    break;
            }
        }
    };

    private void videoStart() {
        isPlaying = true;
        play_btn.setImageResource(R.drawable.player_pause_btn);
        Log.d(TAG, "----videoStart----->>>>>>>");
//        mMediaPlayer.start();
        videoView.start();
        mIvPosition.clearAnimation();
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        anim();
        handler.removeCallbacks(run);
        handler.post(run);
    }

    private void videoProgressUpdate() {
//        long currentPosition = mMediaPlayer.getCurrentPosition();
        long currentPosition = videoView.getCurrentPosition();
        Log.d(TAG, "----onProgressUpdate-cp---->>>>>>>" + currentPosition);
        if (currentPosition >= (rightProgress)) {
//            mMediaPlayer.seekTo((int) leftProgress);
            videoView.seekTo((int)leftProgress);
            mIvPosition.clearAnimation();
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
            anim();
        }
    }

    private void videoPause() {
        isSeeking = false;
        isPlaying = false;
        play_btn.setImageResource(R.drawable.player_play_btn);
        if (videoView != null && videoView.isPlaying()) {
//            mMediaPlayer.pause();
            videoView.pause();

            handler.removeCallbacks(run);
        }
        Log.d(TAG, "----videoPause----->>>>>>>");
        if (mIvPosition.getVisibility() == View.VISIBLE) {
            mIvPosition.setVisibility(View.GONE);
        }
        mIvPosition.clearAnimation();
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    private void videoResum(){
        if (videoView !=null) {
            System.out.println("resumen----");
            videoView.start();
            play_btn.setImageResource(R.drawable.player_pause_btn);
//           videoView.seekTo((int) leftProgress);
            isPlaying = true;
            mIvPosition.clearAnimation();
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
            anim();
            handler.removeCallbacks(run);
            handler.post(run);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
//            mMediaPlayer.seekTo((int) leftProgress);
            videoView.seekTo((int) leftProgress);
//            videoStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPause();
    }

    private Handler handler = new Handler();
    private Runnable run = new Runnable() {

        @Override
        public void run() {
            videoProgressUpdate();
            handler.postDelayed(run, 1000);
        }
    };

    @Override
    protected void onDestroy() {
        NormalProgressDialog.stopLoading();
        ConfigUtils.getInstance().setMagicFilterType(MagicFilterType.NONE);
        if (animator != null) {
            animator.cancel();
        }
        if (mEffectAnimator != null) {
            mEffectAnimator.cancel();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mMp4Composer != null) {
            mMp4Composer.cancel();
        }
        if (mExtractVideoInfoUtil != null) {
            mExtractVideoInfoUtil.release();
        }
        if (mExtractFrameWorkThread != null) {
            mExtractFrameWorkThread.stopExtract();
        }
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        mUIHandler.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);
        //Elimine la vista previa de cada fotograma del vídeo.
        if (!TextUtils.isEmpty(OutPutFileDirPath)) {
            VideoUtil.deleteFile(new File(OutPutFileDirPath));
        }
        //Retire el vídeo recortado, filtre el vídeo.
        String trimmedDirPath = VideoUtil.getTrimmedVideoDir(this, "small_video/trimmedVideo");
        if (!TextUtils.isEmpty(trimmedDirPath)) {
            VideoUtil.deleteFile(new File(trimmedDirPath));
        }
        super.onDestroy();
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
                Util.writeFile(CortarVideoActivity.this, "Comienzo at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
                dialog.dismiss();
                Intent intent = new Intent(CortarVideoActivity.this,VideoCompartir.class);
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
                Util.writeFile(CortarVideoActivity.this, "Comienzo at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
                dialog.dismiss();
                Intent intent = new Intent(CortarVideoActivity.this,VideoCompartir.class);
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
//                regresar = false;
//                reproducir = false;
//                btn_compress.setEnabled(false);
//                btn_select.setEnabled(false);
//                btn_extract_audio.setEnabled(false);
                tv_indicator.setText("Comprimiendo..." + "\n"
                        + "Inicio alas: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.VISIBLE);
                //startTime = System.currentTimeMillis();
                Util.writeFile(CortarVideoActivity.this, "Comienzo at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
//                regresar = true;
//                reproducir = true;
//                btn_compress.setEnabled(true);
//                btn_select.setEnabled(true);
//                btn_extract_audio.setEnabled(true);
//                String previous = tv_indicator.getText().toString();
//                tv_indicator.setText(previous + "\n"
//                        + "Compresion Exitosa!" + "\n"
//                        + "Terminado a las: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
//                pb_compress.setVisibility(View.INVISIBLE);
//                endTime = System.currentTimeMillis();
//                Util.writeFile(CortarVideoActivity.this, "Finalizado at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
//                Util.writeFile(CortarVideoActivity.this, "Total: " + ((endTime - startTime)/1000) + "s" + "\n");
//                Util.writeFile(CortarVideoActivity.this);
//                inputPath = destPath;
//                uriShared = destPath;
//                btn_shared.setVisibility(View.VISIBLE);
//                Glide.with(CortarVideoActivity.this).load(uriShared).into(imageIcon);
//                NormalProgressDialog.stopLoading();
                dialog.dismiss();
                Intent intent = new Intent(CortarVideoActivity.this,VideoCompartir.class);
                intent.putExtra("id",destPath);
                startActivity(intent);
            }

            @Override
            public void onFail() {
                dialog.dismiss();
//                regresar = true;
//                reproducir  =true;
//                btn_compress.setEnabled(true);
//                btn_select.setEnabled(true);
//                btn_extract_audio.setEnabled(true);
//                tv_indicator.setText("Compresion Fallida!");
//                pb_compress.setVisibility(View.INVISIBLE);
//                endTime = System.currentTimeMillis();
                //Util.writeFile(CortarVideoActivity.this, "Compresion Fallida!!!" + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));

            }

            @Override
            public void onProgress(float percent) {
                int p = (int) percent;
                pb_compress.setProgress(p);
                tv_progress.setText(String.valueOf(p) + "%");
            }
        });
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
}