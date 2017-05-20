package com.unicom.chanyi.hostadvisor;


import com.unicom.chanyi.influxdb.InfluxdbTools;
import com.unicom.chanyi.snmp.SnmpTools;
import org.apache.log4j.Logger;

import java.util.Observable;
import java.util.Observer;

public class HostListener implements Observer {

    private Logger logger = Logger.getLogger(HostListener.class);

    @Override
    public void update(Observable o, Object arg) {

        logger.info("主机采集线程异常");


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            logger.error("主机采集线程重启时失败：" + e.getMessage());
        }


        HostArgs HostArgs = (HostArgs) arg;

        InfluxdbTools influxdbTools = new InfluxdbTools(HostArgs.getInfluxdbUrl(), HostArgs.getInfludbUser(), HostArgs.getInfluxdbpass(), HostArgs.getInfluxdbDbname(), HostArgs.getPolicy(), HostArgs.getDuration(), HostArgs.getReplication(), HostArgs.getClear());

        SnmpTools snmpTools = new SnmpTools(HostArgs.getIp(), HostArgs.getSnmpPort());

        HostCollectThread hostCollectThread = new HostCollectThread(influxdbTools, snmpTools, HostArgs);

        hostCollectThread.addObserver(this);

        new Thread(hostCollectThread).start();

        logger.info("主机采集线程正在重启...");
    }
}