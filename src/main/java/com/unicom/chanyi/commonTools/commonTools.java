package com.unicom.chanyi.commonTools;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class commonTools {

    public static InetAddress getInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("unknown host!");
        }
        return null;

    }

    public static String getHostIp(InetAddress netAddress) {
        if (null == netAddress) {
            return null;
        }
        String ip = netAddress.getHostAddress(); //get the ip address
        return ip;
    }


}
