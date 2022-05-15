package com.thenexusreborn.api.networking.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//This represents a server socket that is running to receive connections from other servers, there should be only one running
public class NetworkServerSocket extends Thread {

    private ServerSocket serverSocket;

    private final List<HandlerSocket> handlers = Collections.synchronizedList(new ArrayList<>());

    public NetworkServerSocket(String host, int port) {
        try {
            this.serverSocket = new ServerSocket();
            InetSocketAddress address = new InetSocketAddress(host, port);
            this.serverSocket.bind(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Received connection from " + socket.getInetAddress());
                HandlerSocket handler = new HandlerSocket(socket, this);
                this.handlers.add(handler);
                handler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return false; //TODO
    }

    public List<HandlerSocket> getHandlers() {
        return handlers;
    }

    public void sendCommand(String command) {
        for (HandlerSocket handler : handlers) {
            handler.sendCommand(command);
        }
    }
}