package com.example.bitbusters.models;

public class AsesorChatItem {
    private final String id;
    private final String name;
    private final String lastMessage;
    private final String time;
    private final String initials;
    private final String colorHex;
    private final int    unreadCount;
    private final boolean isRecent;
    private final String proyecto;

    public AsesorChatItem(String id, String name, String lastMessage, String time,
                          String initials, String colorHex, int unreadCount,
                          boolean isRecent, String proyecto) {
        this.id          = id;
        this.name        = name;
        this.lastMessage = lastMessage;
        this.time        = time;
        this.initials    = initials;
        this.colorHex    = colorHex;
        this.unreadCount = unreadCount;
        this.isRecent    = isRecent;
        this.proyecto    = proyecto;
    }

    public String  getId()          { return id; }
    public String  getName()        { return name; }
    public String  getLastMessage() { return lastMessage; }
    public String  getTime()        { return time; }
    public String  getInitials()    { return initials; }
    public String  getColorHex()    { return colorHex; }
    public int     getUnreadCount() { return unreadCount; }
    public boolean isRecent()       { return isRecent; }
    public String  getProyecto()    { return proyecto; }
}
