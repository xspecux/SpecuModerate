package com.specu.specuModerate.Ban;

public class BanInfo {
    private final String name;
    private final String reason;
    private final String admin;
    private final boolean ipBanned;
    private final String date;
    private final String ip;
    private final String bandate;

    public BanInfo(String name, String reason, String admin, boolean ipBanned, String ip, String date, String bandate) {
        this.name = name;
        this.reason = reason;
        this.admin = admin;
        this.ipBanned = ipBanned;
        this.ip = ip;
        this.date = date;
        this.bandate = bandate;
    }

    public String getReason() {
        return reason;
    }

    public String getAdmin() {
        return admin;
    }

    public String getIp() {
        return ip;
    }

    public String getBandate() { return bandate; }
}
