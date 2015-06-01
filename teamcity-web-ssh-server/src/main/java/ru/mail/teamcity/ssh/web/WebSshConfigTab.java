package ru.mail.teamcity.ssh.web;

import com.google.common.collect.Lists;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.util.SessionUser;
import org.jetbrains.annotations.NotNull;
import ru.mail.teamcity.ssh.AppConfiguration;
import ru.mail.teamcity.ssh.config.ConfigHelper;
import ru.mail.teamcity.ssh.config.HostBean;
import ru.mail.teamcity.ssh.shell.ShellManager;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 07/05/15
 * Time: 17:36
 */
public class WebSshConfigTab extends SimpleCustomTab {

    @NotNull
    private final ServerPaths serverPaths;

    public WebSshConfigTab(
            @NotNull final PluginDescriptor pluginDescriptor,
            @NotNull PagePlaces pagePlaces,
            @NotNull ServerPaths serverPaths
    ) {
        super(
                pagePlaces,
                PlaceId.MY_TOOLS_TABS,
                AppConfiguration.PLUGIN_NAME,
                pluginDescriptor.getPluginResourcesPath("webSshConfigTab.jsp"),
                "Web Ssh");
        this.serverPaths = serverPaths;

        addCssFile(pluginDescriptor.getPluginResourcesPath("css/webSshConfig.css"));
        addJsFile(pluginDescriptor.getPluginResourcesPath("js/webSshConfig.js"));
        addJsFile(pluginDescriptor.getPluginResourcesPath("js/webSshShell.js"));
        addJsFile(pluginDescriptor.getPluginResourcesPath("lib/term.js"));

        register();
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest httpServletRequest) {
        SUser user = SessionUser.getUser(httpServletRequest);
        List<HostBean> hosts = Lists.newArrayList();
        try {
            hosts = ConfigHelper.hosts(serverPaths, user);
        } catch (JAXBException e) {
            model.put("error", e.getCause().toString());
            e.printStackTrace();
        }

        model.put("hosts", hosts);
        model.put("connections", ShellManager.getUserConnections(user));
    }
}
