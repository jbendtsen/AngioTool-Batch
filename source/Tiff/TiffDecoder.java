package Tiff;

import java.io.*;
import java.nio.*;
import java.util.*;

// Modified version of ij.io.TiffDecoder

/**
Decodes single and multi-image TIFF files. The LZW decompression
code was contributed by Curtis Rueden.
*/
public class TiffDecoder {

	// tags
	public static final int NEW_SUBFILE_TYPE = 254;
	public static final int IMAGE_WIDTH = 256;
	public static final int IMAGE_LENGTH = 257;
	public static final int BITS_PER_SAMPLE = 258;
	public static final int COMPRESSION = 259;
	public static final int PHOTO_INTERP = 262;
	public static final int IMAGE_DESCRIPTION = 270;
	public static final int STRIP_OFFSETS = 273;
	public static final int ORIENTATION = 274;
	public static final int SAMPLES_PER_PIXEL = 277;
	public static final int ROWS_PER_STRIP = 278;
	public static final int STRIP_BYTE_COUNT = 279;
	public static final int X_RESOLUTION = 282;
	public static final int Y_RESOLUTION = 283;
	public static final int PLANAR_CONFIGURATION = 284;
	public static final int RESOLUTION_UNIT = 296;
	public static final int SOFTWARE = 305;
	public static final int DATE_TIME = 306;
	public static final int ARTIST = 315;
	public static final int HOST_COMPUTER = 316;
	public static final int PREDICTOR = 317;
	public static final int COLOR_MAP = 320;
	public static final int TILE_WIDTH = 322;
	public static final int SAMPLE_FORMAT = 339;
	public static final int JPEG_TABLES = 347;
	public static final int METAMORPH1 = 33628;
	public static final int METAMORPH2 = 33629;
	public static final int IPLAB = 34122;
	public static final int NIH_IMAGE_HDR = 43314;
	public static final int META_DATA_BYTE_COUNTS = 50838; // private tag registered with Adobe
	public static final int META_DATA = 50839; // private tag registered with Adobe
	
	//constants
	static final int UNSIGNED = 1;
	static final int SIGNED = 2;
	static final int FLOATING_POINT = 3;

	//field types
	static final int SHORT = 3;
	static final int LONG = 4;

	// metadata types
	static final int MAGIC_NUMBER = 0x494a494a;  // "IJIJ"
	static final int INFO = 0x696e666f;  // "info" (Info image property)
	static final int LABELS = 0x6c61626c;  // "labl" (slice labels)
	static final int RANGES = 0x72616e67;  // "rang" (display ranges)
	static final int LUTS = 0x6c757473;    // "luts" (channel LUTs)
	static final int PLOT = 0x706c6f74;    // "plot" (serialized plot)
	static final int ROI = 0x726f6920;     // "roi " (ROI)
	static final int OVERLAY = 0x6f766572; // "over" (overlay)
	static final int PROPERTIES = 0x70726f70; // "prop" (properties)
	
	private String directory;
	private String name;
	private String url;
	protected ByteBuffer in;
	protected boolean debugMode;
	private boolean littleEndian;
	private String dInfo;
	private int ifdCount;
	private int[] metaDataCounts;
	private int photoInterp;
		
	public TiffDecoder(File file, ByteBuffer buffer) {
		directory = file.getParent();
		name = file.getName();
		in = buffer;
		url = "";
	}

	final int getInt() throws IOException {
		int b1 = in.get();
		int b2 = in.get();
		int b3 = in.get();
		int b4 = in.get();
		if (littleEndian)
			return ((b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0));
		else
			return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
	}
	
	final long getUnsignedInt() throws IOException {
		return (long)getInt()&0xffffffffL;
	}

	final int getShort() throws IOException {
		int b1 = in.get();
		int b2 = in.get();
		if (littleEndian)
			return ((b2<<8) + b1);
		else
			return ((b1<<8) + b2);
	}

