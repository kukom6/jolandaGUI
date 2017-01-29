import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.net.*;
import java.io.*;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by matej on 27.01.2017.
 */

public class PcManager {

    public enum Status {
        RUNNING,OFFLINE;
    }

    private String ipAddress;
    private String macAddress;
    private String userName;
    private String password;
    private Status status;

    public PcManager(String ipAddress, String macAddress, String userName, String password) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Shutdown pc via ssh
     */
    public void powerOff() {
        runCommandInHost("shutdown -h now");
        System.out.println("Jolanda will be killed");
    }

    /**
     * Power on pc via WOL
     */
    public void powerOn() {
        int PORT = 9;

        String ipStr = ipAddress;
        String macStr = macAddress;

        try {
            byte[] macBytes = getMacBytes(macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipStr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            System.out.println("Wake-on-LAN packet sent.");
        } catch (Exception e) {
            System.out.println("Failed to send Wake-on-LAN packet: + e");
            System.exit(1);
        }

    }

    /**
     * Check temperature via ssh
     */
    public void checkTemperature(){
        runCommandInHost("sensors");
    }

    /**
     * Check if server is running
     * @return true or false
     */
    public boolean isRunning(){
        this.checkPing();
        if(this.status==Status.RUNNING){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Check ping
     * Better way is via InetAddress class but it is not works correctly
     * @return true if pc is running otherwise false
     */
    public void checkPing() {
        System.out.println("!Wait for ping!");
        String pingResult = "";
        String pingCmd;
        if(System.getProperty("os.name").contains("Win")){
            pingCmd = "ping -n 1 -i 3 " + ipAddress;
        }else{
            pingCmd = "ping -c 3 -i 1 " + ipAddress;
        }
        try {
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(pingCmd);

            BufferedReader in = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                pingResult += inputLine;
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(System.getProperty("os.name").contains("Win")) {
            if (pingResult.contains("Reply from 192.168.0.66: bytes=32")) {
                this.status = Status.RUNNING;
            } else if (pingResult.contains("Reply from 192.168.0.1: Destination host unreachable.")) {
                this.status = Status.OFFLINE;
            } else {
                System.err.println("192.168.0.1 doesn't replay");
                this.status = Status.OFFLINE;
            }
        }else{
            if (pingResult.contains("64 bytes from 192.168.0.66: icmp_seq=1 ttl=64")) {
                this.status = Status.RUNNING;
            } else if (pingResult.contains("icmp_seq=1 Destination Host Unreachable")) {
                this.status = Status.OFFLINE;
            } else {
                System.err.println("192.168.0.1 doesn't reply");
                this.status = Status.OFFLINE;
            }
        }
    }

    /**
     * Run specific command on host
     * @param command specific comand
     */
    private void runCommandInHost(String command){
        try{
            JSch jsch=new JSch();

            Session session=jsch.getSession(userName, ipAddress, 22);

            session.setPassword(password);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            String cmd=command;

            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(cmd);

            channel.setInputStream(null);

            //         ((ChannelExec)channel).setErrStream(System.err);

            InputStream in=channel.getInputStream();

            channel.connect();

            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    System.out.print(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    if(in.available()>0) continue;
                    System.out.println("exit-status: "+channel.getExitStatus());
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect();
            session.disconnect();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

    /*
//better way how ping server but it doesn't work because all packet will received.
When host is offline, main router answer that host is unreachable but inet.isReachable is true because message are arrived
    public static boolean checkPing(String ipAddress) {
        InetAddress inet = null;
        try {
            inet = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        boolean reachable = false;
        try {
            reachable = inet.isReachable(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reachable;
    }
    */
}
