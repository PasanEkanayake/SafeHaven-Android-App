package com.example.safehaven;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class NewsViewAdapter extends RecyclerView.Adapter<NewsViewAdapter.NewsViewHolder> {

    private static final String TAG = "NewsViewAdapter";
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
        final News news = newsList.get(position);

        holder.newsTitle.setText(news.getTitle() != null ? news.getTitle() : "");

        String body = news.getNewsBody();
        if (body != null && body.length() > 100) {
            holder.newsBody.setText(body.substring(0, 100) + "...");
        } else {
            holder.newsBody.setText(body != null ? body : "");
        }

        String videoLink = news.getVideoLink();
        holder.newsVideoLink.setText(videoLink != null ? videoLink : "");

        // load image (handles http(s), Google Drive links and Firebase Storage paths / gs://)
        String rawImage = news.getImageUrl();
        loadImageInto(holder.newsImage, rawImage);

        holder.itemView.setOnClickListener(v -> showNewsDetailDialog(news));
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

    // ----------------------------
    // Image loader: supports direct URLs, Google Drive links, gs:// and storage paths
    // ----------------------------
    private void loadImageInto(final ImageView imageView, String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }

        // Convert Google Drive URLs to direct download if needed
        final String converted = convertGoogleDriveUrlIfNeeded(imageUrl.trim());
        Log.d(TAG, "loadImageInto - converted url: " + converted);

        // If a direct HTTP(S) url -> Glide directly
        if (converted.startsWith("http://") || converted.startsWith("https://")) {
            Glide.with(context)
                    .load(converted)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .into(imageView);
            return;
        }

        // Otherwise treat it as Firebase Storage reference (gs:// or path)
        final String refForLambda = converted; // final for listeners
        FirebaseStorage storage = FirebaseStorage.getInstance();

        try {
            final StorageReference storageRef;
            if (refForLambda.startsWith("gs://") || refForLambda.contains("firebasestorage.googleapis.com")) {
                // full gs:// or https firebase storage url
                storageRef = storage.getReferenceFromUrl(refForLambda);
            } else {
                // treat as path like "images/foo.jpg" or "/images/foo.jpg"
                String path = refForLambda.startsWith("/") ? refForLambda.substring(1) : refForLambda;
                storageRef = storage.getReference().child(path);
            }

            storageRef.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            try {
                                Glide.with(context)
                                        .load(uri)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .placeholder(android.R.drawable.ic_menu_report_image)
                                        .into(imageView);
                                Log.d(TAG, "Loaded image from Storage download URL: " + uri);
                            } catch (Exception e) {
                                Log.w(TAG, "Glide failed to load after getDownloadUrl()", e);
                                imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "getDownloadUrl() failed for storageRef (" + refForLambda + ")", e);
                            // final fallback: try Glide with the raw string as Uri.parse
                            try {
                                Uri maybe = Uri.parse(refForLambda);
                                Glide.with(context)
                                        .load(maybe)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .placeholder(android.R.drawable.ic_menu_report_image)
                                        .into(imageView);
                            } catch (Exception ex) {
                                Log.e(TAG, "Fallback parse/load failed for: " + refForLambda, ex);
                                imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception while building StorageReference for: " + refForLambda, e);
            // fallback - try Glide with Uri.parse
            try {
                Uri maybe = Uri.parse(refForLambda);
                Glide.with(context)
                        .load(maybe)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .into(imageView);
            } catch (Exception ex) {
                Log.e(TAG, "Final fallback failed for: " + refForLambda, ex);
                imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }
    }

    // Convert common Google Drive share links to direct download links (same logic as DisasterFloods)
    private String convertGoogleDriveUrlIfNeeded(String url) {
        if (url == null) return null;

        if (url.contains("drive.google.com/uc") || url.contains("googleusercontent.com")) {
            return url;
        }

        try {
            if (url.contains("drive.google.com") && url.contains("/file/d/")) {
                int start = url.indexOf("/file/d/") + "/file/d/".length();
                int end = url.indexOf("/", start);
                if (end == -1) end = url.length();
                String fileId = url.substring(start, end);
                if (!fileId.isEmpty()) {
                    return "https://drive.google.com/uc?export=download&id=" + fileId;
                }
            }

            if (url.contains("drive.google.com") && url.contains("open?id=")) {
                int start = url.indexOf("open?id=") + "open?id=".length();
                int end = url.indexOf("&", start);
                if (end == -1) end = url.length();
                String fileId = url.substring(start, end);
                if (!fileId.isEmpty()) {
                    return "https://drive.google.com/uc?export=download&id=" + fileId;
                }
            }

            return url;
        } catch (Exception e) {
            Log.w(TAG, "Failed to convert Drive URL, returning original", e);
            return url;
        }
    }

    // Show AlertDialog with full article (uses same image loader)
    private void showNewsDetailDialog(final News news) {
        if (context == null) return;

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_news_detail, null);

        final ImageView dialogImage = dialogView.findViewById(R.id.dialogNewsImage);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogNewsTitle);
        TextView dialogBody = dialogView.findViewById(R.id.dialogNewsBody);
        TextView dialogVideoLink = dialogView.findViewById(R.id.dialogNewsVideoLink);
        Button btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        dialogTitle.setText(news.getTitle() != null ? news.getTitle() : "");
        dialogBody.setText(news.getNewsBody() != null ? news.getNewsBody() : "");

        final String videoLink = news.getVideoLink();
        if (!TextUtils.isEmpty(videoLink)) {
            dialogVideoLink.setText(videoLink);
            dialogVideoLink.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoLink));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            dialogVideoLink.setText("No video link");
            dialogVideoLink.setOnClickListener(null);
        }

        // load image for dialog
        loadImageInto(dialogImage, news.getImageUrl());

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        try {
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Unable to show dialog", e);
            Toast.makeText(context, "Unable to show dialog", Toast.LENGTH_SHORT).show();
        }
    }
}
