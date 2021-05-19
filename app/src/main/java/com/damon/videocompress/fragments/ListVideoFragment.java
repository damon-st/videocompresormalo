package com.damon.videocompress.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.damon.videocompress.R;
import com.damon.videocompress.adapters.FileAdapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListVideoFragment extends Fragment {


    private List<File> fileList;
    private RecyclerView recyclerView;

    public ListVideoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_list_video);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(),2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        fileList = loadFiles();
        FileAdapters fileAdapters = new FileAdapters(getActivity(),fileList);
        recyclerView.setAdapter(fileAdapters);

    }

    private List<File> loadFiles(){
        ArrayList<File> inFiles = new ArrayList<>();
        File paremDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            paremDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+File.separator+getString(R.string.app_name));
        }else {
            paremDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+File.separator+getString(R.string.app_name));
        }

        File[] files = paremDir.listFiles();
        if (files != null){
            for (File file: files){
                if (file.getName().endsWith(".mp4")){
                    inFiles.add(file);
                }

            }
        }

//        try {
//            if (files !=null && files.length >0 ) {
//                textView.setVisibility(View.GONE);
//            }else {
//                textView.setVisibility(View.VISIBLE);
//
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        return inFiles;
    }
}