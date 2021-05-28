import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.text.html.ImageView;
import javax.imageio.ImageIO;

import java.util.*;

import javax.imageio.*;

import java.io.InputStreamReader;

import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.awt.Robot;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;


public class Communications {

    Robot robot;
    Rectangle screenSize;
    ServerSocket ss;
    Socket socket, mouseSocket;
    BufferedImage frame;
    BufferedReader socketInput, mouseInput;
    PrintWriter socketOutput, mouseOutput;
    String perform;
    int port;
    DataOutputStream dos;
    DataInputStream dis;
    MouseControll mouseControll;
    boolean connected;
    Image cursor;

    //    this portion is provided in Oracle Docs for getting  IP  addresses of all network interfaces
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    void getIPAddress() throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
            displayInterfaceInformation(netint);
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.printf("Display name: %s\n", netint.getDisplayName());
        System.out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("InetAddress: %s\n", inetAddress);
        }
        System.out.printf("\n");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    void createHost() throws Exception {
        perform = "Host";
        ss = new ServerSocket(6969);
        System.out.println("Choose the correct IP of your network interfaces and then Write this IP address to the client\n");
        getIPAddress();
        System.out.println("waiting for Client.....");
        socket = ss.accept();
        mouseSocket = ss.accept();
        System.out.println("client added");
        System.out.println("mouse connected");
        socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketOutput = new PrintWriter(socket.getOutputStream(), true);
        mouseInput = new BufferedReader(new InputStreamReader(mouseSocket.getInputStream()));
        mouseOutput = new PrintWriter(mouseSocket.getOutputStream(), true);
        mouseControll = new MouseControll();
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());
        cursor = ImageIO.read(new File("mouse.png"));
        connected = true;
    }

    void sendImage(BufferedImage bif) throws Exception {
        Point p = MouseInfo.getPointerInfo().getLocation();
        bif.createGraphics().drawImage(cursor, p.x, p.y, 16, 16, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bif, "jpg", bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        byte[] chunk = new byte[1024];
        int ret;
        socketOutput.println(bos.size());
        if (socketInput.readLine().equals("1")) {
            while (true) {
                ret = bis.read(chunk, 0, chunk.length);
                if (ret == -1)
                    break;
                dos.write(chunk, 0, ret);
            }
        } else {
            connected = false;
            return;
        }
        dos.flush();
        bis.close();
        bos.close();
    }

    void communicateAsHost() throws Exception {
        mouseControll = new MouseControll();
        robot = new Robot();
        screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        System.out.println("sending image");

        new Thread() {
            public void run() {
                try {
                    while (connected)
                        sendImage(robot.createScreenCapture(screenSize));
                    socket.close();
                    ss.close();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }.start();

        //read inputs from remote and perform

        new Thread() {
            public void run() {
                try {
                    while (connected) {
                        String op = mouseInput.readLine();
                        if (op.equals("l")) {
                            mouseControll.leftClickMouse();
                        } else if (op.equals("r")) {
                            mouseControll.rightClickMouse();
                        } else if (op.equals("m")) {
                            int x = Integer.parseInt(mouseInput.readLine());
                            int y = Integer.parseInt(mouseInput.readLine());
                            mouseControll.moveMouse(x, y);
                            mouseOutput.println(1);
                        } else if (op.equals("x")) {
                            connected = false;
                            break;
                        }
                    }
                    mouseSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
