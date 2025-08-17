package com.example.safehaven;

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

public class NewsViewAdapter extends RecyclerView.Adapter<NewsViewAdapter.NewsViewHolder> {

    private Context context;
    private List<News> newsList;

    public NewsViewAdapter(Context context, List<News> newsList) {
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

        holder.newsTitle.setText(news.getTitle());

        // Truncate body to 100 chars
        String body = news.getNewsBody();
        if (body != null && body.length() > 100) {
            holder.newsBody.setText(body.substring(0, 100) + "...");
        } else {
            holder.newsBody.setText(body);
        }

        holder.newsVideoLink.setText(news.getVideoLink());

        Glide.with(context)
                .load(news.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .into(holder.newsImage);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsTitle, newsBody, newsVideoLink;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsBody = itemView.findViewById(R.id.newsBody);
            newsVideoLink = itemView.findViewById(R.id.newsVideoLink);
        }
    }
}
