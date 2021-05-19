package com.damon.videocompress.activitys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.damon.compressvideo.VideoCompress;
import com.damon.videocompress.R;
import com.damon.videocompress.utils.ExactorMediaUtils;
import com.damon.videocompress.utils.LogUtil;
import com.damon.videocompress.utils.ThreadUtil;
import com.damon.videocompress.utils.Util;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.initialization.InitializationStatus;
//import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.content.ContentValues.TAG;


public class CompressVideoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int REQUEST_FOR_VIDEO_FILE = 1000;
    private TextView tv_input, tv_output, tv_indicator, tv_progress;

    private String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
//    private String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/VideoCompress/videos";

    private String inputPath;
    private String outputPath;

    private ProgressBar pb_compress;

    private long startTime, endTime;

    private ImageView imageIcon;

    long videoId;

    private ImageButton btn_shared;

    private String uriShared;

    private boolean regresar = true;
    private boolean reproducir = true;

    private Spinner spinner;

    String valor;

     LinearLayout btn_compress,btn_extract_audio;
     LinearLayout btn_select;
     String caso;
//    private AdView mAdView;
    private String filePath;

//    private FFmpeg ffmpeg;

//    private ProgressDialog progressDialog;
    private int choice = 0;

    com.damon.videocompress.utils.ProgressDialog mProgressDialog;
    private static final int SHOW_PROGRCESS = 1;
    private static final int UPDATE_PROGRCESS = 2;
    private static final int CLOSE_PROGRCESS = 3;
    private long prgValue = 0;
    private long totalValue;

    private LinearLayout cortarVideo;
    private Dialog ponerNombreDialog;
    private String nombreAudio;
    private TextView peso_acutal, peso_nuevo;
    private Uri pesoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress_video);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        AdView adView = new AdView(this);
//        adView.setAdSize(AdSize.BANNER);
//        adView.setAdUnitId(getString(R.string.banner));
//
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {
//            }
//        });
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);


        peso_acutal = findViewById(R.id.tvx_before);
        peso_nuevo = findViewById(R.id.txv_new);
        ponerNombreDialog = new Dialog(this);
        caso = getIntent().getExtras().getString("caso");

        cortarVideo = findViewById(R.id.cortar_video);
        btn_extract_audio = findViewById(R.id.btn_extract_audio);
        spinner = findViewById(R.id.spinner);
        initView();

        if (caso.equals("si")) {
            inputPath = getIntent().getExtras().getString("videoId");
            tv_input.setText(inputPath);
            pesoUri = Uri.parse(inputPath);
        } else {
            videoId = getIntent().getExtras().getLong("videoId");
            Uri videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId);
            pesoUri = videoUri;
            System.out.println(pesoUri);
            try {
                String s = Util.getFilePath(this, videoUri);
//            String s = String.valueOf(Uri.fromFile(new File(Util.getFilePath(this,videoUri))));
                inputPath = s;
                tv_input.setText(inputPath);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        // Uri s = Uri.fromFile(new File(FileUtils.getPath(this,videoUri)));


//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle(null);
//        progressDialog.setCancelable(false);

        mProgressDialog = new com.damon.videocompress.utils.ProgressDialog(this);

        loadFFMpegBinary();


        if (inputPath !=null){
            Glide.with(this).load(inputPath).into(imageIcon);
            imageIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (reproducir){
                        Intent intent = new Intent(CompressVideoActivity.this, VideoCompartir.class);
                        intent.putExtra("id",inputPath);
                        startActivity(intent);
                    }

                }
            });
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.planets_array,R.layout.spinner_text);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);


        btn_extract_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                choice = 4;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    getAudioPermission();
                }else {
                   // extractAudioVideo();
//                    extractAudio(inputPath);
                    mostrarDialogo(inputPath);
                }

