package AngioTool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ReachableTest {
   private InetAddress myIP;
   private InetAddress locatHost;
   private String myIPAddr;
   private String myIPHostName;

   public boolean test() {
      try {
         this.myIP = InetAddress.getByName("smtp.gmail.com");
         return true;
      } catch (UnknownHostException var2) {
         return false;
      } catch (IOException var3) {
         return false;
      }
   }

   public InetAddress getIP() {
      return this.myIP;
   }
}
