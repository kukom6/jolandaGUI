package gui.jolanda;

import gui.jolanda.enums.Status;

/**
 * @author mhajas
 */
public class Computer {

    private String name;
    private String ipAddress;
    private String macAddress;
    private String userName;
    private String password;
    private Status status;

    public Computer(String name, String ipAddress, String macAddress, String userName, String password) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.userName = userName;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
