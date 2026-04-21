package com.example.bitbusters.models;

public class Notification {
    private String id;
    private String name;
    private String message;
    private String time;
    private int avatarResId;
    private int propertyResId;
    private boolean isOld;

    public Notification(String id, String name, String message, String time, int avatarResId, int propertyResId, boolean isOld) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.time = time;
        this.avatarResId = avatarResId;
        this.propertyResId = propertyResId;
        this.isOld = isOld;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public int getAvatarResId() { return avatarResId; }
    public int getPropertyResId() { return propertyResId; }
    public boolean isOld() { return isOld; }
}
