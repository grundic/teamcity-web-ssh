package ru.mail.teamcity.ssh.controller;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.JSchException;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.util.SessionUser;
import org.apache.commons.lang.StringUtils;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.jetbrains.annotations.NotNull;
import ru.mail.teamcity.ssh.config.HostBean;
import ru.mail.teamcity.ssh.config.HostManager;
import ru.mail.teamcity.ssh.config.HostNotFoundException;
import ru.mail.teamcity.ssh.shell.ShellManager;
import ru.mail.teamcity.ssh.shell.SshConnectionInfo;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 22/04/15
 * Time: 01:27
 */
public class SshUpdateHandler extends AbstractReflectorAtmosphereHandler {

    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {
        if (resource.getRequest().getMethod().equalsIgnoreCase("GET")) {
            onOpen(resource);
        } else if (resource.getRequest().getMethod().equals("POST")) {
            doPost(resource);
        }
    }

    private void onOpen(AtmosphereResource resource) throws IOException {
        SUser user = SessionUser.getUser(resource.getRequest());
        HostBean host = null;

        String id = resource.getRequest().getParameter("id");

        try {
            if ((id != null) && StringUtils.isNotEmpty(id)) {
                host = HostManager.load(user, id);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
            sendError(resource, "Xml error", "Looks, like you xml is invalid:" + e.getMessage());
            return;
        } catch (HostNotFoundException e) {
            e.printStackTrace();
            sendError(resource, "Host not found", "Can't get host! Please, check parameters 'id' or 'ip' are correct.");
            return;
        }
        
        if (host == null) {
            resource.getResponse().close();
            return;
        }

        try {
            ShellManager.createSshConnection(user, host, resource);
        } catch (JSchException e) {
            sendError(resource, "Ssh error", "Error establishing connection:" + e.getMessage());
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            sendError(resource, "Algorithm not found", "MD5 algorithm was not found:" + e.getMessage());
        }

        resource.setBroadcaster(resource.getAtmosphereConfig().getBroadcasterFactory().lookup("MyBroadcaster", true));
        resource.suspend();
    }

    private void doPost(AtmosphereResource resource) throws IOException {
        StringBuilder data = new StringBuilder();

        BufferedReader requestReader = resource.getRequest().getReader();
        char[] buf = new char[5120];
        int read;
        while ((read = requestReader.read(buf)) > 0) {
            data.append(buf, 0, read);
        }

        onMessage(resource.getResponse(), data.toString());
    }


    private void onMessage(AtmosphereResponse response, String message) throws IOException {
        if (StringUtils.isNotEmpty(message)) {
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> jsonRoot = new Gson().fromJson(message, type);
            SUser user = SessionUser.getUser(response.resource().getRequest());

            String uuid = response.resource().uuid();
            if (uuid == null) {
                return;
            }
            SshConnectionInfo connectionInfo = ShellManager.get(user, response.resource().uuid());
            if (connectionInfo == null) {
                return;
            }

            String stdin = jsonRoot.get("stdin");
            if (stdin != null && StringUtils.isNotEmpty(stdin)) {
                PrintStream inputToChannel = new PrintStream(connectionInfo.getChannel().getOutputStream(), true);
                inputToChannel.write(stdin.getBytes());
            }

            String resize = jsonRoot.get("resize");
            if (resize != null && StringUtils.isNotEmpty(resize)) {
                Type resizeType = new TypeToken<Map<String, Integer>>() {
                }.getType();
                Map<String, Integer> jsonResize = new Gson().fromJson(resize, resizeType);
                int x = jsonResize.get("x");
                int y = jsonResize.get("y");
                connectionInfo.getChannel().setPtySize(x, y, 0, 0);
            }
        }
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        AtmosphereResponse response = ((AtmosphereResourceImpl) event.getResource()).getResponse(false);

        if (event.isCancelled() || event.isClosedByApplication() || event.isClosedByClient()) {
            onDisconnect(response);
        } else if ((event.getMessage() != null) && List.class.isAssignableFrom(event.getMessage().getClass())) {
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


    private void onResume(AtmosphereResponse response) {
    }

    private void onTimeout(AtmosphereResponse response) {
        close(response.resource());
    }

    private void onDisconnect(AtmosphereResponse response) {
        close(response.resource());
    }

    private void close(AtmosphereResource resource) {
        SUser user = SessionUser.getUser(resource.getRequest());
        String uuid = resource.uuid();
        if (uuid == null) {
            return;
        }
        ShellManager.terminate(user, uuid);
    }

    private void sendError(@NotNull AtmosphereResource resource, @NotNull String title, @NotNull String content) {
        Map<String, String> error = ImmutableMap.of("title", title, "content", content);
        Map<String, Map<String, String>> payload = ImmutableMap.of("error", error);

        resource.write(new Gson().toJson(payload));
    }

    private void sendError(@NotNull AtmosphereResource resource, @NotNull String content) {
        sendError(resource, "Something went wrong", content);
    }
}