package Tiff;

import java.io.*;

// Modified version of ij.io.TiffEncoder and ij.io.ImageWriter

/**Saves an image described by a ImageInfo object as an uncompressed TIFF file.*/
public class TiffEncoder {
	static final int HDR_SIZE = 8;
	static final int MAP_SIZE = 768; // in 16-bit words
	static final int BPS_DATA_SIZE = 6;
	static final int SCALE_DATA_SIZE = 16;
		
	private ImageInfo fi;
	private int bitsPerSample;
	private int photoInterp;
	private int samplesPerPixel;
	private int nEntries;
	private int ifdSize;
	private long imageOffset;
	private int imageSize;
	private long stackSize;
	private byte[] description;
	private int metaDataSize;
	private int nMetaDataTypes;
	private int nMetaDataEntries;
	private int nSliceLabels;
	private int extraMetaDataEntries;
	private int scaleSize;
	private boolean littleEndian = true; //intelByteOrder;
	private byte buffer[] = new byte[8];
	private int colorMapSize = 0;

	public TiffEncoder(ImageInfo fi) {
		this.fi = fi;
		fi.intelByteOrder = littleEndian;
		bitsPerSample = 8;
		samplesPerPixel = fi.samplesPerPixel;
		nEntries = 10;
		int bytesPerPixel = 1;
		int bpsSize = 0;

		switch (fi.fileType) {
			case ImageInfo.GRAY8:
				photoInterp = fi.whiteIsZero?0:1;
				break;
			case ImageInfo.GRAY16_UNSIGNED:
			case ImageInfo.GRAY16_SIGNED:
				bitsPerSample = 16;
				photoInterp = fi.whiteIsZero?0:1;
				if (fi.lutSize>0) {
					nEntries++;
					colorMapSize = MAP_SIZE*2;
				}
				bytesPerPixel = 2;
				break;
			case ImageInfo.GRAY32_FLOAT:
				bitsPerSample = 32;
				photoInterp = fi.whiteIsZero?0:1;
				if (fi.lutSize>0) {
					nEntries++;
					colorMapSize = MAP_SIZE*2;
				}
				bytesPerPixel = 4;
				break;
			case ImageInfo.RGB:
				photoInterp = 2;
				samplesPerPixel = Math.max(samplesPerPixel, 3);
				bytesPerPixel = samplesPerPixel;
				bpsSize = BPS_DATA_SIZE;
				break;
			case ImageInfo.RGB48:
				bitsPerSample = 16;
				photoInterp = 2;
				samplesPerPixel = 3;
				bytesPerPixel = 6;
				fi.nImages /= 3;
				bpsSize = BPS_DATA_SIZE;
				break;
			case ImageInfo.COLOR8:
				photoInterp = 3;
				nEntries++;
				colorMapSize = MAP_SIZE*2;
				break;
			default:
				photoInterp = 0;
		}
		if (fi.unit!=null && fi.pixelWidth!=0 && fi.pixelHeight!=0)
			nEntries += 3; // XResolution, YResolution and ResolutionUnit
		if (fi.fileType==fi.GRAY32_FLOAT)
			nEntries++; // SampleFormat tag
		makeDescriptionString();
		if (description!=null)
			nEntries++;  // ImageDescription tag
		long size = (long)fi.width*fi.height*bytesPerPixel;
		imageSize = size<=0xffffffffL?(int)size:0;
		stackSize = (long)imageSize*fi.nImages;
		metaDataSize = getMetaDataSize();
		if (metaDataSize>0)
			nEntries += 2; // MetaData & MetaDataCounts
		ifdSize = 2 + nEntries*12 + 4;
		int descriptionSize = description!=null?description.length:0;
		scaleSize = fi.unit!=null && fi.pixelWidth!=0 && fi.pixelHeight!=0?SCALE_DATA_SIZE:0;
		imageOffset = HDR_SIZE+ifdSize+bpsSize+descriptionSize+scaleSize+colorMapSize + nMetaDataEntries*4 + metaDataSize;
		fi.offset = (int)imageOffset;
		//System.out.println(imageOffset+", "+ifdSize+", "+bpsSize+", "+descriptionSize+", "+scaleSize+", "+colorMapSize+", "+nMetaDataEntries*4+", "+metaDataSize);
	}
	
