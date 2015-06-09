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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        if (null == id || StringUtil.isEmpty(id)) {
            return null; // TODO error here
        }

        SUser user = SessionUser.getUser(request);
        PresetBean bean = PresetManager.load(user, id);
        if (null != bean) {
            bean.setPassword(""); // because view is read-only - remove password at all
            bean.setHosts(Lists.<HostBean>newArrayList()); // prevent stack overflow during serialization to json
            response.setContentType("application/json");
            response.getOutputStream().write(new Gson().toJson(bean).getBytes());
        }

        return null;
    }
}