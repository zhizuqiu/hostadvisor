package com.unicom.chanyi.startAdvisor;

import org.apache.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class hostController {

    private static Logger logger = Logger.getLogger(hostController.class);

    private Map<String, String> influxdbParam = new HashMap<>();

    private String policy;

    private Integer influxdbInterval;

    public hostController() {

        String type = "pro";                                       // test 为以下的默认值， pro 为读取系统的环境变量

        String influxdb_url = "http://192.168.1.103:8086";    // influxDB的ip和端口
        String influxdb_username = "root";                        // influxDB的用户名
        String influxdb_password = "root";                        // influxDB的用户密码
        String influxdb_dbname = "advisor";                      // influxDB的数据库名字
        String influxdb_policy = "7_day";                         // influxDB保留策略的名字
        String influxdb_interval = "5000";                         // 采集程序的采集间隔

        if (type.equals("pro")) {
            influxdb_url = System.getenv("influxdb_url");
            influxdb_username = System.getenv("influxdb_username");
            influxdb_password = System.getenv("influxdb_password");
            influxdb_dbname = System.getenv("influxdb_dbname");
            influxdb_policy = System.getenv("policy");
            influxdb_interval = System.getenv("influxdbInterval");
        }

        influxdbParam.put("ip", influxdb_url);
        influxdbParam.put("username", influxdb_username);
        influxdbParam.put("psw", influxdb_password);
        influxdbParam.put("dbName", influxdb_dbname);
        policy = influxdb_policy;

        influxdbInterval = Integer.valueOf(influxdb_interval) * 2;
    }

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/health")
    public String health() {
        return "health";
    }

    /****
     * 根据table获取列表
     *
     */
    @RequestMapping(value = "/getListByTable", method = {RequestMethod.POST})
    @ResponseBody
    public List<Map<String, String>> getListByTable(@RequestBody Map param) {

        Object table_obj = param.get("table");
        if (null == table_obj) {
            return new ArrayList<>();
        }
        String table = table_obj.toString();

        String sql = "select * from \"" + policy + "\"." + table + " group by ip order by time desc limit 1;";
        influxdbParam.put("sql", sql);

        List<Map<String, String>> hostList = new ArrayList<>();

        try {
            hostList = this.getInfluxdbListBySql(influxdbParam);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (hostList == null) {
            return new ArrayList<>();
        }
        return hostList;
    }

    /****
     * 根据table、key获取List
     *
     */
    @RequestMapping(value = "/getListByTableAndkeyAndIp", method = {RequestMethod.POST})
    @ResponseBody
    public List<Map<String, String>> getGroupByTableAndkey(@RequestBody Map param) {

        Object keyname_obj = param.get("keyname");
        Object table_obj = param.get("table");
        Object ip_obj = param.get("ip");
        if (keyname_obj == null || table_obj == null || ip_obj == null) {
            return new ArrayList<>();
        }
        String keyname = keyname_obj.toString();
        String table = table_obj.toString();
        String ip = ip_obj.toString();

        String distinctSql = "SELECT DISTINCT(" + keyname + ") FROM \"" + policy + "\"." + table + "  where time > now() - " + influxdbInterval + "ms;";

        influxdbParam.put("sql", distinctSql);

        List<Map<String, String>> hostListDistinct = new ArrayList<>();

        try {
            hostListDistinct = this.getInfluxdbListBySql(influxdbParam);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        Integer count = 0;
        List<String> distictList = new ArrayList<>();
        if (hostListDistinct != null && hostListDistinct.size() > 0) {
            for (Map<String, String> distinctMap : hostListDistinct) {
                String distinct = distinctMap.get("distinct");
                distictList.add(distinct);
                count++;
            }
        }
        if (count == 0) {
            return new ArrayList<>();
        }

        String sql = "select * from \"" + policy + "\"." + table + " where ip = '" + ip + "' and (";

        for (int i = 0; i < distictList.size(); i++) {
            if (i == 0) {
                sql += keyname + "='" + distictList.get(i) + "' ";
            } else {
                sql += "or " + keyname + " ='" + distictList.get(i) + "' ";
            }
        }

        sql += ") order by time desc limit " + count + ";";

        influxdbParam.put("sql", sql);

        List<Map<String, String>> hostList = new ArrayList<>();

        try {
            hostList = this.getInfluxdbListBySql(influxdbParam);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (hostList == null) {
            return new ArrayList<>();
        }
        return hostList;
    }


    /****
     * 获取指定ip和table的列名列表
     *
     */
    @RequestMapping(value = "/getKeyList", method = {RequestMethod.POST})
    @ResponseBody
    public List<Map<String, String>> getCpuKeyList(@RequestBody Map param) {

        Object ip_obj = param.get("ip");
        Object table_obj = param.get("table");
        if (null == ip_obj || null == table_obj) {
            return new ArrayList<>();
        }
        String ip = ip_obj.toString();
        String table = table_obj.toString();

        String sql = "select * from \"" + policy + "\"." + table + " where ip= '" + ip + "' order by time desc limit 1;";
        influxdbParam.put("sql", sql);
        List<Map<String, String>> hostList = new ArrayList<>();

        try {
            hostList = this.getInfluxdbListBySql(influxdbParam);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (hostList == null) {
            return new ArrayList<>();
        }
        return hostList;
    }


    /****
     * 获取指定ip、table、key的详情
     *
     */
    @RequestMapping(value = "/getMore", method = {RequestMethod.POST})
    @ResponseBody
    public List<Map<String, String>> getMore(@RequestBody Map param) {
        Object ip_obj = param.get("ip");
        Object key_obj = param.get("key");
        Object table_obj = param.get("table");
        Object limit_obj = param.get("limit");
        Object name_obj = param.get("name");
        Object timer_type_obj = param.get("timer_type");
        Object from_time_obj = param.get("from_time");
        Object to_time_obj = param.get("to_time");
        Object group_time_obj = param.get("group_time");

        if (null == ip_obj || key_obj == null || table_obj == null || limit_obj == null || timer_type_obj == null) {
            return new ArrayList<>();
        }
        String ip = ip_obj.toString();
        String key = key_obj.toString();
        String table = table_obj.toString();
        String limit = limit_obj.toString();
        String timer_type = timer_type_obj.toString();
        String name = null;
        String from_time = null;
        String to_time = null;
        String group_time = null;

        if (table.equals("disk") || table.equals("network")) {
            if (name_obj == null) {
                return new ArrayList<>();
            } else {
                name = name_obj.toString();
            }
        }

        if (timer_type.equals("1")) {
            if (from_time_obj == null || to_time_obj == null || group_time_obj == null) {
                return new ArrayList<>();
            } else {
                from_time = from_time_obj.toString();
                to_time = to_time_obj.toString();
                group_time = group_time_obj.toString();
            }
        }

        String sql = "select " + key + " from \"" + policy + "\"." + table + " where ip= '" + ip + "' order by time desc limit " + limit + ";";

        if (timer_type.equals("0")) {
            if (table.equals("cpu")) {
                String[] keyArray = new String[]{"ssCpuUser"};
                List<String> keylist = Arrays.asList(keyArray);
                if (keylist.contains(key)) {
                    sql = "select ssCpuUser from \"" + policy + "\"." + table + " where ip= '" + ip + "' order by time desc limit " + limit + ";";
                } else {
                    sql = "select " + key + ",ssCpuUser from \"" + policy + "\"." + table + " where ip= '" + ip + "' order by time desc limit " + limit + ";";
                }

            } else if (table.equals("mem")) {
                String[] keyArray = new String[]{"memTotalReal", "memAvailReal", "memBuffer", "memCached"};
                List<String> keylist = Arrays.asList(keyArray);
                if (keylist.contains(key)) {
                    sql = "select memTotalReal,memAvailReal,memBuffer,memCached from \"" + policy + "\"." + table + " where ip= '" + ip + "' order by time desc limit " + limit + ";";
                } else {
                    sql = "select " + key + " ,memTotalReal,memAvailReal,memBuffer,memCached from \"" + policy + "\"." + table + " where ip= '" + ip + "' order by time desc limit " + limit + ";";
                }

            } else if (table.equals("disk")) {

                String[] keyArray = new String[]{"dskPercent"};
                List<String> keylist = Arrays.asList(keyArray);
                if (keylist.contains(key)) {
                    sql = "select dskPercent from \"" + policy + "\"." + table + " where ip= '" + ip + "' and dskPath = '" + name + "' order by time desc limit " + limit + ";";
                } else {
                    sql = "select " + key + " ,dskPercent from \"" + policy + "\"." + table + " where ip= '" + ip + "' and dskPath = '" + name + "' order by time desc limit " + limit + ";";
                }
            } else if (table.equals("network")) {

                sql = "select " + key + " from \"" + policy + "\"." + table + " where ip= '" + ip + "' and ifDescr = '" + name + "' order by time desc limit " + limit + ";";

            }
        } else {
            if (table.equals("cpu")) {
                String[] keyArray = new String[]{"ssCpuUser"};
                List<String> keylist = Arrays.asList(keyArray);
                if (keylist.contains(key)) {
                    sql = "select mean(ssCpuUser) as ssCpuUser from \"" + policy + "\"." + table + " where ip= '" + ip + "' and  time > now() - " + from_time + " and time < now() - " + to_time + "  group by time(" + group_time + ");";
                } else {
                    sql = "select mean(" + key + ") as " + key + ",mean(ssCpuUser) as ssCpuUser from \"" + policy + "\"." + table + " where ip= '" + ip + "' and  time > now() - " + from_time + " and time < now() - " + to_time + "  group by time(" + group_time + ");";
                }

            } else if (table.equals("mem")) {
                String[] keyArray = new String[]{"memTotalReal", "memAvailReal", "memBuffer", "memCached"};
                List<String> keylist = Arrays.asList(keyArray);
                if (keylist.contains(key)) {
                    sql = "select mean(memTotalReal) as memTotalReal,mean(memAvailReal) as memAvailReal,mean(memBuffer) as memBuffer,mean(memCached) as memCached  from \"" + policy + "\"." + table + " where ip= '" + ip + "' and  time > now() - " + from_time + " and time < now() - " + to_time + "  group by time(" + group_time + ");";
                } else {
                    sql = "select mean(" + key + ") as " + key + " , mean(memTotalReal) as memTotalReal,mean(memAvailReal) as memAvailReal,mean(memBuffer) as memBuffer,mean(memCached) as memCached  from \"" + policy + "\"." + table + " where ip= '" + ip + "' and  time > now() - " + from_time + " and time < now() - " + to_time + "  group by time(" + group_time + ");";
                }

            } else if (table.equals("disk")) {

                String[] keyArray = new String[]{"dskPercent"};
                List<String> keylist = Arrays.asList(keyArray);
                if (keylist.contains(key)) {
                    sql = "select mean(dskPercent) as dskPercent from \"" + policy + "\"." + table + " where ip= '" + ip + "' and dskPath = '" + name + "' and  time > now() - " + from_time + " and time < now() - " + to_time + "  group by time(" + group_time + ");";
                } else {
                    sql = "select mean(" + key + ") as key ,mean(dskPercent) as dskPercent from \"" + policy + "\"." + table + " where ip= '" + ip + "' and dskPath = '" + name + "' and  time > now() - " + from_time + " and time < now() - " + to_time + "  group by time(" + group_time + ");";
                }
            } else if (table.equals("network")) {

                sql = "select mean(" + key + ") as " + key + " from \"" + policy + "\"." + table + " where ip= '" + ip + "' and ifDescr = '" + name + "' and  time > now() - " + from_time + " and time < now() - " + to_time + "  group by time(" + group_time + ");";

            }
        }
        influxdbParam.put("sql", sql);

        List<Map<String, String>> hostList = new ArrayList<>();
        try {
            hostList = this.getInfluxdbListBySql(influxdbParam);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (hostList == null) {
            return new ArrayList<>();
        }
        return hostList;
    }

    private List<Map<String, String>> getInfluxdbListBySql(Map<String, String> param) throws Exception {
        Object Ip_obj = param.get("ip");
        Object username_obj = param.get("username");
        Object psw_obj = param.get("psw");
        Object dbName_obj = param.get("dbName");
        Object sql_obj = param.get("sql");

        if (Ip_obj == null || username_obj == null || psw_obj == null || dbName_obj == null || sql_obj == null) {
            throw new Exception("param is null");
        }

        String Ip = Ip_obj.toString();
        String username = username_obj.toString();
        String psw = psw_obj.toString();
        String dbName = dbName_obj.toString();
        String sql = sql_obj.toString();

        InfluxDB influxDB = InfluxDBFactory.connect(Ip, username, psw);
        Query query = new Query(sql, dbName);
        QueryResult queryResult = influxDB.query(query);

        List<Map<String, String>> result = new ArrayList<>();

        List<QueryResult.Result> qResult = queryResult.getResults();
        if (qResult != null && qResult.size() > 0) {
            QueryResult.Result re = qResult.get(0);
            if (re != null) {
                List<QueryResult.Series> series = re.getSeries();
                if (series != null) {
                    for (QueryResult.Series se : series) {

                        String ip = null;
                        Map<String, String> tags = se.getTags();
                        if (tags != null) {
                            ip = tags.get("ip");
                        }

                        List<String> columns = se.getColumns();
                        if (columns != null) {
                            List<List<Object>> values = se.getValues();
                            if (values != null && values.size() > 0) {
                                for (List<Object> value : values) {
                                    Map<String, String> map = new HashMap<>();
                                    if (ip != null) {
                                        map.put("ip", ip);
                                    }
                                    for (int index = 0; index < columns.size(); index++) {
                                        String key = columns.get(index);
                                        Object val_obj = value.get(index);
                                        if (val_obj != null) {
                                            map.put(key, value.get(index).toString());
                                        }
                                    }
                                    result.add(map);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


}
