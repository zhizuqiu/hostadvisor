package com.unicom.chanyi.cadvisor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpUtil {
    // get方法获取message
    public static HttpGetInfo getByHttpGet(String url) throws Exception {
        HttpGetInfo info = new HttpGetInfo();
        String result = null;
        String status = "";
        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpGet get = new HttpGet(url);
            //数据类型转化
            //String encoding = DatatypeConverter.printBase64Binary("user:password".getBytes("UTF-8"));
            HttpResponse response = httpclient.execute(get);
            //从响应头得到实体
            HttpEntity entity = response.getEntity();
            //获取httpstatus
            int httpcode = response.getStatusLine().getStatusCode();
            status = httpcode + "";
            if (entity != null) {
                InputStream instreams;
                instreams = entity.getContent();
                result = new String(convertStreamToString(instreams).getBytes(), "UTF-8");
                get.abort();
            }
        } catch (IOException e) {
            System.out.print(e.getMessage());
            status = "exception";
            info.setStatus(status);
            info.setMessage(result);
            return info;
        }

        //获取成功的时候
        info.setStatus(status);
        info.setMessage(result);
        return info;
    }

    // 将流转化为字符串
    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
