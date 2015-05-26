package ru.mail.teamcity.ssh.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.*;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.util.SessionUser;
import org.apache.commons.lang.StringUtils;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.jetbrains.annotations.NotNull;
import ru.mail.teamcity.ssh.config.ConfigHelper;
import ru.mail.teamcity.ssh.config.HostBean;
import ru.mail.teamcity.ssh.task.ShellOutputProcessor;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: g.chernyshev
 * Date: 22/04/15
 * Time: 01:27
 */
public class SshUpdateHandler extends AbstractReflectorAtmosphereHandler {

    private final ConcurrentHashMap<String, SshConnectionInfo> shells = new ConcurrentHashMap<String, SshConnectionInfo>();

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

        String id = resource.getRequest().getParameter("id");
        SUser user = SessionUser.getUser(resource.getRequest());
        HostBean host;
        try {
            host = ConfigHelper.load(serverPaths, user, id);
        } catch (JAXBException e) {
            // TODO: handle exception
            e.printStackTrace();
            return;
        }

        System.out.println("onOpen has triggered.");

        JSch jsch = new JSch();
        try {
            Session sshSession = jsch.getSession(host.getLogin(), host.getHost(), host.getPort());
            sshSession.setPassword(host.getPassword());
            sshSession.setConfig("StrictHostKeyChecking", "no");

            sshSession.connect(30000);
            Channel shellChannel = sshSession.openChannel("shell");
            ((ChannelShell) shellChannel).setPtyType("xterm");

            ShellOutputProcessor runnable = new ShellOutputProcessor(shellChannel.getInputStream(), resource);
            Thread thread = new Thread(runnable);
            thread.start();

            shellChannel.connect();

            shells.put(resource.uuid(), new SshConnectionInfo(shellChannel, runnable, thread));
        } catch (JSchException e) {
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

            String stdin = jsonRoot.get("stdin");
            String uuid = response.resource().uuid();
            if (null == uuid) {
                return;
            }
            SshConnectionInfo connectionInfo = shells.get(uuid);
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
        String uuid = resource.uuid();
        if (null == uuid) {
            return;
        }
        SshConnectionInfo connectionInfo = shells.get(uuid);
        if (null == connectionInfo) {
            return;
        }

        stopThread(connectionInfo.thread, connectionInfo.runnable);
        shells.remove(uuid);
    }

    private void stopThread(Thread thread, ShellOutputProcessor runnable) {
        System.out.println("Stopping runnable");
        if (thread != null) {
            runnable.terminate();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread successfully stopped.");
        }
    }


    private class SshConnectionInfo {
        private final Channel channel;
        private final ShellOutputProcessor runnable;
        private final Thread thread;

        private SshConnectionInfo(Channel channel, ShellOutputProcessor runnable, Thread thread) {
            this.channel = channel;
            this.runnable = runnable;
            this.thread = thread;

        }

        public Channel getChannel() {
            return channel;
        }

        public ShellOutputProcessor getRunnable() {
            return runnable;
        }

        public Thread getThread() {
            return thread;
        }

    }
}