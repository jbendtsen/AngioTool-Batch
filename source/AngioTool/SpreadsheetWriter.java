package AngioTool;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

public class SpreadsheetWriter {
    public static class Sheet {
        public String name = "";
        public ArrayList<String> formattedRows = new ArrayList<>();
        public int colsInWidestRow = 0;
    }

    public DateFormat dateFormatter;
    public DateFormat timeFormatter;

    public final File parentFolder;
    public final String fileName;

    HashMap<String, Integer> stringsMap;
    ArrayList<String> stringsList;
    int totalStringCount;

    ArrayList<Sheet> sheets;

    public int currentSheetIdx = 0;
    public boolean shouldSaveAfterEveryRow = true;

    public SpreadsheetWriter(File parentFolder, String name) {
        this.parentFolder = parentFolder;
        this.fileName = name + "-" + System.currentTimeMillis() + ".xlsx";
        this.dateFormatter = DateFormat.getDateInstance(2, new Locale("en", "US"));
        this.timeFormatter = DateFormat.getTimeInstance(2, new Locale("en", "US"));
        this.stringsMap = new HashMap<>();
        this.stringsList = new ArrayList<>();
        this.totalStringCount = 0;
        this.sheets = new ArrayList<>();
        this.currentSheetIdx = 0;
    }

    public static String makeColumnNumber(int n) {
        if (n <= 1)
            return "A";

        byte[] buf = new byte[16];
        int idx = buf.length;

        n--;
        while (n > 0) {
            buf[--idx] = (byte)(0x41 + (n % 26));
            n /= 26;
        }

        return new String(buf, idx, buf.length - idx);
    }

    private int lookupOrAddCellString(String str) {
        Integer idx = stringsMap.get(str);
        if (idx == null) {
            int len = stringsList.size();
            stringsList.add(str);
            return len;
        }
        return idx;
    }

    public void writeRow(Object... values) throws IOException {
        int sheetIdx = currentSheetIdx;
        while (sheetIdx >= sheets.size())
            sheets.add(new Sheet());

        Sheet s = sheets.get(sheetIdx);

        int rowNumber = s.formattedRows.size() + 1;

        StringBuilder sb = new StringBuilder();
        sb.append("<row r=\"");
        sb.append(rowNumber);
        sb.append("\">");

        int colNumber = 0;
        for (Object value : values) {
            colNumber++;
            if (value == null)
                continue;

            sb.append("<c r=\"");
            sb.append(makeColumnNumber(colNumber));
            sb.append(rowNumber);
            sb.append("\"");
            // set style index here with s=""
            if (value instanceof Number) {
                sb.append("><v>");
                sb.append(value.toString());
                sb.append("</v></c>");
            }
            else {
                int idx = lookupOrAddCellString(value.toString());
                totalStringCount++;
                sb.append(" t=\"s\"><v>");
                sb.append(idx);
                sb.append("</v></c>");
            }
        }

        sb.append("</row>");

        if (colNumber > s.colsInWidestRow)
            s.colsInWidestRow = colNumber;

        s.formattedRows.add(sb.toString());

        if (shouldSaveAfterEveryRow)
            save();
    }

    public void save() throws IOException {
        ByteVectorOutputStream vector = new ByteVectorOutputStream();
        ZipOutputStream zip = new ZipOutputStream(vector);

        String dateStr = LocalDateTime.now().atZone(ZoneId.of("GMT")).format(DateTimeFormatter.ISO_INSTANT);

        int sheetCount = sheets.size();
        for (int i = 0; i < sheetCount; i++) {
            Sheet s = sheets.get(i);
            if (s.name == null || s.name.isEmpty())
                s.name = "Sheet" + (i+1);
        }

        String[] folders = new String[] {
            "_rels/",
            "docProps/",
            "xl/",
            "xl/_rels/",
            "xl/worksheets/"
        };

        for (String folder : folders) {
            zip.putNextEntry(new ZipEntry(folder));
            zip.closeEntry();
        }

        writeStringBuilderToZip(zip, "[Content_Types].xml", generateContentTypesXml(sheetCount));
        writeStringBuilderToZip(zip, "_rels/.rels", generateTopRelsXml());
        writeStringBuilderToZip(zip, "docProps/app.xml", generateAppXml(sheets));
        writeStringBuilderToZip(zip, "docProps/core.xml", generateCoreXml("AngioTool-Batch", dateStr));
        writeStringBuilderToZip(zip, "xl/_rels/workbook.xml.rels", generateWorkbookRels(sheetCount));
        writeStringBuilderToZip(zip, "xl/styles.xml", generateStylesXml());
        writeStringBuilderToZip(zip, "xl/workbook.xml", generateWorkbookXml(sheets, dateStr));
        writeStringBuilderToZip(zip, "xl/sharedStrings.xml", generateSharedStringsXml(stringsList, totalStringCount));

        for (int i = 0; i < sheetCount; i++)
            writeStringBuilderToZip(zip, "xl/worksheets/sheet" + (i+1) + ".xml", generateSheetXml(sheets.get(i), dateStr));

        zip.close();

        File outPath = new File(parentFolder, fileName);
        FileOutputStream fileStream = new FileOutputStream(outPath);
        fileStream.write(vector.data, 0, vector.size);
        fileStream.close();
    }

