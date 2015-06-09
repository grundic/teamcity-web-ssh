package ru.mail.teamcity.ssh.config;

import com.google.common.collect.Lists;
import jetbrains.buildServer.controllers.StateField;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * Author: g.chernyshev
 * Date: 02.06.15
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class PresetBean extends AbstractBean {

    public static PresetBean newInstance(PresetBean another){
        PresetBean bean = new PresetBean();
        bean.setId(another.getId());
        bean.setLogin(another.getLogin());
        bean.setPassword(another.getPassword());
        bean.setPrivateKey(another.getPrivateKey());

        bean.setName(another.getName());
        bean.setHosts(another.getHosts());
        return bean;
    }

    @StateField
    String name = "";

    List<HostBean> hosts = Lists.newArrayList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public List<HostBean> getHosts() {
        return hosts;
    }

    public void setHosts(List<HostBean> hosts) {
        this.hosts = hosts;
    }
}
