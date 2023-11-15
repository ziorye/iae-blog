package com.ziorye.proofread.util;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class SystemVarKit {
    public static String getCurrentAppDirPath() {
        return System.getProperty("user.dir");
    }

    public static String getCurrentUserName() {
        return System.getProperty("user.name");
    }

    public static String getCurrentUserDirPath() {
        return System.getProperty("user.home");
    }

    public static String getOsCacheDirPath() {
        return System.getProperty("java.io.tmpdir");
    }

    public static String getIPAddress() {
        InetAddress localHost = null;
        try {
            localHost = Inet4Address.getLocalHost();
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }
        assert localHost != null;
        return localHost.getHostAddress();
    }
}