    static String escapeXmlString(String input) {
        StringBuilder sb = new StringBuilder();

        int len = input.length();
        int start = 0;
        String esc = null;

        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    esc = "&amp;";
                    break;
                case '<':
                    esc = "&lt;";
                    break;
                case '>':
                    esc = "&gt;";
                    break;
                case '\'':
                    esc = "&apos;";
                    break;
                case '"':
                    esc = "&quot;";
                    break;
                default:
                    esc = null;
            }

            if (esc != null) {
                if (i > start)
                    sb.append(input, start, i);
                sb.append(esc);
                start = i + 1;
            }
        }

        if (len > start)
            sb.append(input, start, len);

        return sb.toString();
    }

    static String generateExcelUuid(String seed) {
        byte[] seedBytes = seed.getBytes();
        long hash1 = murmurHash64a(seedBytes, 0, seedBytes.length, 1337);
        long hash2 = murmurHash64a(seedBytes, 0, seedBytes.length, 420);

        byte[] uuid = new byte[36];
        int pos = 0;
        for (int i = 0; i < 32; i++) {
            long h = i < 16 ? hash1 : hash2;
            int d = i == 12 ? 4 : (int)((h >>> ((i&15L)*4L)) & 0x15L);
            uuid[pos++] = (byte)(d + (d < 10 ? 0x30 : 0x57));
            if (i == 7 || i == 11 || i == 15 || i == 19)
                uuid[pos++] = '-';
        }

        return new String(uuid, 0, 36);
    }

    static void writeStringBuilderToZip(ZipOutputStream zip, String path, StringBuilder sb) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(sb.toString().getBytes());
    }

    static StringBuilder generateContentTypesXml(int sheetCount) {
        StringBuilder sb = new StringBuilder();

        sb.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
        );

        for (int i = 0; i < sheetCount; i++) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet");
            sb.append(i + 1);
            sb.append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }

        sb.append(
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
            "<Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>" +
            "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>" +
            "<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>" +
            "</Types>"
        );

        return sb;
    }

    static StringBuilder generateTopRelsXml() {
        StringBuilder sb = new StringBuilder();

        sb.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\"" +
            " Target=\"docProps/app.xml\"/>" +
            "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\"" +
            " Target=\"docProps/core.xml\"/>" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\"" +
            " Target=\"xl/workbook.xml\"/>" +
            "</Relationships>"
        );

        return sb;
    }

    static StringBuilder generateAppXml(ArrayList<Sheet> sheets) {
        StringBuilder sb = new StringBuilder();
        int sheetCount = sheets.size();

        sb.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\"" +
            " xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\">" +
            "<Application>Microsoft Excel</Application>" +
            "<DocSecurity>0</DocSecurity>" +
            "<ScaleCrop>false</ScaleCrop>" +
            "<HeadingPairs><vt:vector size=\"2\" baseType=\"variant\">" +
            "<vt:variant><vt:lpstr>Worksheets</vt:lpstr></vt:variant>" +
            "<vt:variant><vt:i4>"
        );
        sb.append(sheetCount);
        sb.append("</vt:i4>" +
            "</vt:variant></vt:vector>" +
            "</HeadingPairs><TitlesOfParts><vt:vector size=\""
        );
        sb.append(sheetCount);
        sb.append("\" baseType=\"lpstr\">");

        for (int i = 0; i < sheetCount; i++) {
            sb.append("<vt:lpstr>");
            sb.append(escapeXmlString(sheets.get(i).name));
            sb.append("</vt:lpstr>");
        }

        sb.append(
            "</vt:vector></TitlesOfParts><Company></Company>" +
            "<LinksUpToDate>false</LinksUpToDate>" +
            "<SharedDoc>false</SharedDoc>" +
            "<HyperlinksChanged>false</HyperlinksChanged>" +
            "<AppVersion>16.0300</AppVersion></Properties>"
        );

        return sb;
    }

    static StringBuilder generateCoreXml(String creator, String dateStr) {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<cp:coreProperties" +
            " xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\"" +
            " xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" +
            " xmlns:dcterms=\"http://purl.org/dc/terms/\"" +
            " xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\"" +
            " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<dc:creator>"
        );
        sb.append(creator);
        sb.append("</dc:creator><cp:lastModifiedBy>");
        sb.append(creator);
        sb.append("</cp:lastModifiedBy><dcterms:created xsi:type=\"dcterms:W3CDTF\">");
        sb.append(dateStr);
        sb.append("</dcterms:created><dcterms:modified xsi:type=\"dcterms:W3CDTF\">");
        sb.append(dateStr);
        sb.append("</dcterms:modified></cp:coreProperties>");

        return sb;
    }

    static StringBuilder generateWorkbookRels(int sheetCount) {
        StringBuilder sb = new StringBuilder();

        sb.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        );

        for (int i = 0; i < sheetCount; i++) {
            sb.append("<Relationship Id=\"rId");
            sb.append(i + 1);
            sb.append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet");
            sb.append(i + 1);
            sb.append(".xml\"/>");
        }

        sb.append("<Relationship Id=\"rId");
        sb.append(sheetCount + 2);
        sb.append(
            "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\"" +
            " Target=\"sharedStrings.xml\"/>"
        );
        sb.append("<Relationship Id=\"rId");
        sb.append(sheetCount + 1);
        sb.append(
            "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\"" +
            " Target=\"styles.xml\"/>"
        );
        sb.append("</Relationships>");

        return sb;
    }

    static StringBuilder generateStylesXml() {
        StringBuilder sb = new StringBuilder();

        sb.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<styleSheet" +
            " xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"" +
            " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" mc:Ignorable=\"x14ac x16r2\"" +
            " xmlns:x14ac=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac\"" +
            " xmlns:x16r2=\"http://schemas.microsoft.com/office/spreadsheetml/2015/02/main\">" +
            "<numFmts count=\"3\">" +
            "<numFmt numFmtId=\"167\" formatCode=\"&quot;$&quot;#,##0.00_);[Red]\\(&quot;$&quot;#,##0.00\\)\"/>" +
            "<numFmt numFmtId=\"173\" formatCode=\"#,##0.00;[Red]\\-#,##0.00\"/>" +
            "<numFmt numFmtId=\"174\" formatCode=\"&quot;$&quot;#,##0.00;[Red]\\(&quot;$&quot;#,##0.00\\)\"/>" +
            "</numFmts>" +
            "<fonts count=\"3\" x14ac:knownFonts=\"1\">" +
            "<font><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/><family val=\"2\"/><scheme val=\"minor\"/></font>" +
            "<font><b/><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/><family val=\"2\"/><scheme val=\"minor\"/></font>" +
            "<font><b/><sz val=\"16\"/><color theme=\"1\"/><name val=\"Calibri\"/><family val=\"2\"/><scheme val=\"minor\"/></font>" +
            "</fonts>" +
            "<fills count=\"2\">" +
            "<fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"gray125\"/></fill>" +
            "</fills>" +
            "<borders count=\"1\">" +
            "<border><left/><right/><top/><bottom/><diagonal/></border>" +
            "</borders>" +
            "<cellStyleXfs count=\"1\">" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/>" +
            "</cellStyleXfs>" +
            "<cellXfs count=\"7\">" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
            "<xf numFmtId=\"173\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>" +
            "<xf numFmtId=\"174\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\">" +
            "<alignment horizontal=\"right\"/>" +
            "</xf>" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\">" +
            "<alignment horizontal=\"center\"/>" +
            "</xf>" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\">" +
            "<alignment horizontal=\"center\"/>" +
            "</xf>" +
            "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\">" +
            "<alignment horizontal=\"center\"/>" +
            "</xf>" +
            "</cellXfs>" +
            "<cellStyles count=\"1\">" +
            "<cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/>" +
            "</cellStyles>" +
            "<dxfs count=\"0\"/>" +
            "<tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium2\" defaultPivotStyle=\"PivotStyleLight16\"/>" +
            "<extLst>" +
            "<ext uri=\"{EB79DEF2-80B8-43e5-95BD-54CBDDF9020C}\" xmlns:x14=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/main\">" +
            "<x14:slicerStyles defaultSlicerStyle=\"SlicerStyleLight1\"/>" +
            "</ext>" +
            "<ext uri=\"{9260A510-F301-46a8-8635-F512D64BE5F5}\" xmlns:x15=\"http://schemas.microsoft.com/office/spreadsheetml/2010/11/main\">" +
            "<x15:timelineStyles defaultTimelineStyle=\"TimeSlicerStyleLight1\"/>" +
            "</ext>" +
            "</extLst>" +
            "</styleSheet>"
        );

        return sb;
    }

    static StringBuilder generateWorkbookXml(ArrayList<Sheet> sheets, String dateStr) {
        StringBuilder sb = new StringBuilder();

        sb.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<workbook" +
            " xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"" +
            " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"" +
            " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"" +
            " mc:Ignorable=\"x15 xr xr6 xr10 xr2\"" +
            " xmlns:x15=\"http://schemas.microsoft.com/office/spreadsheetml/2010/11/main\"" +
            " xmlns:xr=\"http://schemas.microsoft.com/office/spreadsheetml/2014/revision\"" +
            " xmlns:xr6=\"http://schemas.microsoft.com/office/spreadsheetml/2016/revision6\"" +
            " xmlns:xr10=\"http://schemas.microsoft.com/office/spreadsheetml/2016/revision10\"" +
            " xmlns:xr2=\"http://schemas.microsoft.com/office/spreadsheetml/2015/revision2\">" +
            "<fileVersion appName=\"xl\" lastEdited=\"7\" lowestEdited=\"7\" rupBuild=\"27029\"/>" +
            "<workbookPr defaultThemeVersion=\"166925\"/>" +
            "<xr:revisionPtr revIDLastSave=\"0\" documentId=\"8_{"
        );
        sb.append(generateExcelUuid(dateStr + "_workbook_xml_1"));
        sb.append(
            "}\" xr6:coauthVersionLast=\"47\" xr6:coauthVersionMax=\"47\"" +
            " xr10:uidLastSave=\"{00000000-0000-0000-0000-000000000000}\"/>" +
            "<bookViews>" +
            "<workbookView xWindow=\"2652\" yWindow=\"2652\" windowWidth=\"17280\" windowHeight=\"8964\" xr2:uid=\"{"
        );
        sb.append(generateExcelUuid(dateStr + "_workbook_xml_2"));
        sb.append("}\"/></bookViews><sheets>");

        int sheetCount = sheets.size();
        for (int i = 0; i < sheetCount; i++) {
            sb.append("<sheet name=\"");
            sb.append(escapeXmlString(sheets.get(i).name));
            sb.append("\" sheetId=\"");
            sb.append(i + 1);
            sb.append("\" r:id=\"rId");
            sb.append(i + 1);
            sb.append("\"/>");
        }

        sb.append(
            "</sheets>" +
            "<calcPr calcId=\"191029\"/>" +
            "<extLst>" +
            "<ext uri=\"{140A7094-0E35-4892-8432-C4D2E57EDEB5}\" xmlns:x15=\"http://schemas.microsoft.com/office/spreadsheetml/2010/11/main\">" +
            "<x15:workbookPr chartTrackingRefBase=\"1\"/>" +
            "</ext>" +
            "<ext uri=\"{B58B0392-4F1F-4190-BB64-5DF3571DCE5F}\" xmlns:xcalcf=\"http://schemas.microsoft.com/office/spreadsheetml/2018/calcfeatures\">" +
            "<xcalcf:calcFeatures>" +
            "<xcalcf:feature name=\"microsoft.com:RD\"/>" +
            "<xcalcf:feature name=\"microsoft.com:Single\"/>" +
            "<xcalcf:feature name=\"microsoft.com:FV\"/>" +
            "<xcalcf:feature name=\"microsoft.com:CNMTM\"/>" +
            "<xcalcf:feature name=\"microsoft.com:LET_WF\"/>" +
            "<xcalcf:feature name=\"microsoft.com:LAMBDA_WF\"/>" +
            "<xcalcf:feature name=\"microsoft.com:ARRAYTEXT_WF\"/>" +
            "</xcalcf:calcFeatures>" +
            "</ext>" +
            "</extLst>" +
            "</workbook>"
        );

        return sb;
    }

    static StringBuilder generateSharedStringsXml(ArrayList<String> uniqueStrings, int nTotalStrings) {
        StringBuilder sb = new StringBuilder();

        sb.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\""
        );
        sb.append(nTotalStrings);
        sb.append("\" uniqueCount=\"");
        sb.append(uniqueStrings.size());
        sb.append("\">");

        for (String str : uniqueStrings) {
            sb.append("<si><t>");
            sb.append(escapeXmlString(str));
            sb.append("</t></si>");
        }

        sb.append("</sst>");

        return sb;
    }

    static StringBuilder generateSheetXml(Sheet sheet, String dateStr) {
        StringBuilder sb = new StringBuilder();

        sb.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<worksheet" +
            " xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"" +
            " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"" +
            " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"" +
            " mc:Ignorable=\"x14ac xr xr2 xr3\"" +
            " xmlns:x14ac=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac\"" +
            " xmlns:xr=\"http://schemas.microsoft.com/office/spreadsheetml/2014/revision\"" +
            " xmlns:xr2=\"http://schemas.microsoft.com/office/spreadsheetml/2015/revision2\"" +
            " xmlns:xr3=\"http://schemas.microsoft.com/office/spreadsheetml/2016/revision3\"" +
            " xr:uid=\"{"
        );
        sb.append(generateExcelUuid(dateStr + "_sheet_" + sheet.name));
        sb.append(
            "}\">" +
            "<dimension ref=\"A1:"
        );
        sb.append(makeColumnNumber(sheet.colsInWidestRow));
        sb.append(sheet.formattedRows.size() + 1);
        sb.append(
            "\"/>" +
            "<sheetViews>" +
            "<sheetView workbookViewId=\"0\">" +
            "<selection activeCell=\"A1\" sqref=\"A1\"/>" +
            "</sheetView>" +
            "</sheetViews>" +
            "<sheetFormatPr defaultRowHeight=\"14.4\" x14ac:dyDescent=\"0.3\"/>"
        );

        // maybe add <cols>...</cols> to specify column widths

        sb.append("<sheetData>");

        for (String row : sheet.formattedRows)
            sb.append(row);

        sb.append("</sheetData>");

        // <mergeCells>...</mergeCells> goes here

        sb.append(
            "<pageMargins left=\"0.7\" right=\"0.7\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>" +
            "</worksheet>"
        );

        return sb;
    }

    static long murmurHash64a(byte[] key, int off, int len, long seed) {
        if (off < 0 || len <= 0 || off+len > key.length)
            return 0L;

        final long m = 0xc6a4a7935bd1e995L;
        final int r = 47;

        long h = seed ^ ((long)len * m);

        int blocks = len >> 3;
        for (int i = 0; i < len - 7; i += 8) {
            long k =
                (key[off+i] & 0xffL) |
                ((key[off+i+1] & 0xffL) << 8L) |
                ((key[off+i+2] & 0xffL) << 16L) |
                ((key[off+i+3] & 0xffL) << 24L) |
                ((key[off+i+4] & 0xffL) << 32L) |
                ((key[off+i+5] & 0xffL) << 40L) |
                ((key[off+i+6] & 0xffL) << 48L) |
                ((key[off+i+7] & 0xffL) << 56L);

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        int tail = off + (blocks << 3);
        switch (len & 7) {
            case 7: h ^= (key[tail+6] & 0xffL) << 48L;
            case 6: h ^= (key[tail+5] & 0xffL) << 40L;
            case 5: h ^= (key[tail+4] & 0xffL) << 32L;
            case 4: h ^= (key[tail+3] & 0xffL) << 24L;
            case 3: h ^= (key[tail+2] & 0xffL) << 16L;
            case 2: h ^= (key[tail+1] & 0xffL) << 8L;
            case 1: h ^= (key[tail] & 0xffL);
            h *= m;
        };

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        return h;
    }

    static class ByteVectorOutputStream extends OutputStream {
        public byte[] data;
        public int size;

        public ByteVectorOutputStream() {
            this.data = new byte[64];
            this.size = 0;
        }

        @Override
        public void write(int b) {
            int oldSize = size;
            resize(oldSize + 1);
            data[oldSize] = (byte)(b & 0xff);
        }

        @Override
        public void write(byte[] buf) {
            write(buf, 0, buf.length);
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            if (len <= 0 || off < 0 || off+len > buf.length)
                return;

            int oldSize = size;
            resize(oldSize + len);
            System.arraycopy(buf, off, data, oldSize, len);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}

        public void resize(int newSize) {
            if (newSize <= size)
                return;

            int newCap = data.length;
            while (newCap < newSize)
                newCap = (int)((float)newCap * 1.7) + 1;

            if (newCap > data.length) {
                byte[] newBuf = new byte[newCap];
                if (size > 0)
                    System.arraycopy(data, 0, newBuf, 0, size);
                data = newBuf;
            }

            size = newSize;
        }
    }
}
