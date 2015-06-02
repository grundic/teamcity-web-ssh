package ru.mail.teamcity.ssh.config;

import jetbrains.buildServer.controllers.RememberState;
import jetbrains.buildServer.controllers.StateField;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.util.UUID;

/**
 * Author: g.chernyshev
 * Date: 02.06.15
 */
@SuppressWarnings("UnusedDeclaration")
@XmlSeeAlso({HostBean.class, PresetBean.class})
public abstract class AbstractBean extends RememberState {
    public AbstractBean() {
        // empty constructor for JAXB
    }

    UUID id = null;

    @StateField
    String login = "";

    @StateField
    String password = "";

    String encryptedPassword = "";

    @StateField
    String privateKey = "";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
