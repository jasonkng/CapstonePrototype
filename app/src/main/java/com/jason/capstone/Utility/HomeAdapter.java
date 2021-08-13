package com.jason.capstone.Utility;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jason.capstone.R;

public class HomeAdapter extends FirestoreRecyclerAdapter<HomeModel, HomeAdapter.HomeHolder>{
    private OnItemClickListener listener;

    public HomeAdapter(@NonNull FirestoreRecyclerOptions<HomeModel> options){
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull HomeHolder holder, int position, @NonNull HomeModel model) {
        holder.textViewComponent.setText(model.getComponent());
        holder.textViewDefect.setText(model.getDefect());
        holder.textViewLength.setText(model.getLength());
        holder.textViewDate.setText(model.getDate());
        holder.textViewPriority.setText(model.getPriority());

        setTextColor(holder);
    }

    private void setTextColor(HomeHolder holder) {
        String textPriority = holder.textViewPriority.getText().toString();
        switch (textPriority){
            case "HIGH":
                holder.textViewPriority.setTextColor(Color.parseColor("#FF0000"));
                break;
            case "LOW":
                holder.textViewPriority.setTextColor(Color.parseColor("#008000"));
                break;
        }
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        return new HomeHolder(v);
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    class HomeHolder extends RecyclerView.ViewHolder {

        TextView textViewComponent;
        TextView textViewDefect;
        TextView textViewLength;
        TextView textViewDate;
        TextView textViewPriority;

        public HomeHolder(@NonNull View itemView) {
            super(itemView);
            textViewComponent = itemView.findViewById(R.id.list_component);
            textViewDefect = itemView.findViewById(R.id.list_defect);
            textViewLength = itemView.findViewById(R.id.list_length);
            textViewDate = itemView.findViewById(R.id.list_date);
            textViewPriority = itemView.findViewById(R.id.list_priority);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

        }


    }
}
