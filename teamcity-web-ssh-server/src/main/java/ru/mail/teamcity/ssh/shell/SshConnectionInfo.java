package ru.mail.teamcity.ssh.shell;

import com.jcraft.jsch.Channel;

/**
 * Author: g.chernyshev
 * Date: 26.05.15
 */
public class SshConnectionInfo {
    private final Channel channel;
    private final ShellOutputProcessor thread;

    public SshConnectionInfo(Channel channel, ShellOutputProcessor thread) {
        this.channel = channel;
        this.thread = thread;
    }

    public Channel getChannel() {
        return channel;
    }

    public ShellOutputProcessor getThread() {
        return thread;
    }
}
