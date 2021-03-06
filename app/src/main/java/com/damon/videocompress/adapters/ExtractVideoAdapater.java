package com.damon.videocompress.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.videocompress.R;
import com.damon.videocompress.interfaces.ItemOnClickListener;
import com.damon.videocompress.models.ActionType;
import com.damon.videocompress.models.VideoModel;
import com.damon.videocompress.utils.CustomViewPager;
import com.damon.videocompress.utils.ExactorMediaUtils;
import com.damon.videocompress.utils.ImageUtils;
import com.damon.videocompress.utils.TimeFormat;

import java.util.ArrayList;
import java.util.List;

public class ExtractVideoAdapater extends RecyclerView.Adapter<ExtractVideoAdapater.ViewHolder> {

    private Context mContext;
    private List<VideoModel> mList = new ArrayList<>();
    private ItemOnClickListener clickListener;
    private ActionType actionType;

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public ExtractVideoAdapater(Context mContext, List<VideoModel> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }


    public List<VideoModel> getmList() {
        return mList;
    }

    public void setmList(List<VideoModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public ItemOnClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(ItemOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_adapater_layout, parent,
                false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.videoThumb.setImageBitmap(ImageUtils.getVideoThumbnail(mList.get(position).getUrl()));
        holder.duration.setText(TimeFormat.long2String(mList.get(position).getDuation())+"/"+ ExactorMediaUtils.getFormatSize(mList.get(position).getSize()));
        holder.videoname.setText(mList.get(position).getName());
        holder.itemView.setTag(position);
        holder.extractAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.OnClick(v, mList.get(position));
            }
        });
        holder.extractVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.OnClickModel(v, mList.get(position));
            }
        });
        holder.audioFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.OnClickModel(v, mList.get(position));
            }
        });
        holder.videoFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.OnClickModel(v, mList.get(position));
            }
        });
        if (actionType == ActionType.EXTRACT_TYPE){
            holder.actionbtn.setVisibility(View.VISIBLE);
            holder.actionbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.OnClickModel(v, mList.get(position));
//                    if (mList.get(position).isAction()){
//                        mList.get(position).setAction(false);
////                        holder.actionbtn.setBackground(mContext.getResources().getDrawable(R.drawable.icon_down));
////                        holder.videoActionrl.setVisibility(View.GONE);
//                    }else {
//                        mList.get(position).setAction(true);
////                        holder.videoActionrl.setVisibility(View.VISIBLE);
////                        holder.actionbtn.setBackground(mContext.getResources().getDrawable(R.drawable.icon_up));
//                    }
                }
            });
            holder.actiontv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.OnClickModel(v, mList.get(position));
                }
            });
            holder.extract_audio_ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.OnClickModel(v, mList.get(position));
                }
            });
        }else {
            holder.actionbtn.setVisibility(View.GONE);
            holder.actiontv.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.OnClickModel(v,mList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView videoThumb;
        private TextView videoname;
        private TextView duration;
        private ImageButton actionbtn;
        private CardView videoActionrl;
        private RelativeLayout extractAudio;
        private RelativeLayout extractVideo;
        private RelativeLayout audioFile;
        private RelativeLayout videoFile;
        private CustomViewPager actionPage;
        private TextView actiontv;
        private LinearLayout extract_audio_ll;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumb = itemView.findViewById(R.id.videoThumb);
            videoname  = itemView.findViewById(R.id.videoname);
            duration   = itemView.findViewById(R.id.videoduration);
            actionbtn  = itemView.findViewById(R.id.extract_audio_ib);
            actiontv   = itemView.findViewById(R.id.extract_audio_tv);
            videoActionrl = itemView.findViewById(R.id.video_aciton_ll);
            extractAudio = itemView.findViewById(R.id.extract_audio_rl);
            extract_audio_ll = itemView.findViewById(R.id.extract_audio_ll);
            extractVideo = itemView.findViewById(R.id.extract_video_rl);
            audioFile    = itemView.findViewById(R.id.extract_audiopath_rl);
            videoFile    = itemView.findViewById(R.id.extract_videopath_rl);
            actionPage   = itemView.findViewById(R.id.action_vp);

        }
    }
}
