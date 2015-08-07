package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import serial.SerialConnection;
import bmp085.BMP085;

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;


/**
 * Hello world!
 *
 */
public class Main 
{
    static private class Command {

        public char getCommand() {
            return command;
        }

        public void setCommand(char command) {
            this.command = command;
        }

        public char getValue() {
            return value;
        }

        public void setValue(char value) {
            this.value = value;
        }
        private char command;
        private char value;
        private boolean valid;
        
        public String build(){
            return new String(command+value+'\n'+"");        
        }
        public boolean isValid(){
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
    }
    
    static private byte[] parseInput(String msg){
        byte cmd = msg.getBytes()[0];
        
        char side = msg.charAt(0);
        
        Command conquer = new Command();
        System.out.println("side - " + side);
        
        String digit = msg.substring(1);
        System.out.println("digit - " + digit);

        byte value = Byte.parseByte(digit);
        
        byte[] bytes =  new byte[]{cmd,value,'\n'};
        return bytes;

        
    }
    
    public static void main( String[] args )
    {

        //BMP085 bmp085 = new BMP085();
        //System.out.println(bmp085.toString());

        final SerialConnection serial = new SerialConnection(57600);

        
        
        Undertow server;
        server = Undertow.builder()
                .addHttpListener(8080, "192.168.43.40")
                .setHandler(path()
                        .addPrefixPath("/myapp", websocket(new WebSocketConnectionCallback() {

                            @Override
                            public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
                                channel.getReceiveSetter().set(new AbstractReceiveListener() {

                                    @Override
                                    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                                        final String messageData = message.getData();
                                        for (WebSocketChannel session : channel.getPeerConnections()) {
                                            WebSockets.sendText(messageData, session, null);
                                            System.out.println(messageData);

                                            serial.write(parseInput(messageData));
                                            /*
                                            Command c = parseInput(messageData);
                                            if(c.isValid()) {
                                            String build = c.build();
                                            System.out.println(build);
                                            
                                            serial.write(build);
                                            }
                                                    */
                                        }
                                    }
                                });
                                channel.resumeReceives();
                            }

                        }))
                        .addPrefixPath("/", resource(new ClassPathResourceManager(Main.class.getClassLoader(), Main.class.getPackage()))
                                .addWelcomeFiles("index.html")))
                .build();

        server.start(); 
      
    }
}