    final long readLong() throws IOException {
    	if (littleEndian)
        	return ((long)getInt()&0xffffffffL) + ((long)getInt()<<32);
        else
			return ((long)getInt()<<32) + ((long)getInt()&0xffffffffL);
        	//return in.get()+(in.get()<<8)+(in.get()<<16)+(in.get()<<24)+(in.get()<<32)+(in.get()<<40)+(in.get()<<48)+(in.get()<<56);
    }

    final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

	int OpenImageFileHeader() throws IOException {
	// Open 8-byte Image File Header at start of file.
	// Returns the offset in bytes to the first IFD or -1
	// if this is not a valid tiff file.
		int byteOrder = in.getShort();
		if (byteOrder==0x4949) // "II"
			littleEndian = true;
		else if (byteOrder==0x4d4d) // "MM"
			littleEndian = false;
		else {
			//in.close();
			return -1;
		}
		int magicNumber = getShort(); // 42
		return getInt();
	}
		
	int getValue(int fieldType, int count) throws IOException {
		int value = 0;
		int unused;
		if (fieldType==SHORT && count==1) {
			value = getShort();
			unused = getShort();
		} else
			value = getInt();
		return value;
	}	
	
	void getColorMap(int offset, ImageInfo fi) throws IOException {
		byte[] colorTable16 = new byte[768*2];
		int saveLoc = in.position();
		in.position(offset);
		in.get(colorTable16);
		in.position(saveLoc);
		fi.lutSize = 256;
		fi.reds = new byte[256];
		fi.greens = new byte[256];
		fi.blues = new byte[256];
		int j = 0;
		if (littleEndian) j++;
		int sum = 0;
		for (int i=0; i<256; i++) {
			fi.reds[i] = colorTable16[j];
			sum += fi.reds[i];
			fi.greens[i] = colorTable16[512+j];
			sum += fi.greens[i];
			fi.blues[i] = colorTable16[1024+j];
			sum += fi.blues[i];
			j += 2;
		}
		if (sum!=0 && fi.fileType==ImageInfo.GRAY8)
			fi.fileType = ImageInfo.COLOR8;
	}
	
	byte[] getString(int count, int offset) throws IOException {
		count--; // skip null byte at end of string
		if (count<=3)
			return null;
		byte[] bytes = new byte[count];
		int saveLoc = in.position();
		in.position(offset);
		in.get(bytes);
		in.position(saveLoc);
		return bytes;
	}

	/** Save the image description in the specified ImageInfo. ImageJ
		saves spatial and density calibration data in this string. For
		stacks, it also saves the number of images to avoid having to
		decode an IFD for each image. */
	public void saveImageDescription(byte[] description, ImageInfo fi) {
        String id = new String(description);
        boolean createdByImageJ = id.startsWith("ImageJ");
        if (!createdByImageJ)
			saveMetadata(getName(IMAGE_DESCRIPTION), id);
		if (id.length()<7) return;
		fi.description = id;
        int index1 = id.indexOf("images=");
        if (index1>0 && createdByImageJ && id.charAt(7)!='\n') {
            int index2 = id.indexOf("\n", index1);
            if (index2>0) {
                String images = id.substring(index1+7,index2);
                int n = (int)BatchUtils.parseDouble(images, 0.0);
                if (n>1 && fi.compression==ImageInfo.COMPRESSION_NONE)
                	fi.nImages = n;
            }
        }
	}

	public void saveMetadata(String name, String data) {
		if (data==null) return;
        String str = name+": "+data+"\n";
        if (tiffMetadata==null)
        	tiffMetadata = str;
        else
        	tiffMetadata += str;
	}

