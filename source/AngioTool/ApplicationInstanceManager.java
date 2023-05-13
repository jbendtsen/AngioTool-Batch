package AngioTool;

import Utils.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ApplicationInstanceManager {
   private static ApplicationInstanceListener subListener;
   public static final int SINGLE_INSTANCE_NETWORK_SOCKET = 44332;
   public static final String SINGLE_INSTANCE_SHARED_KEY = "$$NewInstance$$\n";

   public static boolean registerInstance() {
      boolean returnValueOnError = true;

      try {
         final ServerSocket socket = new ServerSocket(44332, 10, InetAddress.getLocalHost());
         Thread instanceListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
               boolean socketClosed = false;

               while(!socketClosed) {
                  if (socket.isClosed()) {
                     socketClosed = true;
                  } else {
                     try {
                        Socket client = socket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        String message = in.readLine();
                        if ("$$NewInstance$$\n".trim().equals(message.trim())) {
                           if (!Utils.isReleaseVersion) {
                              System.out.println("Shared key matched - new application instance found");
                           }

                           ApplicationInstanceManager.fireNewInstance();
                        }

                        in.close();
                        client.close();
                     } catch (IOException var5) {
                        socketClosed = true;
                     }
                  }
               }
            }
         });
         instanceListenerThread.start();
         return true;
      } catch (UnknownHostException var6) {
         return returnValueOnError;
      } catch (IOException var7) {
         try {
            Socket clientSocket = new Socket(InetAddress.getLocalHost(), 44332);
            OutputStream out = clientSocket.getOutputStream();
            out.write("$$NewInstance$$\n".getBytes());
            out.close();
            clientSocket.close();
            return false;
         } catch (UnknownHostException var4) {
            return returnValueOnError;
         } catch (IOException var5) {
            return returnValueOnError;
         }
      }
   }

   public static void setApplicationInstanceListener(ApplicationInstanceListener listener) {
      subListener = listener;
   }

   private static void fireNewInstance() {
      if (subListener != null) {
         subListener.newInstanceCreated();
      }
   }
}
