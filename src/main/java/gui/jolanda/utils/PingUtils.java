package gui.jolanda.utils;

import gui.jolanda.Computer;
import gui.jolanda.enums.Status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author mhajas
 */
public class PingUtils {

    public static final int TIMEOUT = System.getProperty("os.name").contains("Win") ? 60 : 40;

    /**
     * Check ping
     * Better way is via InetAddress class but it is not working correctly
     * @return true if pc is running otherwise false
     */
    public static Status pingComputer(Computer pc) {
        String pingCmd = getCommand(pc.getIpAddress());
        try {
            boolean attempt1;
            boolean attempt2;
            boolean attempt3;
            Runtime r = Runtime.getRuntime();
            do {
                attempt1 = r.exec(pingCmd).waitFor(TIMEOUT, TimeUnit.MILLISECONDS);
                attempt2 = r.exec(pingCmd).waitFor(TIMEOUT, TimeUnit.MILLISECONDS);
                attempt3 = r.exec(pingCmd).waitFor(TIMEOUT, TimeUnit.MILLISECONDS);
            } while(attempt1 != attempt2 && attempt3 == attempt1);
            return attempt1 ? Status.RUNNING : Status.OFFLINE;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getCommand(String host) {
        if (System.getProperty("os.name").contains("Win")) {
            return "ping -n 1 " + host;
        } else {
            return "ping -c 1 " + host;
        }
    }
}