	void decodeNIHImageHeader(int offset, ImageInfo fi) throws IOException {
		int saveLoc = in.position();
		
		in.position(offset+12);
		int version = in.getShort();
		
		in.position(offset+160);
		double scale = in.getDouble();
		if (version>106 && scale!=0.0) {
			fi.pixelWidth = 1.0/scale;
			fi.pixelHeight = fi.pixelWidth;
		} 

		// spatial calibration
		in.position(offset+172);
		int units = in.getShort();
		if (version<=153) units += 5;
		switch (units) {
			case 5: fi.unit = "nanometer"; break;
			case 6: fi.unit = "micrometer"; break;
			case 7: fi.unit = "mm"; break;
			case 8: fi.unit = "cm"; break;
			case 9: fi.unit = "meter"; break;
			case 10: fi.unit = "km"; break;
			case 11: fi.unit = "inch"; break;
			case 12: fi.unit = "ft"; break;
			case 13: fi.unit = "mi"; break;
		}

		// density calibration
		in.position(offset+182);
		int fitType = in.get();
		int unused = in.get();
		int nCoefficients = in.getShort();
		if (fitType==11) {
			fi.calibrationFunction = 21; //Calibration.UNCALIBRATED_OD
			fi.valueUnit = "U. OD";
		} else if (fitType>=0 && fitType<=8 && nCoefficients>=1 && nCoefficients<=5) {
			switch (fitType) {
				case 0: fi.calibrationFunction = 0; break; //Calibration.STRAIGHT_LINE
				case 1: fi.calibrationFunction = 1; break; //Calibration.POLY2
				case 2: fi.calibrationFunction = 2; break; //Calibration.POLY3
				case 3: fi.calibrationFunction = 3; break; //Calibration.POLY4
				case 5: fi.calibrationFunction = 4; break; //Calibration.EXPONENTIAL
				case 6: fi.calibrationFunction = 5; break; //Calibration.POWER
				case 7: fi.calibrationFunction = 6; break; //Calibration.LOG
				case 8: fi.calibrationFunction = 10; break; //Calibration.RODBARD2 (NIH Image)
			}
			fi.coefficients = new double[nCoefficients];
			for (int i=0; i<nCoefficients; i++) {
				fi.coefficients[i] = in.getDouble();
			}
			in.position(offset+234);
			int size = in.get();
			StringBuffer sb = new StringBuffer();
			if (size>=1 && size<=16) {
				for (int i=0; i<size; i++)
					sb.append((char)(in.get()));
				fi.valueUnit = new String(sb);
			} else
				fi.valueUnit = " ";
		}
			
		in.position(offset+260);
		int nImages = in.getShort();
		if (nImages>=2 && (fi.fileType==ImageInfo.GRAY8||fi.fileType==ImageInfo.COLOR8)) {
			fi.nImages = nImages;
			fi.pixelDepth = in.getFloat();	//SliceSpacing
			int skip = in.getShort();		//CurrentSlice
			fi.frameInterval = in.getFloat();
		}
			
		in.position(offset+272);
		float aspectRatio = in.getFloat();
		if (version>140 && aspectRatio!=0.0)
			fi.pixelHeight = fi.pixelWidth/aspectRatio;
		
		in.position(saveLoc);
	}
	
	void dumpTag(int tag, int count, int value, ImageInfo fi) {
		long lvalue = ((long)value)&0xffffffffL;
		String name = getName(tag);
		String cs = (count==1)?"":", count=" + count;
		dInfo += "    " + tag + ", \"" + name + "\", value=" + lvalue + cs + "\n";
		//System.out.println(tag + ", \"" + name + "\", value=" + value + cs + "\n");
	}

