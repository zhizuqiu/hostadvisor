package com.unicom.chanyi.startAdvisor;

import com.unicom.chanyi.commonTools.commonTools;
import com.unicom.chanyi.hostadvisor.HostArgs;
import com.unicom.chanyi.hostadvisor.HostCollectThread;
import com.unicom.chanyi.hostadvisor.HostListener;
import com.unicom.chanyi.influxdb.InfluxdbTools;
import com.unicom.chanyi.snmp.SnmpTools;
import org.apache.log4j.Logger;

public class AdvisorController {

    private static Logger logger = Logger.getLogger(AdvisorController.class);


    public static void main(String[] arg) throws Exception {

        String type = "test";
        String ip = "192.168.1.102";
        String snmpPort = "161";
        String influxdb_url = "http://192.168.1.102:8086";
        String influxdb_username = "root";
        String influxdb_password = "root";
        String influxdb_dbname = "advisor";
        String policy = "7_day";
        String duration = "7d";
        String replication = "1";
        String clear = "true";
        int influxdbInterval = 5000;


        logger.info("采集程序正在启动...");
        logger.info(type);

        if (type.equals("pro")) {

            ip = commonTools.getHostIp(commonTools.getInetAddress());
            snmpPort = System.getenv("snmpPort");
            influxdb_url = System.getenv("influxdb_url");
            influxdb_username = System.getenv("influxdb_username");
            influxdb_password = System.getenv("influxdb_password");
            influxdb_dbname = System.getenv("influxdb_dbname");
            influxdbInterval = Integer.valueOf(System.getenv("influxdbInterval"));
            policy = System.getenv("policy");
            duration = System.getenv("duration");
            replication = System.getenv("replication");
            clear = System.getenv("clear");

        }


        InfluxdbTools influxdbTools = new InfluxdbTools(influxdb_url, influxdb_username, influxdb_password, influxdb_dbname, policy, duration, replication, clear);


        //主机采集进程
        SnmpTools snmpTools = new SnmpTools(ip, snmpPort);

        HostListener listen = new HostListener();

        HostArgs hostArgs = new HostArgs(ip, snmpPort, influxdbInterval, influxdb_url, influxdb_username, influxdb_password, influxdb_dbname, policy, duration, replication, clear);

        logger.info(hostArgs);

        HostCollectThread hostCollectThread = new HostCollectThread(influxdbTools, snmpTools, hostArgs);

        hostCollectThread.addObserver(listen);

        new Thread(hostCollectThread).start();


    }
}