package com.example.safehaven;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<News> newsList;

    public NewsAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);

        holder.newsBody.setText(news.getNewsBody());
        holder.newsVideoLink.setText(news.getVideoLink() != null ? news.getVideoLink() : "No video");

        Glide.with(context).load(news.getImageUrl()).into(holder.newsImage);

        holder.newsVideoLink.setOnClickListener(v -> {
            if (news.getVideoLink() != null && !news.getVideoLink().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getVideoLink()));
                context.startActivity(browserIntent);
            }
        });

        holder.btnDelete.setOnClickListener(v -> deleteNews(news));

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, ManageLatestNews.class);
            intent.putExtra("newsId", news.getId());
            context.startActivity(intent);
        });
    }

    private void deleteNews(News news) {
        new AlertDialog.Builder(context)
                .setTitle("Delete News")
                .setMessage("Are you sure you want to delete this news?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("LatestNews").child(news.getId());
                    dbRef.removeValue();

                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(news.getImageUrl());
                    storageRef.delete();

                    Toast.makeText(context, "News deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsBody, newsVideoLink;
        Button btnEdit, btnDelete;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsBody = itemView.findViewById(R.id.newsBody);
            newsVideoLink = itemView.findViewById(R.id.newsVideoLink);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