//                Intent intent = new Intent(CompressVideoActivity.this, ExtraerAudioActiviy.class);
//                intent.putExtra("path",inputPath);
//                startActivity(intent);

            }
        });

        cortarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(CompressVideoActivity.this,CortarVideoActivity.class);
                Intent intent = new Intent(CompressVideoActivity.this,NewCutVideo.class);
                intent.putExtra("videoPath",inputPath);
                startActivity(intent);
            }
        });

        getDataVideo(peso_acutal,pesoUri,"PESO ACTUAL DEL VIDEO ");

    }

    void mostrarDialogo(String model){
        ponerNombreDialog.setContentView(R.layout.file_save);
        Spinner spinner = ponerNombreDialog.findViewById(R.id.ringtone_type);
        TextView titulo = ponerNombreDialog.findViewById(R.id.texto_dialogo_file);
        EditText nombreEdit = ponerNombreDialog.findViewById(R.id.filename);
        Button btn_cancel = ponerNombreDialog.findViewById(R.id.cancel);
        Button btn_save = ponerNombreDialog.findViewById(R.id.save);


        spinner.setVisibility(View.GONE);
        titulo.setText("ESCRIBE UN NOMBRE PARA EL AUDIO A EXTRAER");
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ponerNombreDialog.dismiss();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nombreAudio = nombreEdit.getText().toString();
                if (!TextUtils.isEmpty(nombreAudio)){
                    try {
                        extractAudio(model,nombreAudio);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ponerNombreDialog.dismiss();
                }else {
                    ponerNombreDialog.dismiss();
                }
            }
        });



        ponerNombreDialog.show();
    }

    private void extractAudio(final String  model,String  nombre) throws Exception{
        totalValue = 300000;
        final File moviesDir;

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            moviesDir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC)+File.separator+getString(R.string.app_name));
            // folder = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());

        }else {
            moviesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+File.separator+getString(R.string.app_name));
            // folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        }

        if (!moviesDir.exists()) {
            moviesDir.mkdirs();
        }
