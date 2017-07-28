package com.unicom.chanyi.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InfluxdbTools {

    private InfluxDB influxDB;
    private String url;
    private String username;
    private String password;
    private String dbname;
    private String policy;
    private String duration;
    private String replication;
    private String clear;


    public InfluxdbTools(String url, String username, String password, String dbname, String policy, String duration, String replication, String clear) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.dbname = dbname;
        this.policy = policy;
        this.duration = duration;
        this.replication = replication;
        this.clear = clear;
    }

    public void init(){
        this.influxDB = InfluxDBFactory.connect(this.url, this.username, this.password);

        if (this.clear.equals("true")){
            influxDB.deleteDatabase(this.dbname);
        }

        this.influxDB.createDatabase(this.dbname);

        Query query = new Query("CREATE RETENTION POLICY \""+this.policy+"\" ON \""+this.dbname+"\" DURATION "+this.duration+" REPLICATION "+this.replication+" DEFAULT",this.dbname);
        influxDB.query(query);

    }



    public void close() {
        this.influxDB.close();
    }


    public void insert(List<InsertValue> insertValueList) throws InfluxdbToolsException {
        try {
            BatchPoints batchPoints = BatchPoints
                    .database(this.dbname)
                    .consistency(InfluxDB.ConsistencyLevel.ALL)
                    .build();

            for (InsertValue insertValue : insertValueList) {
                Point point = Point.measurement(insertValue.getTable())
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .addField("value", insertValue.getValue())
                        .build();
                batchPoints.point(point);
            }

            this.influxDB.write(batchPoints);
        } catch (Exception e) {
            throw new InfluxdbToolsException(e.getMessage());
        }
    }


    public void insert(Map<String, Map<String, Object>> typeMapList,String snmpIp) throws InfluxdbToolsException {

        try {
            for (Map.Entry<String, Map<String, Object>> entry : typeMapList.entrySet()) {
                String type = entry.getKey();
                Map<String, Object> nameMapVariable = entry.getValue();

                BatchPoints batchPoints = BatchPoints
                        .database(this.dbname)
                        .consistency(InfluxDB.ConsistencyLevel.ALL)
                        .build();

                Point.Builder builder = Point.measurement(type);
                builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

                for (Map.Entry<String, Object> entry_inner : nameMapVariable.entrySet()) {
                    String name = entry_inner.getKey();
                    Object value = entry_inner.getValue();

                    if (value instanceof Integer32){
                        Integer v = ((Integer32) value).toInt();
                        builder.addField(name, v);
                    }else if(value instanceof Counter32){
                        Long v = ((Counter32) value).toLong();
                        builder.addField(name, v);
                    }else if(value instanceof Counter64){
                        Long v = ((Counter64) value).toLong();
                        builder.addField(name, v);
                    }else if(value instanceof Number){
                        Number v = ((Number) value);
                        builder.addField(name, v);
                    }else{
                        builder.addField(name,value.toString());
                    }
                }
                builder.tag("ip",snmpIp);

                Point point = builder.build();
                batchPoints.point(point);
                this.influxDB.write(batchPoints);
            }
        } catch (Exception e) {
            throw new InfluxdbToolsException(e.getMessage());
        }
    }
}
