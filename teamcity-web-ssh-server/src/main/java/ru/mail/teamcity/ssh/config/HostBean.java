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

    public HostBean() {
        // empty constructor for JAXB
    }

    UUID presetId = null;

    PresetBean preset = null;

    @StateField
    String host = "";

    @StateField
    int port = 22;

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
}
