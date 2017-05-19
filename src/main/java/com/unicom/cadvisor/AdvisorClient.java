package com.unicom.cadvisor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;






public class AdvisorClient {
	
	
	public static List getUsage(String json){
		List<Map> result = new ArrayList<Map>();
		JSONObject myJsonObject =  JSONObject.fromObject(json);
		Iterator it = myJsonObject.keys();  
        while (it.hasNext()) {  
             String key = it.next().toString(); 
             if(!key.equals("/docker")){
            	 JSONObject docker = myJsonObject.getJSONObject(key);
            	 JSONObject spec = docker.getJSONObject("spec");
            	 String container_name = spec.getJSONArray("aliases").getString(0);
            	 long memlimit = spec.getJSONObject("memory").getLong("limit");
            	 JSONArray stats = docker.getJSONArray("stats");
            	 String curdate = stats.getJSONObject(1).getString("timestamp");
            	 String prevdate = stats.getJSONObject(0).getString("timestamp");
            	 double curcpu = stats.getJSONObject(1).getJSONObject("cpu").getJSONObject("usage").getDouble("total");
            	 double prevcpu = stats.getJSONObject(0).getJSONObject("cpu").getJSONObject("usage").getDouble("total");
            	 double curmem = stats.getJSONObject(1).getJSONObject("memory").getDouble("usage");
            	 //long prevmem = stats.getJSONObject(0).getJSONObject("memory").getLong("usage");
            	 double currx = stats.getJSONObject(1).getJSONObject("network").getJSONArray("interfaces").getJSONObject(0).getDouble("rx_bytes");
            	 double prevrx = stats.getJSONObject(0).getJSONObject("network").getJSONArray("interfaces").getJSONObject(0).getDouble("rx_bytes");
            	 double curtx = stats.getJSONObject(1).getJSONObject("network").getJSONArray("interfaces").getJSONObject(0).getDouble("tx_bytes");
            	 double prevtx = stats.getJSONObject(0).getJSONObject("network").getJSONArray("interfaces").getJSONObject(0).getDouble("tx_bytes");
            	 double intervalNs = getInterval(curdate,prevdate);
            	 System.out.println(intervalNs);
            	 double cpuUsage = (curcpu-prevcpu)/intervalNs;
            	 System.out.println(curcpu-prevcpu);
            	 double memUsage = curmem/memlimit*100;
            	 double rxUsage = (currx-prevrx)/intervalNs*1000000000;
            	 double txUsage = (curtx-prevtx)/intervalNs*1000000000;
            	 Map usagemap = new HashMap();
            	 usagemap.put("container_name", container_name);
            	 usagemap.put("time", curdate);
            	 usagemap.put("cpuUsage", cpuUsage);
            	 usagemap.put("memUsage", memUsage);
            	 usagemap.put("rxUsage", rxUsage);
            	 usagemap.put("txUsage", txUsage);
            	 result.add(usagemap);
             }
             
        }
		return result;
	}

	public static double getInterval(String curdate,String prevdate){
		curdate = curdate.replace("T", " ").replace("Z", "");
		prevdate = prevdate.replace("T", " ").replace("Z", "");
		double cur = Timestamp.valueOf(curdate).getTime();
		double prev = Timestamp.valueOf(prevdate).getTime();
		
		return (cur-prev)* 1000000;
	}
	public static void main(String[] args) {
		String url = "/api/v2.1/stats/docker?type=name&recursive=true&count=2";
		List hostlist = new ArrayList();
		hostlist.add("http://10.161.24.226:9099"+url);
		hostlist.add("http://10.161.24.227:9099"+url);
		hostlist.add("http://10.161.24.249:9099"+url);
		
		AdvisorClient advisorClient = new AdvisorClient();
		List result = new ArrayList();
		for(int i=0;i<hostlist.size();i++) {
			String json = "";
			try {
				HttpGetInfo info = HttpUtil.getByHttpGet(hostlist.get(0).toString());
				json = info.getMessage();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(json);

			List usage = advisorClient.getUsage(json);
			result.addAll(usage);
		}
		System.out.println(result);
		
    	
	}
	
	
	
}
