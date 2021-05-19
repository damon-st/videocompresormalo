package com.damon.videocompress.activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.damon.videocompress.R;
import com.damon.videocompress.utils.CheckPermissionUtils;

public class ExtraerAudioActiviy extends AppCompatActivity {

    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extraer_audio_activiy);
        path = getIntent().getExtras().getString("path");

    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckPermissionUtils.isHasPermissions(this);
    }
}