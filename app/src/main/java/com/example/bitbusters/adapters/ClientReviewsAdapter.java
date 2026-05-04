package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.ClientReview;

import java.util.ArrayList;
import java.util.List;

public class ClientReviewsAdapter extends RecyclerView.Adapter<ClientReviewsAdapter.ViewHolder> {

    private final List<ClientReview> reviews = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClientReview review = reviews.get(position);
        holder.tvName.setText(review.getReviewerName());
        holder.tvStars.setText(buildStars(review.getRating()));
        holder.tvComment.setText(review.getComment());
        holder.tvTime.setText(review.getRelativeTime());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void submitList(@NonNull List<ClientReview> list) {
        reviews.clear();
        reviews.addAll(list);
        notifyDataSetChanged();
    }

    private String buildStars(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < rating ? "★" : "☆");
        }
        return sb.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvStars;
        private final TextView tvComment;
        private final TextView tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvReviewName);
            tvStars = itemView.findViewById(R.id.tvReviewStars);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            tvTime = itemView.findViewById(R.id.tvReviewTime);
        }
    }
}
