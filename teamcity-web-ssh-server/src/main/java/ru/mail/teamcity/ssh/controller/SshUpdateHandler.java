package ru.mail.teamcity.ssh.controller;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.JSchException;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.util.SessionUser;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import ru.mail.teamcity.ssh.config.ConfigHelper;
import ru.mail.teamcity.ssh.config.HostBean;
import ru.mail.teamcity.ssh.shell.ShellManager;
import ru.mail.teamcity.ssh.shell.SshConnectionInfo;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 22/04/15
 * Time: 01:27
 */
public class SshUpdateHandler extends AbstractReflectorAtmosphereHandler {

    @NotNull
    private final ServerPaths serverPaths;

    public SshUpdateHandler(@NotNull ServerPaths serverPaths) {
        this.serverPaths = serverPaths;
    }

    public void onRequest(AtmosphereResource resource) throws IOException {
        if (resource.getRequest().getMethod().equalsIgnoreCase("GET")) {
            onOpen(resource);
        } else if (resource.getRequest().getMethod().equals("POST")) {
            doPost(resource);
        }
    }

    public void onOpen(AtmosphereResource resource) throws IOException {
        SUser user = SessionUser.getUser(resource.getRequest());
        HostBean host = null;

        String id = resource.getRequest().getParameter("id");
        String ip = resource.getRequest().getParameter("ip");

        try {
            if (null != id && StringUtils.isNotEmpty(id)) {
                host = ConfigHelper.load(serverPaths, user, id);
            } else if (null != ip && StringUtils.isNotEmpty(ip)) {
                host = ConfigHelper.findHostByIp(serverPaths, user, ip);
            }
        } catch (JAXBException e) {
            // TODO: handle exception
            e.printStackTrace();
            return;
        }
        if (null == host) {
//            resource.getResponse().setContentType(MediaType.APPLICATION_JSON.getType());
//            resource.getResponse().setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
//            resource.getResponse().destroy();
//            resource.getResponse().write(new Gson().toJson("This is my tesr"));
//            resource.write("Websocket endpoint not found");
//            resource.write("{\"firstName\":\"John\", \"lastName\":\"Doe\"}");
            resource.getResponse().sendError(507, "{\"firstName\":\"John\", \"lastName\":\"Doe\"}");
//            Map<String,String> error = ImmutableMap.of("error", "Host location failed! Please, make sure 'id' or 'ip' parameter is provided and is correct.");
//            resource.write(new Gson().toJson(error));
//            resource.getResponse().sendError(501, new Gson().toJson("Dummy error"));
            resource.close();
            return;
        }

        try {
            ShellManager.createSshConnection(user, host, resource);
        } catch (JSchException e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        resource.setBroadcaster(resource.getAtmosphereConfig().getBroadcasterFactory().lookup("MyBroadcaster", true));
        resource.suspend();
    }

    private void doPost(AtmosphereResource resource) throws IOException {
        StringBuilder data = new StringBuilder();
        BufferedReader requestReader;

        requestReader = resource.getRequest().getReader();
        char[] buf = new char[5120];
        int read;
        while ((read = requestReader.read(buf)) > 0) {
            data.append(buf, 0, read);
        }

        onMessage(resource.getResponse(), data.toString());
    }


    public void onMessage(AtmosphereResponse response, String message) throws IOException {
        if (StringUtils.isNotEmpty(message)) {
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> jsonRoot = new Gson().fromJson(message, type);
            SUser user = SessionUser.getUser(response.resource().getRequest());

            String stdin = jsonRoot.get("stdin");
            String uuid = response.resource().uuid();
            if (null == uuid) {
                return;
            }
            SshConnectionInfo connectionInfo = ShellManager.get(user, response.resource().uuid());
            if (null == connectionInfo) {
                return;
            }
            PrintStream inputToChannel = new PrintStream(connectionInfo.getChannel().getOutputStream(), true);
            inputToChannel.write(stdin.getBytes());
        }
    }

    @Override
    public final void onStateChange(AtmosphereResourceEvent event) throws IOException {
        AtmosphereResponse response = ((AtmosphereResourceImpl) event.getResource()).getResponse(false);

        if (event.isCancelled() || event.isClosedByApplication() || event.isClosedByClient()) {
            onDisconnect(response);
        } else if (event.getMessage() != null && List.class.isAssignableFrom(event.getMessage().getClass())) {
            List<String> messages = List.class.cast(event.getMessage());
            for (String message : messages) {
                onMessage(response, message);
            }
        } else if (event.isResuming()) {
            onResume(response);
        } else if (event.isResumedOnTimeout()) {
            onTimeout(response);
        } else if (event.isSuspended()) {
            onMessage(response, (String) event.getMessage());
        }
        postStateChange(event);
    }


    public void onResume(AtmosphereResponse response) throws IOException {
    }

    public void onTimeout(AtmosphereResponse response) throws IOException {
        close(response.resource());
    }

    public void onDisconnect(AtmosphereResponse response) throws IOException {
        close(response.resource());
    }

    private void close(AtmosphereResource resource) {
        SUser user = SessionUser.getUser(resource.getRequest());
        String uuid = resource.uuid();
        if (null == uuid) {
            return;
        }
        ShellManager.terminate(user, uuid);
    }
}