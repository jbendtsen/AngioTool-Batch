package Batch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/*
- number of sheets
    - inferred from structure (sheet1.xml exists, sheet2.xml exists, etc.)
- sheet content:
    - rows
    - columns
    - per cell:
        - type
        - value
*/

public class XlsxReader {
    public static class SheetCells {
        public Object[] cells;
        public int rows;
        public int cols;
        public int flags = 0;

        public SheetCells(Object[] cells, int rows, int cols) {
            this.cells = cells;
            this.rows = rows;
            this.cols = cols;
        }
    }

    // returns false if the xlsx should be backed up
    public static ArrayList<SheetCells> loadXlsxFromFile(String fname) throws IOException {
        ZipFile zipFile = new ZipFile(fname);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        HashMap<String, byte[]> xmlFiles = new HashMap<>();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = getBaseNameIfEntryIsUseful(entry);
            if (name == null)
                continue;

            xmlFiles.put(name, readSomeBytes(zipFile.getInputStream(entry), entry.getSize()));
        }

        try { zipFile.close(); }
        catch (IOException ignored) {}
        return readSheetsFromXmlFiles(xmlFiles);
    }

    // returns false if the xlsx should be backed up
    public static ArrayList<SheetCells> loadXlsxFromBuffer(byte[] buf, int off, int len) {
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
        if (sharedStringsBuffer == null)
            return null;

        XmlParser.Node sharedStringsXml = XmlParser.parseFromBuffer(sharedStringsBuffer);
        XmlParser.Node stringsTableXml = sharedStringsXml.getChild(0);
        if (stringsTableXml == null || stringsTableXml.children == null)
            return null;

        ArrayList<byte[]> sheetsBuffer = new ArrayList<>();
        ArrayList<XmlParser.Node> sheetsXml = new ArrayList<>();
        while (true) {
            int n = sheetsXml.size() + 1;
            byte[] sheet = xmlFiles.get("sheet" + n + ".xml");
            if (sheet == null)
                break;

            sheetsBuffer.add(sheet);
            sheetsXml.add(XmlParser.parseFromBuffer(sheet));
        }

        if (sheetsXml.isEmpty())
            return null;

        ArrayList<String> stringsList = new ArrayList<>();
        for (XmlParser.Node entry : stringsTableXml.children) {
            XmlParser.Node stringNode = entry.getChild(0);
            if (stringNode != null)
                stringsList.add(stringNode.getInnerValue(sharedStringsBuffer));
        }

        String[] attr = new String[2];
        PointVectorInt positions = new PointVectorInt();
        ArrayList<Object> cellValues = new ArrayList<>();

        ArrayList<SheetCells> outSheets = new ArrayList<>();

        for (int i = 0; i < sheetsXml.size(); i++) {
            positions.size = 0;
            cellValues.clear();

            XmlParser.FlatNode[] flattened = XmlParser.getFlattenedNodes(sheetsXml.get(i));
            if (flattened == null)
                continue;

            byte[] sheetBuf = sheetsBuffer.get(i);
            int totalRows = 0;
            int totalCols = 0;

            for (int j = 0; j < flattened.length; j++) {
                if (flattened[j].attrSpans == null || !"c".equals(flattened[j].getName(sheetBuf)))
                    continue;

                String pos = null;
                String type = null;
                for (int k = 0; k < flattened[j].attrSpans.length; k++) {
                    if (flattened[j].getAttribute(attr, k, sheetBuf)) {
                        if ("r".equals(attr[0]))
                            pos = attr[1];
                        else if ("t".equals(attr[0]))
                            type = attr[1];
                    }
                }

                if (!addRowColToVector(positions, pos))
                    continue;

                totalRows = Math.max(totalRows, positions.buf[positions.size*2-2] + 1);
                totalCols = Math.max(totalCols, positions.buf[positions.size*2-1] + 1);

                XmlParser.FlatNode valueNode = flattened[j].getChild(flattened, 0);
                valueNode = valueNode != null ? valueNode : flattened[j];
                String innerValue = valueNode.getInnerValue(sheetBuf);

                Object value = null;
                
                if ("s".equals(type)) {
                    try {
                        int idx = Integer.parseInt(innerValue);
                        value = stringsList.get(idx);
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

            if (totalRows == 0 || totalCols == 0)
                continue;

            Object[] cells = new Object[totalRows * totalCols];
            for (int j = 0; j < positions.size; j++) {
                int row = positions.buf[2*j];
                int col = positions.buf[2*j+1];
                cells[col + totalCols*row] = cellValues.get(j);
            }

            SheetCells sc = new SheetCells(cells, totalRows, totalCols);
            sc.flags |= appXml.flags;
            outSheets.add(sc);
        }

        return outSheets;
    }

    static String getBaseNameIfEntryIsUseful(ZipEntry entry) {
        String entryName = entry.getName();
        int nameStart = entryName.lastIndexOf('/');
        String name = nameStart > 0 ? entryName.substring(nameStart) : entryName;

        if (!name.equals("app.xml") || !name.equals("sharedStrings.xml")) {
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

    static byte[] readSomeBytes(InputStream stream, long lSize) {
        int size = (int)lSize;
        byte[] buf = new byte[size];
        try {
            stream.read(buf);
        }
        catch (IOException ex) {
            return null;
        }
        return buf;
    }

    static boolean addRowColToVector(PointVectorInt vec, String cellRef) {
        if (cellRef == null || cellRef == "")
            return false;

        int row = -1;
        int col = -1;

        char[] chars = cellRef.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            boolean isRow;
            int delta = 0;
            if (chars[i] >= 'A' && chars[i] <= 'Z') {
                isRow = true;
                delta = 'A';
            }
            else if (chars[i] >= 'a' && chars[i] <= 'z') {
                isRow = true;
                delta = 'a';
            }
            else if (chars[i] >= '0' && chars[i] <= '9') {
                isRow = false;
                delta = '0';
            }
            else {
                return false;
            }

            if (isRow) {
                row = row >= 0 ? row * 26 : 0;
                row += chars[i] - delta;
            }
            else {
                col = col >= 0 ? col * 10 : 0;
                col += chars[i] - delta;
            }
        }

        if (row < 0 || col < 0)
            return false;

        vec.add(row, col);
        return true;
    }
}
