package ru.mail.teamcity.ssh.config;

import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.users.SUser;
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

    private void write(@NotNull final File file, @NotNull AbstractBean bean) throws JAXBException {
        synchronized (getOrCreateLock(file)) {
            bean.setPassword(EncryptUtil.scramble(bean.getPassword()));
            bean.setPrivateKey(EncryptUtil.scramble(bean.getPrivateKey()));
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

    @NotNull
    private File getPluginFolder() {
        return new File(serverPaths.getPluginDataDirectory(), AppConfiguration.PLUGIN_NAME);
    }

    @NotNull
    private File getUserFolder(@NotNull SUser user) {
        return new File(getPluginFolder(), user.getUsername());
    }

    @NotNull
    private File getRootFolder(@NotNull SUser user, @NotNull String configFolder) {
        return new File(getUserFolder(user), configFolder);
    }

    @NotNull
    private File getConfigurationFile(@NotNull SUser user, @NotNull String name, @NotNull String configFolder) {
        String filename = FilenameUtils.getExtension(name).equalsIgnoreCase(CFG_EXT) ? name : (name + "." + CFG_EXT);
        return new File(getRootFolder(user, configFolder), filename);
    }

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

    @Nullable
    <T extends AbstractBean> T load(@NotNull SUser user, @NotNull String name, @NotNull String configFolder, Class<T> type) throws JAXBException {
        File hostConfig = getInstance().getConfigurationFile(user, name, configFolder);
        if (!hostConfig.exists()) {
            return null;
        }
        return type.cast(read(hostConfig));
    }

    void save(@NotNull SUser user, @NotNull String configFolder, @NotNull AbstractBean bean) throws JAXBException {
        if (bean.getId() == null) {
            bean.setId(UUID.randomUUID());
        }

        File hostConfig = getInstance().getConfigurationFile(user, bean.getId().toString(), configFolder);
        hostConfig.getParentFile().mkdirs();
        write(hostConfig, bean);
    }

    void delete(@NotNull SUser user, @NotNull String configFolder, @NotNull String name) {
        File hostConfig = getInstance().getConfigurationFile(user, name, configFolder);
        if (hostConfig.exists()) {
            hostConfig.delete();
        }
    }
}
