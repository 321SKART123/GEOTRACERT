/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.skart123.geotracert.geotracertserver;

/**
 *
 * @author Ksenya
 */
import com.github.skart123.geotracert.geotracertserver.IpTocoordinates.Location;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class MyWebSocketHandler {

    //Поле отвечает за хранение текущей сессии
    private Session connection;
    
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        connection = session;
        System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        try {
            session.getRemote().sendString("Hello browser, I ServerMessage");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String msgToJSON(String[] unit)
    {
        if(unit.length == 4)
        {
            return "[{\"country\":\"" + unit[0] + "\",\"coordinates\":[" + unit[1] + "," + unit[2] + "], \"IP\":\"" + unit[3] + "\"}]";
        }
        else
        {
            return "";
        }
    }
    
    private void sendMessage(Session session, String msg)
    {
        
        try {
            session.getRemote().sendString(msg);            
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @OnWebSocketMessage
    public void onText(String msg) {
        System.out.println("IP from index.html:");
        System.out.println(msg);

        Location locData = new Location();
        String sendmsg = "";        
         //*************************************************************
        // Пример работы: Получения координат по IP
        Traceroute TracerouteMy = null;
        try {
            TracerouteMy = new Traceroute();
        } catch (Exception ex) {
            System.out.println("Error while Traceroute");
        }
        if(null == TracerouteMy)
        {
            sendMessage(connection, "Ошибка при трассировке. Пожалуйста, попробуйте позже.");
        }
        else
        {
            ArrayList<TracerouteItem> result = TracerouteMy.traceroute(msg);
            for (int i=0; i< result.size(); i++)
            {
//                System.out.println("TRACERT "+i);
//                System.out.println(result.get(i).toString());

                if (locData.getIpGeoBaseDataByIp(result.get(i).toString()) == 0) {
                    locData.getLatitude(); // широта и долгота
                    locData.getLongitude();
                    System.out.println("City = " + locData.getCityName() + " Country: " + locData.getCountryName());
                    System.out.println("Latitude = " + locData.getLatitude() + " Longitude: " + locData.getLongitude());
                    String[] unit = {"", "", "", ""};
                    String temp = locData.getCityName();
                    if(temp != "")
                    {
                        unit[0] += "City = " + temp;
                    }
                    unit[0] += " Country: " + locData.getCountryName();
                    unit[1] = String.valueOf(locData.getLatitude());
                    unit[2] = String.valueOf(locData.getLongitude());
                    unit[3] = msg;
                    // Где-то здесь будет метод парсинга и т.д., так что итогом будет String[] или вообще новая структура JSON
                    // Так что в будущем здесь будет цикл, который переводит цепочку данных по каждому IP в JSON
                    //sendMessage(connection, "[{\"country\":\"" + unit[0] + "\",\"coordinates\":{\"Latitude\":" + unit[1] + ",\"Longitude\":" + unit[3] + "}}]");            //"(\"IPs\":" + msgToJSON(unit) + ")"
                    //sendMessage(connection, "[{\"country\":\"" + unit[0] + "\", \"coordinates\":{\"Latitude\":" + unit[1] + ",\"Longitude\":" + unit[2] + "}}]");
                    sendmsg += msgToJSON(unit);
                    if(result.size() != i + 1)
                    {
                        sendmsg += ", ";
                    }
                    sendMessage(connection, "counted" + i + 1 + "ip locations...");
                } else {
                    System.out.println("Произошла ошибка");
                }
            }
            sendMessage(connection, "{\"Count\":" + result.size() + ", \"IPs\":" + sendmsg + "}");
        }
  
        
        //*************************************************************
    }
}

