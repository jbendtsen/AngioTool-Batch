package AngioTool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;

public class SpreadsheetWriter {
    static class BaosProxy extends ByteArrayOutputStream {
        public BaosProxy() {
            super();
        }
        public BaosProxy(int size) {
            super(size);
        }
        public byte[] getBuffer() {
            return this.buf;
        }
        public int getLength() {
            return this.count;
        }
    }

    public DateFormat dateFormatter;
    public DateFormat timeFormatter;

    public final String fileName;

    ArrayList<String> formattedRows;
    int colsInWidestRow;

    public SpreadsheetWriter(String name) {
        this.fileName = name + "-" + System.currentTimeMillis() + ".xlsx";
        this.dateFormatter = DateFormat.getDateInstance(2, new Locale("en", "US"));
        this.timeFormatter = DateFormat.getTimeInstance(2, new Locale("en", "US"));
        this.formattedRows = new ArrayList<String>();
        this.colsInWidestRow = 0;
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

    public void writeRow(Object... values) throws IOException {
        int rowNumber = formattedRows.size() + 1;

        StringBuilder sb = new StringBuilder();
        sb.append("<row r=\"");
        sb.append(rowNumber);
        sb.append("\" customFormat=\"false\" ht=\"14.35\" hidden=\"false\" customHeight=\"false\" outlineLevel=\"0\" collapsed=\"false\">");

        int colNumber = 0;
        for (Object value : values) {
            colNumber++;
            sb.append("<c r=\"");
            sb.append(makeColumnNumber(colNumber));
            sb.append(rowNumber);
            sb.append("\" s=\"0\" t=\"");
            if (value instanceof Number) {
                sb.append("n\"><v>");
                sb.append(value.toString());
                sb.append("</v></c>");
            }
            else {
                sb.append("inlineStr\"><is><t>");
                sb.append(value.toString());
                sb.append("</t></is></c>");
            }
        }

        if (colNumber > colsInWidestRow)
            colsInWidestRow = colNumber;

        sb.append("</row>");
        formattedRows.add(sb.toString());

        save();
    }

    void save() throws IOException {
        BaosProxy stream = new BaosProxy();
        ZipOutputStream zip = new ZipOutputStream(stream);        
        String dateStr = "1970-01-01T11:00:00Z";

        zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
        zip.write((
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"png\" ContentType=\"image/png\"/>" +
            "<Default Extension=\"jpeg\" ContentType=\"image/jpeg\"/>" +
            "<Override PartName=\"/_rels/.rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Override PartName=\"/xl/_rels/workbook.xml.rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
            "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
            "<Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>" +
            "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/></Types>"
        ).getBytes());

        zip.putNextEntry(new ZipEntry("_rels/"));
        zip.closeEntry();

        zip.putNextEntry(new ZipEntry("_rels/.rels"));
        zip.write((
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" " +
            "Target=\"xl/workbook.xml\"/><Relationship Id=\"rId2\" " +
            "Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" " +
            "Target=\"docProps/core.xml\"/></Relationships>"
        ).getBytes());

        zip.putNextEntry(new ZipEntry("docProps/"));
        zip.closeEntry();

        zip.putNextEntry(new ZipEntry("docProps/core.xml"));
        zip.write((
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" " +
            "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<dcterms:created xsi:type=\"dcterms:W3CDTF\">" + dateStr + "</dcterms:created><dc:creator>" +
            "</dc:creator><dc:description></dc:description><dc:language>en-US</dc:language><cp:lastModifiedBy>" +
            "</cp:lastModifiedBy><dcterms:modified xsi:type=\"\\dcterms:W3CDTF\\\">" + dateStr +
            "</dcterms:modified><cp:revision>1</cp:revision><dc:subject></dc:subject><dc:title></dc:title></cp:coreProperties>"
        ).getBytes());

        zip.putNextEntry(new ZipEntry("xl/"));
        zip.closeEntry();

        zip.putNextEntry(new ZipEntry("xl/styles.xml"));
        zip.write((
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"></styleSheet>"
        ).getBytes());

        zip.putNextEntry(new ZipEntry("xl/workbook.xml"));
        zip.write((
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<fileVersion appName=\"Calc\"/><workbookPr backupFile=\"false\" showObjects=\"all\" date1904=\"false\"/>" +
            "<workbookProtection/><bookViews><workbookView showHorizontalScroll=\"true\" showVerticalScroll=\"true\" " +
            "showSheetTabs=\"true\" xWindow=\"0\" yWindow=\"0\" windowWidth=\"16384\" windowHeight=\"8192\" tabRatio=\"500\" " +
            "firstSheet=\"0\" activeTab=\"0\"/></bookViews><sheets><sheet name=\"Sheet1\" sheetId=\"1\" state=\"visible\" " +
            "r:id=\"rId2\"/></sheets><calcPr iterateCount=\"100\" refMode=\"A1\" iterate=\"false\" iterateDelta=\"0.001\"/>" +
            "<extLst><ext xmlns:loext=\"http://schemas.libreoffice.org/\" " +
            "uri=\"{7626C862-2A13-11E5-B345-FEFF819CDC9F}\"><loext:extCalcPr stringRefSyntax=\"CalcA1\"/></ext></extLst></workbook>"
        ).getBytes());

        zip.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
        zip.write((
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"0\" uniqueCount=\"0\"></sst>"
        ).getBytes());

        zip.putNextEntry(new ZipEntry("xl/_rels/"));
        zip.closeEntry();

        zip.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
        zip.write((
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" " +
            "Target=\"styles.xml\"/><Relationship Id=\"rId2\" " +
            "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" " +
            "Target=\"worksheets/sheet1.xml\"/><Relationship Id=\"rId3\" " +
            "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\" " +
            "Target=\"sharedStrings.xml\"/></Relationships>"
        ).getBytes());

        zip.putNextEntry(new ZipEntry("xl/worksheets/"));
        zip.closeEntry();

        zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));