	/** Saves the image as a TIFF file. The OutputStream is not closed.
		The fi.pixels field must contain the image data. If fi.nImages>1
		then fi.pixels must be a 2D array. The fi.offset field is ignored. */
	public void write(OutputStream out) throws IOException {
		writeHeader(out);
		long nextIFD = 0L;
		if (fi.nImages>1)
			nextIFD = imageOffset+stackSize;
		boolean bigTiff = nextIFD+fi.nImages*ifdSize>=0xffffffffL;
		if (bigTiff)
			nextIFD = 0L;
		writeIFD(out, (int)imageOffset, (int)nextIFD);
		if (fi.fileType==ImageInfo.RGB||fi.fileType==ImageInfo.RGB48)
			writeBitsPerPixel(out);
		if (description!=null)
			writeDescription(out);
		if (scaleSize>0)
			writeScale(out);
		if (colorMapSize>0)
			writeColorMap(out);
		if (metaDataSize>0)
			writeMetaData(out);
		writePixelData(out);
		if (nextIFD>0L) {
			int ifdSize2 = ifdSize;
			if (metaDataSize>0) {
				metaDataSize = 0;
				nEntries -= 2;
				ifdSize2 -= 2*12;
			}
			for (int i=2; i<=fi.nImages; i++) {
				if (i==fi.nImages)
					nextIFD = 0;
				else
					nextIFD += ifdSize2;
				imageOffset += imageSize;
				writeIFD(out, (int)imageOffset, (int)nextIFD);
			}
		} else if (bigTiff)
			System.out.println("Stack is larger than 4GB. Most TIFF readers will only open the first image. Use this information to open as raw:\n"+fi);
	}

	private void writePixelData(OutputStream out) throws IOException {
		if (fi.pixels==null)
				throw new IOException("ImageWriter: fi.pixels==null");
		if (fi.nImages>1 && !(fi.pixels instanceof Object[]))
				throw new IOException("ImageWriter: fi.pixels not a stack");
		switch (fi.fileType) {
			case ImageInfo.GRAY8:
			case ImageInfo.COLOR8:
				if (fi.nImages>1)
					write8BitStack(out, (Object[])fi.pixels);
				else
					write8BitImage(out, (byte[])fi.pixels);
				break;
			case ImageInfo.GRAY16_SIGNED:
			case ImageInfo.GRAY16_UNSIGNED:
				if (fi.nImages>1)
					write16BitStack(out, (Object[])fi.pixels);
				else
					write16BitImage(out, (short[])fi.pixels);
				break;
			case ImageInfo.RGB48:
				writeRGB48Image(out, (Object[])fi.pixels);
				break;
			case ImageInfo.GRAY32_FLOAT:
				if (fi.nImages>1)
					writeFloatStack(out, (Object[])fi.pixels);
				else
					writeFloatImage(out, (float[])fi.pixels);
				break;
			case ImageInfo.RGB:
				if (fi.nImages>1)
					writeRGBStack(out, (Object[])fi.pixels);
				else
					writeRGBImage(out, (int[])fi.pixels);
				break;
			default:
			    break;
		}
	}

	private void write8BitImage(OutputStream out, byte[] pixels) throws IOException {
		int bytesWritten = 0;
		int size = fi.width*fi.height*samplesPerPixel;
		int count = getWritingChunkSize(size);
		while (bytesWritten<size) {
			if ((bytesWritten + count)>size)
				count = size - bytesWritten;
			//System.out.println(bytesWritten + " " + count + " " + size);
			out.write(pixels, bytesWritten, count);
			bytesWritten += count;
		}
	}
	
	private void write8BitStack(OutputStream out, Object[] stack) throws IOException {
		for (int i=0; i<fi.nImages; i++)
			write8BitImage(out, (byte[])stack[i]);
	}

