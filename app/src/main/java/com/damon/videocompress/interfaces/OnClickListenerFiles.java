package com.damon.videocompress.interfaces;

import android.view.View;

import java.io.File;


public interface OnClickListenerFiles {
    void onItemClick(View view, File obj, int pos);
    void onItemLongClick(View view, File obj, int pos);
}
