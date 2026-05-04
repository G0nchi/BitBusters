package com.example.bitbusters.models;

public class ClientAppointment {
    public static final String STATUS_PENDING = "Pendiente";
    public static final String STATUS_CONFIRMED = "Confirmada";
    public static final String STATUS_COMPLETED = "Realizada";
    public static final String STATUS_CANCELED = "Cancelada";
    public static final String STATUS_REVIEWED = "Valorada";

    private final String id;
    private final String projectName;
    private final String location;
    private final String date;
    private final String time;
    private final String advisorName;
    private final String advisorInitials;
    private final int advisorColor;
    private final String status;

    public ClientAppointment(
            String id,
            String projectName,
            String location,
            String date,
            String time,
            String advisorName,
            String advisorInitials,
            int advisorColor,
            String status
    ) {
        this.id = id;
        this.projectName = projectName;
        this.location = location;
        this.date = date;
        this.time = time;
        this.advisorName = advisorName;
        this.advisorInitials = advisorInitials;
        this.advisorColor = advisorColor;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getAdvisorName() {
        return advisorName;
    }

    public String getAdvisorInitials() {
        return advisorInitials;
    }

    public int getAdvisorColor() {
        return advisorColor;
    }

    public String getStatus() {
        return status;
    }
}