	private void write16BitImage(OutputStream out, short[] pixels)  throws IOException {
		long bytesWritten = 0L;
		long size = 2L*fi.width*fi.height*samplesPerPixel;
		int count = getWritingChunkSize(size);
		byte[] buffer = new byte[count];

		while (bytesWritten<size) {
			if ((bytesWritten + count)>size)
				count = (int)(size-bytesWritten);
			int j = (int)(bytesWritten/2L);
			int value;
			if (fi.intelByteOrder)
				for (int i=0; i < count; i+=2) {
					value = pixels[j];
					buffer[i] = (byte)value;
					buffer[i+1] = (byte)(value>>>8);
					j++;
				}
			else
				for (int i=0; i < count; i+=2) {
					value = pixels[j];
					buffer[i] = (byte)(value>>>8);
					buffer[i+1] = (byte)value;
					j++;
				}
			out.write(buffer, 0, count);
			bytesWritten += count;
		}
	}
	
	private void write16BitStack(OutputStream out, Object[] stack)  throws IOException {
		for (int i=0; i<fi.nImages; i++) {
			write16BitImage(out, (short[])stack[i]);
		}
	}

	private void writeRGB48Image(OutputStream out, Object[] stack)  throws IOException {
		short[] r = (short[])stack[0];
		short[] g = (short[])stack[1];
		short[] b = (short[])stack[2];
		int size = fi.width*fi.height;
		int count = fi.width*6;
		byte[] buffer = new byte[count];
		for (int line=0; line<fi.height; line++) {
			int index2 = 0;
			int index1 = line*fi.width;
			int value;
			if (fi.intelByteOrder) {
				for (int i=0; i<fi.width; i++) {
					value = r[index1];
					buffer[index2++] = (byte)value;
					buffer[index2++] = (byte)(value>>>8);
					value = g[index1];
					buffer[index2++] = (byte)value;
					buffer[index2++] = (byte)(value>>>8);
					value = b[index1];
					buffer[index2++] = (byte)value;
					buffer[index2++] = (byte)(value>>>8);
					index1++;
				}
			} else {
				for (int i=0; i<fi.width; i++) {
					value = r[index1];
					buffer[index2++] = (byte)(value>>>8);
					buffer[index2++] = (byte)value;
					value = g[index1];
					buffer[index2++] = (byte)(value>>>8);
					buffer[index2++] = (byte)value;
					value = b[index1];
					buffer[index2++] = (byte)(value>>>8);
					buffer[index2++] = (byte)value;
					index1++;
				}
			}
			out.write(buffer, 0, count);
		}
	}

	private void writeFloatImage(OutputStream out, float[] pixels)  throws IOException {
		long bytesWritten = 0L;
		long size = 4L*fi.width*fi.height*samplesPerPixel;
		int count = getWritingChunkSize(size);
		byte[] buffer = new byte[count];
		int tmp;

		while (bytesWritten<size) {
			if ((bytesWritten + count)>size)
				count = (int)(size-bytesWritten);
			int j = (int)(bytesWritten/4L);
			if (fi.intelByteOrder)
				for (int i=0; i < count; i+=4) {
					tmp = Float.floatToRawIntBits(pixels[j]);
					buffer[i]   = (byte)tmp;
					buffer[i+1] = (byte)(tmp>>8);
					buffer[i+2] = (byte)(tmp>>16);
					buffer[i+3] = (byte)(tmp>>24);
					j++;
				}
			else
				for (int i=0; i < count; i+=4) {
					tmp = Float.floatToRawIntBits(pixels[j]);
					buffer[i]   = (byte)(tmp>>24);
					buffer[i+1] = (byte)(tmp>>16);
					buffer[i+2] = (byte)(tmp>>8);
					buffer[i+3] = (byte)tmp;
					j++;
				}
			out.write(buffer, 0, count);
			bytesWritten += count;
		}
	}
	
	private int getWritingChunkSize(long imageSize) {
		if (imageSize<4L)
			return (int)imageSize;
		int count = (int)(imageSize/50L);
		if (count<65536)
			count = 65536;
		if (count>imageSize)
			count = (int)imageSize;
		count = (count/4)*4;
		return count;	
	}
	
	private void writeFloatStack(OutputStream out, Object[] stack)  throws IOException {
		for (int i=0; i<fi.nImages; i++)
			writeFloatImage(out, (float[])stack[i]);
	}