	String getName(int tag) {
		String name;
		switch (tag) {
			case NEW_SUBFILE_TYPE: name="NewSubfileType"; break;
			case IMAGE_WIDTH: name="ImageWidth"; break;
			case IMAGE_LENGTH: name="ImageLength"; break;
			case STRIP_OFFSETS: name="StripOffsets"; break;
			case ORIENTATION: name="Orientation"; break;
			case PHOTO_INTERP: name="PhotoInterp"; break;
			case IMAGE_DESCRIPTION: name="ImageDescription"; break;
			case BITS_PER_SAMPLE: name="BitsPerSample"; break;
			case SAMPLES_PER_PIXEL: name="SamplesPerPixel"; break;
			case ROWS_PER_STRIP: name="RowsPerStrip"; break;
			case STRIP_BYTE_COUNT: name="StripByteCount"; break;
			case X_RESOLUTION: name="XResolution"; break;
			case Y_RESOLUTION: name="YResolution"; break;
			case RESOLUTION_UNIT: name="ResolutionUnit"; break;
			case SOFTWARE: name="Software"; break;
			case DATE_TIME: name="DateTime"; break;
			case ARTIST: name="Artist"; break;
			case HOST_COMPUTER: name="HostComputer"; break;
			case PLANAR_CONFIGURATION: name="PlanarConfiguration"; break;
			case COMPRESSION: name="Compression"; break; 
			case PREDICTOR: name="Predictor"; break; 
			case COLOR_MAP: name="ColorMap"; break; 
			case SAMPLE_FORMAT: name="SampleFormat"; break; 
			case JPEG_TABLES: name="JPEGTables"; break; 
			case NIH_IMAGE_HDR: name="NIHImageHeader"; break; 
			case META_DATA_BYTE_COUNTS: name="MetaDataByteCounts"; break; 
			case META_DATA: name="MetaData"; break; 
			default: name="???"; break;
		}
		return name;
	}

	double getRational(int loc) throws IOException {
		int saveLoc = in.position();
		in.position(loc);
		double numerator = getUnsignedInt();
		double denominator = getUnsignedInt();
		in.position(saveLoc);
		if (denominator!=0.0)
			return numerator/denominator;
		else
			return 0.0;
	}
	
