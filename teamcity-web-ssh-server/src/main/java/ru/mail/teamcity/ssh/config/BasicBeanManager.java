package ru.mail.teamcity.ssh.config;

import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.teamcity.ssh.AppConfiguration;
import ru.mail.teamcity.ssh.ApplicationContextProvider;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Low level class for working with config beans.
 *
 * This class is a singleton, methods of which should be used in
 * <code>HostManager</code> and <code>PresetManager</code> classes.
 *
 * Author: g.chernyshev
 * Date: 02.06.15
 */
public final class BasicBeanManager {

    private static BasicBeanManager instance = null;

    @NotNull
    private final String CFG_EXT = "xml";

    @NotNull
    private final ConcurrentMap<String, Object> fileLock = new ConcurrentHashMap<>();

    @NotNull
    private static final ServerPaths serverPaths = new ApplicationContextProvider().getApplicationContext().getBean("serverPaths", ServerPaths.class);


    private BasicBeanManager() {
    }

    /**
     * Get instance of <code>BasicBeanManager</code>.
     *
     * @return a BasicBeanManager
     */
    public static BasicBeanManager getInstance() {
        if (instance == null) {
            synchronized (BasicBeanManager.class) {
                if (instance == null) {
                    instance = new BasicBeanManager();
                }
            }
        }
        return instance;
    }

    /**
     * Deserialize jaxb object stored in specified file.
     *
     * @param file the file to unmarshal data from
     * @return a newly created AbstractBean object
     * @throws JAXBException
     */
    @Nullable
    private AbstractBean read(@NotNull final File file) throws JAXBException {
        synchronized (getOrCreateLock(file)) {
            JAXBContext context = JAXBContext.newInstance(AbstractBean.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (file.isFile()) {
                AbstractBean bean = (AbstractBean) unmarshaller.unmarshal(file);
                if (EncryptUtil.isScrambled(bean.getPassword())) {
                    bean.setPassword(EncryptUtil.unscramble(bean.getPassword()));
                }
                if (EncryptUtil.isScrambled(bean.getPrivateKey())) {
                    bean.setPrivateKey(EncryptUtil.unscramble(bean.getPrivateKey()));
                }
                return bean;
            }
            return null;
        }
    }

    /**
     * Serialize jaxb object and save it to specified file.
     *
     * @param file file to write object to
     * @param bean object to be marshaled
     * @throws JAXBException
     */
    private void write(@NotNull final File file, @NotNull AbstractBean bean) throws JAXBException {
        synchronized (getOrCreateLock(file)) {
            if (!StringUtil.isEmpty(bean.getPassword())) {
                bean.setPassword(EncryptUtil.scramble(bean.getPassword()));
            }
            if (!StringUtil.isEmpty(bean.getPrivateKey())) {
                bean.setPrivateKey(EncryptUtil.scramble(bean.getPrivateKey()));
            }
            JAXBContext context = JAXBContext.newInstance(AbstractBean.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(bean, file);
        }
    }

    @NotNull
    private synchronized Object getOrCreateLock(@NotNull final File file) {
        final String fileName = file.getAbsolutePath();
        fileLock.putIfAbsent(fileName, new Object());
        return fileLock.get(fileName);
    }

    /**
     * Return path to the current plugin folder.
     *
     * @return path to the plugins folder
     */
    @NotNull
    private File getPluginFolder() {
        return new File(serverPaths.getPluginDataDirectory(), AppConfiguration.PLUGIN_NAME);
    }

    /**
     * Return path on the disk of the specified user account.
     *
     * @param user user account to get path for
     * @return path on the disk of the specified user account
     */
    @NotNull
    private File getUserFolder(@NotNull SUser user) {
        return new File(getPluginFolder(), user.getUsername());
    }


    /**
     * Return root path for some configuration.
     * Plugin stores host and presets in different folders and this method helps to distinguish them.
     *
     * @param user         user account to get path for
     * @param configFolder name of the root configuration folder
     * @return root path for some configuration
     */
    @NotNull
    private File getRootFolder(@NotNull SUser user, @NotNull String configFolder) {
        return new File(getUserFolder(user), configFolder);
    }

    /**
     * Return path to specific configuration file.
     *
     * @param user user account to get path for
     * @param configFolder name of the root configuration folder
     * @param name name of config file
     * @return path to specific configuration file
     */
    @NotNull
    private File getConfigurationFile(@NotNull SUser user, @NotNull String configFolder, @NotNull String name) {
        String filename = FilenameUtils.getExtension(name).equalsIgnoreCase(CFG_EXT) ? name : (name + "." + CFG_EXT);
        return new File(getRootFolder(user, configFolder), filename);
    }

    /**
     * Returns list of available configuration files for specified configuration/user
     * File names should have `.xml` extension and be valid GUIDs.
     *
     * @param user user account to get configs for
     * @param configFolder name of the root configuration folder
     * @return list of available configuration files for specified configuration/user
     */
    @NotNull
    List<String> listConfigurationFiles(@NotNull SUser user, @NotNull String configFolder) {
        List<String> files = new ArrayList<>();

        File root = getRootFolder(user, configFolder);
        if (!root.exists()) {
            return files;
        }

        for (String filename : root.list()) {
            if (FilenameUtils.getExtension(filename).equalsIgnoreCase(CFG_EXT)) {
                String name = FilenameUtils.removeExtension(filename);

                try {
                    //noinspection ResultOfMethodCallIgnored
                    UUID.fromString(name);
                    files.add(name);
                } catch (IllegalArgumentException e) {
                    // skip file not matched to UUID
                }
            }
        }

        return files;
    }

    /**
     * Returns unmarshalled object of type <code>T<code/>, loaded from specified path.
     *
     * @param user user account to get config for
     * @param configFolder name of the root configuration folder
     * @param name name of config file
     * @param type type of returned object
     * @return unmarshalled object of type <code>T<code/>, loaded from specified path
     * @throws JAXBException
     */
    @Nullable
    <T extends AbstractBean> T load(@NotNull SUser user, @NotNull String configFolder, @NotNull String name, Class<T> type) throws JAXBException {
        File hostConfig = getInstance().getConfigurationFile(user, configFolder, name);
        if (!hostConfig.exists()) {
            return null;
        }
        return type.cast(read(hostConfig));
    }

    /**
     * Stores specified bean in file.
     *
     * @param user user account for whom config will be saved
     * @param configFolder name of the root configuration folder
     * @param bean object to save
     * @throws JAXBException
     */
    void save(@NotNull SUser user, @NotNull String configFolder, @NotNull AbstractBean bean) throws JAXBException {
        if (bean.getId() == null) {
            bean.setId(UUID.randomUUID());
        }

        File hostConfig = getInstance().getConfigurationFile(user, configFolder, bean.getId().toString());
        hostConfig.getParentFile().mkdirs();
        write(hostConfig, bean);
    }

    /**
     * Removes specified config file.
     * @param user user account
     * @param configFolder name of the root configuration folder
     * @param name name of config file
     */
    void delete(@NotNull SUser user, @NotNull String configFolder, @NotNull String name) {
        File hostConfig = getInstance().getConfigurationFile(user, configFolder, name);
        if (hostConfig.exists()) {
            hostConfig.delete();
        }
    }
}