        StringBuilder sheetBuilder = new StringBuilder();
        sheetBuilder.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet " +
            "xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" " +
            "xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\" " +
            "xmlns:x14=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/main\" " +
            "xmlns:xr2=\"http://schemas.microsoft.com/office/spreadsheetml/2015/revision2\" " +
            "xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"" +
            "><sheetPr filterMode=\"false\"><pageSetUpPr fitToPage=\"false\"/></sheetPr><dimension ref=\"");

        sheetBuilder.append("A1:");
        sheetBuilder.append(makeColumnNumber(colsInWidestRow));
        int nRows = formattedRows.size();
        sheetBuilder.append(nRows >= 1 ? nRows : 1);

        sheetBuilder.append(
            "\"/><sheetViews><sheetView showFormulas=\"false\" showGridLines=\"true\" showRowColHeaders=\"true\" " +
            "showZeros=\"true\" rightToLeft=\"false\" tabSelected=\"true\" showOutlineSymbols=\"true\" " +
            "defaultGridColor=\"true\" view=\"normal\" topLeftCell=\"A1\" colorId=\"64\" zoomScale=\"100\" " +
            "zoomScaleNormal=\"100\" zoomScalePageLayoutView=\"100\" workbookViewId=\"0\"></sheetView></sheetViews>" +
            "<sheetFormatPr defaultColWidth=\"11.23046875\" defaultRowHeight=\"14.35\" zeroHeight=\"false\" " +
            "outlineLevelRow=\"0\" outlineLevelCol=\"0\"></sheetFormatPr><sheetData>");

        for (String row : formattedRows)
            sheetBuilder.append(row);

        sheetBuilder.append("</sheetData></worksheet>");

        zip.write(sheetBuilder.toString().getBytes());

        zip.close();

        ByteBuffer buffer = ByteBuffer.wrap(stream.getBuffer(), 0, stream.getLength());
        Path outPath = FileSystems.getDefault().getPath(".", fileName);
        FileChannel out = FileChannel.open(outPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        out.write(buffer);
        out.close();
    }
}