	private void writeRGBImage(OutputStream out, int[] pixels)  throws IOException {
		long bytesWritten = 0L;
		long size = 3L*fi.width*fi.height;
		int count = fi.width*24;
		byte[] buffer = new byte[count];
		while (bytesWritten<size) {
			if ((bytesWritten+count)>size)
				count = (int)(size-bytesWritten);
			int j = (int)(bytesWritten/3L);
			for (int i=0; i<count; i+=3) {
				buffer[i]   = (byte)(pixels[j]>>16);	//red
				buffer[i+1] = (byte)(pixels[j]>>8);	//green
				buffer[i+2] = (byte)pixels[j];		//blue
				j++;
			}
			out.write(buffer, 0, count);
			bytesWritten += count;
		}
	}
	
	private void writeRGBStack(OutputStream out, Object[] stack)  throws IOException {
		for (int i=0; i<fi.nImages; i++)
			writeRGBImage(out, (int[])stack[i]);
	}

	int getMetaDataSize() {
		nSliceLabels = 0;
		nMetaDataEntries = 0;
		int size = 0;
		int nTypes = 0;
		if (fi.info!=null && fi.info.length()>0) {
			nMetaDataEntries = 1;
			size = fi.info.length()*2;
			nTypes++;
		}
		if (fi.sliceLabels!=null) {
			int max = Math.min(fi.sliceLabels.length, fi.nImages);
			boolean isNonNullLabel = false;
			for (int i=0; i<max; i++) {
				if (fi.sliceLabels[i]!=null && fi.sliceLabels[i].length()>0) {
					isNonNullLabel = true;
					break;
				}
			}
			if (isNonNullLabel) {
				for (int i=0; i<max; i++) {
					nSliceLabels++;
					if (fi.sliceLabels[i]!=null)
						size += fi.sliceLabels[i].length()*2;
				}
				if (nSliceLabels>0) nTypes++;
				nMetaDataEntries += nSliceLabels;
			}
		}

		if (fi.displayRanges!=null) {
			nMetaDataEntries++;
			size += fi.displayRanges.length*8;
			nTypes++;
		}

		if (fi.channelLuts!=null) {
			for (int i=0; i<fi.channelLuts.length; i++) {
                if (fi.channelLuts[i]!=null)
                    size += fi.channelLuts[i].length;
            }
			nTypes++;
			nMetaDataEntries += fi.channelLuts.length;
		}

		if (fi.plot!=null) {
			nMetaDataEntries++;
			size += fi.plot.length;
			nTypes++;
		}

		if (fi.roi!=null) {
			nMetaDataEntries++;
			size += fi.roi.length;
			nTypes++;
		}

		if (fi.overlay!=null) {
			for (int i=0; i<fi.overlay.length; i++) {
				if (fi.overlay[i]!=null)
					size += fi.overlay[i].length;
			}
			nTypes++;
			nMetaDataEntries += fi.overlay.length;
		}

		if (fi.properties!=null) {
			for (int i=0; i<fi.properties.length; i++)
				size += fi.properties[i].length()*2;
			nTypes++;
			nMetaDataEntries += fi.properties.length;
		}

		if (fi.metaDataTypes!=null && fi.metaData!=null && fi.metaData[0]!=null
		&& fi.metaDataTypes.length==fi.metaData.length) {
			extraMetaDataEntries = fi.metaData.length;
			nTypes += extraMetaDataEntries;
			nMetaDataEntries += extraMetaDataEntries;
			for (int i=0; i<extraMetaDataEntries; i++) {
                if (fi.metaData[i]!=null)
                    size += fi.metaData[i].length;
            }
		}
		if (nMetaDataEntries>0) nMetaDataEntries++; // add entry for header
		int hdrSize = 4 + nTypes*8;
		if (size>0) size += hdrSize;
		nMetaDataTypes = nTypes;
		return size;
	}
	
	/** Writes the 8-byte image file header. */
	void writeHeader(OutputStream out) throws IOException {
		byte[] hdr = new byte[8];
		if (littleEndian) {
			hdr[0] = 73; // "II" (Intel byte order)
			hdr[1] = 73;
			hdr[2] = 42;  // 42 (magic number)
			hdr[3] = 0;
			hdr[4] = 8;  // 8 (offset to first IFD)
			hdr[5] = 0;
			hdr[6] = 0;
			hdr[7] = 0;
		} else {
			hdr[0] = 77; // "MM" (Motorola byte order)
			hdr[1] = 77;
			hdr[2] = 0;  // 42 (magic number)
			hdr[3] = 42;
			hdr[4] = 0;  // 8 (offset to first IFD)
			hdr[5] = 0;
			hdr[6] = 0;
			hdr[7] = 8;
		}
		out.write(hdr);
	}
	
