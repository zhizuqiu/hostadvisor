package com.unicom.chanyi.advisor;


import com.unicom.chanyi.influxdb.InfluxdbTools;
import com.unicom.chanyi.snmp.SnmpTools;
import org.apache.log4j.Logger;

import java.util.Observable;
import java.util.Observer;

public class Listener implements Observer {

    private Logger logger = Logger.getLogger(Listener.class);

    @Override
    public void update(Observable o, Object arg) {

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            logger.error("重启时失败：" + e.getMessage());
        }

        logger.info("线程异常");

        Args args = (Args) arg;

        InfluxdbTools influxdbTools = new InfluxdbTools(args.getInfluxdbUrl(), args.getInfludbUser(), args.getInfluxdbpass(), args.getInfluxdbDbname(), args.getPolicy(), args.getDuration(), args.getReplication(), args.getClear());

        SnmpTools snmpTools = new SnmpTools(args.getIp(), args.getSnmpPort());

        CollectThread collectThread = new CollectThread(influxdbTools, snmpTools, args);

        collectThread.addObserver(this);

        new Thread(collectThread).start();

        logger.info("正在重启...");
    }
}