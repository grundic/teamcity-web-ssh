package ru.mail.teamcity.ssh.task;

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

        byte[] buff = new byte[1024]; // TODO hardcoded constant
        try {
            int count;
            while ((count = outFromChannel.read(buff)) != -1) {
                if (!running) {
                    break;
                }
//                String chunk = new String(buff, 0, count);
                String chunk = printc(buff, 0, count);
                //response.write(chunk);
                response.write(buff, 0, count);
                System.out.println("Count: " + count);
                System.out.println("Chunk: " + chunk);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static char[] hex2char = "0123456789ABCDEF".toCharArray();

    public static String printc(byte[] buffer) {
        return printc(buffer, 0, buffer.length);
    }

    public static String printc(byte[] buffer, int offset, int count) {
        StringBuilder builder = new StringBuilder(4 * count);
        int limit = offset + count;
        for (int i = offset; i < limit; i++) {
            int b = buffer[i];
            if (b < 0x20) {
                builder.append("\\x");
                builder.append(hex2char[(b & 0xF0) >> 4]);
                builder.append(hex2char[(b & 0xF)]);
            } else {
                if (b == '"' || b == '\\') {
                    builder.append('\\');
                }
                builder.append((char) b);
            }
        }
        return builder.toString();
    }

    public void terminate() {
        running = false;
    }
}
