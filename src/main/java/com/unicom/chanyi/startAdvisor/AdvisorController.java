package com.unicom.chanyi.startAdvisor;

import com.unicom.chanyi.advisor.Args;
import com.unicom.chanyi.advisor.CollectThread;
import com.unicom.chanyi.advisor.Listener;
import com.unicom.chanyi.influxdb.InfluxdbTools;
import com.unicom.chanyi.snmp.SnmpTools;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class AdvisorController {

    private static Logger logger = Logger.getLogger(AdvisorController.class);


    @RequestMapping("/health")
    @ResponseBody
    String home() {
        return "health";
    }


    public static void main(String[] arg) throws Exception {
        SpringApplication.run(AdvisorController.class, arg);

        String type = "pro";
        String snmpIp = "10.161.24.226";
        String snmpPort = "161";
        String influxdb_url = "http://10.161.24.226:8086";
        String influxdb_username = "root";
        String influxdb_password = "root";
        String influxdb_dbname = "hostAdvisor";
        String policy = "7_day";
        String duration = "7d";
        String replication = "1";
        String clear = "true";

        int influxdbInterval = 5000;

        logger.info("采集程序正在启动...");
        logger.info(type);

        if (type.equals("pro")) {
            snmpIp = System.getenv("snmpIp");
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

        SnmpTools snmpTools = new SnmpTools(snmpIp, snmpPort);

        Listener listen = new Listener();

        Args args = new Args(snmpIp, snmpPort, influxdbInterval, influxdb_url, influxdb_username, influxdb_password, influxdb_dbname,policy,duration,replication,clear);

        logger.info(args);

        CollectThread collectThread = new CollectThread(influxdbTools, snmpTools, args);

        collectThread.addObserver(listen);

        new Thread(collectThread).start();

    }
}