package Xlsx;

import Utils.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class XlsxReader {
    public static class SheetCells {
        public Object[] cells;
        public RefVector<Object[]> valueRows;
        public int rows;
        public int cols;
        public int flags = 0;

        public SheetCells(Object[] cells, int rows, int cols) {
            this.cells = cells;
            this.valueRows = null;
            this.rows = rows;
            this.cols = cols;
        }

        public SheetCells(RefVector<Object[]> valueRows) {
            this.cells = null;
            this.valueRows = valueRows;
            this.rows = valueRows.size;
            int c = 0;
            for (int i = 0; i < rows; i++) {
                if (valueRows.size > c)
                    c = valueRows.size;
            }
            this.cols = c;
        }
    }

    public static ArrayList<SheetCells> loadXlsxFromFile(String fname) throws IOException {
        ZipFile zipFile = new ZipFile(fname);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        HashMap<String, byte[]> xmlFiles = new HashMap<>();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = getBaseNameIfEntryIsUseful(entry);
            if (name == null)
                continue;

            long size = entry.getSize();
            xmlFiles.put(name, readSomeBytes(zipFile.getInputStream(entry), size));
        }

        try { zipFile.close(); }
        catch (IOException ignored) {}
        return readSheetsFromXmlFiles(xmlFiles);
    }

    public static ArrayList<SheetCells> loadXlsxFromBuffer(byte[] buf, int off, int len) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(buf, off, len));

        HashMap<String, byte[]> xmlFiles = new HashMap<>();
        while (true) {
            ZipEntry entry = null;
            try { entry = zipStream.getNextEntry(); }
            catch (IOException ignored) {}
            if (entry == null)
                break;

            String name = getBaseNameIfEntryIsUseful(entry);
            if (name == null)
                continue;

            xmlFiles.put(name, readSomeBytes(zipStream, entry.getSize()));
        }

        try { zipStream.close(); }
        catch (IOException ignored) {}
        return readSheetsFromXmlFiles(xmlFiles);
    }

    static ArrayList<SheetCells> readSheetsFromXmlFiles(HashMap<String, byte[]> xmlFiles) {
        XmlParser.Node appXml = XmlParser.parseFromBuffer(xmlFiles.get("app.xml"));

        byte[] sharedStringsBuffer = xmlFiles.get("sharedStrings.xml");
        if (sharedStringsBuffer == null) {
            System.out.println("sharedStringsBuffer was null");
            return null;
        }

        XmlParser.Node sharedStringsXml = XmlParser.parseFromBuffer(sharedStringsBuffer);
        if (sharedStringsXml == null || sharedStringsXml.children == null) {
            System.out.println("sharedStringsXml: " + sharedStringsXml);
            if (sharedStringsXml != null)
                System.out.println("sharedStringsXml.children was null" + sharedStringsXml);
            return null;
        }

        RefVector<byte[]> sheetsBuffer = new RefVector<>(byte[].class);
        RefVector<XmlParser.Node> sheetsXml = new RefVector<>(XmlParser.Node.class);
        while (true) {
            int n = sheetsXml.size + 1;
            byte[] sheet = xmlFiles.get("sheet" + n + ".xml");
            if (sheet == null)
                break;

            sheetsBuffer.add(sheet);
            sheetsXml.add(XmlParser.parseFromBuffer(sheet));
        }

        if (sheetsXml.size == 0)
            return null;

        RefVector<String> stringsList = new RefVector<>(String.class);
        for (XmlParser.Node entry : sharedStringsXml.children) {
            XmlParser.Node stringNode = entry.getChild(0);
            if (stringNode != null)
                stringsList.add(stringNode.getInnerValue(sharedStringsBuffer));
        }

        String[] attr = new String[2];
        IntVector positions = new IntVector();
        RefVector<Object> cellValues = new RefVector<>(Object.class);

        ArrayList<SheetCells> outSheets = new ArrayList<>();

        for (int i = 0; i < sheetsXml.size; i++) {
            positions.size = 0;
            cellValues.clear();

            XmlParser.FlatNode[] flattened = XmlParser.getFlattenedNodes(sheetsXml.buf[i]);
            if (flattened == null) {
                System.out.println("" + i + ": flattened was null");
                continue;
            }

            byte[] sheetBuf = sheetsBuffer.buf[i];
            int totalRows = 0;
            int totalCols = 0;

            for (int j = 0; j < flattened.length - 1; j++) {
                if (flattened[j].attrSpans == null || !"c".equals(flattened[j].getName(sheetBuf)))
                    continue;

                String pos = null;
                String type = null;
                for (int k = 0; k < flattened[j].attrSpans.length / 4; k++) {
                    if (flattened[j].getAttribute(attr, k, sheetBuf)) {
                        if ("r".equals(attr[0]))
                            pos = attr[1];
                        else if ("t".equals(attr[0]))
                            type = attr[1];
                    }
                }

                if (!addRowColToVector(positions, pos)) {
                    System.out.println("Invalid cell reference: " + pos);
                    continue;
                }

                totalRows = Math.max(totalRows, positions.buf[positions.size-2] + 1);
                totalCols = Math.max(totalCols, positions.buf[positions.size-1] + 1);

                XmlParser.FlatNode valueNode = flattened[j].getChild(flattened, 0);

                String innerValue = valueNode.getInnerValue(sheetBuf);
                Object value = null;
                
                if ("s".equals(type)) {
                    try {
                        int idx = Integer.parseInt(innerValue);
                        value = stringsList.buf[idx];
                    }
                    catch (Exception ignored) {}
                }
                else {
                    try {
                        if (innerValue.indexOf(".") >= 0)
                            value = Double.parseDouble(innerValue);
                        else
                            value = Integer.parseInt(innerValue);
                    }
                    catch (Exception ex) {
                        value = innerValue;
                    }
                }

                cellValues.add(value);
            }

            if (totalRows == 0 || totalCols == 0) {
                continue;
            }

            Object[] cells = new Object[totalRows * totalCols];
            for (int j = 0; j < positions.size; j += 2) {
                int row = positions.buf[j];
                int col = positions.buf[j+1];
                cells[col + totalCols*row] = cellValues.buf[j >>> 1];
            }

            SheetCells sc = new SheetCells(cells, totalRows, totalCols);
            sc.flags |= appXml.flags;
            outSheets.add(sc);
        }

        return outSheets;
    }

    static String getBaseNameIfEntryIsUseful(ZipEntry entry) {
        String entryName = entry.getName();
        int nameStart = entryName.lastIndexOf('/') + 1;
        String name = nameStart > 0 && nameStart < entryName.length() ?
            entryName.substring(nameStart) :
            entryName;

        if (!name.equals("app.xml") && !name.equals("sharedStrings.xml")) {
            if (!name.startsWith("sheet") || !name.endsWith(".xml"))
                return null;

            try {
                Integer.parseInt(name.substring(5, name.length() - 4));
            }
            catch (Exception ex) {
                return null;
            }
        }

        return name;
    }

    static byte[] readSomeBytes(InputStream stream, long lSize) throws IOException {
        int size = (int)lSize;
        byte[] buf = new byte[size];
        int off = 0;
        try {
            while (true) {
                int res = stream.read(buf, off, size - off);
                if (res <= 0) // should be < 0
                    break;
                off += res;
            }
            if (off < size)
                throw new IOException("ZIP entry could not be fully read (" + off + "/" + size + " bytes were read)");
        }
        catch (IOException ex) {
            return null;
        }
        return buf;
    }

    static boolean addRowColToVector(IntVector vec, String cellRef) {
        if (cellRef == null || cellRef.length() == 0)
            return false;

        int row = 0;
        int col = 0;

        char[] chars = cellRef.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            boolean isRow;
            int delta = 0;
            if (chars[i] >= 'A' && chars[i] <= 'Z') {
                isRow = false;
                delta = 'A' - 1;
            }
            else if (chars[i] >= 'a' && chars[i] <= 'z') {
                isRow = false;
                delta = 'a' - 1;
            }
            else if (chars[i] >= '0' && chars[i] <= '9') {
                isRow = true;
                delta = '0';
            }
            else {
                return false;
            }

            if (isRow)
                row = row * 10 + chars[i] - delta;
            else
                col = col * 26 + chars[i] - delta;
        }

        row--;
        col--;

        if (row < 0 || col < 0)
            return false;

        vec.add(row);
        vec.add(col);
        return true;
    }
}
