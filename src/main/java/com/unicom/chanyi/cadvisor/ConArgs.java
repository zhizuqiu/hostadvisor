package com.unicom.chanyi.cadvisor;

public class ConArgs {
    private String ip;
    private String cadvisorPort;
    private String cadvisorUrl;
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
        return "ConArgs{" +
                "ip='" + ip + '\'' +
                ", cadvisorPort='" + cadvisorPort + '\'' +
                ", cadvisorUrl='" + cadvisorUrl + '\'' +
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

    public String getCadvisorPort() {
        return cadvisorPort;
    }

    public void setCadvisorPort(String cadvisorPort) {
        this.cadvisorPort = cadvisorPort;
    }

    public String getCadvisorUrl() {
        return cadvisorUrl;
    }

    public void setCadvisorUrl(String cadvisorUrl) {
        this.cadvisorUrl = cadvisorUrl;
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

    public ConArgs(String ip, String cadvisorPort, String cadvisorUrl, int influxdbInterval, String influxdbUrl, String infludbUser, String influxdbpass, String influxdbDbname, String policy, String duration, String replication, String clear) {

        this.ip = ip;
        this.cadvisorPort = cadvisorPort;
        this.cadvisorUrl = cadvisorUrl;
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
