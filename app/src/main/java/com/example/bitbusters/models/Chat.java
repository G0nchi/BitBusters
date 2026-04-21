package com.example.bitbusters.models;

public class Chat {
    private String id;
    private String name;
    private String lastMessage;
    private String time;
    private String initials;
    private String colorHex;
    private int unreadCount;
    private boolean isRecent;

    public Chat(String id, String name, String lastMessage, String time, String initials, String colorHex, int unreadCount, boolean isRecent) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.initials = initials;
        this.colorHex = colorHex;
        this.unreadCount = unreadCount;
        this.isRecent = isRecent;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public String getInitials() { return initials; }
    public String getColorHex() { return colorHex; }
    public int getUnreadCount() { return unreadCount; }
    public boolean isRecent() { return isRecent; }
}
