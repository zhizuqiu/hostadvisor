package com.unicom.chanyi.cadvisor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Timestamp;
import java.util.*;

public class CadvisorTool {
    private String ip;
    private String port;
    private String url;

    public CadvisorTool(String ip, String port, String url) {
        this.ip = ip;
        this.port = port;
        this.url = url;
    }

    public String getIp() {
        return ip;
    }

    public List<Map> getCadvisorMessage() throws Exception {

        List<Map> result = new ArrayList<Map>();

        String json = "";

        HttpGetInfo info = HttpUtil.getByHttpGet("http://"+this.ip+":"+this.port+this.url);

        json = info.getMessage();

        result = this.getUsage(json);

        return result;
    }

    private List<Map> getUsage(String json) {
        List<Map> result = new ArrayList<Map>();
        JSONObject myJsonObject = JSONObject.fromObject(json);
        Iterator it = myJsonObject.keys();
        while (it.hasNext()) {
            String key = it.next().toString();
            if (!key.equals("/docker")) {
                JSONObject docker = myJsonObject.getJSONObject(key);
                JSONObject spec = docker.getJSONObject("spec");
                String container_name = spec.getJSONArray("aliases").getString(0);
                Long memlimit = spec.getJSONObject("memory").getLong("limit");
                JSONArray stats = docker.getJSONArray("stats");
                String curdate = stats.getJSONObject(1).getString("timestamp");
                String prevdate = stats.getJSONObject(0).getString("timestamp");
                Double curcpu = stats.getJSONObject(1).getJSONObject("cpu").getJSONObject("usage").getDouble("total");
                Double prevcpu = stats.getJSONObject(0).getJSONObject("cpu").getJSONObject("usage").getDouble("total");
                Double curmem = stats.getJSONObject(1).getJSONObject("memory").getDouble("usage");
                //long prevmem = stats.getJSONObject(0).getJSONObject("memory").getLong("usage");
                Double currx = stats.getJSONObject(1).getJSONObject("network").getJSONArray("interfaces").getJSONObject(0).getDouble("rx_bytes");
                Double prevrx = stats.getJSONObject(0).getJSONObject("network").getJSONArray("interfaces").getJSONObject(0).getDouble("rx_bytes");
                Double curtx = stats.getJSONObject(1).getJSONObject("network").getJSONArray("interfaces").getJSONObject(0).getDouble("tx_bytes");
                Double prevtx = stats.getJSONObject(0).getJSONObject("network").getJSONArray("interfaces").getJSONObject(0).getDouble("tx_bytes");
                Double intervalNs = this.getInterval(curdate, prevdate);
                Double cpuUsage = (curcpu - prevcpu) / intervalNs;
                Double memUsage = curmem / memlimit * 100;
                Double rxUsage = (currx - prevrx) / intervalNs * 1000000000;
                Double txUsage = (curtx - prevtx) / intervalNs * 1000000000;
                Map<String ,String> usagemap = new HashMap<String ,String>();
                usagemap.put("container_name", container_name);
                usagemap.put("container_time", curdate);
                usagemap.put("cpuUsage", cpuUsage.toString());
                usagemap.put("memUsage", memUsage.toString());
                usagemap.put("rxUsage", rxUsage.toString());
                usagemap.put("txUsage", txUsage.toString());
                result.add(usagemap);
            }
        }
        return result;
    }

    private double getInterval(String curdate, String prevdate) {
        curdate = curdate.replace("T", " ").replace("Z", "");
        prevdate = prevdate.replace("T", " ").replace("Z", "");
        double cur = Timestamp.valueOf(curdate).getTime();
        double prev = Timestamp.valueOf(prevdate).getTime();

        return (cur - prev) * 1000000;
    }


}
