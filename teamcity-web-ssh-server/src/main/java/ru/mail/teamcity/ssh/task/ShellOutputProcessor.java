package ru.mail.teamcity.ssh.task;

import org.atmosphere.cpr.AtmosphereResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        InputStreamReader isr = new InputStreamReader(outFromChannel);
        BufferedReader reader = new BufferedReader(isr, 4096);


        char[] chars = new char[1024];
        synchronized (isr) { // TODO -- is this redundant?
            try {
                int count;
                while ((count = isr.read(chars)) != -1) {
//            while ((count = reader.read(chars)) != -1) {
                    if (!running) {
                        break;
                    }
                    String chunk = new String(chars, 0, count);
                    response.write(chunk);
                    System.out.println("Count: " + count);
                    System.out.println("Chunk: " + chunk);
                    Thread.sleep(50);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void terminate() {
        running = false;
    }
}
