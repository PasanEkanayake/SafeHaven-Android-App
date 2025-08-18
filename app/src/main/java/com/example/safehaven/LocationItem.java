package com.example.safehaven;

public class LocationItem {
    public String id;
    public String name;
    public double lat;
    public double lng;
    public String category;
    public String address;
    public String phone;
    public String notes;
    public String createdBy;
    public long timestamp;

    public LocationItem() { } // required

    public LocationItem(String id, String name, double lat, double lng,
                        String category, String address, String phone,
                        String notes, String createdBy, long timestamp) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.category = category;
        this.address = address;
        this.phone = phone;
        this.notes = notes;
        this.createdBy = createdBy;
        this.timestamp = timestamp;
    }

}
