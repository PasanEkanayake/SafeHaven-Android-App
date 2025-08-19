package com.example.safehaven;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<News> newsList;
    private List<String> keys; // Firebase keys
    private DatabaseReference dbRef;

    public NewsAdapter(Context context, List<News> newsList, List<String> keys) {
        this.context = context;
        this.newsList = newsList;
        this.keys = keys;
        dbRef = FirebaseDatabase.getInstance().getReference("LatestNews");
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        String key = keys.get(position);


        holder.btnDelete.setOnClickListener(v -> {
            dbRef.child(key).removeValue()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "News deleted!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsTitle, newsBody, newsVideoLink;
        Button btnDelete;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsBody = itemView.findViewById(R.id.newsBody);
            newsVideoLink = itemView.findViewById(R.id.newsVideoLink);
            btnDelete = itemView.findViewById(R.id.btnDeleteNews);
        }
    }
}
