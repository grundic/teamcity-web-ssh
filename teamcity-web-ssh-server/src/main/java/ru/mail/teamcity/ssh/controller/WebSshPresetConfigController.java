package ru.mail.teamcity.ssh.controller;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
import ru.mail.teamcity.ssh.config.PresetBean;
import ru.mail.teamcity.ssh.config.PresetManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 03/06/15
 * Time: 01:06
 */
public class WebSshPresetConfigController extends BaseFormXmlController {
    @NotNull
    private final PluginDescriptor pluginDescriptor;

    @NotNull
    private final ServerPaths serverPaths;

    public WebSshPresetConfigController(
            @NotNull SBuildServer buildServer,
            @NotNull WebControllerManager webControllerManager,
            @NotNull PluginDescriptor pluginDescriptor,
            @NotNull ServerPaths serverPaths
    ) {
        super(buildServer);
        this.pluginDescriptor = pluginDescriptor;
        this.serverPaths = serverPaths;
        webControllerManager.registerController("/webSshPresetConfigController.html", this);

    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        Map<String, Object> params = new HashMap<String, Object>();
        SUser user = SessionUser.getUser(httpServletRequest);

        PresetBean bean = null;
        String id = httpServletRequest.getParameter("id");
        if (null != id) {
            try {
                bean = PresetManager.load(serverPaths, user, id);
                String encryptedPassword = RSACipher.encryptDataForWeb(bean.getPassword());
                bean.setEncryptedPassword(encryptedPassword);
                bean.setPassword("");
            } catch (JAXBException e) {
                // TODO: handle error
                e.printStackTrace();
            }
        }
        bean = null == bean ? new PresetBean() : bean;

        params.put("bean", bean);
        return new ModelAndView(pluginDescriptor.getPluginResourcesPath("webSshPresetConfigDialog.jsp"), params);
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

        PresetManager.delete(serverPaths, user, id);
    }

    private void save(@NotNull HttpServletRequest httpServletRequest, @NotNull Element element) {
        PresetBean bean = new PresetBean();
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
            PresetManager.save(serverPaths, user, bean);
        } catch (IOException e) {
            errors.addError("ioException", e.getMessage());
            writeErrors(element, errors);
        } catch (JAXBException e) {
            e.printStackTrace();
            errors.addError("jaxbException", e.getMessage());
            writeErrors(element, errors);
        }
    }

    private ActionErrors validate(PresetBean bean) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }
}
