package com.damon.videocompress.fragments;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.damon.videocompress.R;
import com.damon.videocompress.activitys.CompressMultiImg;
import com.damon.videocompress.adapters.ImageAdapter;
import com.damon.videocompress.interfaces.OnClickListener;
import com.damon.videocompress.models.ModelImages;
import com.damon.videocompress.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ImagesListFragment extends Fragment {


    private RecyclerView recyclerView;

    private ArrayList<ModelImages> videosList = new ArrayList<ModelImages>();
    private ImageAdapter adapterVideoList;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback;
    private Toolbar toolbar;



    SwipeRefreshLayout swipeRefreshLayout;

    public ImagesListFragment() {
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
        return inflater.inflate(R.layout.fragment_images_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        recyclerView = view.findViewById(R.id.recyclerImages);
        recyclerView.setHasFixedSize(true);
       // recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL,false));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(),2);
        recyclerView.setLayoutManager(gridLayoutManager); //3 = column count
        adapterVideoList = new ImageAdapter(videosList,view.getContext());
        recyclerView.setAdapter(adapterVideoList);
        swipeRefreshLayout = view.findViewById(R.id.reflesh);
        checkPermissions(view);

        initToolbar(view);
        actionModeCallback = new ActionModeCallback();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                   getActivity().recreate();
                    // view.getContext().startActivity(new Intent(view.getContext(),MainActivity.class));
                }catch (Exception e){
                    swipeRefreshLayout.setRefreshing(false);
                }

                //loadVideos();

            }
        });
        adapterVideoList.setOnClickListener(new OnClickListener() {
            @Override
            public void onItemClick(View view, ModelImages obj, int pos) {
                if (adapterVideoList.getSelectedItemCount() >0){
                    enableActionMode(pos);
                }else {
                    ModelImages modelImages = adapterVideoList.getItem(pos);
                    System.out.println(modelImages);
                }
            }

            @Override
            public void onItemLongClick(View view, ModelImages obj, int pos) {
                enableActionMode(pos);
            }
        });
    }


    private void enableActionMode(int postion){
        if (actionMode == null){
          actionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(actionModeCallback);
        }

        toggleSelection(postion);
    }

    private void toggleSelection(int position){
        adapterVideoList.toggleSelection(position);
        int count = adapterVideoList.getSelectedItemCount();
        if (count ==0){
            actionMode.finish();
        }else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }


    private void checkPermissions(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            } else {
                loadVideos();
            }
        } else {
            loadVideos();
        }
    }


    private void loadVideos() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE,MediaStore.Images.Media.HEIGHT,MediaStore.Images.Media.WIDTH};
                String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

                Cursor cursor = getActivity().getApplication().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                    int whithColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH);
                    int heighColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        String title = cursor.getString(titleColumn);
                        int duration = cursor.getInt(durationColumn);
                        int withcolum = cursor.getInt(whithColumn);
                        int heighcolum = cursor.getInt(heighColumn);
                        String  resolucion = heighcolum + " x " + withcolum ;

                        float valor  =cursor.getLong(durationColumn);
                        int s = Math.round(valor);
                        int mg = s / (1024 * 1024);
                        int kb = s / 1024;

                        Uri data = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                        String duration_formatted;
                        int sec = (duration / 1000) % 60;
                        int min = (duration / (1000 * 60)) % 60;
                        int hrs = duration / (1000 * 60 * 60);

                        if (hrs == 0) {
                            duration_formatted = String.valueOf(min).concat(":".concat(String.format(Locale.UK, "%02d", sec)));
                        } else {
                            duration_formatted = String.valueOf(hrs).concat(":".concat(String.format(Locale.UK, "%02d", min).concat(":".concat(String.format(Locale.UK, "%02d", sec)))));
                        }

                        videosList.add(new ModelImages(id, data, title, String.valueOf(s),resolucion));
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapterVideoList.notifyItemInserted(videosList.size() - 1);
//                              //  adapterVideoList.notifyItemRangeChanged(0,videosList.size());
//                            }
//                        });
                    }
                }

            }
        }.start();
    }

    private class ActionModeCallback implements ActionMode.Callback{
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Tools.setSystemBarColor(getActivity(),R.color.colorDarkBlue2);
            mode.getMenuInflater().inflate(R.menu.menu_delete,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete){
                deleteInboxes();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapterVideoList.clearSelections();
            actionMode = null;
            Tools.setSystemBarColor(getActivity(),R.color.colorPrimary);
        }
    }

    private void deleteInboxes(){
       List<Integer> selectedItemPostions = adapterVideoList.getSelectedItems();
//        for (int i = selectedItemPostions.size()-1; i >=0 ; i--){
//            adapterVideoList.removeData(selectedItemPostions.get(i));
//        }
//        adapterVideoList.notifyDataSetChanged();
        ArrayList<String> data = new ArrayList<>();
        for (int i = selectedItemPostions.size()-1; i >=0 ; i--){
            data.add(adapterVideoList.datas(selectedItemPostions.get(i)));
        }
        Intent intent = new Intent(getActivity(), CompressMultiImg.class);
        intent.putStringArrayListExtra("data",data);
        getActivity().startActivity(intent);
    }


    private void initToolbar(View v){
        toolbar = (Toolbar)v.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Selecciona Imagenes");
        Tools.setSystemBarColor(getActivity(),R.color.grey);
    }
}