	ImageInfo OpenIFD() throws IOException {
	// Get Image File Directory data
		int tag, fieldType, count, value;
		int nEntries = getShort();
		if (nEntries<1 || nEntries>1000)
			return null;
		ifdCount++;
		/*
		if ((ifdCount%50)==0 && ifdCount>0)
			System.out.println("Opening IFDs: "+ifdCount);
		*/
		ImageInfo fi = new ImageInfo();
		fi.fileType = ImageInfo.BITMAP;  //BitsPerSample defaults to 1
		for (int i=0; i<nEntries; i++) {
			tag = getShort();
			fieldType = getShort();
			count = getInt();
			value = getValue(fieldType, count);
			int lvalue = value;
			if (debugMode && ifdCount<10) dumpTag(tag, count, value, fi);
			switch (tag) {
				case IMAGE_WIDTH: 
					fi.width = value;
					fi.intelByteOrder = littleEndian;
					break;
				case IMAGE_LENGTH: 
					fi.height = value;
					break;
 				case STRIP_OFFSETS:
					if (count==1)
						fi.stripOffsets = new int[] {value};
					else {
						int saveLoc = in.position();
						in.position(lvalue);
						fi.stripOffsets = new int[count];
						for (int c=0; c<count; c++)
							fi.stripOffsets[c] = getInt();
						in.position(saveLoc);
					}
					fi.offset = count>0?fi.stripOffsets[0]:value;
					if (count>1 && (((long)fi.stripOffsets[count-1])&0xffffffffL)<(((long)fi.stripOffsets[0])&0xffffffffL))
						fi.offset = fi.stripOffsets[count-1];
					break;
				case STRIP_BYTE_COUNT:
					if (count==1)
						fi.stripLengths = new int[] {value};
					else {
						int saveLoc = in.position();
						in.position(lvalue);
						fi.stripLengths = new int[count];
						for (int c=0; c<count; c++) {
							if (fieldType==SHORT)
								fi.stripLengths[c] = getShort();
							else
								fi.stripLengths[c] = getInt();
						}
						in.position(saveLoc);
					}
					break;
 				case PHOTO_INTERP:
 					photoInterp = value;
 					fi.whiteIsZero = value==0;
					break;
				case BITS_PER_SAMPLE:
						if (count==1) {
							if (value==8)
								fi.fileType = ImageInfo.GRAY8;
							else if (value==16)
								fi.fileType = ImageInfo.GRAY16_UNSIGNED;
							else if (value==32)
								fi.fileType = ImageInfo.GRAY32_INT;
							else if (value==12)
								fi.fileType = ImageInfo.GRAY12_UNSIGNED;
							else if (value==10)
								fi.fileType = ImageInfo.GRAY10_UNSIGNED;
							else if (value==1)
								fi.fileType = ImageInfo.BITMAP;
							else
								error("Unsupported BitsPerSample: " + value);
						} else if (count>1) {
							int saveLoc = in.position();
							in.position(lvalue);
							int bitDepth = getShort();
							if (bitDepth==8)
								fi.fileType = ImageInfo.GRAY8;
							else if (bitDepth==16)
								fi.fileType = ImageInfo.GRAY16_UNSIGNED;
							else
								error("ImageJ cannot open interleaved "+bitDepth+"-bit images.");
							in.position(saveLoc);
						}
						break;
				case SAMPLES_PER_PIXEL:
					fi.samplesPerPixel = value;
					if (value==3 && fi.fileType==ImageInfo.GRAY8)
						fi.fileType = ImageInfo.RGB;
					else if (value==3 && fi.fileType==ImageInfo.GRAY16_UNSIGNED)
						fi.fileType = ImageInfo.RGB48;
					else if (value==4 && fi.fileType==ImageInfo.GRAY8)
						fi.fileType = photoInterp==5?ImageInfo.CMYK:ImageInfo.ARGB;
					else if (value==4 && fi.fileType==ImageInfo.GRAY16_UNSIGNED) {
						fi.fileType = ImageInfo.RGB48;
						if (photoInterp==5)  //assume cmyk
							fi.whiteIsZero = true;
					}
					break;
				case ROWS_PER_STRIP:
					fi.rowsPerStrip = value;
					break;
				case X_RESOLUTION:
					double xScale = getRational(lvalue); 
					if (xScale!=0.0) fi.pixelWidth = 1.0/xScale; 
					break;
				case Y_RESOLUTION:
					double yScale = getRational(lvalue); 
					if (yScale!=0.0) fi.pixelHeight = 1.0/yScale; 
					break;
				case RESOLUTION_UNIT:
					if (value==1&&fi.unit==null)
						fi.unit = " ";
					else if (value==2) {
						if (fi.pixelWidth==1.0/72.0) {
							fi.pixelWidth = 1.0;
							fi.pixelHeight = 1.0;
						} else
							fi.unit = "inch";
					} else if (value==3)
						fi.unit = "cm";
					break;
				case PLANAR_CONFIGURATION:  // 1=chunky, 2=planar
					if (value==2 && fi.fileType==ImageInfo.RGB48)
							 fi.fileType = ImageInfo.RGB48_PLANAR;
					else if (value==2 && fi.fileType==ImageInfo.RGB)
						fi.fileType = ImageInfo.RGB_PLANAR;
					else if (value!=2 && !(fi.samplesPerPixel==1||fi.samplesPerPixel==3||fi.samplesPerPixel==4)) {
						String msg = "Unsupported SamplesPerPixel: " + fi.samplesPerPixel;
						error(msg);
					}
					break;
				case COMPRESSION:
					if (value==5)  {// LZW compression
						fi.compression = ImageInfo.LZW;
						if (fi.fileType==ImageInfo.GRAY12_UNSIGNED||fi.fileType==ImageInfo.GRAY10_UNSIGNED)
							error("ImageJ cannot open 10-bit or 12-bit LZW-compressed TIFFs");
					} else if (value==32773)  // PackBits compression
						fi.compression = ImageInfo.PACK_BITS;
					else if (value==32946 || value==8) //8=Adobe deflate
						fi.compression = ImageInfo.ZIP;
					else if (value!=1 && value!=0 && !(value==7&&fi.width<500)) {
						// don't abort with Spot camera compressed (7) thumbnails
						// otherwise, this is an unknown compression type
						fi.compression = ImageInfo.COMPRESSION_UNKNOWN;
						error("ImageJ cannot open TIFF files " +
							"compressed in this fashion ("+value+")");
					}
					break;
				case PREDICTOR:
					if (value==2) {
						if (fi.compression==ImageInfo.LZW)
							fi.compression = ImageInfo.LZW_WITH_DIFFERENCING;
						else if (fi.compression==ImageInfo.ZIP)
							fi.compression = ImageInfo.ZIP_WITH_DIFFERENCING;
					} else if (value==3)
						System.out.println("TiffDecoder: unsupported predictor value of 3");
					break;
				case COLOR_MAP: 
					if (count==768)
						getColorMap(lvalue, fi);
					break;
				case TILE_WIDTH:
					error("ImageJ cannot open tiled TIFFs.\nTry using the Bio-Formats plugin.");
					break;
				case SAMPLE_FORMAT:
					if (fi.fileType==ImageInfo.GRAY32_INT && value==FLOATING_POINT)
						fi.fileType = ImageInfo.GRAY32_FLOAT;
					if (fi.fileType==ImageInfo.GRAY16_UNSIGNED) {
						if (value==SIGNED)
							fi.fileType = ImageInfo.GRAY16_SIGNED;
						if (value==FLOATING_POINT)
							error("ImageJ cannot open 16-bit float TIFFs");
					}
					break;
				case JPEG_TABLES:
					if (fi.compression==ImageInfo.JPEG)
						error("Cannot open JPEG-compressed TIFFs with separate tables");
					break;
				case IMAGE_DESCRIPTION: 
					if (ifdCount==1) {
						byte[] s = getString(count, lvalue);
						if (s!=null) saveImageDescription(s,fi);
					}
					break;
				case ORIENTATION:
					fi.nImages = 0; // file not created by ImageJ so look at all the IFDs
					break;
				case METAMORPH1: case METAMORPH2:
					if ((name.indexOf(".STK")>0||name.indexOf(".stk")>0) && fi.compression==ImageInfo.COMPRESSION_NONE) {
						if (tag==METAMORPH2)
							fi.nImages=count;
						else
							fi.nImages=9999;
					}
					break;
				case IPLAB: 
					fi.nImages=value;
					break;
				case NIH_IMAGE_HDR: 
					if (count==256)
						decodeNIHImageHeader(value, fi);
					break;
 				case META_DATA_BYTE_COUNTS: 
					int saveLoc = in.position();
					in.position(lvalue);
					metaDataCounts = new int[count];
					for (int c=0; c<count; c++)
						metaDataCounts[c] = getInt();
					in.position(saveLoc);
					break;
 				case META_DATA: 
 					getMetaData(value, fi);
 					break;
				default:
					if (tag>10000 && tag<32768 && ifdCount>1)
						return null;
			}
		}
		fi.fileFormat = fi.TIFF;
		fi.fileName = name;
		fi.directory = directory;
		return fi;
	}

