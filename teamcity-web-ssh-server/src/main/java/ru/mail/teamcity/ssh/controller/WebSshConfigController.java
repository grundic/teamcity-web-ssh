package ru.mail.teamcity.ssh.controller;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
import ru.mail.teamcity.ssh.config.HostBean;
import ru.mail.teamcity.ssh.config.HostManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * User: g.chernyshev
 * Date: 01/05/15
 * Time: 16:41
 */
public class WebSshConfigController extends BaseFormXmlController {

    @NotNull
    private final PluginDescriptor pluginDescriptor;

    @NotNull
    private final ServerPaths serverPaths;

    private static final Pattern validIpAddressRegex = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    // TODO fix host regex: it should not complain on underscore symbol
    private static final Pattern validHostnameRegex = Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");

    public WebSshConfigController(
            @NotNull SBuildServer buildServer,
            @NotNull WebControllerManager webControllerManager,
            @NotNull PluginDescriptor pluginDescriptor,
            @NotNull ServerPaths serverPaths
    ) {
        super(buildServer);
        this.pluginDescriptor = pluginDescriptor;
        this.serverPaths = serverPaths;
        webControllerManager.registerController("/webSshConfigController.html", this);
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        Map<String, Object> params = new HashMap<String, Object>();
        SUser user = SessionUser.getUser(httpServletRequest);

        HostBean bean = null;
        String id = httpServletRequest.getParameter("id");
        if (null != id) {
            try {
                bean = HostManager.load(serverPaths, user, id);
                String encryptedPassword = RSACipher.encryptDataForWeb(bean.getPassword());
                bean.setEncryptedPassword(encryptedPassword);
                bean.setPassword("");
            } catch (JAXBException e) {
                // TODO: handle error
                e.printStackTrace();
            }
        }
        bean = null == bean ? new HostBean() : bean;

        params.put("bean", bean);
        return new ModelAndView(pluginDescriptor.getPluginResourcesPath("webSshConfig.jsp"), params);
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Element element) {
        String delete = httpServletRequest.getParameter("delete");
        if (null != delete && delete.equalsIgnoreCase("true")) {
            delete(httpServletRequest);
        } else {
            save(httpServletRequest, element);
        }
    }

    private void delete(@NotNull HttpServletRequest httpServletRequest) {
        String id = httpServletRequest.getParameter("id");
        if (null == id) {
            return;
        }
        SUser user = SessionUser.getUser(httpServletRequest);

        HostManager.delete(serverPaths, user, id);
    }

    private void save(@NotNull HttpServletRequest httpServletRequest, @NotNull Element element) {
        HostBean bean = new HostBean();
        bindFromRequest(httpServletRequest, bean);
        String decryptedPassword = RSACipher.decryptWebRequestData(bean.getEncryptedPassword());
        bean.setPassword(decryptedPassword);

        ActionErrors errors = validate(bean);
        if (errors.hasErrors()) {
            writeErrors(element, errors);
            return;
        }

        SUser user = SessionUser.getUser(httpServletRequest);
        try {
            HostManager.save(serverPaths, user, bean);
        } catch (IOException e) {
            errors.addError("ioException", e.getMessage());
            writeErrors(element, errors);
        } catch (JAXBException e) {
            errors.addError("jaxbException", e.getMessage());
            writeErrors(element, errors);
        }
    }

    private ActionErrors validate(HostBean bean) {
        ActionErrors errors = new ActionErrors();

        // Hostname validation
        if (StringUtil.isEmptyOrSpaces(bean.getHost())) {
            errors.addError("emptyHost", "Hostname could not be empty.");
        } else if (!validHostnameRegex.matcher(bean.getHost()).matches() && !validIpAddressRegex.matcher(bean.getHost()).matches()) {
            errors.addError("badHostValue", "Hostname should be valid host or ip address.");
        }

        // Port validation
        if (bean.getPort() <= 0) {
            errors.addError("badPortValue", "Port should be positive number.");
        }

        // Login validation
        if (StringUtil.isEmptyOrSpaces(bean.getLogin())) {
            errors.addError("emptyLogin", "Login could not be empty.");
        }

        // Password validation
        if (StringUtil.isEmptyOrSpaces(bean.getPassword())) {
            errors.addError("emptyPassword", "Password could not be empty.");
        }

        return errors;
    }
}
