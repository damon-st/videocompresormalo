package com.damon.videocompress.interfaces;

import android.view.View;

import com.damon.videocompress.models.VideoModel;

public interface ItemOnClickListener {

    void OnClick(View view, Object object);
    void OnClickModel(View view, VideoModel model);

}
