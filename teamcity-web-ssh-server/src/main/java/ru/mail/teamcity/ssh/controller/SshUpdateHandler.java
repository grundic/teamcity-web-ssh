package ru.mail.teamcity.ssh.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.*;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.util.SessionUser;
import org.apache.commons.lang.StringUtils;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.handler.OnMessage;
import org.jetbrains.annotations.NotNull;
import ru.mail.teamcity.ssh.config.ConfigHelper;
import ru.mail.teamcity.ssh.config.HostBean;
import ru.mail.teamcity.ssh.task.ShellOutputProcessor;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 22/04/15
 * Time: 01:27
 */
public class SshUpdateHandler extends OnMessage<String> {

    @NotNull
    private final ServerPaths serverPaths;

    private JSch jsch = null;

    private Thread thread = null;
    private ShellOutputProcessor runnable = null;
    private PrintStream inputToChannel = null;

    private Channel shellChannel = null;

    public SshUpdateHandler(@NotNull ServerPaths serverPaths) {
        this.serverPaths = serverPaths;
    }

    @Override
    public void onOpen(AtmosphereResource resource) throws IOException {
        super.onOpen(resource);

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

        jsch = new JSch();
        try {
            Session sshSession = jsch.getSession(host.getLogin(), host.getHost(), host.getPort());
            sshSession.setPassword(host.getPassword());
            sshSession.setConfig("StrictHostKeyChecking", "no");

            sshSession.connect(30000);
            shellChannel = sshSession.openChannel("shell");
            ((ChannelShell) shellChannel).setPtyType("xterm");

            runnable = new ShellOutputProcessor(shellChannel.getInputStream(), resource.getResponse());
            thread = new Thread(runnable);
            thread.start();

            inputToChannel = new PrintStream(shellChannel.getOutputStream(), true);
            shellChannel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        resource.suspend();
    }

    @Override
    public void onMessage(AtmosphereResponse response, String message) throws IOException {
        if (StringUtils.isNotEmpty(message)) {
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> jsonRoot = new Gson().fromJson(message, type);

            String stdin = jsonRoot.get("stdin");
            inputToChannel.write(stdin.getBytes());
        }
    }

    @Override
    public void onTimeout(AtmosphereResponse response) throws IOException {
        super.onTimeout(response);
        shellChannel.disconnect();
        stopThread();
    }

    @Override
    public void onDisconnect(AtmosphereResponse response) throws IOException {
        super.onDisconnect(response);
        shellChannel.disconnect();
        stopThread();
    }

    private void stopThread() {
        System.out.println("Stopping thread");
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
}