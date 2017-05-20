package com.unicom.chanyi.cadvisor;


import com.unicom.chanyi.influxdb.InfluxdbTools;
import org.apache.log4j.Logger;

import java.util.Observable;
import java.util.Observer;

public class ConListener implements Observer {

    private Logger logger = Logger.getLogger(ConListener.class);

    @Override
    public void update(Observable o, Object arg) {

        logger.info("容器采集线程异常");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            logger.error("容器采集线程重启时失败：" + e.getMessage());
        }


        ConArgs conArgs = (ConArgs) arg;

        InfluxdbTools influxdbTools = new InfluxdbTools(conArgs.getInfluxdbUrl(), conArgs.getInfludbUser(), conArgs.getInfluxdbpass(), conArgs.getInfluxdbDbname(), conArgs.getPolicy(), conArgs.getDuration(), conArgs.getReplication(), conArgs.getClear());

        CadvisorTool cadvisorTool = new CadvisorTool(conArgs.getIp(), conArgs.getCadvisorPort(),conArgs.getCadvisorUrl());

        ConCollectThread conCollectThread = new ConCollectThread(influxdbTools, cadvisorTool, conArgs);

        conCollectThread.addObserver(this);

        new Thread(conCollectThread).start();

        logger.info("容器采集线程正在重启...");
    }
}