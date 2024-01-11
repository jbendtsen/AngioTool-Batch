import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class EnumerateSimplePoints {

    public static void main(String[] args) {
        final int n = 1 << 26;
        byte[] map = new byte[n / 8];

        for (int i = 0; i < n; i++) {
            boolean isSimple = isSimplePoint(i);
            map[i >>> 3] |= (byte)(isSimple ? (1 << (7 - (i & 7))) : 0);
        }
        try (FileOutputStream stream = new FileOutputStream("all-simple-points.bin")) {
            stream.write(map);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

   public static boolean isSimplePoint(int neighborBits) {
      int label = 2;

      for(int i = 0; i < 26; ++i) {
         if (((neighborBits >>> i) & 1) == 1) {
            switch(i) {
               case 0:
               case 1:
               case 3:
               case 4:
               case 9:
               case 10:
               case 12:
                  neighborBits = octreeLabeling(1, label, neighborBits);
                  break;
               case 2:
               case 5:
               case 11:
               case 13:
                  neighborBits = octreeLabeling(2, label, neighborBits);
                  break;
               case 6:
               case 7:
               case 14:
               case 15:
                  neighborBits = octreeLabeling(3, label, neighborBits);
                  break;
               case 8:
               case 16:
                  neighborBits = octreeLabeling(4, label, neighborBits);
                  break;
               case 17:
               case 18:
               case 20:
               case 21:
                  neighborBits = octreeLabeling(5, label, neighborBits);
                  break;
               case 19:
               case 22:
                  neighborBits = octreeLabeling(6, label, neighborBits);
                  break;
               case 23:
               case 24:
                  neighborBits = octreeLabeling(7, label, neighborBits);
                  break;
               case 25:
                  neighborBits = octreeLabeling(8, label, neighborBits);
            }

            if (++label - 2 >= 2) {
               return false;
            }
         }
      }

      return true;
   }

    public static int octreeLabeling(int octant, int label, int neighborBits) {
      if (octant == 1) {
         if ((neighborBits & 1) == 1) {
            neighborBits &= ~1;
         }

         if (((neighborBits >> 1) & 1) == 1) {
            neighborBits &= ~(1 << 1);
            neighborBits = octreeLabeling(2, label, neighborBits);
         }

         if (((neighborBits >> 3) & 1) == 1) {
            neighborBits &= ~(1 << 3);
            neighborBits = octreeLabeling(3, label, neighborBits);
         }

         if (((neighborBits >> 4) & 1) == 1) {
            neighborBits &= ~(1 << 4);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 9) & 1) == 1) {
            neighborBits &= ~(1 << 9);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 10) & 1) == 1) {
            neighborBits &= ~(1 << 10);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 12) & 1) == 1) {
            neighborBits &= ~(1 << 12);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }
      }

      if (octant == 2) {
         if (((neighborBits >> 1) & 1) == 1) {
            neighborBits &= ~(1 << 1);
            neighborBits = octreeLabeling(1, label, neighborBits);
         }

         if (((neighborBits >> 4) & 1) == 1) {
            neighborBits &= ~(1 << 4);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 10) & 1) == 1) {
            neighborBits &= ~(1 << 10);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 2) & 1) == 1) {
            neighborBits &= ~(1 << 2);
         }

         if (((neighborBits >> 5) & 1) == 1) {
            neighborBits &= ~(1 << 5);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 11) & 1) == 1) {
            neighborBits &= ~(1 << 11);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 13) & 1) == 1) {
            neighborBits &= ~(1 << 13);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 3) {
         if (((neighborBits >> 3) & 1) == 1) {
            neighborBits &= ~(1 << 3);
            neighborBits = octreeLabeling(1, label, neighborBits);
         }

         if (((neighborBits >> 4) & 1) == 1) {
            neighborBits &= ~(1 << 4);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 12) & 1) == 1) {
            neighborBits &= ~(1 << 12);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 6) & 1) == 1) {
            neighborBits &= ~(1 << 6);
         }

         if (((neighborBits >> 7) & 1) == 1) {
            neighborBits &= ~(1 << 7);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 14) & 1) == 1) {
            neighborBits &= ~(1 << 14);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 15) & 1) == 1) {
            neighborBits &= ~(1 << 15);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 4) {
         if (((neighborBits >> 4) & 1) == 1) {
            neighborBits &= ~(1 << 4);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
         }

         if (((neighborBits >> 5) & 1) == 1) {
            neighborBits &= ~(1 << 5);
            neighborBits = octreeLabeling(2, label, neighborBits);
         }

         if (((neighborBits >> 13) & 1) == 1) {
            neighborBits &= ~(1 << 13);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 7) & 1) == 1) {
            neighborBits &= ~(1 << 7);
            neighborBits = octreeLabeling(3, label, neighborBits);
         }

         if (((neighborBits >> 15) & 1) == 1) {
            neighborBits &= ~(1 << 15);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 8) & 1) == 1) {
            neighborBits &= ~(1 << 8);
         }

         if (((neighborBits >> 16) & 1) == 1) {
            neighborBits &= ~(1 << 16);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 5) {
         if (((neighborBits >> 9) & 1) == 1) {
            neighborBits &= ~(1 << 9);
            neighborBits = octreeLabeling(1, label, neighborBits);
         }

         if (((neighborBits >> 10) & 1) == 1) {
            neighborBits &= ~(1 << 10);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 12) & 1) == 1) {
            neighborBits &= ~(1 << 12);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 17) & 1) == 1) {
            neighborBits &= ~(1 << 17);
         }

         if (((neighborBits >> 18) & 1) == 1) {
            neighborBits &= ~(1 << 18);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 20) & 1) == 1) {
            neighborBits &= ~(1 << 20);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 21) & 1) == 1) {
            neighborBits &= ~(1 << 21);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 6) {
         if (((neighborBits >> 10) & 1) == 1) {
            neighborBits &= ~(1 << 10);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 11) & 1) == 1) {
            neighborBits &= ~(1 << 11);
            neighborBits = octreeLabeling(2, label, neighborBits);
         }

         if (((neighborBits >> 13) & 1) == 1) {
            neighborBits &= ~(1 << 13);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 18) & 1) == 1) {
            neighborBits &= ~(1 << 18);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 21) & 1) == 1) {
            neighborBits &= ~(1 << 21);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 19) & 1) == 1) {
            neighborBits &= ~(1 << 19);
         }

         if (((neighborBits >> 22) & 1) == 1) {
            neighborBits &= ~(1 << 22);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 7) {
         if (((neighborBits >> 12) & 1) == 1) {
            neighborBits &= ~(1 << 12);
            neighborBits = octreeLabeling(1, label, neighborBits);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 14) & 1) == 1) {
            neighborBits &= ~(1 << 14);
            neighborBits = octreeLabeling(3, label, neighborBits);
         }

         if (((neighborBits >> 15) & 1) == 1) {
            neighborBits &= ~(1 << 15);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 20) & 1) == 1) {
            neighborBits &= ~(1 << 20);
            neighborBits = octreeLabeling(5, label, neighborBits);
         }

         if (((neighborBits >> 21) & 1) == 1) {
            neighborBits &= ~(1 << 21);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }

         if (((neighborBits >> 23) & 1) == 1) {
            neighborBits &= ~(1 << 23);
         }

         if (((neighborBits >> 24) & 1) == 1) {
            neighborBits &= ~(1 << 24);
            neighborBits = octreeLabeling(8, label, neighborBits);
         }
      }

      if (octant == 8) {
         if (((neighborBits >> 13) & 1) == 1) {
            neighborBits &= ~(1 << 13);
            neighborBits = octreeLabeling(2, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 15) & 1) == 1) {
            neighborBits &= ~(1 << 15);
            neighborBits = octreeLabeling(3, label, neighborBits);
            neighborBits = octreeLabeling(4, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 16) & 1) == 1) {
            neighborBits &= ~(1 << 16);
            neighborBits = octreeLabeling(4, label, neighborBits);
         }

         if (((neighborBits >> 21) & 1) == 1) {
            neighborBits &= ~(1 << 21);
            neighborBits = octreeLabeling(5, label, neighborBits);
            neighborBits = octreeLabeling(6, label, neighborBits);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 22) & 1) == 1) {
            neighborBits &= ~(1 << 22);
            neighborBits = octreeLabeling(6, label, neighborBits);
         }

         if (((neighborBits >> 24) & 1) == 1) {
            neighborBits &= ~(1 << 24);
            neighborBits = octreeLabeling(7, label, neighborBits);
         }

         if (((neighborBits >> 25) & 1) == 1) {
            neighborBits &= ~(1 << 25);
         }
      }
      
      return neighborBits;
   }
}
