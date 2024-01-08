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

    // returns false if the xlsx should be backed up
    public boolean loadXlsxFromFile(String fname) throws IOException {
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

        zipFile.close();
        return readSheetsFromXmlFiles(xmlFiles);
    }

    // returns false if the xlsx should be backed up
    public boolean loadXlsxFromBuffer(byte[] buf, int off, int len) {
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

    boolean readSheetsFromXmlFiles(HashMap<String, byte[]> xmlFiles) {
        XmlParser.Node appXml = XmlParser.parseFromBuffer(xmlFiles.get("app.xml"));
        if (appXml == null)
            return false;

        XmlParser.Node sharedStringsXml = XmlParser.parseFromBuffer(xmlFiles.get("sharedStrings.xml"));
        if (sharedStringsXml == null)
            return false;

        ArrayList<XmlParser.Node> sheetsXml = new ArrayList<>();
        while (true) {
            int n = sheetsXml.size() + 1;
            XmlParser.Node sheet = XmlParser.parseFromBuffer(xmlFiles.get("sheet" + n + ".xml"));
            if (sheet == null)
                break;

            sheetsXml.add(sheet);
        }

        if (sheetsXml.isEmpty())
            return false;

        // ...

        return (appXml.flags & (1 << 31)) != 0;
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
}
