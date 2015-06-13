package ru.mail.teamcity.ssh.web;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.util.SessionUser;
import org.jetbrains.annotations.NotNull;
import ru.mail.teamcity.ssh.AppConfiguration;
import ru.mail.teamcity.ssh.config.HostBean;
import ru.mail.teamcity.ssh.config.HostManager;
import ru.mail.teamcity.ssh.config.PresetBean;
import ru.mail.teamcity.ssh.config.PresetManager;
import ru.mail.teamcity.ssh.shell.ShellManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 07/05/15
 * Time: 17:36
 */
public class WebSshConfigTab extends SimpleCustomTab {

    public WebSshConfigTab(
            @NotNull final PluginDescriptor pluginDescriptor,
            @NotNull PagePlaces pagePlaces
    ) {
        super(
                pagePlaces,
                PlaceId.MY_TOOLS_TABS,
                AppConfiguration.PLUGIN_NAME,
                pluginDescriptor.getPluginResourcesPath("webSshConfigTab.jsp"),
                "Web Ssh");

        addCssFile(pluginDescriptor.getPluginResourcesPath("css/webSshConfig.css"));
        addJsFile(pluginDescriptor.getPluginResourcesPath("js/webSshConfig.js"));

        register();
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest httpServletRequest) {
        SUser user = SessionUser.getUser(httpServletRequest);
        ActionErrors errors = new ActionErrors();
        List<HostBean> hosts = HostManager.list(user, errors);
        List<PresetBean> presets = PresetManager.list(user, errors);

        model.put("hosts", hosts);
        model.put("presets", presets);
        model.put("connections", ShellManager.getUserConnections(user));
        model.put("errors", errors);
    }
}
