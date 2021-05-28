package com.example.remo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {

    boolean connected;
    private String ip;
    private Socket socket, mouseSocket, soundSocket;
    private BufferedReader socketInput, mouseInput;
    private PrintWriter socketOutput, mouseOutput;
    private static final int port = 6969;
    private DataOutputStream dos;
    private DataInputStream dis;
    private static Connection instance;

    private Connection() {
    }

    public static Connection getInstance() {
        if (instance != null)
            return instance;
        instance = new Connection();
        return instance;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public String getIP() {
        if (ip == null || ip.equals(""))
            return "";
        return ip;
    }

    public boolean connect() {

        if (ip == null || ip.equals(""))
            return false;
        try {
            socket = new Socket(ip, port);
            mouseSocket = new Socket(ip, port);
            socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOutput = new PrintWriter(socket.getOutputStream(), true);
            mouseInput = new BufferedReader(new InputStreamReader(mouseSocket.getInputStream()));
            mouseOutput = new PrintWriter(mouseSocket.getOutputStream(), true);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        connected = true;
        return true;
    }

    private byte[] receiveImageData() throws Exception {
        byte[] chunk = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int ret;
        int imsize = Integer.parseInt(socketInput.readLine());
        socketOutput.println(1);
        while (imsize > 0) {
            ret = dis.read(chunk, 0, chunk.length);
            imsize -= ret;
            bos.write(chunk, 0, ret);
        }
        bos.flush();
        return bos.toByteArray();
    }

    public Bitmap getImage() throws Exception {
        byte[] img = receiveImageData();
        return BitmapFactory.decodeByteArray(img, 0, img.length);
    }

    public void sendMouseMove(int dx, int dy) throws Exception {
        if (dx != 0 || dy != 0) {
            mouseOutput.println("m");
            mouseOutput.println(dx);
            mouseOutput.println(dy);
            mouseInput.readLine();
        }

    }

    public void sendMouseLeftClick() throws Exception {
        mouseOutput.println("l");
    }

    public void sendMouseRightClick() throws Exception {
        mouseOutput.println("r");
    }

    public boolean disconnectDisplay() {
        try {
            connected = false;
            socketInput.readLine();
            socketOutput.println(0);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean disconnectMouse() {
        try {
            connected = false;
            mouseOutput.println("x");
            mouseSocket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