	/** Writes one 12-byte IFD entry. */
	void writeEntry(OutputStream out, int tag, int fieldType, int count, int value) throws IOException {
		writeShort(out, tag);
		writeShort(out, fieldType);
		writeInt(out, count);
		if (count==1 && fieldType==TiffDecoder.SHORT) {
			writeShort(out, value);
			writeShort(out, 0);
		} else
			writeInt(out, value); // may be an offset
	}
	
	/** Writes one IFD (Image File Directory). */
	void writeIFD(OutputStream out, int imageOffset, int nextIFD) throws IOException {	
		int tagDataOffset = HDR_SIZE + ifdSize;
		writeShort(out, nEntries);
		writeEntry(out, TiffDecoder.NEW_SUBFILE_TYPE, 4, 1, 0);
		writeEntry(out, TiffDecoder.IMAGE_WIDTH, 4, 1, fi.width);
		writeEntry(out, TiffDecoder.IMAGE_LENGTH, 4, 1, fi.height);
		if (fi.fileType==ImageInfo.RGB||fi.fileType==ImageInfo.RGB48) {
			writeEntry(out, TiffDecoder.BITS_PER_SAMPLE,  3, 3, tagDataOffset);
			tagDataOffset += BPS_DATA_SIZE;
		} else
			writeEntry(out, TiffDecoder.BITS_PER_SAMPLE,  3, 1, bitsPerSample);
		writeEntry(out, TiffDecoder.COMPRESSION,  3, 1, 1);	//No Compression
		writeEntry(out, TiffDecoder.PHOTO_INTERP, 3, 1, photoInterp);
		if (description!=null) {
			writeEntry(out, TiffDecoder.IMAGE_DESCRIPTION, 2, description.length, tagDataOffset);
			tagDataOffset += description.length;
		}
		writeEntry(out, TiffDecoder.STRIP_OFFSETS,    4, 1, imageOffset);
		writeEntry(out, TiffDecoder.SAMPLES_PER_PIXEL,3, 1, samplesPerPixel);
		writeEntry(out, TiffDecoder.ROWS_PER_STRIP,   4, 1, fi.height);
		writeEntry(out, TiffDecoder.STRIP_BYTE_COUNT, 4, 1, imageSize);
		if (fi.unit!=null && fi.pixelWidth!=0 && fi.pixelHeight!=0) {
			writeEntry(out, TiffDecoder.X_RESOLUTION, 5, 1, tagDataOffset);
			writeEntry(out, TiffDecoder.Y_RESOLUTION, 5, 1, tagDataOffset+8);
			tagDataOffset += SCALE_DATA_SIZE;
			int unit = 1;
			if (fi.unit.equals("inch"))
				unit = 2;
			else if (fi.unit.equals("cm"))
				unit = 3;
			writeEntry(out, TiffDecoder.RESOLUTION_UNIT, 3, 1, unit);
		}
		if (fi.fileType==fi.GRAY32_FLOAT) {
			int format = TiffDecoder.FLOATING_POINT;
			writeEntry(out, TiffDecoder.SAMPLE_FORMAT, 3, 1, format);
		}
		if (colorMapSize>0) {
			writeEntry(out, TiffDecoder.COLOR_MAP, 3, MAP_SIZE, tagDataOffset);
			tagDataOffset += MAP_SIZE*2;
		}
		if (metaDataSize>0) {
			writeEntry(out, TiffDecoder.META_DATA_BYTE_COUNTS, 4, nMetaDataEntries, tagDataOffset);
			writeEntry(out, TiffDecoder.META_DATA, 1, metaDataSize, tagDataOffset+4*nMetaDataEntries);
			tagDataOffset += nMetaDataEntries*4 + metaDataSize;
		}
		writeInt(out, nextIFD);
	}
	
