package com.example.safehaven;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
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

    private static final String TAG = "NewsViewAdapter";
    private Context context;
    private List<News> newsList;

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_IMAGE = "imageUrl";
    public static final String EXTRA_NEWS_BODY = "newsBody";
    public static final String EXTRA_BODY_IMAGE = "newsBodyUrl";
    public static final String EXTRA_VIDEO = "videoLink";

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
        if (news == null) return;

        holder.newsTitle.setText(news.getNewsTitle() != null ? news.getNewsTitle() : "");

        String body = news.getNewsBody();
        if (!TextUtils.isEmpty(body)) {
            String truncated = body.length() > 120 ? body.substring(0, 120).trim() + "..." : body;
            holder.newsSummary.setText(truncated);
            holder.newsSummary.setVisibility(View.VISIBLE);
        } else {
            holder.newsSummary.setVisibility(View.GONE);
        }

        String imageUrl = news.getNewsImage();
        Log.d(TAG, "onBindViewHolder position=" + position + " imageUrl=" + imageUrl);
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(context).load(imageUrl).centerCrop().into(holder.newsImage);
        } else {
            holder.newsImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent detailIntent = new Intent(context, NewsDetail.class);
            detailIntent.putExtra(EXTRA_TITLE, news.getNewsTitle());
            detailIntent.putExtra(EXTRA_IMAGE, news.getNewsImage());
            detailIntent.putExtra(EXTRA_NEWS_BODY, news.getNewsBody());
            detailIntent.putExtra(EXTRA_BODY_IMAGE, news.getNewsBodyImage());
            detailIntent.putExtra(EXTRA_VIDEO, news.getNewsVideoLink());
            context.startActivity(detailIntent);
        });
    }

    @Override
    public int getItemCount() { return newsList == null ? 0 : newsList.size(); }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsTitle, newsSummary;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsSummary = itemView.findViewById(R.id.newsSummary);
        }
    }
}