	void getMetaData(int loc, ImageInfo fi) throws IOException {
		if (metaDataCounts==null || metaDataCounts.length==0)
			return;
		int maxTypes = 10;
		int saveLoc = in.position();
		in.position(loc);
		int n = metaDataCounts.length;
		int hdrSize = metaDataCounts[0];
		if (hdrSize<12 || hdrSize>804) {
			in.position(saveLoc);
			return;
		}
		int magicNumber = getInt();
		if (magicNumber!=MAGIC_NUMBER)  { // "IJIJ"
			in.position(saveLoc);
			return;
		}
		int nTypes = (hdrSize-4)/8;
		int[] types = new int[nTypes];
		int[] counts = new int[nTypes];		
		if (debugMode) {
			dInfo += "Metadata:\n";
			dInfo += "   Entries: "+(metaDataCounts.length-1)+"\n";
			dInfo += "   Types: "+nTypes+"\n";
		}
		int extraMetaDataEntries = 0;
		int index = 1;
		for (int i=0; i<nTypes; i++) {
			types[i] = getInt();
			counts[i] = getInt();
			if (types[i]<0xffffff)
				extraMetaDataEntries += counts[i];
			if (debugMode) {
				String id = "unknown";
				if (types[i]==INFO) id = "Info property";
				if (types[i]==LABELS) id = "slice labels";
				if (types[i]==RANGES) id = "display ranges";
				if (types[i]==LUTS) id = "luts";
				if (types[i]==PLOT) id = "plot";
				if (types[i]==ROI) id = "roi";
				if (types[i]==OVERLAY) id = "overlay";
				if (types[i]==PROPERTIES) id = "properties";
				int len = metaDataCounts[index];
				int count = counts[i];
				index += count;
				if (index>=metaDataCounts.length) index=1;
				String lenstr = count==1?", length=":", length[0]=";
				dInfo += "   "+i+", type="+id+", count="+count+lenstr+len+"\n";
			}
		}
		fi.metaDataTypes = new int[extraMetaDataEntries];
		fi.metaData = new byte[extraMetaDataEntries][];
		int start = 1;
		int eMDindex = 0;
		for (int i=0; i<nTypes; i++) {
			if (types[i]==INFO)
				getInfoProperty(start, fi);
			else if (types[i]==LABELS)
				getSliceLabels(start, start+counts[i]-1, fi);
			else if (types[i]==RANGES)
				getDisplayRanges(start, fi);
			else if (types[i]==LUTS)
				getLuts(start, start+counts[i]-1, fi);
			else if (types[i]==PLOT)
				getPlot(start, fi);
			else if (types[i]==ROI)
				getRoi(start, fi);
			else if (types[i]==OVERLAY)
				getOverlay(start, start+counts[i]-1, fi);
			else if (types[i]==PROPERTIES)
				getProperties(start, start+counts[i]-1, fi);
			else if (types[i]<0xffffff) {
				for (int j=start; j<start+counts[i]; j++) { 
					int len = metaDataCounts[j]; 
					fi.metaData[eMDindex] = new byte[len]; 
					in.get(fi.metaData[eMDindex], 0, len); 
					fi.metaDataTypes[eMDindex] = types[i]; 
					eMDindex++; 
				} 
			} else
				skipUnknownType(start, start+counts[i]-1);
			start += counts[i];
		}
		in.position(saveLoc);
	}

