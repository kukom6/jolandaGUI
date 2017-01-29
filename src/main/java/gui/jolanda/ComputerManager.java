package gui.jolanda;

import gui.jolanda.enums.Status;


import java.awt.*;

import static gui.jolanda.utils.NetworkUtils.sendWOL;
import static gui.jolanda.utils.PingUtils.pingComputer;
import static gui.jolanda.utils.NetworkUtils.runCommandInHost;
import static gui.jolanda.utils.WaitUtils.pause;
import static java.lang.Math.round;


/**
 * Created by matej on 27.01.2017.
 */

public class ComputerManager {

    public static final int WAIT_LIMIT = 20;
    private Computer computer;

    public ComputerManager(Computer computer) {
        this.computer = computer;
    }

    /**
     * Shutdown pc via ssh
     */
    public void powerOff() {
        if (!isRunning()) {
            System.out.println(computer.getName() + " is already offline");
            return;
        }

        System.out.println(computer.getName() + " will be shut down. Please wait");

        executeCommand("shutdown -h now");

        pause(10000);
    }

    /**
     * Power on pc via WOL
     */
    public void powerOn() {
        if (isRunning()) {
            System.out.println(computer.getName() + " is already running");
            return;
        }
        sendWOL(computer);
        System.out.println("Wake-on-LAN packet sent.");
        System.out.println(computer.getName() + " is starting. Please wait");

        int limit = WAIT_LIMIT;

        long startTime = System.currentTimeMillis();

        while (!isRunning() && limit != 0) {
            pause(3000);
            limit--;
        }

        if (limit == 0) {
            System.out.println(computer.getName() + " haven't been started in 60 second. Check status manually");
        } else {
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println(computer.getName() + " is in good shape, isn't she? It took her only " + round(elapsedTime / 1000) + " s to turn on.");
        }
    }

    /**
     * Check temperature via ssh
     */
    public void checkTemperature() {
        executeCommand("sensors");
    }

    /**
     * Check if server is running
     *
     * @return true or false
     */
    public boolean isRunning() {
        return computer.getStatus() == Status.RUNNING;
    }

    /**
     * Check ping
     * Better way is via InetAddress class but it is not working correctly
     *
     * @return true if pc is running otherwise false
     */
    public void checkStatus() {
        computer.setStatus(pingComputer(computer));
    }

    public void executeCommand(String command) {
        runCommandInHost(computer, command);
    }

}
