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
 * Manager for working with host configs.
 * <p/>
 * Author: g.chernyshev
 * Date: 02.06.15
 */
public final class HostManager {
    private static final String CONFIG_FOLDER_NAME = "hosts";

    private HostManager() {
    }

    private static final LoadingCache<Pair<SUser, String>, HostBean> cache = CacheBuilder.
            newBuilder().
            expireAfterAccess(12, TimeUnit.HOURS).
            build(
                    new CacheLoader<Pair<SUser, String>, HostBean>() {
                        @Override
                        public HostBean load(@NotNull Pair<SUser, String> key) throws JAXBException, HostNotFoundException, PresetNotFoundException {
                            HostBean bean = HostManager.lazyLoad(key.getFirst(), key.getSecond());
                            if (bean != null) {
                                if (bean.getPresetId() != null) {
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

    /**
     * Load host configuration from file.
     *
     * @param user user account, for whom configuration is loaded
     * @param name filename of host, which configuration is loaded
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

    /**
     * Lazy load host configuration from file.
     * This method doesn't use cache and doesn't load preset of the host.
     *
     * @param user user account, for whom configuration is loaded
     * @param name filename of host, which configuration is loaded
     * @return
     * @throws JAXBException
     */
    private static HostBean lazyLoad(@NotNull SUser user, @NotNull String name) throws JAXBException {
        return BasicBeanManager.getInstance().load(user, CONFIG_FOLDER_NAME, name, HostBean.class);
    }

    /**
     * Return list of available hosts for specific user.
     *
     * @param user user account
     * @return list of available hosts for specific user
     * @throws JAXBException
     * @throws HostNotFoundException
     */
    @NotNull
    static List<HostBean> list(@NotNull SUser user) throws JAXBException, HostNotFoundException {
        List<HostBean> hosts = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(user, CONFIG_FOLDER_NAME)) {
            HostBean host = load(user, filename);
            hosts.add(host);
        }
        return hosts;
    }

    /**
     * Return list of available hosts for specific user.
     * This method doesn't use cache and doesn't load preset of the host.
     *
     * @param user user account
     * @return list of available hosts for specific user
     * @throws JAXBException
     */
    static List<HostBean> lazyList(@NotNull SUser user) throws JAXBException {
        List<HostBean> hosts = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(user, CONFIG_FOLDER_NAME)) {
            HostBean host = lazyLoad(user, filename);
            hosts.add(host);
        }
        return hosts;
    }

    /**
     * Return list of available hosts for specific user.
     * This method doesn't throws an exception, instead it fill errors container.
     *
     * @param user   user account
     * @param errors error container
     * @return list of available hosts for specific user
     */
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
     * Returns host, matching specified ip address.
     *
     * @param user user account
     * @param ip ip address
     * @return host if it's found or null
     * @throws JAXBException
     * @throws HostNotFoundException
     */
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
     * @param user user account
     * @param bean data bean, that is to be saved
     * @throws JAXBException
     */
    public static void save(@NotNull SUser user, HostBean bean) throws JAXBException, HostNotFoundException {
        PresetBean originalPreset = null;

        if (bean.getId() != null) {
            HostBean originalHost = load(user, bean.getId().toString());
            originalPreset = originalHost.getPreset();
        }

        BasicBeanManager.getInstance().save(user, CONFIG_FOLDER_NAME, bean);
        cache.invalidate(new Pair<>(user, bean.getId().toString()));

        if (bean.getPresetId() != null) {
            PresetManager.getCache().invalidate(new Pair<>(user, bean.getPresetId().toString()));
        }
        if (originalPreset != null) {
            PresetManager.getCache().invalidate(new Pair<>(user, originalPreset.getId().toString()));
        }
    }

    /**
     * Remove specified host config.
     *
     * @param user user account
     * @param name name of config to remove
     */
    public static void delete(@NotNull SUser user, @NotNull String name) {
        BasicBeanManager.getInstance().delete(user, CONFIG_FOLDER_NAME, name);
        cache.invalidate(new Pair<>(user, name));
    }
}

