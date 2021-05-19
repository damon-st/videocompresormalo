package com.damon.videocompress.adapters;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.damon.videocompress.R;
import com.damon.videocompress.interfaces.OnClickListenerFiles;
import com.damon.videocompress.utils.TimeAgo;
import com.damon.videocompress.activitys.CompressImageActitivy;
import com.damon.videocompress.activitys.ImageViewActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileImagesAdapter  extends RecyclerView.Adapter<FileImagesAdapter.FilesViewHolder> {

    List<File> fileList;
    Context activity;
    TimeAgo timeAgo;

    ArrayList<Long> d;
    private SparseBooleanArray selected_items;
    private OnClickListenerFiles onClickListener = null;
    private int current_selected_idx = -1;

    public void setOnClickListener(OnClickListenerFiles onClickListener) {
        this.onClickListener = onClickListener;
    }

    public FileImagesAdapter(List<File> fileList, Context activity) {
        this.fileList = fileList;
        this.activity = activity;
        this.selected_items = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.list_file_item,parent,false);
        int height = parent.getMeasuredHeight()/2;
        view.setMinimumHeight(height);
        timeAgo  =new TimeAgo();
        d = new ArrayList<>();
        return new FilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilesViewHolder holder, final int position) {
        holder.ic_play.setVisibility(View.GONE);

//        Glide.with(activity).load(fileList.get(position).getPath())
//                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(holder.imagen_file);
        Glide.with(activity).load(fileList.get(position).getPath()).into(holder.imagen_file);

        File file = fileList.get(position);

        String name = fileList.get(position).getName();
        holder.titulo_file.setText(name);

        String date = timeAgo.getTimeAgo(fileList.get(position).lastModified());
        holder.modifi_date.setText("Añadido "+ date);

        long size = fileList.get(position).length();
        int valor = Math.round(size);
        int mb = valor /(1024*1024);
        int kb = valor / 1024;

        if (kb > 1000){
            holder.duration_or_size.setText(mb + "MB");
        }else {
            holder.duration_or_size.setText(kb+  "KB");
        }

        holder.linear_file.setActivated(selected_items.get(position,false));

        holder.linear_shared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sharedImage(Uri.fromFile(fileList.get(position)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.linear_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    deleteFile(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.linear_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendCompress(Uri.fromFile(fileList.get(position)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.imagen_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentImageView(Uri.fromFile(fileList.get(position)));
            }
        });

        holder.linear_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener ==null) return;
                onClickListener.onItemClick(v,file,position);
            }
        });
        holder.linear_file.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onClickListener==null)return false;
                onClickListener.onItemLongClick(v,file,position);
                return true;
            }
        });

        toggleCheckedIcon(holder,position);
    }

    private void toggleCheckedIcon(FilesViewHolder holder, int position) {
        if (selected_items.get(position,false)){
            holder.selecte_image.setVisibility(View.VISIBLE);
            if (current_selected_idx == position) resetCurrentIndex();
        }else {
            holder.selecte_image.setVisibility(View.GONE);
            if (current_selected_idx ==position) resetCurrentIndex();
        }
    }

    private void resetCurrentIndex(){
        current_selected_idx = -1;
    }

    public void clearSelections(){
        selected_items.clear();
        notifyDataSetChanged();
    }

    public void toggleSelection(int pos){
        current_selected_idx = pos;
        if (selected_items.get(pos,false)){
            selected_items.delete(pos);
        }else {
            selected_items.put(pos,true);
        }
        notifyItemChanged(pos);
    }
    public int getSelectedItemCount(){
       return selected_items.size();
    }

    public List<Integer> getSelectedItems(){
        List<Integer> items = new ArrayList<>(selected_items.size());
        for (int i =0; i < selected_items.size(); i++){
            items.add(selected_items.keyAt(i));
        }
        return items;
    }

    public Uri path(int pos){
        return Uri.parse(fileList.get(pos).getPath());
    }

    private void sentImageView(Uri fromFile) {
        String  url = fromFile.getPath();
        Intent intent  = new Intent(activity, ImageViewActivity.class);
        intent.putExtra("url",url);
        activity.startActivity(intent);
    }

    private void sendCompress(Uri fromFile) throws Exception{
        String data = fromFile.getPath();
        Intent intent = new Intent(activity, CompressImageActitivy.class);
        intent.putExtra("caso",true);
        intent.putExtra("videoId",data);
        activity.startActivity(intent);

    }

    private void deleteFile(int position) throws Exception {
        fileList.get(position).delete();
        fileList.remove(position);
        notifyDataSetChanged();
    }


    private void sharedImage(Uri uri) throws Exception{
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(uri.getPath()));
        activity.startActivity(Intent.createChooser(intent,"Compartir Via"));
    }


    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class FilesViewHolder extends RecyclerView.ViewHolder{

        public ImageView imagen_file,ic_play,selecte_image;
        public TextView titulo_file,modifi_date;
        public TextView duration_or_size;
        public LinearLayout linear_shared,linear_delete,linear_compress,linear_file;


        public FilesViewHolder(@NonNull View itemView) {
            super(itemView);

            duration_or_size = itemView.findViewById(R.id.tv_duration);
            titulo_file = itemView.findViewById(R.id.tv_title);
            imagen_file = itemView.findViewById(R.id.imageView_thumbnail);
            linear_shared = itemView.findViewById(R.id.linear_shared);
            linear_delete = itemView.findViewById(R.id.linear_delete);
            linear_compress = itemView.findViewById(R.id.linear_compress);
            ic_play = itemView.findViewById(R.id.play_video);
            modifi_date = itemView.findViewById(R.id.modifique_date);
            linear_file = itemView.findViewById(R.id.linear_file_compress);
            selecte_image = itemView.findViewById(R.id.imageView_select);
        }
    }
}
