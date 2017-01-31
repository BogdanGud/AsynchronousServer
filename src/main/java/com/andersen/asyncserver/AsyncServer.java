package com.andersen.asyncserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncServer {
    public AsyncServer() {
        try {
            final AsynchronousServerSocketChannel listener =
                    AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(9898));
            listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel ch, Void att) {
                    listener.accept(null, this);

                    ch.write(ByteBuffer.wrap("Hello, I am UpperCase Server, let's have a conversation!\n".getBytes()));
                    ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
                    try {
                        int bytesRead = ch.read(byteBuffer).get(20, TimeUnit.SECONDS);
                        boolean running = true;
                        while (bytesRead != -1 && running) {
                            System.out.println("bytes read: " + bytesRead);
                            if (byteBuffer.position() > 2) {
                                byteBuffer.flip();
                                byte[] lineBytes = new byte[bytesRead];
                                byteBuffer.get(lineBytes, 0, bytesRead);
                                String line = new String(lineBytes);
                                String outLine = line.toUpperCase();
                                System.out.println("Message: " + line);
                                TimeUnit.MILLISECONDS.sleep(500);
                                ch.write(ByteBuffer.wrap(outLine.getBytes()));
                                byteBuffer.clear();
                                bytesRead = ch.read(byteBuffer).get(30, TimeUnit.SECONDS);
                            } else {
                                running = false;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        ch.write(ByteBuffer.wrap("Good Bye\n".getBytes()));
                        System.out.println("Connection timed out, closing connection");
                    }

                    System.out.println("End of conversation");
                    try {
                        // Close the connection if we need to
                        if (ch.isOpen()) {
                            ch.close();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, Void att) {
                    ///...
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AsyncServer server = new AsyncServer();
        try {
            Thread.sleep(600000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}