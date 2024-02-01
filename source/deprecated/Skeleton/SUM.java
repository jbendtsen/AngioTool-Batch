package Skeleton;

import ij.ImagePlus;
import ij.ImageStack;

public class SUM {
   private ImagePlus imRef;
   private static int width = 0;
   private static int height = 0;
   private static int depth = 0;
   private static ImageStack inputImage = null;

   public static byte N(ImageStack image, int x, int y, int z) {
      return getPixel(image, x, y - 1, z);
   }

   public static byte S(ImageStack image, int x, int y, int z) {
      return getPixel(image, x, y + 1, z);
   }

   public static byte E(ImageStack image, int x, int y, int z) {
      return getPixel(image, x + 1, y, z);
   }

   public static byte W(ImageStack image, int x, int y, int z) {
      return getPixel(image, x - 1, y, z);
   }

   public static byte U(ImageStack image, int x, int y, int z) {
      return getPixel(image, x, y, z + 1);
   }

   public static byte B(ImageStack image, int x, int y, int z) {
      return getPixel(image, x, y, z - 1);
   }

   public static byte[] getNeighborhood(ImageStack image, int x, int y, int z) {
      return new byte[]{
         getPixel(image, x - 1, y - 1, z - 1),
         getPixel(image, x, y - 1, z - 1),
         getPixel(image, x + 1, y - 1, z - 1),
         getPixel(image, x - 1, y, z - 1),
         getPixel(image, x, y, z - 1),
         getPixel(image, x + 1, y, z - 1),
         getPixel(image, x - 1, y + 1, z - 1),
         getPixel(image, x, y + 1, z - 1),
         getPixel(image, x + 1, y + 1, z - 1),
         getPixel(image, x - 1, y - 1, z),
         getPixel(image, x, y - 1, z),
         getPixel(image, x + 1, y - 1, z),
         getPixel(image, x - 1, y, z),
         getPixel(image, x, y, z),
         getPixel(image, x + 1, y, z),
         getPixel(image, x - 1, y + 1, z),
         getPixel(image, x, y + 1, z),
         getPixel(image, x + 1, y + 1, z),
         getPixel(image, x - 1, y - 1, z + 1),
         getPixel(image, x, y - 1, z + 1),
         getPixel(image, x + 1, y - 1, z + 1),
         getPixel(image, x - 1, y, z + 1),
         getPixel(image, x, y, z + 1),
         getPixel(image, x + 1, y, z + 1),
         getPixel(image, x - 1, y + 1, z + 1),
         getPixel(image, x, y + 1, z + 1),
         getPixel(image, x + 1, y + 1, z + 1)
      };
   }

   public static byte getPixel(ImageStack image, int x, int y, int z) {
      return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth ? ((byte[])image.getPixels(z + 1))[x + y * width] : 0;
   }

