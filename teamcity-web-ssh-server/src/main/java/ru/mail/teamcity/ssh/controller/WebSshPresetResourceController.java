package ru.mail.teamcity.ssh.controller;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import ru.mail.teamcity.ssh.config.HostBean;
import ru.mail.teamcity.ssh.config.PresetBean;
import ru.mail.teamcity.ssh.config.PresetManager;
import ru.mail.teamcity.ssh.config.PresetNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: g.chernyshev
 * Date: 04.06.15
 */
public class WebSshPresetResourceController extends AbstractController {


    public WebSshPresetResourceController(
            @NotNull WebControllerManager webControllerManager
    ) {
        webControllerManager.registerController("/webSshPresetResource.html", this);
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws IOException, JAXBException {
        response.setContentType("application/json");

        final String id = request.getParameter("id");
        if ((id == null) || StringUtil.isEmpty(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Parameter id is missing or empty!");
            response.getOutputStream().write(new Gson().toJson(error).getBytes());
            return null;
        }

        SUser user = SessionUser.getUser(request);
        try {
            PresetBean bean = PresetBean.newInstance(PresetManager.load(user, id));
            bean.setPassword(""); // because view is read-only - remove password at all
            bean.setHosts(Lists.<HostBean>newArrayList()); // prevent stack overflow during serialization to json
            response.getOutputStream().write(new Gson().toJson(bean).getBytes());
        } catch (PresetNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to load preset by " + id + " id!");
            response.getOutputStream().write(new Gson().toJson(error).getBytes());
        }

        return null;
    }
}
