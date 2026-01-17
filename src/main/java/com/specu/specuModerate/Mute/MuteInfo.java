package com.specu.specuModerate.Mute;

import java.time.LocalDateTime;

public class MuteInfo {
    private String name;
    private String admin;
    private String reason;
    private LocalDateTime date;

    public MuteInfo(String name, String admin, String reason, LocalDateTime date) {
        this.name = name;
        this.admin = admin;
        this.reason = reason;
        this.date = date;
    }

    public String getAdmin() { return admin; }

    public String getReason() { return reason;}

    public LocalDateTime getDate() {
        return date;
    }
}
