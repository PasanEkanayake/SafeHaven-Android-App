package com.example.safehaven;

public class News {
    private String title, newsBody, imageUrl, videoLink;

    public News() {} // Empty constructor for Firebase

    public News(String title, String newsBody, String imageUrl, String videoLink) {
        this.title = title;
        this.newsBody = newsBody;
        this.imageUrl = imageUrl;
        this.videoLink = videoLink;
    }

    public String getTitle() { return title; }
    public String getNewsBody() { return newsBody; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoLink() { return videoLink; }
}
