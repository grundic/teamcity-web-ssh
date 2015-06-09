package ru.mail.teamcity.ssh.web;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 11/05/15
 * Time: 10:22
 */
public class WebSshShellPage extends BaseController {

    @NotNull
    private final PluginDescriptor pluginDescriptor;

    public WebSshShellPage(
            @NotNull WebControllerManager webControllerManager,
            @NotNull PluginDescriptor pluginDescriptor
    ) {
        this.pluginDescriptor = pluginDescriptor;
        webControllerManager.registerController("/webSshShell.html", this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("queryString", httpServletRequest.getQueryString());
        return new ModelAndView(pluginDescriptor.getPluginResourcesPath("webSshShell.jsp"), params);
    }
}