	void getInfoProperty(int first, ImageInfo fi) throws IOException {
		int len = metaDataCounts[first];
	    byte[] buffer = new byte[len];
		in.get(buffer, 0, len);
		len /= 2;
		char[] chars = new char[len];
		if (littleEndian) {
			for (int j=0, k=0; j<len; j++)
				chars[j] = (char)(buffer[k++]&255 + ((buffer[k++]&255)<<8));
		} else {
			for (int j=0, k=0; j<len; j++)
				chars[j] = (char)(((buffer[k++]&255)<<8) + (buffer[k++]&255));
		}
		fi.info = new String(chars);
	}

	void getSliceLabels(int first, int last, ImageInfo fi) throws IOException {
		fi.sliceLabels = new String[last-first+1];
	    int index = 0;
	    byte[] buffer = new byte[metaDataCounts[first]];
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			if (len>0) {
				if (len>buffer.length)
					buffer = new byte[len];
				in.get(buffer, 0, len);
				len /= 2;
				char[] chars = new char[len];
				if (littleEndian) {
					for (int j=0, k=0; j<len; j++)
						chars[j] = (char)(buffer[k++]&255 + ((buffer[k++]&255)<<8));
				} else {
					for (int j=0, k=0; j<len; j++)
						chars[j] = (char)(((buffer[k++]&255)<<8) + (buffer[k++]&255));
				}
				fi.sliceLabels[index++] = new String(chars);
				//System.out.println(i+"  "+fi.sliceLabels[i-1]+"  "+len);
			} else
				fi.sliceLabels[index++] = null;
		}
	}

	void getDisplayRanges(int first, ImageInfo fi) throws IOException {
		int n = metaDataCounts[first]/8;
		fi.displayRanges = new double[n];
		for (int i=0; i<n; i++)
			fi.displayRanges[i] = readDouble();
	}

	void getLuts(int first, int last, ImageInfo fi) throws IOException {
		fi.channelLuts = new byte[last-first+1][];
	    int index = 0;
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			fi.channelLuts[index] = new byte[len];
            in.get(fi.channelLuts[index], 0, len);
            index++;
		}
	}

	void getRoi(int first, ImageInfo fi) throws IOException {
		int len = metaDataCounts[first];
		fi.roi = new byte[len]; 
		in.get(fi.roi, 0, len); 
	}

	void getPlot(int first, ImageInfo fi) throws IOException {
		int len = metaDataCounts[first];
		fi.plot = new byte[len];
		in.get(fi.plot, 0, len);
	}

	void getOverlay(int first, int last, ImageInfo fi) throws IOException {
		fi.overlay = new byte[last-first+1][];
	    int index = 0;
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			fi.overlay[index] = new byte[len];
            in.get(fi.overlay[index], 0, len);
            index++;
		}
	}

	void getProperties(int first, int last, ImageInfo fi) throws IOException {
		fi.properties = new String[last-first+1];
	    int index = 0;
	    byte[] buffer = new byte[metaDataCounts[first]];
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			if (len>buffer.length)
				buffer = new byte[len];
			in.get(buffer, 0, len);
			len /= 2;
			char[] chars = new char[len];
			if (littleEndian) {
				for (int j=0, k=0; j<len; j++)
					chars[j] = (char)(buffer[k++]&255 + ((buffer[k++]&255)<<8));
			} else {
				for (int j=0, k=0; j<len; j++)
					chars[j] = (char)(((buffer[k++]&255)<<8) + (buffer[k++]&255));
			}
			fi.properties[index++] = new String(chars);
		}
	}

	void error(String message) throws IOException {
		//if (in!=null) in.close();
		throw new IOException(message);
	}
	
	void skipUnknownType(int first, int last) throws IOException {
	    byte[] buffer = new byte[metaDataCounts[first]];
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
            if (len>buffer.length)
                buffer = new byte[len];
            in.get(buffer, 0, len);
		}
	}

	public void enableDebugging() {
		debugMode = true;
	}
		
	public ArrayList<ImageInfo> getTiffImages() throws IOException {
        int ifdOffset = OpenImageFileHeader();
		if (ifdOffset<0L) {
			//in.close();
			return null;
		}

		ArrayList<ImageInfo> images = new ArrayList<>();
		while (ifdOffset>0L) {
			in.position(ifdOffset);
			ImageInfo fi = OpenIFD();
			if (fi!=null) {
				images.add(fi);
				ifdOffset = getInt();
			} else
				ifdOffset = 0;
			if (debugMode && ifdCount<10) dInfo += "nextIFD=" + ifdOffset + "\n";
			if (fi!=null && fi.nImages>1)
				ifdOffset = 0;   // ignore extra IFDs in ImageJ and NIH Image stacks
		}

		return images;
	}
	
	String getGapInfo(ImageInfo[] fi) {
		if (fi.length<2) return "0";
		long minGap = Long.MAX_VALUE;
		long maxGap = -Long.MAX_VALUE;
		for (int i=1; i<fi.length; i++) {
			long gap = fi[i].getOffset()-fi[i-1].getOffset();
			if (gap<minGap) minGap = gap;
			if (gap>maxGap) maxGap = gap;
		}
		long imageSize = fi[0].width*fi[0].height*fi[0].getBytesPerPixel();
		minGap -= imageSize;
		maxGap -= imageSize;
		if (minGap==maxGap)
			return ""+minGap;
		else 
			return "varies ("+minGap+" to "+maxGap+")";
	}

}

