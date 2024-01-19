package AngioTool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.BitSet;

public class ATURLEncoder {
   private static BitSet safeCharacters = new BitSet(256);
   private static final char[] hexadecimal = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

   public static String encodePath(String path) {
      System.out.println("printint array");
      Arrays.toString(safeCharacters.toByteArray());
      int maxBytesPerChar = 10;
      StringBuffer rewrittenPath = new StringBuffer(path.length());
      ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);

      OutputStreamWriter writer;
      try {
         writer = new OutputStreamWriter(buf, "UTF8");
      } catch (Exception var12) {
         var12.printStackTrace();
         writer = new OutputStreamWriter(buf);
      }

      for(int i = 0; i < path.length(); ++i) {
         int c = path.charAt(i);
         if (safeCharacters.get(c)) {
            rewrittenPath.append((char)c);
         } else {
            try {
               writer.write(c);
               writer.flush();
            } catch (IOException var13) {
               buf.reset();
               continue;
            }

            byte[] ba = buf.toByteArray();

            for(int j = 0; j < ba.length; ++j) {
               byte toEncode = ba[j];
               rewrittenPath.append("%");
               int low = toEncode & 15;
               int high = (toEncode & 240) >> 4;
               rewrittenPath.append(hexadecimal[high]);
               rewrittenPath.append(hexadecimal[low]);
            }

            buf.reset();
         }
      }

      return rewrittenPath.toString();
   }

   static {
      for(int i = 97; i <= 122; ++i) {
         safeCharacters.set(i);
      }

      for(int var1 = 65; var1 <= 90; ++var1) {
         safeCharacters.set(var1);
      }

      for(int var2 = 48; var2 <= 57; ++var2) {
         safeCharacters.set(var2);
      }

      safeCharacters.set(36);
      safeCharacters.set(45);
      safeCharacters.set(95);
      safeCharacters.set(46);
      safeCharacters.set(43);
      safeCharacters.set(33);
      safeCharacters.set(42);
      safeCharacters.set(39);
      safeCharacters.set(40);
      safeCharacters.set(41);
      safeCharacters.set(44);
      safeCharacters.set(47);
      safeCharacters.set(58);
      safeCharacters.set(64);
      safeCharacters.set(38);
      safeCharacters.set(61);
   }
}
