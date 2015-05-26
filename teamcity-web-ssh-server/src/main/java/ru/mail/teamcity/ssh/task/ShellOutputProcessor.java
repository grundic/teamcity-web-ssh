package ru.mail.teamcity.ssh.task;

import com.google.gson.Gson;
import org.atmosphere.cpr.AtmosphereResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: g.chernyshev
 * Date: 29/04/15
 * Time: 00:27
 */
public class ShellOutputProcessor implements Runnable {

    InputStream outFromChannel;
    AtmosphereResource resource;
    private volatile boolean running = true;


    public ShellOutputProcessor(InputStream outFromChannel, AtmosphereResource resource) {
        this.outFromChannel = outFromChannel;
        this.resource = resource;
    }

    public void run() {
        System.out.println("Starting thread " + this);

        byte[] buff = new byte[5120];
        int count;
        try {
            while ((count = outFromChannel.read(buff)) != -1) {
                if (!running) {
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
        running = false;
    }
}
