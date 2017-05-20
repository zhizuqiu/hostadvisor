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

        //配置信息
        String type = "test";                                     // test 为以下的默认值， pro 为读取系统的环境变量
        String ip = "192.168.1.102";                            // 要采集主机的IP
        String snmpPort = "161";                                  // 要采集主机的net-snmp端口，默认为161
        String influxdb_url = "http://192.168.1.102:8086";    // influxDB的ip和端口
        String influxdb_username = "root";                        // influxDB的用户名
        String influxdb_password = "root";                        // influxDB的用户密码
        String influxdb_dbname = "advisor";                      // influxDB的数据库名字
        String policy = "7_day";                                 // influxDB保留策略的名字
        String duration = "7d";                                   // influxDB保留策略的时间，这里是7天自动删除
        String replication = "1";                                 // influxDB保留策略备份数量
        String clear = "false";                                   // 每次启动是否清空数据库
        int influxdbInterval = 5000;                               // 采集频率，单位毫秒
        //配置信息 END

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