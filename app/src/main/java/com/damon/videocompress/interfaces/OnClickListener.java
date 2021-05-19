package com.damon.videocompress.interfaces;

import android.view.View;

import com.damon.videocompress.models.ModelImages;

public interface OnClickListener {
    void onItemClick(View view, ModelImages obj, int pos);
    void onItemLongClick(View view, ModelImages obj, int pos);

}
