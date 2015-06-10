package ru.mail.teamcity.ssh.shell;

import com.google.gson.Gson;
import org.atmosphere.cpr.AtmosphereResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: g.chernyshev
 * Date: 29/04/15
 * Time: 00:27
 */
public class ShellOutputProcessor extends Thread {

    private final InputStream outFromChannel;
    private final AtmosphereResource resource;
    private volatile boolean terminated = false;


    public ShellOutputProcessor(InputStream outFromChannel, AtmosphereResource resource) {
        this.outFromChannel = outFromChannel;
        this.resource = resource;
    }

    @Override
    public void run() {

        try {
            byte[] buff = new byte[5120];
            int count;
            while ((count = outFromChannel.read(buff)) != -1) {
                if (terminated) {
                    break;
                }
                String chunk = new String(buff, 0, count);
                resource.write(new Gson().toJson(chunk));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void terminate() {
        terminated = true;
    }
}
