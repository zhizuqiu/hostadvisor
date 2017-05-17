package com.unicom.chanyi.advisor;


import com.unicom.chanyi.influxdb.InfluxdbTools;
import com.unicom.chanyi.influxdb.InfluxdbToolsException;
import com.unicom.chanyi.snmp.SnmpResult;
import com.unicom.chanyi.snmp.SnmpTools;
import com.unicom.chanyi.snmp.SnmpToolsException;
import org.apache.log4j.Logger;
import org.snmp4j.smi.OID;

import java.io.IOException;
import java.util.*;

public class CollectThread extends Observable implements Runnable {

    private InfluxdbTools influxdbTools;

    private SnmpTools snmpTools;

    private Args args;

    private Logger logger = Logger.getLogger(CollectThread.class);

    public CollectThread(InfluxdbTools influxdbTools, SnmpTools snmpTools, Args args) {
        this.influxdbTools = influxdbTools;
        this.snmpTools = snmpTools;
        this.args = args;
    }

    public Args getArgs() {
        return args;
    }

    // 通知观察者
    public void sendNotify(Args args) {
        super.setChanged();
        notifyObservers(args);
    }

    @Override
    public void run() {

        try {
            this.snmpTools.init();
        } catch (IOException e) {
            logger.error(e.getMessage());
            sendNotify(this.getArgs());
        }
        try {
            this.influxdbTools.init();
        } catch (Exception e) {
            logger.error(e.getMessage());
            sendNotify(this.getArgs());
        }


        int count = 0;
        while (true) {

            try {
                Thread.sleep(this.args.getInfluxdbInterval());
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                sendNotify(this.getArgs());
                return;
            }


            //<"cpu",<"name","variable">>
            Map<String, Map<String, String>> typeMapList = new HashMap<String, Map<String, String>>();

            //初始化typeMapList
            typeMapList.put("cpu", new HashMap<String, String>());
            typeMapList.put("mem", new HashMap<String, String>());

            //name = value
            //cpu
            String ssCpuUser = "1.3.6.1.4.1.2021.11.9.0";   //用户CPU百分比
            String ssCpuSystem = "1.3.6.1.4.1.2021.11.10.0";    //系统CPU百分比
            String ssCpuIdle = "1.3.6.1.4.1.2021.11.11.0";  //空闲CPU百分比
            String ssCpuRawUser = "1.3.6.1.4.1.2021.11.50.0";
            String ssCpuRawNice = "1.3.6.1.4.1.2021.11.51.0";
            String ssCpuRawSystem = "1.3.6.1.4.1.2021.11.52.0";
            String ssCpuRawIdle = "1.3.6.1.4.1.2021.11.53.0";
            String ssCpuRawWait = "1.3.6.1.4.1.2021.11.54.0";
            String ssCpuRawKernel = "1.3.6.1.4.1.2021.11.55.0";
            String ssCpuRawInterrupt = "1.3.6.1.4.1.2021.11.56.0";

            //mem
            String memTotalSwap = "1.3.6.1.4.1.2021.4.3.0";
            String memAvailSwap = "1.3.6.1.4.1.2021.4.4.0";
            String memTotalReal = "1.3.6.1.4.1.2021.4.5.0";
            String memAvailReal = "1.3.6.1.4.1.2021.4.6.0";
            String memTotalFree = "1.3.6.1.4.1.2021.4.11.0";
            String memShared = "1.3.6.1.4.1.2021.4.13.0";
            String memBuffer = "1.3.6.1.4.1.2021.4.14.0";
            String memCached = "1.3.6.1.4.1.2021.4.15.0";


            //<"name","type">
            Map<String, String> nameMapType = new HashMap<String, String>();
            nameMapType.put("ssCpuUser", "cpu");
            nameMapType.put("ssCpuSystem", "cpu");
            nameMapType.put("ssCpuIdle", "cpu");
            nameMapType.put("ssCpuRawUser", "cpu");
            nameMapType.put("ssCpuRawNice", "cpu");
            nameMapType.put("ssCpuRawSystem", "cpu");
            nameMapType.put("ssCpuRawIdle", "cpu");
            nameMapType.put("ssCpuRawWait", "cpu");
            nameMapType.put("ssCpuRawKernel", "cpu");
            nameMapType.put("ssCpuRawInterrupt", "cpu");

            //mem
            nameMapType.put("memTotalSwap", "mem");
            nameMapType.put("memAvailSwap", "mem");
            nameMapType.put("memTotalReal", "mem");
            nameMapType.put("memAvailReal", "mem");
            nameMapType.put("memTotalFree", "mem");
            nameMapType.put("memShared", "mem");
            nameMapType.put("memBuffer", "mem");
            nameMapType.put("memCached", "mem");


            //<"value","name">
            Map<String, String> valueMapName = new HashMap<String, String>();

            //cpu
            valueMapName.put(ssCpuUser, "ssCpuUser");
            valueMapName.put(ssCpuSystem, "ssCpuSystem");
            valueMapName.put(ssCpuIdle, "ssCpuIdle");
            valueMapName.put(ssCpuRawUser, "ssCpuRawUser");
            valueMapName.put(ssCpuRawNice, "ssCpuRawNice");
            valueMapName.put(ssCpuRawSystem, "ssCpuRawSystem");
            valueMapName.put(ssCpuRawIdle, "ssCpuRawIdle");
            valueMapName.put(ssCpuRawWait, "ssCpuRawWait");
            valueMapName.put(ssCpuRawKernel, "ssCpuRawKernel");
            valueMapName.put(ssCpuRawInterrupt, "ssCpuRawInterrupt");

            //mem
            valueMapName.put(memTotalSwap, "memTotalSwap");
            valueMapName.put(memAvailSwap, "memAvailSwap");
            valueMapName.put(memTotalReal, "memTotalReal");
            valueMapName.put(memAvailReal, "memAvailReal");
            valueMapName.put(memTotalFree, "memTotalFree");
            valueMapName.put(memShared, "memShared");
            valueMapName.put(memBuffer, "memBuffer");
            valueMapName.put(memCached, "memCached");


            List<OID> oids = new ArrayList<OID>();
            //cpu
            oids.add(new OID(ssCpuUser));
            oids.add(new OID(ssCpuSystem));
            oids.add(new OID(ssCpuIdle));
            oids.add(new OID(ssCpuRawUser));
            oids.add(new OID(ssCpuRawNice));
            oids.add(new OID(ssCpuRawSystem));
            oids.add(new OID(ssCpuRawIdle));
            oids.add(new OID(ssCpuRawWait));
            oids.add(new OID(ssCpuRawKernel));
            oids.add(new OID(ssCpuRawInterrupt));

            //mem
            oids.add(new OID(memTotalSwap));
            oids.add(new OID(memAvailSwap));
            oids.add(new OID(memTotalReal));
            oids.add(new OID(memAvailReal));
            oids.add(new OID(memTotalFree));
            oids.add(new OID(memShared));
            oids.add(new OID(memBuffer));
            oids.add(new OID(memCached));

            //cpu and mem 的操作
            try {
                this.oprate(oids, valueMapName, nameMapType, typeMapList);
            } catch (Exception e) {
                this.sendNotify(this.getArgs());
                return;
            }


            //获取硬盘列表
            OID oid_disk = new OID("1.3.6.1.4.1.2021.9.1.1");
            List<SnmpResult> snmpResults_disk = new ArrayList<SnmpResult>();
            try {
                snmpResults_disk = snmpTools.getList(oid_disk);
            } catch (IOException e) {
                logger.error(e.getMessage());
                sendNotify(this.getArgs());
                return;
            }

            //<"disk",<"name","variable">>
            Map<String, Map<String, String>> typeMapList_disk = new HashMap<String, Map<String, String>>();
            //初始化typeMapList
            typeMapList_disk.put("disk", new HashMap<String, String>());


            for (SnmpResult snmpResult : snmpResults_disk) {
                String dskIndex = "1.3.6.1.4.1.2021.9.1.1." + snmpResult.getVariable();
                String dskPath = "1.3.6.1.4.1.2021.9.1.2." + snmpResult.getVariable();
                String dskDevice = "1.3.6.1.4.1.2021.9.1.3." + snmpResult.getVariable();
                String dskTotal = "1.3.6.1.4.1.2021.9.1.6." + snmpResult.getVariable();
                String dskAvail = "1.3.6.1.4.1.2021.9.1.7." + snmpResult.getVariable();
                String dskUsed = "1.3.6.1.4.1.2021.9.1.8." + snmpResult.getVariable();
                String dskPercent = "1.3.6.1.4.1.2021.9.1.9." + snmpResult.getVariable();

                //<"name","type">
                Map<String, String> nameMapType_disk = new HashMap<String, String>();
                nameMapType_disk.put("dskIndex", "disk");
                nameMapType_disk.put("dskPath", "disk");
                nameMapType_disk.put("dskDevice", "disk");
                nameMapType_disk.put("dskTotal", "disk");
                nameMapType_disk.put("dskAvail", "disk");
                nameMapType_disk.put("dskUsed", "disk");
                nameMapType_disk.put("dskPercent", "disk");

                //<"value","name">
                Map<String, String> valueMapName_disk = new HashMap<String, String>();

                //disk
                valueMapName_disk.put(dskIndex, "dskIndex");
                valueMapName_disk.put(dskPath, "dskPath");
                valueMapName_disk.put(dskDevice, "dskDevice");
                valueMapName_disk.put(dskTotal, "dskTotal");
                valueMapName_disk.put(dskAvail, "dskAvail");
                valueMapName_disk.put(dskUsed, "dskUsed");
                valueMapName_disk.put(dskPercent, "dskPercent");


                List<OID> oids_disk = new ArrayList<OID>();
                //disk
                oids_disk.add(new OID(dskIndex));
                oids_disk.add(new OID(dskPath));
                oids_disk.add(new OID(dskDevice));
                oids_disk.add(new OID(dskTotal));
                oids_disk.add(new OID(dskAvail));
                oids_disk.add(new OID(dskUsed));
                oids_disk.add(new OID(dskPercent));

                try {
                    this.oprate(oids_disk, valueMapName_disk, nameMapType_disk, typeMapList_disk);
                } catch (Exception e) {
                    this.sendNotify(this.getArgs());
                    return;
                }

            }

            //获取网络列表
            OID oid_network = new OID("1.3.6.1.2.1.2.2.1.1");
            List<SnmpResult> snmpResults_network = new ArrayList<SnmpResult>();
            try {
                snmpResults_network = snmpTools.getList(oid_network);
            } catch (IOException e) {
                logger.error(e.getMessage());
                sendNotify(this.getArgs());
                return;
            }

            //<"network",<"name","variable">>
            Map<String, Map<String, String>> typeMapList_network = new HashMap<String, Map<String, String>>();
            //初始化typeMapList
            typeMapList_network.put("network", new HashMap<String, String>());


            for (SnmpResult snmpResult : snmpResults_disk) {
                String ifDescr = "1.3.6.1.2.1.2.2.1.2." + snmpResult.getVariable();
                String ifInOctets = "1.3.6.1.2.1.2.2.1.10." + snmpResult.getVariable();
                String ifOutOctets = "1.3.6.1.2.1.2.2.1.16." + snmpResult.getVariable();
                String ifInUcastPkts = "1.3.6.1.2.1.2.2.1.11." + snmpResult.getVariable();
                String ifOutUcastPkts = "1.3.6.1.2.1.2.2.1.17." + snmpResult.getVariable();

                //<"name","type">
                Map<String, String> nameMapType_network = new HashMap<String, String>();
                nameMapType_network.put("ifDescr", "network");
                nameMapType_network.put("ifInOctets", "network");
                nameMapType_network.put("ifOutOctets", "network");
                nameMapType_network.put("ifInUcastPkts", "network");
                nameMapType_network.put("ifOutUcastPkts", "network");

                //<"value","name">
                Map<String, String> valueMapName_network = new HashMap<String, String>();

                //network
                valueMapName_network.put(ifDescr, "ifDescr");
                valueMapName_network.put(ifInOctets, "ifInOctets");
                valueMapName_network.put(ifOutOctets, "ifOutOctets");
                valueMapName_network.put(ifInUcastPkts, "ifInUcastPkts");
                valueMapName_network.put(ifOutUcastPkts, "ifOutUcastPkts");


                List<OID> oids_network = new ArrayList<OID>();
                //network
                oids_network.add(new OID(ifDescr));
                oids_network.add(new OID(ifInOctets));
                oids_network.add(new OID(ifOutOctets));
                oids_network.add(new OID(ifInUcastPkts));
                oids_network.add(new OID(ifOutUcastPkts));

                try {
                    this.oprate(oids_network, valueMapName_network, nameMapType_network, typeMapList_network);
                } catch (Exception e) {
                    this.sendNotify(this.getArgs());
                    return;
                }

            }

            logger.debug("-------------------");

            /*
            count++;
            if (count > 10) {
                sendNotify(this.getArgs());
                break;
            }
            */

        }
    }

    private void oprate(List<OID> oids, Map<String, String> valueMapName, Map<String, String> nameMapType, Map<String, Map<String, String>> typeMapList) throws Exception {
        List<SnmpResult> snmpResults = new ArrayList<SnmpResult>();
        try {
            snmpResults = snmpTools.getOne(oids);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new Exception(e.getMessage());
        } catch (SnmpToolsException e) {
            throw new Exception(e.getMessage());
        }

        for (SnmpResult snmpResult : snmpResults) {
            String name = valueMapName.get(snmpResult.getOid().toString());
            String variable = snmpResult.getVariable().toString();
            if (name != null) {
                String type = nameMapType.get(name);
                if (type != null) {
                    Map<String, String> map = typeMapList.get(type);
                    if (map == null) {
                        map = new HashMap<String, String>();
                    }
                    map.put(name, variable);
                } else {
                    logger.error("error : type is null");
                }
            } else {
                logger.error("error : name is null");
            }
        }
        try {
            influxdbTools.insert(typeMapList, this.snmpTools.getIp());
        } catch (InfluxdbToolsException e) {
            throw new Exception(e.getMessage());
        }
        logger.debug(typeMapList);

    }

}