   public static void setPixel(ImageStack image, int x, int y, int z, byte value) {
      if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
         ((byte[])image.getPixels(z + 1))[x + y * width] = value;
      }
   }

   public static boolean isEulerInvariant(byte[] neighbors, int[] LUT) {
      int eulerChar = 0;
      char n = '\u0001';
      if (neighbors[24] == 1) {
         n = (char)(n | 128);
      }

      if (neighbors[25] == 1) {
         n = (char)(n | '@');
      }

      if (neighbors[15] == 1) {
         n = (char)(n | ' ');
      }

      if (neighbors[16] == 1) {
         n = (char)(n | 16);
      }

      if (neighbors[21] == 1) {
         n = (char)(n | '\b');
      }

      if (neighbors[22] == 1) {
         n = (char)(n | 4);
      }

      if (neighbors[12] == 1) {
         n = (char)(n | 2);
      }

      eulerChar += LUT[n];
      n = '\u0001';
      if (neighbors[26] == 1) {
         n = (char)(n | 128);
      }

      if (neighbors[23] == 1) {
         n = (char)(n | '@');
      }

      if (neighbors[17] == 1) {
         n = (char)(n | ' ');
      }

      if (neighbors[14] == 1) {
         n = (char)(n | 16);
      }

      if (neighbors[25] == 1) {
         n = (char)(n | '\b');
      }

      if (neighbors[22] == 1) {
         n = (char)(n | 4);
      }

      if (neighbors[16] == 1) {
         n = (char)(n | 2);
      }

      eulerChar += LUT[n];
      n = '\u0001';
      if (neighbors[18] == 1) {
         n = (char)(n | 128);
      }

      if (neighbors[21] == 1) {
         n = (char)(n | '@');
      }

      if (neighbors[9] == 1) {
         n = (char)(n | ' ');
      }

      if (neighbors[12] == 1) {
         n = (char)(n | 16);
      }

      if (neighbors[19] == 1) {
         n = (char)(n | '\b');
      }

      if (neighbors[22] == 1) {
         n = (char)(n | 4);
      }

      if (neighbors[10] == 1) {
         n = (char)(n | 2);
      }

      eulerChar += LUT[n];
      n = '\u0001';
      if (neighbors[20] == 1) {
         n = (char)(n | 128);
      }

      if (neighbors[23] == 1) {
         n = (char)(n | '@');
      }

      if (neighbors[19] == 1) {
         n = (char)(n | ' ');
      }

      if (neighbors[22] == 1) {
         n = (char)(n | 16);
      }

      if (neighbors[11] == 1) {
         n = (char)(n | '\b');
      }

      if (neighbors[14] == 1) {
         n = (char)(n | 4);
      }

      if (neighbors[10] == 1) {
         n = (char)(n | 2);
      }

      eulerChar += LUT[n];
      n = '\u0001';
      if (neighbors[6] == 1) {
         n = (char)(n | 128);
      }

      if (neighbors[15] == 1) {
         n = (char)(n | '@');
      }

      if (neighbors[7] == 1) {
         n = (char)(n | ' ');
      }

      if (neighbors[16] == 1) {
         n = (char)(n | 16);
      }

      if (neighbors[3] == 1) {
         n = (char)(n | '\b');
      }

      if (neighbors[12] == 1) {
         n = (char)(n | 4);
      }

      if (neighbors[4] == 1) {
         n = (char)(n | 2);
      }

      eulerChar += LUT[n];
      n = '\u0001';
      if (neighbors[8] == 1) {
         n = (char)(n | 128);
      }

      if (neighbors[7] == 1) {
         n = (char)(n | '@');
      }

      if (neighbors[17] == 1) {
         n = (char)(n | ' ');
      }

      if (neighbors[16] == 1) {
         n = (char)(n | 16);
      }

      if (neighbors[5] == 1) {
         n = (char)(n | '\b');
      }

      if (neighbors[4] == 1) {
         n = (char)(n | 4);
      }

      if (neighbors[14] == 1) {
         n = (char)(n | 2);
      }

      eulerChar += LUT[n];
      n = '\u0001';
      if (neighbors[0] == 1) {
         n = (char)(n | 128);
      }

      if (neighbors[9] == 1) {
         n = (char)(n | '@');
      }

      if (neighbors[3] == 1) {
         n = (char)(n | ' ');
      }

      if (neighbors[12] == 1) {
         n = (char)(n | 16);
      }

      if (neighbors[1] == 1) {
         n = (char)(n | '\b');
      }

      if (neighbors[10] == 1) {
         n = (char)(n | 4);
      }

      if (neighbors[4] == 1) {
         n = (char)(n | 2);
      }

      eulerChar += LUT[n];
      n = '\u0001';
      if (neighbors[2] == 1) {
         n = (char)(n | 128);
      }

      if (neighbors[1] == 1) {
         n = (char)(n | '@');
      }

      if (neighbors[11] == 1) {
         n = (char)(n | ' ');
      }

      if (neighbors[10] == 1) {
         n = (char)(n | 16);
      }

      if (neighbors[5] == 1) {
         n = (char)(n | '\b');
      }

      if (neighbors[4] == 1) {
         n = (char)(n | 4);
      }

      if (neighbors[14] == 1) {
         n = (char)(n | 2);
      }

      eulerChar += LUT[n];
      return eulerChar == 0;
   }

   public static boolean isSimplePoint(byte[] neighbors) {
      int[] cube = new int[26];
      int i = 0;

      for(int var4 = 0; var4 < 13; ++var4) {
         cube[var4] = neighbors[var4];
      }

      for(int var5 = 14; var5 < 27; ++var5) {
         cube[var5 - 1] = neighbors[var5];
      }

      int label = 2;

      for(int var6 = 0; var6 < 26; ++var6) {
         if (cube[var6] == 1) {
            switch(var6) {
               case 0:
               case 1:
               case 3:
               case 4:
               case 9:
               case 10:
               case 12:
                  octreeLabeling(1, label, cube);
                  break;
               case 2:
               case 5:
               case 11:
               case 13:
                  octreeLabeling(2, label, cube);
                  break;
               case 6:
               case 7:
               case 14:
               case 15:
                  octreeLabeling(3, label, cube);
                  break;
               case 8:
               case 16:
                  octreeLabeling(4, label, cube);
                  break;
               case 17:
               case 18:
               case 20:
               case 21:
                  octreeLabeling(5, label, cube);
                  break;
               case 19:
               case 22:
                  octreeLabeling(6, label, cube);
                  break;
               case 23:
               case 24:
                  octreeLabeling(7, label, cube);
                  break;
               case 25:
                  octreeLabeling(8, label, cube);
            }

            if (++label - 2 >= 2) {
               return false;
            }
         }
      }

      return true;
   }

   public static void octreeLabeling(int octant, int label, int[] cube) {
      if (octant == 1) {
         if (cube[0] == 1) {
            cube[0] = label;
         }

         if (cube[1] == 1) {
            cube[1] = label;
            octreeLabeling(2, label, cube);
         }

         if (cube[3] == 1) {
            cube[3] = label;
            octreeLabeling(3, label, cube);
         }

         if (cube[4] == 1) {
            cube[4] = label;
            octreeLabeling(2, label, cube);
            octreeLabeling(3, label, cube);
            octreeLabeling(4, label, cube);
         }

         if (cube[9] == 1) {
            cube[9] = label;
            octreeLabeling(5, label, cube);
         }

         if (cube[10] == 1) {
            cube[10] = label;
            octreeLabeling(2, label, cube);
            octreeLabeling(5, label, cube);
            octreeLabeling(6, label, cube);
         }

         if (cube[12] == 1) {
            cube[12] = label;
            octreeLabeling(3, label, cube);
            octreeLabeling(5, label, cube);
            octreeLabeling(7, label, cube);
         }
      }

      if (octant == 2) {
         if (cube[1] == 1) {
            cube[1] = label;
            octreeLabeling(1, label, cube);
         }

         if (cube[4] == 1) {
            cube[4] = label;
            octreeLabeling(1, label, cube);
            octreeLabeling(3, label, cube);
            octreeLabeling(4, label, cube);
         }

         if (cube[10] == 1) {
            cube[10] = label;
            octreeLabeling(1, label, cube);
            octreeLabeling(5, label, cube);
            octreeLabeling(6, label, cube);
         }

         if (cube[2] == 1) {
            cube[2] = label;
         }

         if (cube[5] == 1) {
            cube[5] = label;
            octreeLabeling(4, label, cube);
         }

         if (cube[11] == 1) {
            cube[11] = label;
            octreeLabeling(6, label, cube);
         }

         if (cube[13] == 1) {
            cube[13] = label;
            octreeLabeling(4, label, cube);
            octreeLabeling(6, label, cube);
            octreeLabeling(8, label, cube);
         }
      }

      if (octant == 3) {
         if (cube[3] == 1) {
            cube[3] = label;
            octreeLabeling(1, label, cube);
         }

         if (cube[4] == 1) {
            cube[4] = label;
            octreeLabeling(1, label, cube);
            octreeLabeling(2, label, cube);
            octreeLabeling(4, label, cube);
         }

         if (cube[12] == 1) {
            cube[12] = label;
            octreeLabeling(1, label, cube);
            octreeLabeling(5, label, cube);
            octreeLabeling(7, label, cube);
         }

         if (cube[6] == 1) {
            cube[6] = label;
         }

         if (cube[7] == 1) {
            cube[7] = label;
            octreeLabeling(4, label, cube);
         }

         if (cube[14] == 1) {
            cube[14] = label;
            octreeLabeling(7, label, cube);
         }

         if (cube[15] == 1) {
            cube[15] = label;
            octreeLabeling(4, label, cube);
            octreeLabeling(7, label, cube);
            octreeLabeling(8, label, cube);
         }
      }

      if (octant == 4) {
         if (cube[4] == 1) {
            cube[4] = label;
            octreeLabeling(1, label, cube);
            octreeLabeling(2, label, cube);
            octreeLabeling(3, label, cube);
         }

         if (cube[5] == 1) {
            cube[5] = label;
            octreeLabeling(2, label, cube);
         }

         if (cube[13] == 1) {
            cube[13] = label;
            octreeLabeling(2, label, cube);
            octreeLabeling(6, label, cube);
            octreeLabeling(8, label, cube);
         }

         if (cube[7] == 1) {
            cube[7] = label;
            octreeLabeling(3, label, cube);
         }

         if (cube[15] == 1) {
            cube[15] = label;
            octreeLabeling(3, label, cube);
            octreeLabeling(7, label, cube);
            octreeLabeling(8, label, cube);
         }

         if (cube[8] == 1) {
            cube[8] = label;
         }

         if (cube[16] == 1) {
            cube[16] = label;
            octreeLabeling(8, label, cube);
         }
      }

      if (octant == 5) {
         if (cube[9] == 1) {
            cube[9] = label;
            octreeLabeling(1, label, cube);
         }

         if (cube[10] == 1) {
            cube[10] = label;
            octreeLabeling(1, label, cube);
            octreeLabeling(2, label, cube);
            octreeLabeling(6, label, cube);
         }

         if (cube[12] == 1) {
            cube[12] = label;
            octreeLabeling(1, label, cube);
            octreeLabeling(3, label, cube);
            octreeLabeling(7, label, cube);
         }

         if (cube[17] == 1) {
            cube[17] = label;
         }

         if (cube[18] == 1) {
            cube[18] = label;
            octreeLabeling(6, label, cube);
         }

         if (cube[20] == 1) {
            cube[20] = label;
            octreeLabeling(7, label, cube);
         }

         if (cube[21] == 1) {
            cube[21] = label;
            octreeLabeling(6, label, cube);
            octreeLabeling(7, label, cube);
            octreeLabeling(8, label, cube);
         }
      }

      if (octant == 6) {
         if (cube[10] == 1) {
            cube[10] = label;
            octreeLabeling(1, label, cube);
            octreeLabeling(2, label, cube);
            octreeLabeling(5, label, cube);
         }

         if (cube[11] == 1) {
            cube[11] = label;
            octreeLabeling(2, label, cube);
         }

         if (cube[13] == 1) {
            cube[13] = label;
            octreeLabeling(2, label, cube);
            octreeLabeling(4, label, cube);
            octreeLabeling(8, label, cube);
         }

         if (cube[18] == 1) {
            cube[18] = label;
            octreeLabeling(5, label, cube);
         }

         if (cube[21] == 1) {
            cube[21] = label;
            octreeLabeling(5, label, cube);
            octreeLabeling(7, label, cube);
            octreeLabeling(8, label, cube);
         }

         if (cube[19] == 1) {
            cube[19] = label;
         }

         if (cube[22] == 1) {
            cube[22] = label;
            octreeLabeling(8, label, cube);
         }
      }

      if (octant == 7) {
         if (cube[12] == 1) {
            cube[12] = label;
            octreeLabeling(1, label, cube);
            octreeLabeling(3, label, cube);
            octreeLabeling(5, label, cube);
         }

         if (cube[14] == 1) {
            cube[14] = label;
            octreeLabeling(3, label, cube);
         }

         if (cube[15] == 1) {
            cube[15] = label;
            octreeLabeling(3, label, cube);
            octreeLabeling(4, label, cube);
            octreeLabeling(8, label, cube);
         }

         if (cube[20] == 1) {
            cube[20] = label;
            octreeLabeling(5, label, cube);
         }

         if (cube[21] == 1) {
            cube[21] = label;
            octreeLabeling(5, label, cube);
            octreeLabeling(6, label, cube);
            octreeLabeling(8, label, cube);
         }

         if (cube[23] == 1) {
            cube[23] = label;
         }

         if (cube[24] == 1) {
            cube[24] = label;
            octreeLabeling(8, label, cube);
         }
      }

      if (octant == 8) {
         if (cube[13] == 1) {
            cube[13] = label;
            octreeLabeling(2, label, cube);
            octreeLabeling(4, label, cube);
            octreeLabeling(6, label, cube);
         }

         if (cube[15] == 1) {
            cube[15] = label;
            octreeLabeling(3, label, cube);
            octreeLabeling(4, label, cube);
            octreeLabeling(7, label, cube);
         }

         if (cube[16] == 1) {
            cube[16] = label;
            octreeLabeling(4, label, cube);
         }

         if (cube[21] == 1) {
            cube[21] = label;
            octreeLabeling(5, label, cube);
            octreeLabeling(6, label, cube);
            octreeLabeling(7, label, cube);
         }

         if (cube[22] == 1) {
            cube[22] = label;
            octreeLabeling(6, label, cube);
         }

         if (cube[24] == 1) {
            cube[24] = label;
            octreeLabeling(7, label, cube);
         }

         if (cube[25] == 1) {
            cube[25] = label;
         }
      }
   }

   public static void fillEulerLUT(int[] LUT) {
      LUT[1] = 1;
      LUT[3] = -1;
      LUT[5] = -1;
      LUT[7] = 1;
      LUT[9] = -3;
      LUT[11] = -1;
      LUT[13] = -1;
      LUT[15] = 1;
      LUT[17] = -1;
      LUT[19] = 1;
      LUT[21] = 1;
      LUT[23] = -1;
      LUT[25] = 3;
      LUT[27] = 1;
      LUT[29] = 1;
      LUT[31] = -1;
      LUT[33] = -3;
      LUT[35] = -1;
      LUT[37] = 3;
      LUT[39] = 1;
      LUT[41] = 1;
      LUT[43] = -1;
      LUT[45] = 3;
      LUT[47] = 1;
      LUT[49] = -1;
      LUT[51] = 1;
      LUT[53] = 1;
      LUT[55] = -1;
      LUT[57] = 3;
      LUT[59] = 1;
      LUT[61] = 1;
      LUT[63] = -1;
      LUT[65] = -3;
      LUT[67] = 3;
      LUT[69] = -1;
      LUT[71] = 1;
      LUT[73] = 1;
      LUT[75] = 3;
      LUT[77] = -1;
      LUT[79] = 1;
      LUT[81] = -1;
      LUT[83] = 1;
      LUT[85] = 1;
      LUT[87] = -1;
      LUT[89] = 3;
      LUT[91] = 1;
      LUT[93] = 1;
      LUT[95] = -1;
      LUT[97] = 1;
      LUT[99] = 3;
      LUT[101] = 3;
      LUT[103] = 1;
      LUT[105] = 5;
      LUT[107] = 3;
      LUT[109] = 3;
      LUT[111] = 1;
      LUT[113] = -1;
      LUT[115] = 1;
      LUT[117] = 1;
      LUT[119] = -1;
      LUT[121] = 3;
      LUT[123] = 1;
      LUT[125] = 1;
      LUT[127] = -1;
      LUT[129] = -7;
      LUT[131] = -1;
      LUT[133] = -1;
      LUT[135] = 1;
      LUT[137] = -3;
      LUT[139] = -1;
      LUT[141] = -1;
      LUT[143] = 1;
      LUT[145] = -1;
      LUT[147] = 1;
      LUT[149] = 1;
      LUT[151] = -1;
      LUT[153] = 3;
      LUT[155] = 1;
      LUT[157] = 1;
      LUT[159] = -1;
      LUT[161] = -3;
      LUT[163] = -1;
      LUT[165] = 3;
      LUT[167] = 1;
      LUT[169] = 1;
      LUT[171] = -1;
      LUT[173] = 3;
      LUT[175] = 1;
      LUT[177] = -1;
      LUT[179] = 1;
      LUT[181] = 1;
      LUT[183] = -1;
      LUT[185] = 3;
      LUT[187] = 1;
      LUT[189] = 1;
      LUT[191] = -1;
      LUT[193] = -3;
      LUT[195] = 3;
      LUT[197] = -1;
      LUT[199] = 1;
      LUT[201] = 1;
      LUT[203] = 3;
      LUT[205] = -1;
      LUT[207] = 1;
      LUT[209] = -1;
      LUT[211] = 1;
      LUT[213] = 1;
      LUT[215] = -1;
      LUT[217] = 3;
      LUT[219] = 1;
      LUT[221] = 1;
      LUT[223] = -1;
      LUT[225] = 1;
      LUT[227] = 3;
      LUT[229] = 3;
      LUT[231] = 1;
      LUT[233] = 5;
      LUT[235] = 3;
      LUT[237] = 3;
      LUT[239] = 1;
      LUT[241] = -1;
      LUT[243] = 1;
      LUT[245] = 1;
      LUT[247] = -1;
      LUT[249] = 3;
      LUT[251] = 1;
      LUT[253] = 1;
      LUT[255] = -1;
   }

   public static void setImage(ImagePlus imRef) {
      width = imRef.getWidth();
      height = imRef.getHeight();
      depth = imRef.getStackSize();
   }
}
