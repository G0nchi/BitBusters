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

    // Convenience constructor for simple test/sample notifications
    public Notification(String name, String message, String time) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.message = message;
        this.time = time;
        this.avatarResId = 0; // no avatar by default
        this.propertyResId = 0; // no property image by default
        this.isOld = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public int getAvatarResId() { return avatarResId; }
    public int getPropertyResId() { return propertyResId; }
    public boolean isOld() { return isOld; }
}
