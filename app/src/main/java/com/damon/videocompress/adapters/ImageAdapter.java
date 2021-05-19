package com.damon.videocompress.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.damon.videocompress.interfaces.OnClickListener;
import com.damon.videocompress.models.ModelImages;
import com.damon.videocompress.R;
import com.damon.videocompress.activitys.CompressImageActitivy;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {


    ArrayList<ModelImages> videosList = new ArrayList<ModelImages>();
    Context context;
    ArrayList<Long> d ;

    private SparseBooleanArray selected_items;
    private OnClickListener onClickListener = null;
    private int current_selected_idx = -1;


    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public ImageAdapter(ArrayList<ModelImages> videosList, Context context) {
        this.videosList = videosList;
        this.context = context;
        selected_items = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_image, parent, false);
        d= new ArrayList<>();
        int heigh = parent.getMeasuredHeight()/2;
        itemView.setMinimumHeight(heigh);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ModelImages item = videosList.get(position);
        holder.tv_title.setText(item.getTitle());
        int valor = Integer.parseInt(item.getDuration());
        int mg = valor / (1024 * 1024);
        int kb = valor / 1024;

        if (kb > 1000){
            holder.tv_duration.setText(mg + " MB");
        }else {
            holder.tv_duration.setText(kb + " KB");
        }
//        Glide.with(context).load(item.getData())
//                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(holder.imgView_thumbnail);

        holder.tv_display.setVisibility(View.VISIBLE);
        holder.tv_display.setText(item.getDisplay());

        Glide.with(context).load(item.getData()).into(holder.imgView_thumbnail);



        holder.lyt_parent.setActivated(selected_items.get(position,false));

        holder.imgView_thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CompressImageActitivy.class);
                intent.putExtra("caso",false);
                intent.putExtra("videoId", item.getId());
               // intent.putExtra("d",String.valueOf(item.getData()));
                v.getContext().startActivity(intent);
                System.out.println(item.getData());
            }
        });

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener ==null)return;
                onClickListener.onItemClick(v,item,position);
            }
        });

        holder.lyt_parent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onClickListener ==null) return false;
                onClickListener.onItemLongClick(v,item,position);
                return true;
            }
        });

        toggleCheckedIcon(holder,position);

    }



    private void toggleCheckedIcon(MyViewHolder holder,int position){
        if (selected_items.get(position,false)){
            holder.imgView_thumbnail.setVisibility(View.VISIBLE);
            holder.image_select.setVisibility(View.VISIBLE);
            if (current_selected_idx == position) resetCurrentIndex();
        }else {
            holder.image_select.setVisibility(View.GONE);
            holder.imgView_thumbnail.setVisibility(View.VISIBLE);
            if (current_selected_idx == position) resetCurrentIndex();
        }
    }


    public void dataID(long id){
        d.add(id);
    }

    public ArrayList<Long> data (){
        return d;
    }


    public void setImest(ArrayList<ModelImages> newvideosList){
        int cutretnSize = videosList.size();
        videosList.clear();
        videosList.addAll(newvideosList);
        notifyItemRangeRemoved(0,cutretnSize);
        notifyItemRangeInserted(0,newvideosList.size());
    }
    public void addDATA(ArrayList<ModelImages> newvideosList){
        int initialSize = videosList.size();
        videosList.addAll(newvideosList);
        notifyItemRangeInserted(initialSize, videosList.size()-1); //Correct position
    }

    public ModelImages getItem(int position){
        return videosList.get(position);
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

    public void clearSelections(){
        selected_items.clear();
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedItems(){
        List<Integer> items = new ArrayList<>(selected_items.size());
        for (int i =0; i<selected_items.size(); i++){
            items.add(selected_items.keyAt(i));
        }
        return items;
    }

    public void removeData(int position){
        videosList.remove(position);
        resetCurrentIndex();
    }

    public String  datas(int position){
       return String.valueOf(videosList.get(position).getId());
    }



    private void resetCurrentIndex(){
        current_selected_idx = -1;
    }

    public int getSelectedItemCount(){
        return selected_items.size();
    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgView_thumbnail,image_select;
        TextView tv_title, tv_duration,tv_display;
        CheckBox select_image_check_box;
        View lyt_parent;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_duration = itemView.findViewById(R.id.tv_duration);
            imgView_thumbnail = itemView.findViewById(R.id.imageView_thumbnail);
            tv_display = itemView.findViewById(R.id.display);
            select_image_check_box = itemView.findViewById(R.id.check_box);
            lyt_parent = (View) itemView.findViewById(R.id.lyt_parent);
            image_select = itemView.findViewById(R.id.imageView_select);
        }
    }
}
