package com.example.safehaven;

public class News {
    // Field names must match the keys in Firebase: title, imageUrl, newsBody, newsBodyUrl, videoLink
    public String title;
    public String imageUrl;
    public String newsBody;
    public String newsBodyUrl;
    public String videoLink;

    public News() { }

    public News(String title, String imageUrl, String newsBody, String newsBodyUrl, String videoLink) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.newsBody = newsBody;
        this.newsBodyUrl = newsBodyUrl;
        this.videoLink = videoLink;
    }

    public String getNewsTitle() { return title; }
    public void setNewsTitle(String title) { this.title = title; }

    public String getNewsImage() { return imageUrl; }
    public void setNewsImage(String imageUrl) { this.imageUrl = imageUrl; }

    public String getNewsBody() { return newsBody; }
    public void setNewsBody(String newsBody) { this.newsBody = newsBody; }

    public String getNewsBodyImage() { return newsBodyUrl; }
    public void setNewsBodyImage(String newsBodyUrl) { this.newsBodyUrl = newsBodyUrl; }

    public String getNewsVideoLink() { return videoLink; }
    public void setNewsVideoLink(String videoLink) { this.videoLink = videoLink; }
}