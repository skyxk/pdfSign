package com.clt.ess.utils;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketUtils {

    public static boolean wordToPdfClient(String wordPath) {
        int port = 9990;
        boolean blConnect = false;
        Socket socket = null;
        BufferedReader socketIn = null;
        PrintWriter socketOut = null;
        byte ipAddressTemp[] = {127, 0, 0, 1};
        try {
            InetAddress ipAddress = InetAddress.getByAddress(ipAddressTemp);
            socket = new Socket(ipAddress, port);
            socket.setSoTimeout(15000);
            blConnect = true;
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOut = new PrintWriter(socket.getOutputStream());
            socketOut.println("ESSB" + wordPath + "ESSE");				//sFilePath 是要转换的word文件的完整路径
            socketOut.flush();
            String inTemp = socketIn.readLine();

            if(inTemp.length() == 0){
                System.out.println("time out");
                return false;
            }
            if (inTemp.contains("0")){
                return false;
            }
            if (inTemp.contains("1")){
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } finally{
            if(blConnect){
                try {
                    if(socketIn != null)
                        socketIn.close();
                    if(socketOut != null)
                        socketOut.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }



}
