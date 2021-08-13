package com.jason.capstone.Utility;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.jason.capstone.R;
import com.squareup.picasso.Picasso;

public class HistoryAdapter extends FirestoreRecyclerAdapter<HistoryModel, HistoryAdapter.HistoryHolder> {
    private HomeAdapter.OnItemClickListener listener;

    public HistoryAdapter(@NonNull FirestoreRecyclerOptions<HistoryModel> options) {
        super(options);
    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.swipe_item, parent, false);
        return new HistoryHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position, @NonNull HistoryModel model){
        holder.tvTitle.setText(model.getTitle());
        holder.tvDesc.setText(model.getDescription());
        holder.tvDate.setText(model.getDate());
        Picasso.get().load(model.getImageUri()).fit().into(holder.ivImage);
}


    public class HistoryHolder extends RecyclerView.ViewHolder{
        public TextView tvTitle;
        public TextView tvDesc;
        public TextView tvDate;
        public ImageView ivImage;

        public HistoryHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.swipe_title);
            tvDesc = itemView.findViewById(R.id.swipe_description);
            tvDate = itemView.findViewById(R.id.swipe_date);
            ivImage = itemView.findViewById(R.id.swipe_image);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null){
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }



    }

}
