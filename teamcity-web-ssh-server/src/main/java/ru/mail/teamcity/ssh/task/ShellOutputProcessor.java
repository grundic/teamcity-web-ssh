package ru.mail.teamcity.ssh.task;

import com.google.gson.Gson;
import org.atmosphere.cpr.AtmosphereResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: g.chernyshev
 * Date: 29/04/15
 * Time: 00:27
 */
public class ShellOutputProcessor implements Runnable {

    InputStream outFromChannel;
    AtmosphereResponse response;
    private volatile boolean running = true;


    public ShellOutputProcessor(InputStream outFromChannel, AtmosphereResponse response) {
        this.outFromChannel = outFromChannel;
        this.response = response;
    }

    public void run() {
        System.out.println("Starting thread " + this);

        byte[] buff = new byte[1024];
        int count;
        try {
            while ((count = outFromChannel.read(buff)) != -1) {
                if (!running) {
                    break;
                }
                String chunk = new String(buff, 0, count);
                response.write(new Gson().toJson(chunk));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void terminate() {
        running = false;
    }
}
