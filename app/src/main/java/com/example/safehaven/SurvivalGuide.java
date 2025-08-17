package com.example.safehaven;

public class SurvivalGuide {
    public String beforePdfUrl;
    public String afterPdfUrl;
    public String youtubeLink;

    public SurvivalGuide() {} // Needed for Firebase

    public SurvivalGuide(String beforePdfUrl, String afterPdfUrl, String youtubeLink) {
        this.beforePdfUrl = beforePdfUrl;
        this.afterPdfUrl = afterPdfUrl;
        this.youtubeLink = youtubeLink;
    }
}
