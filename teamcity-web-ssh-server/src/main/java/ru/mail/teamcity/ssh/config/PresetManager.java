package ru.mail.teamcity.ssh.config;

import com.google.common.collect.Lists;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: g.chernyshev
 * Date: 03/06/15
 * Time: 00:36
 */
public class PresetManager {
    protected static String CONFIG_FOLDER_NAME = "presets";

    @Nullable
    public static PresetBean load(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String name) throws JAXBException {
        PresetBean bean = BasicBeanManager.getInstance().load(serverPaths, user, name, CONFIG_FOLDER_NAME, PresetBean.class);
        if (null != bean) {
            List<HostBean> hosts = Lists.newArrayList();
            for (HostBean host : HostManager.lazyList(serverPaths, user)) {
                if (null != host.getPresetId() && host.getPresetId().equals(bean.getId())) {
                    host.setPreset(bean);
                    hosts.add(host);
                }
            }
            bean.setHosts(hosts);
        }
        return bean;
    }

    @NotNull
    public static List<PresetBean> list(@NotNull ServerPaths serverPaths, @NotNull SUser user) throws JAXBException {
        List<PresetBean> beans = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(serverPaths, user, CONFIG_FOLDER_NAME)) {
            PresetBean bean = load(serverPaths, user, filename);
            beans.add(bean);
        }
        return beans;
    }

    public static void save(@NotNull ServerPaths serverPaths, @NotNull SUser user, PresetBean bean) throws IOException, JAXBException {
        BasicBeanManager.getInstance().save(serverPaths, user, CONFIG_FOLDER_NAME, bean);
    }

    public static void delete(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String name) {
        BasicBeanManager.getInstance().delete(serverPaths, user, CONFIG_FOLDER_NAME, name);
    }

}
