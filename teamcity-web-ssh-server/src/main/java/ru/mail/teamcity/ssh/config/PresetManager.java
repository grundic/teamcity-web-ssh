package ru.mail.teamcity.ssh.config;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * User: g.chernyshev
 * Date: 03/06/15
 * Time: 00:36
 */
public final class PresetManager {
    @NotNull
    private static final String CONFIG_FOLDER_NAME = "presets";

    private PresetManager() {
    }

    private static final LoadingCache<Pair<SUser, String>, PresetBean> cache = CacheBuilder.
            newBuilder().
            expireAfterAccess(12, TimeUnit.HOURS).
            build(
                    new CacheLoader<Pair<SUser, String>, PresetBean>() {
                        @Override
                        public PresetBean load(@NotNull Pair<SUser, String> key) throws JAXBException, PresetNotFoundException {
                            PresetBean bean = BasicBeanManager.getInstance().load(
                                    key.getFirst(), CONFIG_FOLDER_NAME, key.getSecond(), PresetBean.class
                            );

                            if (bean != null) {
                                List<HostBean> hosts = Lists.newArrayList();
                                for (HostBean host : HostManager.lazyList(key.getFirst())) {
                                    if ((host.getPresetId() != null) && host.getPresetId().equals(bean.getId())) {
                                        host.setPreset(bean);
                                        hosts.add(host);
                                    }
                                }
                                bean.setHosts(hosts);
                                return bean;
                            } else {
                                throw new PresetNotFoundException();
                            }
                        }
                    }
            );

    static LoadingCache<Pair<SUser, String>, PresetBean> getCache() {
        return cache;
    }

    /**
     * Load preset configuration from file.
     *
     * @param user user account
     * @param name name of config
     * @return loaded preset
     * @throws JAXBException
     * @throws PresetNotFoundException
     */
    @NotNull
    public static PresetBean load(@NotNull SUser user, @NotNull String name) throws JAXBException, PresetNotFoundException {
        try {
            return cache.get(new Pair<>(user, name));
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(e.getCause(), JAXBException.class, PresetNotFoundException.class);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Return list of available presets for specific user.
     *
     * @param user user account
     * @return list of available presets for specific user
     * @throws JAXBException
     * @throws PresetNotFoundException
     */
    @NotNull
    public static List<PresetBean> list(@NotNull SUser user) throws JAXBException, PresetNotFoundException {
        List<PresetBean> beans = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(user, CONFIG_FOLDER_NAME)) {
            PresetBean bean = load(user, filename);
            beans.add(bean);
        }
        return beans;
    }

    /**
     * Return list of available presets for specific user.
     * This method doesn't throws an exception, instead it fill errors container.
     *
     * @param user   user account
     * @param errors error container
     * @return list of available presets for specific user
     */
    @NotNull
    public static List<PresetBean> list(@NotNull SUser user, @NotNull ActionErrors errors) {
        List<PresetBean> beans = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(user, CONFIG_FOLDER_NAME)) {
            try {
                beans.add(load(user, filename));
            } catch (JAXBException | PresetNotFoundException e) {
                e.printStackTrace();
                errors.addError(filename, ExceptionUtil.getDisplayMessage(e));
            }
        }
        return beans;
    }


    /**
     * Save configuration to file.
     *
     * @param user user account
     * @param bean data bean, that is to be saved
     * @throws JAXBException
     */
    public static void save(@NotNull SUser user, PresetBean bean) throws JAXBException {
        BasicBeanManager.getInstance().save(user, CONFIG_FOLDER_NAME, bean);
        cache.invalidate(new Pair<>(user, bean.getId().toString()));
    }

    /**
     * Remove specified preset config.
     *
     * @param user user account
     * @param name name of config to remove
     */
    public static void delete(@NotNull SUser user, @NotNull String name) {
        BasicBeanManager.getInstance().delete(user, CONFIG_FOLDER_NAME, name);
        cache.invalidate(new Pair<>(user, name));
    }
}
