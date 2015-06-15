package ru.mail.teamcity.ssh.web;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import ru.mail.teamcity.ssh.config.HostBean;
import ru.mail.teamcity.ssh.config.HostManager;
import ru.mail.teamcity.ssh.config.HostNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
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
    protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        Map<String, Object> params = new HashMap<>();

        SUser user = SessionUser.getUser(httpServletRequest);
        HostBean host = null;
        ActionErrors errors = new ActionErrors();

        String id = httpServletRequest.getParameter("id");
        String ip = httpServletRequest.getParameter("ip");

        try {
            if ((id != null) && StringUtils.isNotEmpty(id)) {
                host = HostManager.load(user, id);
            } else if ((ip != null) && StringUtils.isNotEmpty(ip)) {
                host = HostManager.findHostByIp(user, ip);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
            errors.addError("Xml error", "Looks, like you xml is invalid:" + e.getMessage());
        } catch (HostNotFoundException e) {
            e.printStackTrace();
            errors.addError("Host not found", "Can't get host! Please, check parameters 'id' or 'ip' are correct.");
        }

        params.put("bean", host);
        params.put("errors", errors);
        return new ModelAndView(pluginDescriptor.getPluginResourcesPath("webSshShell.jsp"), params);
    }
}
