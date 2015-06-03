package ru.mail.teamcity.ssh.config;

import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.users.SUser;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.teamcity.ssh.AppConfiguration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Author: g.chernyshev
 * Date: 02.06.15
 */
public class BasicBeanManager {

    private static BasicBeanManager instance = null;

    @NotNull
    private final String CFG_EXT = "xml";

    @NotNull
    private final ConcurrentMap<String, Object> fileLock = new ConcurrentHashMap<String, Object>();

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
    private File getPluginFolder(@NotNull ServerPaths serverPaths) {
        return new File(serverPaths.getPluginDataDirectory(), AppConfiguration.PLUGIN_NAME);
    }

    @NotNull
    private File getUserFolder(@NotNull ServerPaths serverPaths, @NotNull SUser user) {
        return new File(getPluginFolder(serverPaths), user.getUsername());
    }

    @NotNull
    private File getRootFolder(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String configFolder) {
        return new File(getUserFolder(serverPaths, user), configFolder);
    }

    @NotNull
    protected File getConfigurationFile(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String name, @NotNull String configFolder) {
        if (!FilenameUtils.getExtension(name).equalsIgnoreCase(CFG_EXT)) {
            name += "." + CFG_EXT;
        }
        return new File(getRootFolder(serverPaths, user, configFolder), name);
    }

    @NotNull
    protected List<String> listConfigurationFiles(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String configFolder) {
        List<String> files = new ArrayList<String>();

        File root = getRootFolder(serverPaths, user, configFolder);
        if (!root.exists()) {
            return files;
        }
        Collections.addAll(files, root.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return FilenameUtils.getExtension(name).equalsIgnoreCase(CFG_EXT);
            }
        }));
        return files;
    }

    @Nullable
    protected <T extends AbstractBean> T load(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String name, @NotNull String configFolder, Class<T> type) throws JAXBException {
        File hostConfig = BasicBeanManager.getInstance().getConfigurationFile(serverPaths, user, name, configFolder);
        if (!hostConfig.exists()) {
            return null;
        }
        return type.cast(read(hostConfig));
    }

    protected void save(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String configFolder, @NotNull AbstractBean bean) throws JAXBException {
        if (null == bean.getId()) {
            bean.setId(UUID.randomUUID());
        }

        File hostConfig = BasicBeanManager.getInstance().getConfigurationFile(serverPaths, user, bean.getId().toString(), configFolder);
        hostConfig.getParentFile().mkdirs();
        write(hostConfig, bean);
    }

    protected void delete(@NotNull ServerPaths serverPaths, @NotNull SUser user, @NotNull String configFolder, @NotNull String name) {
        File hostConfig = BasicBeanManager.getInstance().getConfigurationFile(serverPaths, user, name, configFolder);
        if (hostConfig.exists()) {
            hostConfig.delete();
        }
    }
}