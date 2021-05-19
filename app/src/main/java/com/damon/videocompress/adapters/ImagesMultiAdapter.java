package com.damon.videocompress.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.damon.videocompress.R;
import com.damon.videocompress.activitys.ImageViewActivity;

import java.util.List;

public class ImagesMultiAdapter extends RecyclerView.Adapter<ImagesMultiAdapter.ImageViewHolder>  {

    private Activity activity;
    private List<Uri> uriList;

    public ImagesMultiAdapter(Activity activity, List<Uri> uriList) {
        this.activity = activity;
        this.uriList = uriList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(activity).inflate(R.layout.list_file_item,parent,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.ic_play.setVisibility(View.GONE);
        holder.titulo_file.setVisibility(View.GONE);
        holder.duration_or_size.setVisibility(View.GONE);
        holder.modifique_date.setVisibility(View.GONE);
        holder.linear_compress.setVisibility(View.GONE);
        holder.linear_delete.setVisibility(View.GONE);
        Glide.with(activity).load(uriList.get(position).getPath()).into(holder.imagen_file);
        holder.linear_shared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compartir(uriList.get(position));
            }
        });

        holder.imagen_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verImage(uriList.get(position).getPath());
            }
        });
    }

    private void verImage(String path) {
        Intent intent = new Intent(activity, ImageViewActivity.class);
        intent.putExtra("url",path);
        activity.startActivity(intent);
    }

    private void compartir(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(uri.getPath()));
        activity.startActivity(Intent.createChooser(intent,"Compartir via"));
    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView imagen_file,ic_play;
        public TextView titulo_file,modifique_date;
        public TextView duration_or_size;
        public LinearLayout linear_shared,linear_delete,linear_compress;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            duration_or_size = itemView.findViewById(R.id.tv_duration);
            titulo_file = itemView.findViewById(R.id.tv_title);
            imagen_file = itemView.findViewById(R.id.imageView_thumbnail);
            linear_shared = itemView.findViewById(R.id.linear_shared);
            linear_delete = itemView.findViewById(R.id.linear_delete);
            linear_compress = itemView.findViewById(R.id.linear_compress);
            ic_play = itemView.findViewById(R.id.play_video);
            modifique_date = itemView.findViewById(R.id.modifique_date);
        }
    }
}
