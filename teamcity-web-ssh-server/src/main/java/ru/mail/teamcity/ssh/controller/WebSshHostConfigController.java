package ru.mail.teamcity.ssh.controller;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
import ru.mail.teamcity.ssh.config.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 01/05/15
 * Time: 16:41
 */
public class WebSshHostConfigController extends BaseFormXmlController {

    @NotNull
    private final PluginDescriptor pluginDescriptor;

    public WebSshHostConfigController(
            @NotNull SBuildServer buildServer,
            @NotNull WebControllerManager webControllerManager,
            @NotNull PluginDescriptor pluginDescriptor
    ) {
        super(buildServer);
        this.pluginDescriptor = pluginDescriptor;
        webControllerManager.registerController("/webSshHostConfigController.html", this);
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        Map<String, Object> params = new HashMap<>();
        SUser user = SessionUser.getUser(httpServletRequest);
        ActionErrors errors = new ActionErrors();

        List<PresetBean> presets = PresetManager.list(user, errors);

        HostBean bean = null;
        String id = httpServletRequest.getParameter("id");
        if (id != null) {
            try {
                bean = HostBean.newInstance(HostManager.load(user, id));
                String encryptedPassword = RSACipher.encryptDataForWeb(bean.getPassword());
                bean.setEncryptedPassword(encryptedPassword);
                bean.setPassword("");

            } catch (JAXBException | HostNotFoundException e) {
                params.put("error", ExceptionUtil.getDisplayMessage(e));
                e.printStackTrace();
            }
        }
        bean = (bean == null) ? new HostBean() : bean;

        params.put("bean", bean);
        params.put("presets", presets);
        return new ModelAndView(pluginDescriptor.getPluginResourcesPath("webSshHostConfigDialog.jsp"), params);
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Element element) {
        String delete = httpServletRequest.getParameter("delete");
        if ((delete != null) && delete.equalsIgnoreCase("true")) {
            delete(httpServletRequest);
        } else {
            save(httpServletRequest, element);
        }
    }

    private void delete(@NotNull HttpServletRequest httpServletRequest) {
        String id = httpServletRequest.getParameter("id");
        if (id == null) {
            return;
        }
        SUser user = SessionUser.getUser(httpServletRequest);

        HostManager.delete(user, id);
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
            HostManager.save(user, bean);
        } catch (JAXBException e) {
            errors.addError("jaxbException", e.getMessage());
            writeErrors(element, errors);
        } catch (HostNotFoundException e) {
            errors.addError("hostNotFound", e.getMessage());
            writeErrors(element, errors);
        }
    }

    private ActionErrors validate(HostBean bean) {
        ActionErrors errors = new ActionErrors();

        // Hostname validation
        if (StringUtil.isEmptyOrSpaces(bean.getHost())) {
            errors.addError("emptyHost", "Hostname could not be empty.");
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
