package com.damon.videocompress.adapters;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.damon.videocompress.R;
import com.damon.videocompress.fragments.VideoList;
import com.damon.videocompress.fragments.ImagesListFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {


    Context context;

    public TabsAccessorAdapter(@NonNull FragmentManager fm,Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                VideoList videoList = new VideoList();
                return videoList;
            case 1:
                ImagesListFragment imagesListFragment = new ImagesListFragment();
                return imagesListFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                String  video = context.getString(R.string.video);
                return video;
            case 1:
                String image  = context.getString(R.string.images);
                return image;

            default:
                return null;
        }
    }
}
