package ru.mail.teamcity.ssh.config;

import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.teamcity.ssh.AppConfiguration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Author: g.chernyshev
 * Date: 29.04.15
 */
public class ConfigHelper {
    @NotNull
    private static final ConcurrentMap<String, Object> fileLock = new ConcurrentHashMap<String, Object>();

    @Nullable
    private static HostBean read(@NotNull final File file) throws JAXBException {
        synchronized (getOrCreateLock(file)) {
            JAXBContext context = JAXBContext.newInstance(HostBean.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (file.isFile()) {
                HostBean host = (HostBean) unmarshaller.unmarshal(file);
                if (EncryptUtil.isScrambled(host.getPassword())) {
                    host.setPassword(EncryptUtil.unscramble(host.getPassword()));
                }
                return host;
            }
            return null;
        }
    }

    private static void write(@NotNull final File file, @NotNull HostBean host) throws JAXBException {
        synchronized (getOrCreateLock(file)) {
            host.setPassword(EncryptUtil.scramble(host.getPassword()));
            JAXBContext context = JAXBContext.newInstance(HostBean.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(host, file);
        }
    }

    @NotNull
    private synchronized static Object getOrCreateLock(@NotNull final File file) {
        final String fileName = file.getAbsolutePath();
        fileLock.putIfAbsent(fileName, new Object());
        return fileLock.get(fileName);
    }

    @NotNull
    private static File getPluginFolder(@NotNull ServerPaths serverPaths) {
        return new File(serverPaths.getPluginDataDirectory(), AppConfiguration.PLUGIN_NAME);
    }

    @NotNull
    private static File getUserFolder(@NotNull ServerPaths serverPaths, @NotNull SUser user) {
        return new File(getPluginFolder(serverPaths), user.getUsername());
    }

    @NotNull
    private static File getHostFile(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String name) {
        // TODO: how to work properly with file extensions in Java?
        if (!name.endsWith(".xml")) {
            name += ".xml";
        }
        return new File(getUserFolder(serverPaths, user), name);
    }

    public static List<String> listHostFiles(@NotNull ServerPaths serverPaths, @NotNull SUser user) {
        List<String> files = new ArrayList<String>();
        File userFolder = getUserFolder(serverPaths, user);
        if (!userFolder.exists()) {
            return files;
        }
        Collections.addAll(files, userFolder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        }));
        return files;
    }

    public static List<HostBean> hosts(@NotNull ServerPaths serverPaths, @NotNull SUser user) throws JAXBException {
        List<HostBean> hosts = new ArrayList<HostBean>();

        for (String filename : listHostFiles(serverPaths, user)) {
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
        File hostConfig = getHostFile(serverPaths, user, name);
        if (!hostConfig.exists()) {
            return null;
        }
        return read(hostConfig);
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
        File pluginFolder = getPluginFolder(serverPaths);
        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }
        File userFolder = getUserFolder(serverPaths, user);
        if (!userFolder.exists()) {
            userFolder.mkdir();
        }

        if (null == bean.getId()) {
            bean.setId(UUID.randomUUID());
        }

        File hostConfig = getHostFile(serverPaths, user, bean.getId().toString());
        write(hostConfig, bean);
    }

    public static void delete(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String name) {
        File hostConfig = getHostFile(serverPaths, user, name);
        if (hostConfig.exists()) {
            hostConfig.delete();
        }
    }
}
