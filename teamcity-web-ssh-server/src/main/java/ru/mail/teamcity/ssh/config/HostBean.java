package ru.mail.teamcity.ssh.config;

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
public class HostBean extends AbstractBean {

    public static HostBean newInstance(HostBean another) {
        HostBean bean = new HostBean();
        bean.setId(another.getId());
        bean.setLogin(another.getLogin());
        bean.setPassword(another.getPassword());
        bean.setPrivateKey(another.getPrivateKey());
        bean.setTheme(another.getTheme());

        bean.setPresetId(another.getPresetId());
        bean.setPreset(another.getPreset());
        bean.setHost(another.getHost());
        bean.setPort(another.getPort());
        return bean;
    }

    UUID presetId = null;

    PresetBean preset = null;

    @StateField
    String host = "";

    @StateField
    int port = 22;

    @StateField
    String theme = "";

    public UUID getPresetId() {
        return presetId;
    }

    public void setPresetId(UUID presetId) {
        this.presetId = presetId;
    }

    @XmlTransient
    public PresetBean getPreset() {
        return preset;
    }

    public void setPreset(PresetBean preset) {
        this.preset = preset;
    }

    @XmlTransient
    public AbstractBean getDelegate() {
        return (preset == null) ? this : preset;
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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
