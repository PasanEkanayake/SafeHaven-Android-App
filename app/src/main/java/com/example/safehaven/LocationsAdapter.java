package com.example.safehaven;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {
    private List<LocationModel> locations;

    public LocationsAdapter(List<LocationModel> locations) {
        this.locations = locations;
    }

    public void updateList(List<LocationModel> newList) {
        this.locations = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationModel loc = locations.get(position);
        holder.name.setText(loc.getName());
        holder.type.setText(loc.getType());
        holder.phone.setText("Phone: " + loc.getPhone());
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, phone;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvLocationName);
            type = itemView.findViewById(R.id.tvLocationType);
            phone = itemView.findViewById(R.id.tvLocationPhone);
        }
    }
}