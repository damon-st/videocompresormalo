package com.damon.videocompress.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.damon.videocompress.R;
import com.damon.videocompress.utils.TimeAgo;
import com.damon.videocompress.activitys.CompressVideoActivity;
import com.damon.videocompress.activitys.VideoCompartir;

import java.io.File;
import java.util.List;

public class FileAdapters extends RecyclerView.Adapter<FileAdapters.FilesViewHolder>{

    private Activity context;
    private List<File> fileList;

    TimeAgo timeAgo;

    public FileAdapters(Activity context, List<File> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_file_item,parent,false);
        int height = parent.getMeasuredHeight()/2;
        view.setMinimumHeight(height);
        timeAgo = new TimeAgo();
        return new FilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesViewHolder holder, final int position) {
//        Glide.with(context).load(fileList.get(position).getPath())
//                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(holder.imagen_file);
        Glide.with(context).load(fileList.get(position).getPath()).into(holder.imagen_file);

        holder.titulo_file.setText(fileList.get(position).getName());

        String data = timeAgo.getTimeAgo(fileList.get(position).lastModified());
        holder.modifique_date.setText("Añadido "+data);

        long size = fileList.get(position).length();
        int valor = Math.round(size);
        int mb = valor / (1024 * 1024);
        int kb = valor / 1024;

        if (kb > 1000){
            holder.duration_or_size.setText(mb + "MB");
        }else {
            holder.duration_or_size.setText(kb + "KB");
        }


        holder.linear_shared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    shared(Uri.fromFile(fileList.get(position)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.ic_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = fileList.get(position).getPath();
                Intent intent = new Intent(context, VideoCompartir.class);
                intent.putExtra("id",path);
                context.startActivity(intent);
            }
        });
        holder.linear_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int postion = position;
                deleteFile(position);
            }
        });

        holder.linear_compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendCompres(Uri.fromFile(fileList.get(position)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void sendCompres(Uri  fromFile)throws Exception  {
        String  s = fromFile.getPath();
        Intent intent = new Intent(context, CompressVideoActivity.class);
        intent.putExtra("videoId",s);
        intent.putExtra("caso","si");
        context.startActivity(intent);
    }

    private void deleteFile(int position) {
        fileList.get(position).delete();
        fileList.remove(position);
        notifyDataSetChanged();
    }


    private void shared(Uri path) throws Exception{
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(path.getPath()));
        context.startActivity(Intent.createChooser(intent,"Compartir via"));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class FilesViewHolder extends RecyclerView.ViewHolder{

        public ImageView imagen_file,ic_play;
        public TextView titulo_file,modifique_date;
        public TextView duration_or_size;
        public LinearLayout linear_shared,linear_delete,linear_compress;


        public FilesViewHolder(@NonNull View itemView) {
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
