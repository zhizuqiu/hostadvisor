package com.unicom.chanyi.advisor;

public class Args {
    private String ip;
    private String snmpPort;
    private int influxdbInterval;
    private String InfluxdbUrl;
    private String InfludbUser;
    private String Influxdbpass;
    private String InfluxdbDbname;
    private String policy;
    private String duration;
    private String replication;
    private String clear;

    @Override
    public String toString() {
        return "Args{" +
                "ip='" + ip + '\'' +
                ", snmpPort='" + snmpPort + '\'' +
                ", influxdbInterval=" + influxdbInterval +
                ", InfluxdbUrl='" + InfluxdbUrl + '\'' +
                ", InfludbUser='" + InfludbUser + '\'' +
                ", Influxdbpass='" + Influxdbpass + '\'' +
                ", InfluxdbDbname='" + InfluxdbDbname + '\'' +
                ", policy='" + policy + '\'' +
                ", duration='" + duration + '\'' +
                ", replication='" + replication + '\'' +
                ", clear='" + clear + '\'' +
                '}';
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSnmpPort() {
        return snmpPort;
    }

    public void setSnmpPort(String snmpPort) {
        this.snmpPort = snmpPort;
    }

    public int getInfluxdbInterval() {
        return influxdbInterval;
    }

    public void setInfluxdbInterval(int influxdbInterval) {
        this.influxdbInterval = influxdbInterval;
    }

    public String getInfluxdbUrl() {
        return InfluxdbUrl;
    }

    public void setInfluxdbUrl(String influxdbUrl) {
        InfluxdbUrl = influxdbUrl;
    }

    public String getInfludbUser() {
        return InfludbUser;
    }

    public void setInfludbUser(String infludbUser) {
        InfludbUser = infludbUser;
    }

    public String getInfluxdbpass() {
        return Influxdbpass;
    }

    public void setInfluxdbpass(String influxdbpass) {
        Influxdbpass = influxdbpass;
    }

    public String getInfluxdbDbname() {
        return InfluxdbDbname;
    }

    public void setInfluxdbDbname(String influxdbDbname) {
        InfluxdbDbname = influxdbDbname;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getReplication() {
        return replication;
    }

    public void setReplication(String replication) {
        this.replication = replication;
    }

    public String getClear() {
        return clear;
    }

    public void setClear(String clear) {
        this.clear = clear;
    }

    public Args(String ip, String snmpPort, int influxdbInterval, String influxdbUrl, String infludbUser, String influxdbpass, String influxdbDbname, String policy, String duration, String replication, String clear) {

        this.ip = ip;
        this.snmpPort = snmpPort;
        this.influxdbInterval = influxdbInterval;
        InfluxdbUrl = influxdbUrl;
        InfludbUser = infludbUser;
        Influxdbpass = influxdbpass;
        InfluxdbDbname = influxdbDbname;
        this.policy = policy;
        this.duration = duration;
        this.replication = replication;
        this.clear = clear;
    }
}
