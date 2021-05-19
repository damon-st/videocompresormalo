package com.damon.videocompress.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.videocompress.R;
import com.damon.videocompress.adapters.FileImagesAdapter;
import com.damon.videocompress.interfaces.OnClickListenerFiles;
import com.damon.videocompress.utils.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ListImagesFragment extends Fragment {


    private RecyclerView recyclerView;
    private List<File> fileList;


    private ActionMode actionMode;
    private ActionModeCallback  actionModeCallback;
    FileImagesAdapter adapter;
    public ListImagesFragment() {
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
        return inflater.inflate(R.layout.fragment_list_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView  =view.findViewById(R.id.recycer_list_images);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        actionModeCallback = new ActionModeCallback();
        fileList = getFiles();
         adapter = new FileImagesAdapter(fileList,getContext());
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(new OnClickListenerFiles() {
            @Override
            public void onItemClick(View view, File obj, int pos) {
                if (adapter.getSelectedItemCount() > 0)enableActionMode(pos);
            }

            @Override
            public void onItemLongClick(View view, File obj, int pos) {
                enableActionMode(pos);
            }
        });
    }

    private void enableActionMode(int pos) {
        if (actionMode ==null){
            actionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(actionModeCallback);
        }

        toggleSelection(pos);
    }

    private void toggleSelection(int pos) {
        adapter.toggleSelection(pos);
        int count = adapter.getSelectedItemCount();
        if (count ==0 ){
            actionMode.finish();
        }else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private List<File> getFiles() {
        ArrayList<File> infiles = new ArrayList<>();
        File dir;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            dir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)+File.separator+getString(R.string.app_name));
        }else {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+getString(R.string.app_name));
        }

        File[] files = dir.listFiles();
        if (files!=null){
            for (File file : files){
                if (file.getName().endsWith(".jpeg")){
                    infiles.add(file);
                }
            }
        }

        return infiles;
    }


    private class ActionModeCallback implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Tools.setSystemBarColor(getActivity(),R.color.colorDarkBlue2);
            mode.getMenuInflater().inflate(R.menu.menu_shared,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.shared_menu){
                //codigo aqui
                compartirMulti();
                mode.finish();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelections();
            actionMode = null;
            Tools.setSystemBarColor(getActivity(),R.color.colorPrimary);
        }
    }

    private void compartirMulti() {
        List<Integer> selectedItemsPost = adapter.getSelectedItems();
        ArrayList<Uri> data = new ArrayList<>();
        for (int i = selectedItemsPost.size()-1 ; i>=0; i--){
            data.add(adapter.path(selectedItemsPost.get(i)));
        }

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM,data);
        getActivity().startActivity(Intent.createChooser(intent,"Compartir Via"));
    }
}