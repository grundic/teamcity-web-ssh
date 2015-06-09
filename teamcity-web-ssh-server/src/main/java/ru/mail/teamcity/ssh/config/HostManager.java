package ru.mail.teamcity.ssh.config;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Author: g.chernyshev
 * Date: 02.06.15
 */
public class HostManager {
    private static final String CONFIG_FOLDER_NAME = "hosts";

    private final static LoadingCache<Pair<SUser, String>, HostBean> cache = CacheBuilder.
            newBuilder().
            expireAfterAccess(12, TimeUnit.HOURS).
            build(
                    new CacheLoader<Pair<SUser, String>, HostBean>() {
                        @Override
                        public HostBean load(@NotNull Pair<SUser, String> key) throws JAXBException, HostNotFoundException, PresetNotFoundException {
                            HostBean bean = HostManager.lazyLoad(key.getFirst(), key.getSecond());
                            if (null != bean) {
                                if (null != bean.getPresetId()) {
                                    PresetBean preset = PresetManager.load(key.getFirst(), bean.getPresetId().toString());
                                    bean.setPreset(preset);
                                }
                                return bean;
                            } else {
                                throw new HostNotFoundException();
                            }
                        }
                    }
            );


    @NotNull
    private static List<HostBean> list(@NotNull SUser user) throws JAXBException, HostNotFoundException {
        List<HostBean> hosts = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(user, CONFIG_FOLDER_NAME)) {
            HostBean host = load(user, filename);
            hosts.add(host);
        }
        return hosts;
    }

    @NotNull
    public static List<HostBean> list(@NotNull SUser user, @NotNull ActionErrors errors) {
        List<HostBean> hosts = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(user, CONFIG_FOLDER_NAME)) {
            try {
                hosts.add(load(user, filename));
            } catch (JAXBException | HostNotFoundException e) {
                e.printStackTrace();
                errors.addError(filename, ExceptionUtil.getDisplayMessage(e));
            }
        }
        return hosts;
    }

    /**
     * Load host configuration from file.
     *
     * @param user - Teamcity user, for whom configuration is loaded
     * @param name - filename of host, which configuration is loaded
     * @return configuration for given user/host
     * @throws JAXBException
     */
    @NotNull
    public static HostBean load(@NotNull SUser user, @NotNull String name) throws JAXBException, HostNotFoundException {
        try {
            return cache.get(new Pair<>(user, name));
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(e.getCause(), JAXBException.class, HostNotFoundException.class);
            throw new IllegalStateException(e);
        }
    }

    private static HostBean lazyLoad(@NotNull SUser user, @NotNull String name) throws JAXBException {
        return BasicBeanManager.getInstance().load(user, name, CONFIG_FOLDER_NAME, HostBean.class);
    }

    static List<HostBean> lazyList(@NotNull SUser user) throws JAXBException {
        List<HostBean> hosts = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(user, CONFIG_FOLDER_NAME)) {
            HostBean host = lazyLoad(user, filename);
            hosts.add(host);
        }
        return hosts;
    }

    @Nullable
    public static HostBean findHostByIp(@NotNull SUser user, @NotNull String ip) throws JAXBException, HostNotFoundException {
        InetAddress requiredIp;

        try {
            requiredIp = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            return null;
        }
        for (HostBean host : list(user)) {
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
     * @param user - Teamcity user, for whom configuration is saved
     * @param bean - data bean, that is to be saved
     * @throws JAXBException
     */
    public static void save(@NotNull SUser user, HostBean bean) throws JAXBException, HostNotFoundException {
        PresetBean originalPreset = null;
        HostBean originalHost;

        if (null != bean.getId()) {
            originalHost = load(user, bean.getId().toString());
            originalPreset = originalHost.getPreset();
        }

        BasicBeanManager.getInstance().save(user, CONFIG_FOLDER_NAME, bean);
        cache.invalidate(new Pair<>(user, bean.getId().toString()));

        if (null != bean.getPresetId()) {
            PresetManager.getCache().invalidate(new Pair<>(user, bean.getPresetId().toString()));
        }
        if (null != originalPreset) {
            PresetManager.getCache().invalidate(new Pair<>(user, originalPreset.getId().toString()));
        }
    }

    public static void delete(@NotNull SUser user, @NotNull String name) {
        BasicBeanManager.getInstance().delete(user, CONFIG_FOLDER_NAME, name);
        cache.invalidate(new Pair<>(user, name));
    }
}

