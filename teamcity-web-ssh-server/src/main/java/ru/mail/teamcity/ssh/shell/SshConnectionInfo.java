package ru.mail.teamcity.ssh.shell;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;

/**
 * Author: g.chernyshev
 * Date: 26.05.15
 */
public class SshConnectionInfo {
    private final ChannelShell channel;
    private final ShellOutputProcessor thread;

    public SshConnectionInfo(Channel channel, ShellOutputProcessor thread) {
        this.channel = (ChannelShell) channel;
        this.thread = thread;
    }

    public ChannelShell getChannel() {
        return channel;
    }

    public ShellOutputProcessor getThread() {
        return thread;
    }
}
