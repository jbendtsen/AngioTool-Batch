package AngioTool;

import Utils.BatchUtils;
import Utils.ByteVectorOutputStream;
import Utils.RefVector;
import Pixels.Rgb;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.lang.reflect.Field;

public class ATPreferences
{
    public static void savePreferences(Object params, String fileName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("# " + AngioTool.VERSION + " Preferences\n");
        sb.append("# " + new Date() + "\n\n");

        Field[] fields = params.getClass().getDeclaredFields();
        try {
            for (Field f : fields) {
                if (!BatchUtils.shouldPersistField(f))
                    continue;

                String name = f.getName();
                String type = f.getType().getSimpleName();
                sb.append('.');
                sb.append(name);
                sb.append('.');
                sb.append(type);
                sb.append('=');
                sb.append(getStringOfArrayOrObject(f.get(params)));
                sb.append('\n');
            }
        }
        catch (IllegalAccessException ex) {
            BatchUtils.showExceptionInDialogBox(ex);
            return;
        }

        try {
            File path = new File(getPrefsDir(), fileName);
            FileOutputStream out = new FileOutputStream(path);
            out.write(sb.toString().getBytes());
            out.close();
        }
        catch (IOException ex) {
            BatchUtils.showExceptionInDialogBox(ex);
        }
    }

    public static RefVector<String> load(Object params, Class contextClass, String fileName) throws IOException
    {
        File atFolder = getPrefsDir();

        InputStream f = new FileInputStream(new File(atFolder, fileName));

        //if (f == null)
            //return "AT_Prefs.txt not found in AngioTool.jar or in " + AngioTool.prefsDir;

        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[512];
        while (true) {
            int res = f.read(buf);
            if (res <= 0)
                break;
            sb.append(new String(buf, 0, res));
        }
        /*
        catch (IOException ex) {
            String msg = ex.getMessage();
            return msg != null ? msg : "Failed to read from " + fileName;
        }
        */

        return populatePreferences(params, sb.toString());
    }

    public static RefVector<String> populatePreferences(Object params, String text)
    {
        HashMap<String, Field> map = new HashMap<>();
        Field[] fields = params.getClass().getDeclaredFields();
        for (Field f : fields)
            map.put(f.getName(), f);

        RefVector<String> errors = new RefVector<>(String.class);

        String[] lines = text.split("\n");
        for (String l : lines) {
            if (l.length() < 2 || l.charAt(0) == '#')
                continue;

            int nameStartIdx = l.charAt(0) == '.' ? 1 : 0;
            int typeStartIdx = l.indexOf('.', nameStartIdx) + 1;
            int valueStartIdx = l.indexOf('=') + 1;

            if (nameStartIdx < 0 || typeStartIdx <= 0 || valueStartIdx <= 0)
                continue;

            String name = l.substring(1, typeStartIdx - 1);
            String type = l.substring(typeStartIdx, valueStartIdx - 1);
            String valueStr = l.substring(valueStartIdx);

            Field f = map.get(name);
            if (f != null) {
                Object value;

                try {
                    if (type.endsWith("[]"))
                        value = parseArray(valueStr, type);
                    else if (type.equals("Rgb"))
                        value = new Rgb(valueStr);
                    else if (type.equals("boolean") || type.equals("bool"))
                        value = parseBool(valueStr);
                    else if (type.equals("int"))
                        value = Integer.parseInt(valueStr);
                    else if (type.equals("double"))
                        value = Double.parseDouble(valueStr);
                    else
                        value = valueStr;
                }
                catch (Exception ex) {
                    String message = ex.getMessage();
                    message = message != null ? message : ex.getClass().getSimpleName();
                    errors.add(message);
                    continue;
                }

                try {
                    f.set(params, value);
                }
                catch (IllegalAccessException ex) {
                    String message = ex.getMessage();
                    message = message != null ? message : ex.getClass().getSimpleName();
                    errors.add(message);
                    continue;
                }
            }
        }

        return errors;
    }

    public static String getStringOfArrayOrObject(Object obj)
    {
        ByteVectorOutputStream bv = new ByteVectorOutputStream();
        writeArrayToString(bv, obj);
        return bv.toString();
    }

    public static void writeArrayToString(ByteVectorOutputStream bv, Object obj)
    {
        if (obj instanceof String) {
            bv.add((String)obj);
        }
        else if (obj instanceof Object[]) {
            Object[] objs = (Object[])obj;
            bv.add('[');
            for (int i = 0; i < objs.length; i++)
                writeArrayToString(bv, objs[i]);
            bv.add(']');
        }
        else if (obj instanceof boolean[]) {
            boolean[] bools = (boolean[])obj;
            bv.add('[');
            for (int i = 0; i < bools.length; i++) {
                if (i > 0)
                    bv.add(", ");
                bv.add(bools[i] ? 'T' : 'F');
            }
            bv.add(']');
        }
        else if (obj instanceof byte[])
            bv.add(Arrays.toString((byte[])obj));
        else if (obj instanceof char[])
            bv.add(Arrays.toString((char[])obj));
        else if (obj instanceof short[])
            bv.add(Arrays.toString((short[])obj));
        else if (obj instanceof int[])
            bv.add(Arrays.toString((int[])obj));
        else if (obj instanceof long[])
            bv.add(Arrays.toString((long[])obj));
        else if (obj instanceof float[])
            bv.add(Arrays.toString((float[])obj));
        else if (obj instanceof double[])
            bv.add(Arrays.toString((double[])obj));
        else
            bv.add(obj != null ? obj.toString() : "null");
    }

    public static Object parseArray(String value, String type) throws Exception
    {
        Object array;
        if (type.equals("double[]")) {
            array = BatchUtils.getSomeDoubles(value);
        }
        else {
            throw new Exception("ATPreferences.parseArray() only works with \"double[]\", not \"" + type + "\"");
        }

        return array;
    }

    public static Boolean parseBool(String value)
    {
        char c = value.charAt(0);
        c = c >= 'A' && c <= 'Z' ? (char)(c + 0x20) : c;
        return c == 't' || c == 'y';
    }

    public static File getPrefsDir() throws IOException
    {
        File atFolder = new File(System.getProperty("user.home"), "AngioTool-Batch");
        if (!atFolder.exists())
            atFolder.mkdir();
        return atFolder;
    }
}
