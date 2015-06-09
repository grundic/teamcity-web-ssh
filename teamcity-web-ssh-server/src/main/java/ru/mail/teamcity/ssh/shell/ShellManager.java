package ru.mail.teamcity.ssh.shell;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jcraft.jsch.*;
import jetbrains.buildServer.users.SUser;
import org.atmosphere.cpr.AtmosphereResource;
import org.jetbrains.annotations.NotNull;
import ru.mail.teamcity.ssh.config.HostBean;

import java.io.IOException;
import java.util.Collection;

/**
 * Author: g.chernyshev
 * Date: 26.05.15
 */
public class ShellManager {
    private static final Table<Long, String, SshConnectionInfo> userShells = HashBasedTable.create();

    public static void createSshConnection(@NotNull SUser user, @NotNull HostBean host, @NotNull AtmosphereResource resource) throws IOException, JSchException {
        JSch jsch = new JSch();

        Session sshSession = jsch.getSession(host.getDelegate().getLogin(), host.getHost(), host.getPort());
        sshSession.setPassword(host.getDelegate().getPassword());
        sshSession.setConfig("StrictHostKeyChecking", "no");

        sshSession.connect(30000);
        Channel shellChannel = sshSession.openChannel("shell");
        ((ChannelShell) shellChannel).setPtyType("xterm");

        ShellOutputProcessor thread = new ShellOutputProcessor(shellChannel.getInputStream(), resource);
        thread.start();

        shellChannel.connect();

        add(user, resource.uuid(), new SshConnectionInfo(shellChannel, thread));
    }

    private static synchronized void add(@NotNull SUser user, @NotNull String uuid, @NotNull SshConnectionInfo connectionInfo) {
        userShells.put(user.getId(), uuid, connectionInfo);
    }

    public static synchronized SshConnectionInfo get(@NotNull SUser user, @NotNull String uuid) {
        return userShells.get(user.getId(), uuid);
    }

    public static synchronized Collection<SshConnectionInfo> getUserConnections(@NotNull SUser user) {
        return userShells.row(user.getId()).values();
    }

    public static synchronized void terminate(@NotNull SUser user, @NotNull String uuid) {
        SshConnectionInfo connectionInfo = get(user, uuid);

        connectionInfo.getChannel().disconnect();

        ShellOutputProcessor thread = connectionInfo.getThread();
        connectionInfo.getThread().terminate();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Thread successfully stopped.");


        userShells.remove(user.getId(), uuid);
    }
}
