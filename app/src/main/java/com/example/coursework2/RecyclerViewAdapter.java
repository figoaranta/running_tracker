package com.example.coursework2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "mobile";

    private List<Record> mRecord;
    private Context mContext;
    private onItemClickListener onItemClickListener;

    public RecyclerViewAdapter(Context mContext, List<Record> mRecord, onItemClickListener onItemClickListener) {
        this.mRecord = mRecord;
        this.mContext = mContext;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);
        ViewHolder holder = new ViewHolder(view,onItemClickListener);
        return holder;
    }

//    Reference: https://bumptech.github.io/glide/
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.id.setText("ID: "+String.valueOf(mRecord.get(position).getId()));
        holder.name.setText(mRecord.get(position).getName());
        holder.date.setText(mRecord.get(position).getDate());
        byte[] bitmap = mRecord.get(position).getImage();
        if(bitmap != null){
            Bitmap image = BitmapFactory.decodeByteArray(bitmap, 0 , bitmap.length);
            Glide.with(mContext).asBitmap().load(image).into(holder.image);
        }else{
            Glide.with(mContext).asBitmap().load(R.mipmap.ic_launcher).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        if(mRecord == null){
            return 0;
        }else{
            return mRecord.size();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Record> records){
        if(mRecord !=null){
            mRecord.clear();
            mRecord.addAll(records);
            notifyDataSetChanged();
        }else{
            mRecord = records;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name,date,id;
        onItemClickListener onItemClickListener;
        CircleImageView image;

        public ViewHolder(@NonNull View itemView, onItemClickListener onItemClickListener) {
            super(itemView);
            name = itemView.findViewById(R.id.image_name);
            image = itemView.findViewById(R.id.image);
            id = itemView.findViewById(R.id.id);
            date = itemView.findViewById(R.id.date);
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(getAdapterPosition());
        }
    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }
}
