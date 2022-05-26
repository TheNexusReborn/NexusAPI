package com.thenexusreborn.api.network.socket;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.network.cmd.*;
import com.thenexusreborn.api.server.ServerInfo;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class NexusSocket extends Thread {
    private Socket socket;
    
    private String host;
    private int port;
    
    private AtomicBoolean active = new AtomicBoolean(false);
    
    private ServerInfo serverInfo;
    
    public NexusSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public NexusSocket(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        while (!isActive()) {
            //This loop will prevent the main loop from running until the socket is initialized, which will prevent the loop stopping before the socket is initialized
            if (socket != null) {
                //Socket was initialized, but not closed
                break;
            }
        }
        
        while (isActive()) {
            //Main loop for the socket
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Object object = in.readObject();
                if (object instanceof String) { //Good thing is that the instanceof check doubles as a null check
                    new Thread(() -> handleCommand((String) object)).start(); 
                    //Handles input in a separate thread to prevent logic that takes a while from preventing more commands from being received
                }
            } catch (Exception e) {
                e.printStackTrace();
                setActive(false); //Close on exception
            }
        }
        
        close();
    }
    
    public void close() {
        setActive(false);
        if (socket != null) {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void connect() throws IOException {
        this.socket = new Socket(host, port);
        setActive(true);
    }
    
    public boolean isActive() {
        return active.get();
    }
    
    public void setActive(boolean value) {
        this.active.set(value);
    }
    
    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    
    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
    
    public void handleCommand(String cmd) {
        SocketCommandManager manager = NexusAPI.getApi().getNetworkManager().getSocketCommandManager();
        String[] cmdSplit = cmd.split(" ");
        
        if (cmdSplit == null) {
            NexusAPI.getApi().getLogger().warning("Handled a command that did not have any spaces in it: " + cmd);
            return;
        }
        
        if (cmdSplit.length == 1) {
            NexusAPI.getApi().getLogger().warning("Handled a command that only provided one argument: " + cmd);
            return;
        }
        
        String cmdName = cmdSplit[0];
        SocketCommand command = manager.getCommand(cmdName);
        if (command == null) {
            NexusAPI.getApi().getLogger().warning("Received command from socket with no registered command handler: " + cmdName);
            return;
        }
        
        String source = cmdSplit[1];
        String[] args = new String[cmdSplit.length - 2];
        if (cmdSplit.length > 2) {
            System.arraycopy(cmdSplit, 2, args, 0, cmdSplit.length);
        }
        
        command.onCommand(this, source, args);
    }
    
    public void sendCommand(String command) {
        if (socket == null) {
            return;
        }
        
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(command);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
