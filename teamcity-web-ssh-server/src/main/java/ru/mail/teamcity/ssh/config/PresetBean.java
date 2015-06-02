package ru.mail.teamcity.ssh.config;

import jetbrains.buildServer.controllers.StateField;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Author: g.chernyshev
 * Date: 02.06.15
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class PresetBean extends AbstractBean {
    @StateField
    String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