//
//        File mDir = new File(MainApplication.AudioFileDir);
//        if (!mDir.exists()) {
//            mDir.mkdirs();
//        }
        ThreadUtil.INST.excute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void run() {
                FileOutputStream audioOutputStream = null;
                MediaExtractor mediaExtractor = new MediaExtractor();//声明多媒体提取器
                LogUtil.d(TAG, "audio:excute:");
                sendHandleMessage(0,SHOW_PROGRCESS);//通知显示提取进度条
                File audioFile = new File(moviesDir, nombre+".mp3");//新建提取文件名
                try {
                    audioOutputStream = new FileOutputStream(audioFile);
                    mediaExtractor.setDataSource(model);
                    int audioTrackIndex = -1;
                    int trackCount = mediaExtractor.getTrackCount();
                    LogUtil.d("mineType","   trackCount:"+trackCount);

                    for (int i = 0; i < trackCount; i++) {
                        MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                        String mineType = trackFormat.getString(MediaFormat.KEY_MIME);
                        LogUtil.d("mineType","mineType:"+mineType);
                        //音频信道
                        if (mineType.startsWith("audio")) {
                            audioTrackIndex = i;
                        }
                    }
                    //Cambie al canal de audio.
                    try {
                        mediaExtractor.selectTrack(audioTrackIndex);
                    }catch (Exception e){
                        if (mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CompressVideoActivity.this, "Lo sentimos no se puede extraer el audio \n Error al extraer el audio ", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                        return;
                    }


                    ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
                    while (true) {
                        int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);

                        if (readSampleCount < 0) {
                            sendHandleMessage(mediaExtractor.getSampleTime()/1000,CLOSE_PROGRCESS);
                            break;
                        }
                        sendHandleMessage(mediaExtractor.getSampleTime()/1000,UPDATE_PROGRCESS);

                        //保存音频信息
                        byte[] buffer = new byte[readSampleCount];
                        byteBuffer.get(buffer);
                        /************************* 用来为aac添加adts头**************************/
                        byte[] aacaudiobuffer = new byte[readSampleCount + 7];
                        addADTStoPacket(aacaudiobuffer, readSampleCount + 7);
                        System.arraycopy(buffer, 0, aacaudiobuffer, 7, readSampleCount);
                        audioOutputStream.write(aacaudiobuffer);
                        /***************************************close**************************/
                        byteBuffer.clear();
                        mediaExtractor.advance();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    mediaExtractor.release();
                    mediaExtractor = null;
                    try {
                        audioOutputStream.flush();
                        audioOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendHandleMessage(long object,int what){
        Message msg=new Message();
        msg.obj= object;//message的内容
        msg.what=what;//指定message
        handler.sendMessage(msg);//handler发送message
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHOW_PROGRCESS:
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    prgValue = 0;
                    break;
                case UPDATE_PROGRCESS:
                    prgValue =  (long)msg.obj;
                    int progress = (int) (prgValue * 1.0f / totalValue * 100);
                    LogUtil.d("prgValue","value:"+progress);
                    mProgressDialog.setProgressBarValue(progress);
                    break;
                case CLOSE_PROGRCESS:
                    prgValue = 0;
                    mProgressDialog.dismiss();
                    Toast.makeText(CompressVideoActivity.this,"Exito al extraer",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CompressVideoActivity.this, AudioListaActivity.class);
//                    intent.putExtra("path",inputPath);
                    startActivity(intent);
                    break;
            }
        }
    };


    private static void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = ExactorMediaUtils.getFreqIdx(44100);
        int chanCfg = 2; // CPE

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }



    
    private void initView(){
        imageIcon = findViewById(R.id.imagen_video);
        btn_shared = findViewById(R.id.shared_video);
        btn_select = (LinearLayout) findViewById(R.id.btn_select);
        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                /* 开启Pictures画面Type设定为image */
                //intent.setType("video/*;image/*");
                //intent.setType("audio/*"); //选择音频
                intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_FOR_VIDEO_FILE);
            }
        });

        btn_compress  = (LinearLayout) findViewById(R.id.btn_compress);

        btn_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  final String destPath = tv_output.getText().toString() + File.separator + "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(new Date()) + ".mp4";
                String file_name = UUID.randomUUID() + ".mp4";
                String destPath = tv_output.getText().toString() + File.separator + file_name ;
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
                switch (valor){
                    case "Compresion Baja":
                        try {
                            compresionBaja(destPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Compresion Media":
                        try {
                            compresionMedia(destPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Compresion Alta":
                        try {
                            compresionAlta(destPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }


            }
        });


        tv_input = (TextView) findViewById(R.id.tv_input);
        tv_output = (TextView) findViewById(R.id.tv_output);
        tv_output.setText(outputDir+"/"+getString(R.string.app_name));
        tv_indicator = (TextView) findViewById(R.id.tv_indicator);
        tv_progress = (TextView) findViewById(R.id.tv_progress);

        pb_compress = (ProgressBar) findViewById(R.id.pb_compress);
        pb_compress.setMax(100);

        btn_shared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedVideo(uriShared);
            }
        });
    }

    private void compresionAlta(final String destPath) throws Exception {
        VideoCompress.compressVideoHigh(tv_input.getText().toString(), destPath, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                regresar = false;
                reproducir = false;
                btn_compress.setEnabled(false);
                btn_select.setEnabled(false);
                btn_extract_audio.setEnabled(false);
                cortarVideo.setEnabled(false);
                tv_indicator.setText("Comprimiendo..." + "\n"
                        + "Inicio alas: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.VISIBLE);
                startTime = System.currentTimeMillis();
                Util.writeFile(CompressVideoActivity.this, "Comienzo at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
                regresar = true;
                reproducir = true;
                btn_compress.setEnabled(true);
                btn_select.setEnabled(true);
                btn_extract_audio.setEnabled(true);
                cortarVideo.setEnabled(true);
                String previous = tv_indicator.getText().toString();
                tv_indicator.setText(previous + "\n"
                        + "Compresion Exitosa!" + "\n"
                        + "Terminado a las: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.INVISIBLE);
                endTime = System.currentTimeMillis();
                Util.writeFile(CompressVideoActivity.this, "Finalizado at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
                Util.writeFile(CompressVideoActivity.this, "Total: " + ((endTime - startTime)/1000) + "s" + "\n");
                Util.writeFile(CompressVideoActivity.this);
                inputPath = destPath;
                uriShared = destPath;

                File datas = new File(destPath);
                long peso = datas.length();
                mostrarPesoNuevo(peso);

                btn_shared.setVisibility(View.VISIBLE);
                Glide.with(CompressVideoActivity.this).load(uriShared).into(imageIcon);
                Intent intent = new Intent(CompressVideoActivity.this,VideoCompartir.class);
                intent.putExtra("id",uriShared);
                startActivity(intent);
            }

            @Override
            public void onFail() {
                regresar = true;
                reproducir  =true;
                btn_compress.setEnabled(true);
                btn_select.setEnabled(true);
                btn_extract_audio.setEnabled(true);
                cortarVideo.setEnabled(true);
                tv_indicator.setText("Compresion Fallida!");
                pb_compress.setVisibility(View.INVISIBLE);
                endTime = System.currentTimeMillis();
                Util.writeFile(CompressVideoActivity.this, "Compresion Fallida!!!" + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
            }

            @Override
            public void onProgress(float percent) {
                int p = (int) percent;
                pb_compress.setProgress(p);
                tv_progress.setText(String.valueOf(p) + "%");
            }
        });
    }

    private void compresionMedia(final String destPath) throws Exception  {
        VideoCompress.compressVideoMedium(tv_input.getText().toString(), destPath, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                regresar = false;
                reproducir = false;
                btn_compress.setEnabled(false);
                btn_select.setEnabled(false);
                btn_extract_audio.setEnabled(false);
                cortarVideo.setEnabled(false);
                tv_indicator.setText("Comprimiendo..." + "\n"
                        + "Inicio alas: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.VISIBLE);
                startTime = System.currentTimeMillis();
                Util.writeFile(CompressVideoActivity.this, "Comienzo at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
                regresar = true;
                reproducir  = true;
                btn_compress.setEnabled(true);
                btn_select.setEnabled(true);
                btn_extract_audio.setEnabled(true);
                cortarVideo.setEnabled(true);
                String previous = tv_indicator.getText().toString();
                tv_indicator.setText(previous + "\n"
                        + "Compresion Exitosa!" + "\n"
                        + "Terminado a las: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.INVISIBLE);
                endTime = System.currentTimeMillis();
                Util.writeFile(CompressVideoActivity.this, "Finalizado at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
                Util.writeFile(CompressVideoActivity.this, "Total: " + ((endTime - startTime)/1000) + "s" + "\n");
                Util.writeFile(CompressVideoActivity.this);
                inputPath = destPath;
                uriShared = destPath;

                File datas = new File(destPath);
                long peso = datas.length();
                mostrarPesoNuevo(peso);

                btn_shared.setVisibility(View.VISIBLE);
                Glide.with(CompressVideoActivity.this).load(uriShared).into(imageIcon);
                Intent intent = new Intent(CompressVideoActivity.this,VideoCompartir.class);
                intent.putExtra("id",uriShared);
                startActivity(intent);
            }

            @Override
            public void onFail() {
                regresar = true;
                reproducir = true;
                btn_compress.setEnabled(true);
                btn_select.setEnabled(true);
                btn_extract_audio.setEnabled(true);
                cortarVideo.setEnabled(true);
                tv_indicator.setText("Compresion Fallida!");
                pb_compress.setVisibility(View.INVISIBLE);
                endTime = System.currentTimeMillis();
                Util.writeFile(CompressVideoActivity.this, "Compresion Fallida!!!" + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
            }

            @Override
            public void onProgress(float percent) {
                int p = (int) percent;
                pb_compress.setProgress(p);
                tv_progress.setText(String.valueOf(p) + "%");
            }
        });
    }

    private void compresionBaja(final String destPath) throws  Exception{
        VideoCompress.compressVideoLow(tv_input.getText().toString(), destPath, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                regresar = false;
                reproducir = false;
                btn_compress.setEnabled(false);
                btn_select.setEnabled(false);
                btn_extract_audio.setEnabled(false);
                cortarVideo.setEnabled(false);
                tv_indicator.setText("Comprimiendo..." + "\n"
                        + "Inicio alas: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.VISIBLE);
                startTime = System.currentTimeMillis();
                Util.writeFile(CompressVideoActivity.this, "Comienzo at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
            }

            @Override
            public void onSuccess() {
                regresar = true;
                reproducir = true;
                btn_compress.setEnabled(true);
                btn_select.setEnabled(true);
                btn_extract_audio.setEnabled(true);
                cortarVideo.setEnabled(true);
                String previous = tv_indicator.getText().toString();
                tv_indicator.setText(previous + "\n"
                        + "Compresion Exitosa!" + "\n"
                        + "Terminado a las: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
                pb_compress.setVisibility(View.INVISIBLE);
                endTime = System.currentTimeMillis();
                Util.writeFile(CompressVideoActivity.this, "Finalizado at: " + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()) + "\n");
                Util.writeFile(CompressVideoActivity.this, "Total: " + ((endTime - startTime)/1000) + "s" + "\n");
                Util.writeFile(CompressVideoActivity.this);
                inputPath = destPath;
                uriShared = destPath;
                btn_shared.setVisibility(View.VISIBLE);

                File datas = new File(destPath);
                long peso = datas.length();
                mostrarPesoNuevo(peso);

                Glide.with(CompressVideoActivity.this).load(uriShared).into(imageIcon);
                Intent intent = new Intent(CompressVideoActivity.this,VideoCompartir.class);
                intent.putExtra("id",uriShared);
                startActivity(intent);
            }

            @Override
            public void onFail() {
                regresar = true;
                reproducir  =true;
                btn_compress.setEnabled(true);
                btn_select.setEnabled(true);
                btn_extract_audio.setEnabled(true);
                cortarVideo.setEnabled(true);
                tv_indicator.setText("Compresion Fallida!");
                pb_compress.setVisibility(View.INVISIBLE);
                endTime = System.currentTimeMillis();
                Util.writeFile(CompressVideoActivity.this, "Compresion Fallida!!!" + new SimpleDateFormat("HH:mm:ss", getLocale()).format(new Date()));
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

    @SuppressWarnings("deprecation")
    public static Locale getSystemLocaleLegacy(Configuration config){
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getSystemLocale(Configuration config){
        return config.getLocales().get(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOR_VIDEO_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
//                inputPath = data.getData().getPath();
//                tv_input.setText(inputPath);

                try {
                    inputPath = Util.getFilePath(this, data.getData());
                    tv_input.setText(inputPath);
                    if (inputPath !=null){
                        Glide.with(this).load(inputPath).into(imageIcon);
                        getDataVideo(peso_acutal, data.getData(),"PESO ACTUAL DEL VIDEO");
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

//                inputPath = "/storage/emulated/0/DCIM/Camera/VID_20170522_172417.mp4"; // 图片文件路径
//                tv_input.setText(inputPath);// /storage/emulated/0/DCIM/Camera/VID_20170522_172417.mp4
            }
        }
    }

    protected void SharedVideo(String  absolutePath){
        Intent compartirAudio = new Intent(android.content.Intent.ACTION_SEND);
        compartirAudio.setType("video/*");
        compartirAudio.putExtra(Intent.EXTRA_STREAM,
                Uri.parse(absolutePath)); //En getPackageName() da error por no estar definido
        startActivity(Intent.createChooser(compartirAudio, "Compartir vía"));
    }

    @Override
    public void onBackPressed() {
        if (regresar){
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                valor = "Compresion Baja";
                break;
            case 1:
                valor = "Compresion Media";
                break;
            case 2:
                valor = "Compresion Alta";
                break;

            default:
                valor = "Compresion Baja";
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    private void getAudioPermission() {
        //requestAudioPermissions();
        String[] params = null;
        String recordAudio = Manifest.permission.RECORD_AUDIO;
        String modifyAudio = Manifest.permission.MODIFY_AUDIO_SETTINGS;

        int hasRecordAudioPermission = ActivityCompat.checkSelfPermission(this, recordAudio);
        int hasModifyAudioPermission = ActivityCompat.checkSelfPermission(this, modifyAudio);
        List<String> permissions = new ArrayList<String>();

        if (hasRecordAudioPermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(recordAudio);
        if (hasModifyAudioPermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(modifyAudio);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }
        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(CompressVideoActivity.this,
                    params,
                    200);
        } else
//            extractAudio(inputPath);
              mostrarDialogo(inputPath);
    }

    /**
     * Command for extracting audio from video
     */
    private void extractAudioVideo() {
//        progressDialog.setMessage("Extrayendo...");
//        progressDialog.show();
        File moviesDir;

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            moviesDir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC)+File.separator+getString(R.string.app_name));
            // folder = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());

        }else {
            moviesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+File.separator+getString(R.string.app_name));
            // folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        }

        if (!moviesDir.exists()) {
            moviesDir.mkdirs();
        }

        String filePrefix = UUID.randomUUID().toString();
        String fileExtn = ".mp3";
//        String yourRealPath = getPath(CompressVideoActivity.this, inputPath);
        String yourRealPath = inputPath;
        File dest = new File(moviesDir, filePrefix + fileExtn);

        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
        }
        Log.d("TAG", "startTrim: src: " + yourRealPath);
        Log.d("TAG", "startTrim: dest: " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();

        String[] complexCommand = {"-y", "-i", yourRealPath, "-vn", "-ar", "44100", "-ac", "2", "-b:a", "256k", "-f", "mp3", filePath};

        execFFmpegBinary(complexCommand);
    }



    private void execFFmpegBinary(final String[] command) {
        try {
//         int rc  =  FFmpeg.execute(command);
//            if (rc == RETURN_CODE_SUCCESS) {
//                progressDialog.dismiss();
//                Log.i(Config.TAG, "Command execution completed successfully.");
//                Toast.makeText(this, "COMPRESCION COMPLETA", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(CompressVideoActivity.this, AudioListaActivity.class);
//                startActivity(intent);
//            } else if (rc == RETURN_CODE_CANCEL) {
//                Log.i(Config.TAG, "Command execution cancelled by user.");
//            } else {
//                Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
//                Config.printLastCommandOutput(Log.INFO);
//            }
//            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
//                @Override
//                public void onFailure(String s) {
//                    Toast.makeText(CompressVideoActivity.this, "FALLO AL EXTRAER EL AUDIO", Toast.LENGTH_SHORT).show();
//                    Log.d("TAG", "FAILED with output : " + s);
//                }
//
//                @Override
//                public void onSuccess(String s) {
//                    Log.d("TAG", "SUCCESS with output : " + s);
//                    if (choice == 1 || choice == 2 || choice == 5 || choice == 6 || choice == 7) {
////                        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
////                        intent.putExtra(FILEPATH, filePath);
////                        startActivity(intent);
//                    } else if (choice == 3) {
////                        Intent intent = new Intent(MainActivity.this, PreviewImageActivity.class);
////                        intent.putExtra(FILEPATH, filePath);
////                        startActivity(intent);
//                    } else if (choice == 4) {
//                        Intent intent = new Intent(CompressVideoActivity.this, AudioListaActivity.class);
////                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
//                        Toast.makeText(CompressVideoActivity.this, "EXITO AL EXTRAER EL AUDIO", Toast.LENGTH_SHORT).show();
//                    }
////                    else if (choice == 8) {
////                        choice = 9;
////                        reverseVideoCommand();
////                    } else if (Arrays.equals(command, lastReverseCommand)) {
////                        choice = 10;
////                        concatVideoCommand();
////                    } else if (choice == 10) {
////                        File moviesDir = Environment.getExternalStoragePublicDirectory(
////                                Environment.DIRECTORY_MOVIES
////                        );
////                        File destDir = new File(moviesDir, ".VideoPartsReverse");
////                        File dir = new File(moviesDir, ".VideoSplit");
////                        if (dir.exists())
////                            deleteDir(dir);
////                        if (destDir.exists())
////                            deleteDir(destDir);
////                        choice = 11;
////                        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
////                        intent.putExtra(FILEPATH, filePath);
////                        startActivity(intent);
////                    }
//                }
//
//                @Override
//                public void onProgress(String s) {
//                    Log.d("TAG", "Started command : ffmpeg " + command);
//                    if (choice == 8)
//                        progressDialog.setMessage("progress : splitting video " + s);
//                    else if (choice == 9)
//                        progressDialog.setMessage("progress : reversing splitted videos " + s);
//                    else if (choice == 10)
//                        progressDialog.setMessage("progress : concatenating reversed videos " + s);
//                    else
//                        progressDialog.setMessage("progress : " + s);
//                    Log.d("TAG", "progress : " + s);
//                }
//
//                @Override
//                public void onStart() {
//                    Log.d("TAG", "Started command : ffmpeg " + command);
//                    progressDialog.setMessage("Processing...");
//                    progressDialog.show();
//                }
//
//                @Override
//                public void onFinish() {
//                    Log.d("TAG", "Finished command : ffmpeg " + command);
//                    if (choice != 8 && choice != 9 && choice != 10) {
//                        progressDialog.dismiss();
//                    }
//
//                }
//            });
        } catch (Exception e) {
            // do nothing for now
        }
    }


    private void loadFFMpegBinary(){

//        try {
//            if (ffmpeg == null) {
//                Log.d("TAG", "ffmpeg : era nulo");
//                ffmpeg = FFmpeg.getInstance(this);
//            }
//            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
//                @Override
//                public void onFailure() {
//                    showUnsupportedExceptionDialog();
//                }
//
//                @Override
//                public void onSuccess() {
//                    Log.d("TAG", "ffmpeg : correct Loaded");
//                }
//            });
//        } catch (FFmpegNotSupportedException e) {
//            showUnsupportedExceptionDialog();
//        } catch (Exception e) {
//            Log.d("TAG", "EXception no controlada : " + e);
//        }
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(CompressVideoActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CompressVideoActivity.this.finish();
                    }
                })
                .create()
                .show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    extractAudio(inputPath);
                    mostrarDialogo(inputPath);
                }
            }
            case MY_PERMISSIONS_RECORD_AUDIO:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    extractAudioVideo();
                }
            }

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
//            extractAudioVideo();
        }
    }


//    class Threads extends Thread{
//        @Override
//        public void run() {
//            super.run();
//            getDataVideo();
//        }
//    }

    private void getDataVideo(TextView peso_acutal,Uri pesoUri,String msg){
        new Thread(){
            @Override
            public void run() {
                super.run();
                String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.SIZE,MediaStore.Video.Media.DURATION,MediaStore.Video.Media.RESOLUTION};
                Cursor cursor = getApplication().getContentResolver().query(pesoUri, null, null, null, null);
                if (cursor!=null){
                    System.out.println("SI ENTRA ");
                    int duration_video_colum = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE);
                    float valors = 0;
                    cursor.moveToFirst();
                    float valor  =cursor.getLong(duration_video_colum);
//                    int s = Math.round(valor);
//                    int mg = s / (1024 * 1024);
//                    int kb = s / 1024;

                    double kb = getFileSizeInKB(valor);
                    double mg = getFileSizeInMB(valor);
                    double sizeGB = getFileSizeInGB(valor);

                    System.out.println(kb + "KB");
                    System.out.println(mg + " MG");
                    System.out.println(sizeGB+" GB");
                    cursor.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (kb <= fB &&kb>=100.0){
                                peso_acutal.setText(msg + "\n " + kb + " KB");
                            } else if (mg <= fB && mg<=kb){
                                peso_acutal.setText(msg +"\n "+mg + " MB");
                            }else if (sizeGB <= fB && sizeGB <=100.0) {
                                peso_acutal.setText(msg +"\n"+sizeGB+ " GB");
                            }
                        }
                    });

                }
                System.out.println("FINAL ");
            }
        }.start();
    }
    private final static double fB = 1024.0;

    private static double getFileSizeInKB (double f) {
        f = (f/fB);
        int fs = (int) Math.pow(10,2);
        return Math.rint(f*fs)/fs;
    }


    private static double getFileSizeInMB (double f) {
        f = f / Math.pow(fB,2);
        int fs = (int) Math.pow(10,2);
        return Math.rint(f*fs)/fs;
    }

    private static double getFileSizeInGB (double f) {
        f = f / Math.pow(fB,3);
        int fs = (int) Math.pow(10,2);
        return Math.rint(f*fs)/fs;
    }

    private void mostrarPesoNuevo(float valor){
        int s = Math.round(valor);
        int mg = s / (1024 * 1024);
        int kb = s / 1024;
        if (kb > 1000){
            peso_nuevo.setText("NUEVO PESO DEL VIDEO " +"\n "+mg + " MB");
        }else {
            peso_nuevo.setText("NUEVO PESO DEL VIDEO " + "\n " + kb + " KB");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(getApplicationContext()).clearMemory();
        clearApplicationData();
        getCacheDir().deleteOnExit();
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