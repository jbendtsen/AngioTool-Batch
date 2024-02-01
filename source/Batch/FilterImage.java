public class FilterImage {
    public void filter(int type) {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		byte[] pixels2 = (byte[])getPixelsCopy();
		if (width==1) {
        	filterEdge(type, pixels2, roiHeight, roiX, roiY, 0, 1);
        	return;
		}
		int offset, sum1, sum2=0, sum=0;
        int[] values = new int[10];
        if (type==MEDIAN_FILTER) values = new int[10];
        int rowOffset = width;
        int count;
        int binaryForeground = 255 - binaryBackground;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			p2 = pixels2[offset-rowOffset-1]&0xff;
			p3 = pixels2[offset-rowOffset]&0xff;
			p5 = pixels2[offset-1]&0xff;
			p6 = pixels2[offset]&0xff;
			p8 = pixels2[offset+rowOffset-1]&0xff;
			p9 = pixels2[offset+rowOffset]&0xff;

			for (int x=xMin; x<=xMax; x++) {
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1]&0xff;
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1]&0xff;
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1]&0xff;

				switch (type) {
					case BLUR_MORE:
						sum = (p1+p2+p3+p4+p5+p6+p7+p8+p9+4)/9;
						break;
					case FIND_EDGES: // 3x3 Sobel filter
	        			sum1 = p1 + 2*p2 + p3 - p7 - 2*p8 - p9;
	        			sum2 = p1  + 2*p4 + p7 - p3 - 2*p6 - p9;
	        			sum = (int)Math.sqrt(sum1*sum1 + sum2*sum2);
	        			if (sum> 255) sum = 255;
						break;
					case MEDIAN_FILTER:
						values[1]=p1; values[2]=p2; values[3]=p3; values[4]=p4; values[5]=p5;
						values[6]=p6; values[7]=p7; values[8]=p8; values[9]=p9;
						sum = findMedian(values);
						break;
					case MIN:
						sum = p5;
						if (p1<sum) sum = p1;
						if (p2<sum) sum = p2;
						if (p3<sum) sum = p3;
						if (p4<sum) sum = p4;
						if (p6<sum) sum = p6;
						if (p7<sum) sum = p7;
						if (p8<sum) sum = p8;
						if (p9<sum) sum = p9;
						break;
					case MAX:
						sum = p5;
						if (p1>sum) sum = p1;
						if (p2>sum) sum = p2;
						if (p3>sum) sum = p3;
						if (p4>sum) sum = p4;
						if (p6>sum) sum = p6;
						if (p7>sum) sum = p7;
						if (p8>sum) sum = p8;
						if (p9>sum) sum = p9;
						break;
					case ERODE:
						if (p5==binaryBackground)
							sum = binaryBackground;
						else {
							count = 0;
							if (p1==binaryBackground) count++;
							if (p2==binaryBackground) count++;
							if (p3==binaryBackground) count++;
							if (p4==binaryBackground) count++;
							if (p6==binaryBackground) count++;
							if (p7==binaryBackground) count++;
							if (p8==binaryBackground) count++;
							if (p9==binaryBackground) count++;							
							if (count>=binaryCount)
								sum = binaryBackground;
							else
							sum = binaryForeground;
						}
						break;
					case DILATE:
						if (p5==binaryForeground)
							sum = binaryForeground;
						else {
							count = 0;
							if (p1==binaryForeground) count++;
							if (p2==binaryForeground) count++;
							if (p3==binaryForeground) count++;
							if (p4==binaryForeground) count++;
							if (p6==binaryForeground) count++;
							if (p7==binaryForeground) count++;
							if (p8==binaryForeground) count++;
							if (p9==binaryForeground) count++;							
							if (count>=binaryCount)
								sum = binaryForeground;
							else
								sum = binaryBackground;
						}
						break;
				}
				
				pixels[offset++] = (byte)sum;
			}
		}
        if (xMin==1) filterEdge(type, pixels2, roiHeight, roiX, roiY, 0, 1);
        if (yMin==1) filterEdge(type, pixels2, roiWidth, roiX, roiY, 1, 0);
        if (xMax==width-2) filterEdge(type, pixels2, roiHeight, width-1, roiY, 0, 1);
        if (yMax==height-2) filterEdge(type, pixels2, roiWidth, roiX, height-1, 1, 0);
	}

	void filterEdge(int type, byte[] pixels2, int n, int x, int y, int xinc, int yinc) {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
        int sum=0, sum1, sum2;
        int count;
        int binaryForeground = 255 - binaryBackground;
		int bg = binaryBackground;
		int fg = binaryForeground;
		
		for (int i=0; i<n; i++) {
			if ((!Prefs.padEdges && type==ERODE) || type==DILATE) {
				p1=getEdgePixel0(pixels2,bg,x-1,y-1); p2=getEdgePixel0(pixels2,bg,x,y-1); p3=getEdgePixel0(pixels2,bg,x+1,y-1);
				p4=getEdgePixel0(pixels2,bg,x-1,y); p5=getEdgePixel0(pixels2,bg,x,y); p6=getEdgePixel0(pixels2,bg,x+1,y);
				p7=getEdgePixel0(pixels2,bg,x-1,y+1); p8=getEdgePixel0(pixels2,bg,x,y+1); p9=getEdgePixel0(pixels2,bg,x+1,y+1);
			}  else if (Prefs.padEdges && type==ERODE) {
				p1=getEdgePixel1(pixels2,fg, x-1,y-1); p2=getEdgePixel1(pixels2,fg,x,y-1); p3=getEdgePixel1(pixels2,fg,x+1,y-1);
				p4=getEdgePixel1(pixels2,fg, x-1,y); p5=getEdgePixel1(pixels2,fg,x,y); p6=getEdgePixel1(pixels2,fg,x+1,y);
				p7=getEdgePixel1(pixels2,fg,x-1,y+1); p8=getEdgePixel1(pixels2,fg,x,y+1); p9=getEdgePixel1(pixels2,fg,x+1,y+1);
			} else {
				p1=getEdgePixel(pixels2,x-1,y-1); p2=getEdgePixel(pixels2,x,y-1); p3=getEdgePixel(pixels2,x+1,y-1);
				p4=getEdgePixel(pixels2,x-1,y); p5=getEdgePixel(pixels2,x,y); p6=getEdgePixel(pixels2,x+1,y);
				p7=getEdgePixel(pixels2,x-1,y+1); p8=getEdgePixel(pixels2,x,y+1); p9=getEdgePixel(pixels2,x+1,y+1);
			}
            switch (type) {
                case BLUR_MORE:
                    sum = (p1+p2+p3+p4+p5+p6+p7+p8+p9+4)/9;
                    break;
                case FIND_EDGES: // 3x3 Sobel filter
                    sum1 = p1 + 2*p2 + p3 - p7 - 2*p8 - p9;
                    sum2 = p1  + 2*p4 + p7 - p3 - 2*p6 - p9;
                    sum = (int)Math.sqrt(sum1*sum1 + sum2*sum2);
                    if (sum> 255) sum = 255;
                    break;
                case MIN:
                    sum = p5;
                    if (p1<sum) sum = p1;
                    if (p2<sum) sum = p2;
                    if (p3<sum) sum = p3;
                    if (p4<sum) sum = p4;
                    if (p6<sum) sum = p6;
                    if (p7<sum) sum = p7;
                    if (p8<sum) sum = p8;
                    if (p9<sum) sum = p9;
                    break;
                case MAX:
                    sum = p5;
                    if (p1>sum) sum = p1;
                    if (p2>sum) sum = p2;
                    if (p3>sum) sum = p3;
                    if (p4>sum) sum = p4;
                    if (p6>sum) sum = p6;
                    if (p7>sum) sum = p7;
                    if (p8>sum) sum = p8;
                    if (p9>sum) sum = p9;
                    break;
				case ERODE:
					if (p5==binaryBackground)
						sum = binaryBackground;
					else {
						count = 0;
						if (p1==binaryBackground) count++;
						if (p2==binaryBackground) count++;
						if (p3==binaryBackground) count++;
						if (p4==binaryBackground) count++;
						if (p6==binaryBackground) count++;
						if (p7==binaryBackground) count++;
						if (p8==binaryBackground) count++;
						if (p9==binaryBackground) count++;							
						if (count>=binaryCount)
							sum = binaryBackground;
						else
						sum = binaryForeground;
					}
					break;
				case DILATE:
					if (p5==binaryForeground)
						sum = binaryForeground;
					else {
						count = 0;
						if (p1==binaryForeground) count++;
						if (p2==binaryForeground) count++;
						if (p3==binaryForeground) count++;
						if (p4==binaryForeground) count++;
						if (p6==binaryForeground) count++;
						if (p7==binaryForeground) count++;
						if (p8==binaryForeground) count++;
						if (p9==binaryForeground) count++;							
						if (count>=binaryCount)
							sum = binaryForeground;
						else
							sum = binaryBackground;
					}
					break;
            }
            pixels[x+y*width] = (byte)sum;
            x+=xinc; y+=yinc;
        }
    }
}
