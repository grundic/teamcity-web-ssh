package ru.mail.teamcity.ssh.config;

import jetbrains.buildServer.controllers.RememberState;
import jetbrains.buildServer.controllers.StateField;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.UUID;

/**
 * Author: g.chernyshev
 * Date: 29.04.15
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class HostBean extends RememberState {

    public HostBean() {
        // empty constructor for JAXB
    }

    UUID id = null;

    @StateField
    String host = "";

    @StateField
    int port = 22;

    @StateField
    String login = "";

    @StateField
    String password = "";

    String encryptedPassword = "";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlTransient
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
}
