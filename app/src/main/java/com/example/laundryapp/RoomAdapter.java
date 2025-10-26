package com.example.laundryapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(RoomItem room);
    }

    private final List<RoomItem> rooms;
    private final OnRoomClickListener listener;

    public RoomAdapter(List<RoomItem> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_card, parent, false);
        return new RoomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomItem room = rooms.get(position);

        holder.roomTitle.setText(room.title);
        holder.roomSubtitle.setText(room.subtitle);

        // optional: custom icon per room later
        // holder.roomIcon.setImageResource(android.R.drawable.ic_menu_home);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null){
                listener.onRoomClick(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView roomIcon;
        TextView roomTitle;
        TextView roomSubtitle;
        ImageView roomArrow;
        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomIcon = itemView.findViewById(R.id.roomIcon);
            roomTitle = itemView.findViewById(R.id.roomTitle);
            roomSubtitle = itemView.findViewById(R.id.roomSubtitle);
            roomArrow = itemView.findViewById(R.id.roomArrow);
        }
    }
}

