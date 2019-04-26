package com.clt.ess.utils;

import com.clt.ess.webservice.SendFileDataHandler;

import javax.jws.WebMethod;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Properties;

public class SocketUtils {

    public static String hostname;
    private static void initData() {
        Properties prop = new Properties();
        try{
            //读取属性文件a.properties
            InputStream in = SocketUtils.class.getClassLoader().getResource("config.properties").openStream();
            prop.load(in);     ///加载属性列表
            Iterator<String> it=prop.stringPropertyNames().iterator();
            while(it.hasNext()){
                String key=it.next();
                if ("FtpPath".equals(key)){
                    hostname=prop.getProperty(key);
                }
            }
            in.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    public static boolean wordToPdfClient(String wordPath) {
//        initData();
        int port = 9990;
        boolean blConnect = false;
        Socket socket = null;
        BufferedReader socketIn = null;
        PrintWriter socketOut = null;
//        int ipAddressTemp[] = {10, 41, 0, 66};10.41.0.66
        try {
//            InetAddress ipAddress = InetAddress.getByAddress(ipAddressTemp);
            socket = new Socket("10.41.0.66", port);
            socket.setSoTimeout(150000);
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

    public static void main(String[] args) {
        Properties prop = new Properties();
        try{
            //读取属性文件a.properties
            InputStream in = SocketUtils.class.getClassLoader().getResource("config.properties").openStream();
            prop.load(in);     ///加载属性列表
            Iterator<String> it=prop.stringPropertyNames().iterator();
            while(it.hasNext()){
                String key=it.next();
                System.out.println(key+":"+prop.getProperty(key));
            }
            in.close();
        }
        catch(Exception e){
            System.out.println(e);
        }


        System.out.println(SocketUtils.class.getClassLoader().getResource(""));
    }



}
