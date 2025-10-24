package com.example.laundryapp;

public class Machine {
    public final String title;
    public final String subtitle;
    public final int iconRes; // 0 if none

    public Machine(String title, String subtitle, int iconRes) {
        this.title = title;
        this.subtitle = subtitle;
        this.iconRes = iconRes;
    }
}
