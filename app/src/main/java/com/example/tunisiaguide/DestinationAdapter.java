package com.example.tunisiaguide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private Context context;
    private List<Destination> destinationList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Destination destination);
    }

    public DestinationAdapter(Context context, List<Destination> destinationList, OnItemClickListener listener) {
        this.context = context;
        this.destinationList = destinationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination destination = destinationList.get(position);
        holder.tvTitle.setText(destination.getTitle());
        holder.tvRegion.setText(destination.getRegion());
        holder.tvCategory.setText(destination.getCategory());
        holder.tvDescription.setText(destination.getDescription());

        Glide.with(context)
                .load(destination.getImageUrl())
                .placeholder(R.drawable.ic_launcher)
                .into(holder.ivDestination);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(destination);
            }
        });
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDestination;
        TextView tvTitle, tvRegion, tvCategory, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDestination = itemView.findViewById(R.id.ivDestination);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRegion = itemView.findViewById(R.id.tvRegion);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
