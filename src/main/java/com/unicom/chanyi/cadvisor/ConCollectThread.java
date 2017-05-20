package com.unicom.chanyi.cadvisor;


import com.unicom.chanyi.influxdb.InfluxdbTools;
import com.unicom.chanyi.influxdb.InfluxdbToolsException;
import org.apache.log4j.Logger;

import java.util.*;

public class ConCollectThread extends Observable implements Runnable {

    private InfluxdbTools influxdbTools;

    private CadvisorTool cadvisorTool;

    private ConArgs ConArgs;

    private Logger logger = Logger.getLogger(ConCollectThread.class);

    public ConCollectThread(InfluxdbTools influxdbTools, CadvisorTool cadvisorTool, ConArgs ConArgs) {
        this.influxdbTools = influxdbTools;
        this.cadvisorTool = cadvisorTool;
        this.ConArgs = ConArgs;
    }

    public ConArgs getConArgs() {
        return ConArgs;
    }

    // 通知观察者
    public void sendNotify(ConArgs ConArgs) {
        super.setChanged();
        notifyObservers(ConArgs);
    }

    @Override
    public void run() {

        try {
            this.influxdbTools.init();
        } catch (Exception e) {
            logger.error(e.getMessage());
            sendNotify(this.getConArgs());
            return;
        }

        int count = 0;
        while (true) {

            try {
                Thread.sleep(this.ConArgs.getInfluxdbInterval());
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                sendNotify(this.getConArgs());
                return;
            }


            List<Map> result = new ArrayList<Map>();
            try {
                result = this.cadvisorTool.getCadvisorMessage();
            } catch (Exception e) {
                logger.error(e.getMessage());
                sendNotify(this.getConArgs());
                return;
            }

            if (result == null) {
                logger.error("获取容器信息失败，为空");
            } else {
                for (Map map : result) {
                    Map<String, Map<String, String>> typeMapList = new HashMap<String, Map<String, String>>();
                    typeMapList.put("container", map);
                    try {
                        influxdbTools.insert(typeMapList, this.cadvisorTool.getIp());
                    } catch (InfluxdbToolsException e) {
                        logger.error(e.getMessage());
                        sendNotify(this.getConArgs());
                        return;
                    }
                    logger.info(typeMapList);
                }
            }
            logger.info("-------------------");

            count++;
            if (count > 10) {
                sendNotify(this.getConArgs());
                break;
            }
        }
    }


}
