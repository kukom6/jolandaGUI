package gui.jolanda.utils;

import gui.jolanda.Computer;
import gui.jolanda.enums.Status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author mhajas
 */
public class PingUtils {
    public static final int TIMEOUT = 20;

    /**
     * Check ping
     * Better way is via InetAddress class but it is not working correctly
     * @return true if pc is running otherwise false
     * TODO: Probably won't work on Windows, it is necessary to change win command to send only one packet
     */
    public static Status pingComputer(Computer pc) {
        String pingCmd = getCommand(pc.getIpAddress());
        boolean attempt1;
        boolean attempt2;
        try {
            Runtime r = Runtime.getRuntime();
            do {
                attempt1 = r.exec(pingCmd).waitFor(TIMEOUT, TimeUnit.MILLISECONDS);
                attempt2 = r.exec(pingCmd).waitFor(TIMEOUT, TimeUnit.MILLISECONDS);
            } while(attempt1 != attempt2);
            return attempt1 ? Status.RUNNING : Status.OFFLINE;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getCommand(String host) {
        if (System.getProperty("os.name").contains("Win")) {
            return "ping -n 1 -i 3 " + host;
        } else {
            return "ping -c 1 " + host;
        }
    }
}


