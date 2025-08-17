package com.example.safehaven;

public class News {
    private String id;
    private String imageUrl;
    private String newsBody;
    private String videoLink;

    public News() {}

    public News(String id, String imageUrl, String newsBody, String videoLink) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.newsBody = newsBody;
        this.videoLink = videoLink;
    }

    public String getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getNewsBody() { return newsBody; }
    public String getVideoLink() { return videoLink; }

    public void setId(String id) { this.id = id; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setNewsBody(String newsBody) { this.newsBody = newsBody; }
    public void setVideoLink(String videoLink) { this.videoLink = videoLink; }
}
