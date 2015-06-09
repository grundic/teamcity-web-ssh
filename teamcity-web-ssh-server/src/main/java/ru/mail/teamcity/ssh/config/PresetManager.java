package ru.mail.teamcity.ssh.config;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * User: g.chernyshev
 * Date: 03/06/15
 * Time: 00:36
 */
public class PresetManager {
    @NotNull
    private static String CONFIG_FOLDER_NAME = "presets";

    private final static LoadingCache<Pair<SUser, String>, PresetBean> cache = CacheBuilder.
            newBuilder().
            expireAfterAccess(12, TimeUnit.HOURS).
            build(
                    new CacheLoader<Pair<SUser, String>, PresetBean>() {
                        @Override
                        public PresetBean load(@NotNull Pair<SUser, String> key) throws JAXBException {
                            PresetBean bean = BasicBeanManager.getInstance().load(
                                    key.getFirst(), key.getSecond(), CONFIG_FOLDER_NAME, PresetBean.class
                            );

                            if (null != bean) {
                                List<HostBean> hosts = Lists.newArrayList();
                                for (HostBean host : HostManager.lazyList(key.getFirst())) {
                                    if (null != host.getPresetId() && host.getPresetId().equals(bean.getId())) {
                                        host.setPreset(bean);
                                        hosts.add(host);
                                    }
                                }
                                bean.setHosts(hosts);
                            }
                            return bean;
                        }
                    }
            );

    static LoadingCache<Pair<SUser, String>, PresetBean> getCache() {
        return cache;
    }

    @Nullable
    public static PresetBean load(@NotNull SUser user, @NotNull String name) throws JAXBException {
        try {
            return cache.get(new Pair<>(user, name));
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(e.getCause(), JAXBException.class);
            throw new IllegalStateException(e);
        }
    }

    @NotNull
    public static List<PresetBean> list(@NotNull SUser user) throws JAXBException {
        List<PresetBean> beans = new ArrayList<>();

        for (String filename : BasicBeanManager.getInstance().listConfigurationFiles(user, CONFIG_FOLDER_NAME)) {
            PresetBean bean = load(user, filename);
            beans.add(bean);
        }
        return beans;
    }

    public static void save(@NotNull SUser user, PresetBean bean) throws IOException, JAXBException {
        BasicBeanManager.getInstance().save(user, CONFIG_FOLDER_NAME, bean);
        cache.invalidate(new Pair<>(user, bean.getId().toString()));
    }

    public static void delete(@NotNull SUser user, @NotNull String name) {
        BasicBeanManager.getInstance().delete(user, CONFIG_FOLDER_NAME, name);
        cache.invalidate(new Pair<>(user, name));
    }

}
