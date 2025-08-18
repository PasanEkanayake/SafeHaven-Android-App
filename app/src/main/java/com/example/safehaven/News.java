package com.example.safehaven;

public class News {
    private String title;
    private String newsBody;
    private String imageUrl;
    private String videoLink;

    public News() {
        // required empty constructor for Firebase
    }

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

    public void setTitle(String title) { this.title = title; }
    public void setNewsBody(String newsBody) { this.newsBody = newsBody; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setVideoLink(String videoLink) { this.videoLink = videoLink; }
}
