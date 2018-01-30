package com.erick.multimediademo.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.erick.multimediademo.R;
import com.erick.multimediademo.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2018/1/30 0030.
 */

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameViewHolder> {
    private List<String> mList;

    @Override
    public FrameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frame_item,null);
        return new FrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FrameViewHolder holder, int position) {
        String path = mList.get(position);
        File file = new File(path);

        holder.imageView.setImageResource(R.mipmap.ic_launcher);
        holder.framName.setText("");
        if (file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            holder.imageView.setImageBitmap(bitmap);

            holder.framName.setText(FileUtil.getFileNameFromPath(path));
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void setList(List<String> pathList) {
        mList = pathList;
        notifyDataSetChanged();
    }

    public class FrameViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView framName;
        public FrameViewHolder(View v) {
            super(v);

            imageView = (ImageView) v.findViewById(R.id.img_frame);
            framName = (TextView) v.findViewById(R.id.frame_name);
        }
    }
}
