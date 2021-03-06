package com.damon.videocompress.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.damon.videocompress.R;
import com.damon.videocompress.adapters.ExtractVideoAdapater;
import com.damon.videocompress.fragments.BaseFragment;
import com.damon.videocompress.interfaces.ItemOnClickListener;
import com.damon.videocompress.models.ActionType;
import com.damon.videocompress.models.VideoModel;
import com.damon.videocompress.utils.EventType;
import com.damon.videocompress.utils.ExactorMediaUtils;
import com.damon.videocompress.utils.LogUtil;
import com.damon.videocompress.utils.MainApplication;
import com.damon.videocompress.utils.MessageEvent;
import com.damon.videocompress.utils.ProgressDialog;
import com.damon.videocompress.utils.ThreadUtil;
import com.damon.videocompress.utils.TimeFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ExtraerAudioFragment extends BaseFragment implements View.OnClickListener {


    private static final int SHOW_PROGRCESS = 1;
    private static final int UPDATE_PROGRCESS = 2;
    private static final int CLOSE_PROGRCESS = 3;

    private TextView actionTextV;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ExtractVideoAdapater mVideoAdapater;
    private List<VideoModel> mList = new ArrayList<>();
    private ImageView addFileIv;
    private ProgressDialog mProgressDialog;
    private long prgValue = 0;
    private long totalValue;






    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHOW_PROGRCESS:
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
                    Toast.makeText(mContext,"Exito al extraer",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        mProgressDialog = new ProgressDialog(mContext);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_extraer_audio, container, false);
        actionTextV = view.findViewById(R.id.select_video_tv);
        addFileIv  = view.findViewById(R.id.add_file_iv);
        actionTextV.setOnClickListener(this);
        addFileIv.setOnClickListener(this);
        actionTextV.setVisibility(View.GONE);
        mRecyclerView = view.findViewById(R.id.media_extractor_rv);
        mLayoutManager = new LinearLayoutManager(mContext);//??????
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onEvent(MessageEvent event) {
        super.onEvent(event);
        if (event.getType() == EventType.VIDEOMODEL){
            mList.add(event.getModel());
            mVideoAdapater.setmList(mList);
            actionTextV.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mVideoAdapater = new ExtractVideoAdapater(mContext,mList);
        mVideoAdapater.setActionType(ActionType.EXTRACT_TYPE);
        mVideoAdapater.setClickListener(clickListener);
        mRecyclerView.setAdapter(mVideoAdapater);

        if (mList.size() > 0){
            actionTextV.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }else {
            mRecyclerView.setVisibility(View.GONE);
            actionTextV.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.select_video_tv:
            case R.id.add_file_iv:
                Intent intent = new Intent(mContext, SelectVideoActivity.class);
                mContext.startActivity(intent);
                break;
        }
    }

    private ItemOnClickListener clickListener = new ItemOnClickListener() {
        @Override
        public void OnClick(View view, Object o) {
            VideoModel model = (VideoModel) o;
            LogUtil.d("VideoModel",model.getName()+" d:"+model.getUrl());
        }

        @Override
        public void OnClickModel(View view, VideoModel model) {
            switch (view.getId()){
                case R.id.extract_audio_rl:
                case R.id.extract_audio_ll:
                case R.id.extract_audio_ib:
                case R.id.extract_audio_tv:
                    extractAudio(model);
                    break;
                case R.id.extract_video_rl:
                    extractVideo(model);
                    break;
                case R.id.extract_audiopath_rl:
                    openAudioFile();
                    break;
            }
        }
    };

    private void ShowSelectDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("????????????:");
        final String[] items = new String[]{"mp3", "acc"};
        builder.setSingleChoiceItems(items, 2, new DialogInterface.OnClickListener() {/*?????????????????????????????????*/
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void extractAudio(final VideoModel model){
        totalValue = model.getDuation();
        final File moviesDir;

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            moviesDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)+File.separator+getString(R.string.app_name));
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
                MediaExtractor mediaExtractor = new MediaExtractor();//????????????????????????
                LogUtil.d(TAG, "audio:excute:");
                sendHandleMessage(0,SHOW_PROGRCESS);//???????????????????????????
                File audioFile = new File(moviesDir, TimeFormat.getCurrentTime()+".mp3");//?????????????????????
                try {
                    audioOutputStream = new FileOutputStream(audioFile);
                    mediaExtractor.setDataSource(model.getUrl());
                    int audioTrackIndex = -1;
                    int trackCount = mediaExtractor.getTrackCount();
                    LogUtil.d("mineType","   trackCount:"+trackCount);

                    for (int i = 0; i < trackCount; i++) {
                        MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                        String mineType = trackFormat.getString(MediaFormat.KEY_MIME);
                        LogUtil.d("mineType","mineType:"+mineType);
                        //????????????
                        if (mineType.startsWith("audio")) {
                            audioTrackIndex = i;
                        }
                    }
                    //?????????????????????
                    mediaExtractor.selectTrack(audioTrackIndex);

                    ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
                    while (true) {
                        int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);

                        if (readSampleCount < 0) {
                            sendHandleMessage(mediaExtractor.getSampleTime()/1000,CLOSE_PROGRCESS);
                            break;
                        }
                        sendHandleMessage(mediaExtractor.getSampleTime()/1000,UPDATE_PROGRCESS);

                        //??????????????????
                        byte[] buffer = new byte[readSampleCount];
                        byteBuffer.get(buffer);
                        /************************* ?????????aac??????adts???**************************/
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
    private void extractVideo(final VideoModel model){
        totalValue = model.getDuation();
        File mDir = new File(MainApplication.VideoFileDir);
        if (!mDir.exists()) {
            mDir.mkdirs();
        }
        ThreadUtil.INST.excute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void run() {
                FileOutputStream videoOutputStream = null;
                MediaExtractor mediaExtractor = new MediaExtractor();//????????????????????????
                sendHandleMessage(0,SHOW_PROGRCESS);//???????????????????????????
                File videoFile = new File(MainApplication.VideoFileDir, TimeFormat.getCurrentTime()+".h264");//
                try {
                    videoOutputStream = new FileOutputStream(videoFile);
                    mediaExtractor.setDataSource(model.getUrl());
                    int videoTrackIndex = -1;
                    int trackCount = mediaExtractor.getTrackCount();

                    for (int i = 0; i < trackCount; i++) {
                        MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                        String mineType = trackFormat.getString(MediaFormat.KEY_MIME);
                        //????????????
                        if (mineType.startsWith("video")) {
                            videoTrackIndex = i;
                        }
                    }
                    if (videoTrackIndex == -1)
                        return;
                    mediaExtractor.selectTrack(videoTrackIndex);

                    ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
                    while (true) {
                        int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);
                        Log.d(TAG, "video:readSampleCount:" + readSampleCount);
                        if (readSampleCount < 0) {
                            sendHandleMessage(mediaExtractor.getSampleTime()/1000,CLOSE_PROGRCESS);
                            break;
                        }
                        sendHandleMessage(mediaExtractor.getSampleTime()/1000,UPDATE_PROGRCESS);
                        //????????????????????????
                        byte[] buffer = new byte[readSampleCount];
                        byteBuffer.get(buffer);
                        videoOutputStream.write(buffer);//buffer ????????? videooutputstream???
                        byteBuffer.clear();
                        mediaExtractor.advance();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    mediaExtractor.release();
                    mediaExtractor = null;
                    try {
                        videoOutputStream.flush();
                        videoOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendHandleMessage(long object,int what){
        Message msg=new Message();
        msg.obj= object;//message?????????
        msg.what=what;//??????message
        handler.sendMessage(msg);//handler??????message
    }

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
    private void openAudioFile(){
        File file = new File(Environment.getExternalStorageDirectory()+"/Extractor/Audio/");//mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //7.0??????????????????????????????FileProvider??????????????????https://blog.csdn.net/growing_tree/article/details/71190741
        Uri uri = FileProvider.getUriForFile(mContext,mContext.getPackageName(),file);
        intent.setData(uri);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,200);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}