	/** Writes the 6 bytes of data required by RGB BitsPerSample tag. */
	void writeBitsPerPixel(OutputStream out) throws IOException {
		int bitsPerPixel = fi.fileType==ImageInfo.RGB48?16:8;
		writeShort(out, bitsPerPixel);
		writeShort(out, bitsPerPixel);
		writeShort(out, bitsPerPixel);
	}

	/** Writes the 16 bytes of data required by the XResolution and YResolution tags. */
	void writeScale(OutputStream out) throws IOException {
		double xscale = 1.0/fi.pixelWidth;
		double yscale = 1.0/fi.pixelHeight;
		double scale = 1000000.0;
		if (xscale*scale>Integer.MAX_VALUE||yscale*scale>Integer.MAX_VALUE)
			scale = (int)(Integer.MAX_VALUE/Math.max(xscale,yscale));
		writeInt(out, (int)(xscale*scale));
		writeInt(out, (int)scale);
		writeInt(out, (int)(yscale*scale));
		writeInt(out, (int)scale);
	}

	/** Writes the variable length ImageDescription string. */
	void writeDescription(OutputStream out) throws IOException {
		out.write(description,0,description.length);
	}

	/** Writes color palette following the image. */
	void writeColorMap(OutputStream out) throws IOException {
		byte[] colorTable16 = new byte[MAP_SIZE*2];
		int j=littleEndian?1:0;
		for (int i=0; i<fi.lutSize; i++) {
			colorTable16[j] = fi.reds[i];
			colorTable16[512+j] = fi.greens[i];
			colorTable16[1024+j] = fi.blues[i];
			j += 2;
		}
		out.write(colorTable16);
	}
	
	/** Writes image metadata ("info" property, 
		stack slice labels, channel display ranges, luts, ROIs,
		overlays, properties and extra metadata). */
	void writeMetaData(OutputStream out) throws IOException {
	
		// write byte counts (META_DATA_BYTE_COUNTS tag)
		writeInt(out, 4+nMetaDataTypes*8); // header size	
		if (fi.info!=null && fi.info.length()>0)
			writeInt(out, fi.info.length()*2);
		for (int i=0; i<nSliceLabels; i++) {
			if (fi.sliceLabels[i]==null)
				writeInt(out, 0);
			else
				writeInt(out, fi.sliceLabels[i].length()*2);
		}
		if (fi.displayRanges!=null)
			writeInt(out, fi.displayRanges.length*8);
		if (fi.channelLuts!=null) {
			for (int i=0; i<fi.channelLuts.length; i++)
				writeInt(out, fi.channelLuts[i].length);
		}
		if (fi.plot!=null)
			writeInt(out, fi.plot.length);
		if (fi.roi!=null)
			writeInt(out, fi.roi.length);
		if (fi.overlay!=null) {
			for (int i=0; i<fi.overlay.length; i++)
				writeInt(out, fi.overlay[i].length);
		}
		if (fi.properties!=null) {
			for (int i=0; i<fi.properties.length; i++)
				writeInt(out, fi.properties[i].length()*2);
		}
		for (int i=0; i<extraMetaDataEntries; i++)
			writeInt(out, fi.metaData[i].length);	
		
		// write header (META_DATA tag header)
		writeInt(out, TiffDecoder.MAGIC_NUMBER); // "IJIJ"
		if (fi.info!=null) {
			writeInt(out, TiffDecoder.INFO); // type="info"
			writeInt(out, 1); // count
		}
		if (nSliceLabels>0) {
			writeInt(out, TiffDecoder.LABELS); // type="labl"
			writeInt(out, nSliceLabels); // count
		}
		if (fi.displayRanges!=null) {
			writeInt(out, TiffDecoder.RANGES); // type="rang"
			writeInt(out, 1); // count
		}
		if (fi.channelLuts!=null) {
			writeInt(out, TiffDecoder.LUTS); // type="luts"
			writeInt(out, fi.channelLuts.length); // count
		}
		if (fi.plot!=null) {
			writeInt(out, TiffDecoder.PLOT); // type="plot"
			writeInt(out, 1); // count
		}
		if (fi.roi!=null) {
			writeInt(out, TiffDecoder.ROI); // type="roi "
			writeInt(out, 1); // count
		}
		if (fi.overlay!=null) {
			writeInt(out, TiffDecoder.OVERLAY); // type="over"
			writeInt(out, fi.overlay.length); // count
		}
		if (fi.properties!=null) {
			writeInt(out, TiffDecoder.PROPERTIES); // type="prop"
			writeInt(out, fi.properties.length); // count
		}
		for (int i=0; i<extraMetaDataEntries; i++) {
			writeInt(out, fi.metaDataTypes[i]);
			writeInt(out, 1); // count
		}
		
		// write data (META_DATA tag body)
		if (fi.info!=null)
			writeChars(out, fi.info);
		for (int i=0; i<nSliceLabels; i++) {
			if (fi.sliceLabels[i]!=null)
				writeChars(out, fi.sliceLabels[i]);
		}
		if (fi.displayRanges!=null) {
			for (int i=0; i<fi.displayRanges.length; i++)
				writeDouble(out, fi.displayRanges[i]);
		}
		if (fi.channelLuts!=null) {
			for (int i=0; i<fi.channelLuts.length; i++)
				out.write(fi.channelLuts[i]);
		}
		if (fi.plot!=null)
			out.write(fi.plot);
		if (fi.roi!=null)
			out.write(fi.roi);
		if (fi.overlay!=null) {
			for (int i=0; i<fi.overlay.length; i++)
				out.write(fi.overlay[i]);
		}
		if (fi.properties!=null) {
			for (int i=0; i<fi.properties.length; i++)
				writeChars(out, fi.properties[i]);
		}
		for (int i=0; i<extraMetaDataEntries; i++)
			out.write(fi.metaData[i]); 					
	}

