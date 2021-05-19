package com.damon.videocompress.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.videocompress.R;
import com.damon.videocompress.activitys.CutAudioActivity;
import com.damon.videocompress.utils.TimeAgo;

import java.io.File;
import java.util.List;

public class AudioListAdapter  extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder>{


    private List<File> allFiles;
    private TimeAgo timeAgo;
    private onItemListClick onItemListClick;

    private Activity activity;

    public AudioListAdapter(List<File> allFiles, AudioListAdapter.onItemListClick onItemListClick, Activity activity) {
        this.allFiles = allFiles;
        this.onItemListClick = onItemListClick;
        this.activity = activity;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item,parent,false);
        timeAgo = new TimeAgo();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AudioViewHolder holder, final int position) {
        holder.list_title.setText(allFiles.get(position).getName());
        holder.list_date.setText("Añadido "+timeAgo.getTimeAgo(allFiles.get(position).lastModified()));

        long size = allFiles.get(position).length();
        int valor = Math.round(size);
        int mb = valor /(1024*1024);
        int kb = valor / 1024;

        if (kb > 1000){
            holder.size_audio.setText("Peso del Archivo "+mb + "MB");
        }else {
            holder.size_audio.setText("Peso del Archivo "+kb+  "KB");
        }


        holder.more_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(holder.itemView.getContext(),v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.delete_item:
                                deleteAudio(item.getOrder());
                                break;

                            case R.id.shared_audio:
                                sharedAudio(allFiles.get(position).getAbsolutePath());
                                break;
                            case R.id.cortar_audio:
                                sendEditAudio(allFiles.get(position).getPath());
                                break;
                        }
                        return false;
                    }
                });

                menu.inflate(R.menu.more_action);
                menu.show();
            }
        });
    }

    private void sendEditAudio(String path) {
        Intent intent  = new Intent(activity, CutAudioActivity.class);
        intent.putExtra("audiopath",path);
        activity.startActivity(intent);
    }


    private void sharedAudio(String absolutePath) {
        System.out.println(absolutePath);
//        try {
//            Uri uri = FileProvider.getUriForFile(activity, "com.example.android.fileprovider", new File(absolutePath));
//            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//            sharingIntent.setType("file_paths/xml");
//
//            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
//            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            Context context = activity;
//            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(sharingIntent, PackageManager.MATCH_DEFAULT_ONLY);
//            for (ResolveInfo resolveInfo : resInfoList) {
//                String packageName = resolveInfo.activityInfo.packageName;
//                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }
//            activity.startActivity(sharingIntent);
//        } catch (Exception e) {
//            Log.e("Error", e.getMessage());
//        }
        Intent compartirAudio = new Intent(android.content.Intent.ACTION_SEND);
        compartirAudio.setType("audio/*");
        compartirAudio.putExtra(Intent.EXTRA_STREAM,
                Uri.parse(absolutePath)); //En getPackageName() da error por no estar definido
        activity.startActivity(Intent.createChooser(compartirAudio, "Compartir vía"));
    }

    private void deleteAudio(int order) {
        allFiles.get(order).delete();
        allFiles.remove(order);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return allFiles.size();
    }


    public class AudioViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView list_image;
        private TextView list_title,list_date,size_audio;
        private ImageView more_action;
        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            list_image  = itemView.findViewById(R.id.list_image_view);
            list_title = itemView.findViewById(R.id.list_date);
            list_date = itemView.findViewById(R.id.list_title);
            more_action = itemView.findViewById(R.id.more_action);
            size_audio = itemView.findViewById(R.id.size_audio);
            list_image.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListClick.onClickListener(allFiles.get(getAdapterPosition()),getAdapterPosition());
        }
    }

    public interface onItemListClick{
        void onClickListener(File file, int position);
    }
}
