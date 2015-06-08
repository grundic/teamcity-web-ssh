package ru.mail.teamcity.ssh.config;

import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: g.chernyshev
 * Date: 02.06.15
 */
public class HostManager {
    private static String CONFIG_FOLDER_NAME = "hosts";


    @NotNull
    public static List<HostBean> list(@NotNull ServerPaths serverPaths, @NotNull SUser user) throws JAXBException {
        List<HostBean> hosts = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(serverPaths, user, CONFIG_FOLDER_NAME)) {
            HostBean host = load(serverPaths, user, filename);
            hosts.add(host);
        }
        return hosts;
    }

    /**
     * Load host configuration from file.
     *
     * @param serverPaths - instance of server paths
     * @param user        - Teamcity user, for whom configuration is loaded
     * @param name        - filename of host, which configuration is loaded
     * @return configuration for given user/host
     * @throws JAXBException
     */
    @Nullable
    public static HostBean load(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String name) throws JAXBException {
        HostBean bean = lazyLoad(serverPaths, user, name);
        if (null != bean && null != bean.getPresetId()) {
            PresetBean preset = PresetManager.load(serverPaths, user, bean.getPresetId().toString());
            bean.setPreset(preset);
        }
        return bean;
    }

    protected static HostBean lazyLoad(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String name) throws JAXBException {
        return BasicBeanManager.getInstance().load(serverPaths, user, name, CONFIG_FOLDER_NAME, HostBean.class);
    }

    protected static List<HostBean> lazyList(@NotNull ServerPaths serverPaths, @NotNull SUser user) throws JAXBException {
        List<HostBean> hosts = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(serverPaths, user, CONFIG_FOLDER_NAME)) {
            HostBean host = lazyLoad(serverPaths, user, filename);
            hosts.add(host);
        }
        return hosts;
    }

    @Nullable
    public static HostBean findHostByIp(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String ip) throws JAXBException {
        InetAddress requiredIp;

        try {
            requiredIp = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            return null;
        }
        for (HostBean host : list(serverPaths, user)) {
            try {
                InetAddress hostIp = InetAddress.getByName(host.getHost());
                if (hostIp.getHostAddress().equalsIgnoreCase(requiredIp.getHostAddress())) {
                    return host;
                }
            } catch (UnknownHostException e) {
                // skip it
            }
        }
        return null;
    }

    /**
     * Save configuration to file.
     *
     * @param serverPaths - instance of server paths
     * @param user        - Teamcity user, for whom configuration is saved
     * @param bean        - data bean, that is to be saved
     * @throws IOException
     * @throws JAXBException
     */
    public static void save(@NotNull ServerPaths serverPaths, @NotNull SUser user, HostBean bean) throws IOException, JAXBException {
        BasicBeanManager.getInstance().save(serverPaths, user, CONFIG_FOLDER_NAME, bean);
    }

    public static void delete(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String name) {
        BasicBeanManager.getInstance().delete(serverPaths, user, CONFIG_FOLDER_NAME, name);
    }
}

