package com.damon.videocompress.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.damon.videocompress.R;
import com.damon.videocompress.fragments.ListImagesFragment;
import com.damon.videocompress.fragments.ListVideoFragment;

public class TabListImageVideo extends FragmentPagerAdapter {

    Context context;
    public TabListImageVideo(@NonNull FragmentManager fm,Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ListVideoFragment videoFragment = new ListVideoFragment();
                return videoFragment;
            case 1:
                ListImagesFragment listImagesFragment = new ListImagesFragment();
                return  listImagesFragment;


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
                return context.getString(R.string.videos_comprimidos);

            case 1:
                return context.getString(R.string.images_comrpimidos);

            default:
                return null;
        }
    }
}