	/** Creates an optional image description string for saving calibration data.
		For stacks, also saves the stack size so ImageJ can open the stack without
		decoding an IFD for each slice.*/
	void makeDescriptionString() {
		if (fi.description!=null) {
			if (fi.description.charAt(fi.description.length()-1)!=(char)0)
				fi.description += " ";
			description = fi.description.getBytes();
			description[description.length-1] = (byte)0;
		} else
			description = null;
	}
		
	final void writeShort(OutputStream out, int v) throws IOException {
		if (littleEndian) {
       		out.write(v&255);
        	out.write((v>>>8)&255);
 		} else {
        	out.write((v>>>8)&255);
        	out.write(v&255);
        }
	}

	final void writeInt(OutputStream out, int v) throws IOException {
		if (littleEndian) {
        	out.write(v&255);
        	out.write((v>>>8)&255);
        	out.write((v>>>16)&255);
         	out.write((v>>>24)&255);
		} else {
        	out.write((v>>>24)&255);
        	out.write((v>>>16)&255);
        	out.write((v>>>8)&255);
        	out.write(v&255);
        }
	}

    final void writeLong(OutputStream out, long v) throws IOException {
    	if (littleEndian) {
			buffer[7] = (byte)(v>>>56);
			buffer[6] = (byte)(v>>>48);
			buffer[5] = (byte)(v>>>40);
			buffer[4] = (byte)(v>>>32);
			buffer[3] = (byte)(v>>>24);
			buffer[2] = (byte)(v>>>16);
			buffer[1] = (byte)(v>>> 8);
			buffer[0] = (byte)v;
			out.write(buffer, 0, 8);
        } else {
			buffer[0] = (byte)(v>>>56);
			buffer[1] = (byte)(v>>>48);
			buffer[2] = (byte)(v>>>40);
			buffer[3] = (byte)(v>>>32);
			buffer[4] = (byte)(v>>>24);
			buffer[5] = (byte)(v>>>16);
			buffer[6] = (byte)(v>>> 8);
			buffer[7] = (byte)v;
			out.write(buffer, 0, 8);
        }
     }

    final void writeDouble(OutputStream out, double v) throws IOException {
		writeLong(out, Double.doubleToLongBits(v));
    }
    
	final void writeChars(OutputStream out, String s) throws IOException {
        int len = s.length();
        if (littleEndian) {
			for (int i = 0 ; i < len ; i++) {
				int v = s.charAt(i);
				out.write(v&255); 
				out.write((v>>>8)&255); 
			}
        } else {
			for (int i = 0 ; i < len ; i++) {
				int v = s.charAt(i);
				out.write((v>>>8)&255); 
				out.write(v&255); 
			}
        }
    }
    
}

