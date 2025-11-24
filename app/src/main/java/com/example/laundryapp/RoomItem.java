package com.example.laundryapp;

public class RoomItem {
    public final String locationID;
    public final String roomId;    // e.g. "bb1_room_1" (used to query Firestore later)
    public final String title;     // e.g. "BB1 Room #1"
    public final String subtitle;  // e.g. "4 machines available"

    public RoomItem(String locationID, String roomId, String title, String subtitle) {
        this.locationID = locationID;
        this.roomId = roomId;
        this.title = title;
        this.subtitle = subtitle;
    }
}