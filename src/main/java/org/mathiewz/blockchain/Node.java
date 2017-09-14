package org.mathiewz.blockchain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Node<T extends Serializable> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);
    
    private List<Socket> nodes = new ArrayList<>();
    
    private Block<T> currentBlock;
    
    public Node(int localPort, String remoteAdress, int remotePort) throws IOException, ClassNotFoundException {
        this(localPort, null);
        Socket socket = new Socket(remoteAdress, remotePort);
        nodes.add(socket);
        ask(socket);
    }
    
    public Node(int port, Block<T> firstBlock) {
        LOGGER.info("I run on port {}", port);
        currentBlock = firstBlock;
        new Thread() {
            @Override
            public void run() {
                try (ServerSocket ss = new ServerSocket(port)) {
                    while (true) {
                        Socket s = ss.accept();
                        nodes.add(s);
                        Thread t = new NodeThread(s);
                        t.start();
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }.start();
        
    }
    
    public void addBlock(T value) throws IOException {
        currentBlock = new Block<>(value, currentBlock);
        emit(currentBlock);
    }
    
    public void addNode(String remoteAdress, Integer remotePort) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(remoteAdress, remotePort);
        nodes.add(socket);
        ask(socket);
    }
    
    @SuppressWarnings("unchecked")
    private void ask(Socket socket) throws IOException, ClassNotFoundException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.write("blockchain");
        out.newLine();
        out.flush();
        LOGGER.info("ask for syncing");
        byte[] bytes = Base64.getDecoder().decode(in.readLine().getBytes());
        new NodeThread(socket).start();
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        receive((Block<T>) objectInputStream.readObject());
    }
    
    private void receive(Block<T> block) throws IOException {
        LOGGER.info("recu bloc {}", block);
        Block<T> newBlock = Block.mergeChains(currentBlock, block);
        if (newBlock != currentBlock) {
            currentBlock = newBlock;
            emit(newBlock);
        }
    }
    
    private void emit(Block<T> newBlock) throws IOException {
        for (Socket node : nodes) {
            send(node, newBlock);
        }
    }
    
    private void send(Socket socket, Block<T> newBlock) throws IOException {
        if (socket.isClosed()) {
            nodes.remove(socket);
            return;
        }
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(newBlock);
        out.write(new String(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray())));
        out.newLine();
        out.flush();
        LOGGER.info("Send the blocks");
        byteArrayOutputStream.close();
    }
    
    public Block<T> getBlockChain() {
        return currentBlock;
    }
    
    private class NodeThread extends Thread {
        private Socket socket;
        
        private NodeThread(Socket socket) {
            this.socket = socket;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line = null;
                while ((line = in.readLine()) != null) {
                    if ("blockchain".equals(line)) {
                        send(socket, currentBlock);
                    } else {
                        byte[] bytes = Base64.getDecoder().decode(line.getBytes());
                        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
                        receive((Block<T>) objectInputStream.readObject());
                    }
                }
            } catch (SocketException e) {
                LOGGER.info("Un noeud injoignable");
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
    
}
