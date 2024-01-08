package Batch;

import java.util.ArrayList;

public class XmlParser {
    public static class Node {
        int flags = 0; // |= child.flags
        int lastIdx = 0;
        int nameStart = 0;
        int nameLen = 0;
        int innerStart = 0;
        int innerLen = 0;
        PointVectorInt attrSpans = null;
        ArrayList<Node> children = null;
    }

    public static Node parseFromBuffer(byte[] buf) {
        return buf != null ? parseFromBuffer(buf, 0, buf.length) : null;
    }

    public static Node parseFromBuffer(byte[] buf, int off, int len) {
        if (buf == null)
            return null;
        if (off < 0 || len <= 0 || off+len > buf.length)
            return null;

        Node cur = new Node();
        int tagLenNonWs = -1;
        int attrStart = 0;
        int attrLen = 0;
        int valueStart = 0;
        int valueLen = 0;
        boolean isComment = false;
        boolean isSelfContained = false;
        byte waitUntilEndTagChar = 0;
        byte quoteChar = 0;
        byte prev = 0;
        byte prev2 = 0;

        long last8 = 0;
        long last16 = 0;

        int i;
        for (i = off; i < off+len; i++) {
            byte c = buf[i];
            last16 = (last16 << 8) | ((last8 >> 56L) & 0xffL);
            last8 = (last8 << 8) | (c & 0xffL);

            if ((c & 0x80) != 0) {
                if ((c & 0x60) == 0x40)
                    i++;
                if ((c & 0x70) == 0x60)
                    i += 2;
                if ((c & 0x78) == 0x70)
                    i += 3;
                continue;
            }

            if (isComment) {
                // AngioTool__Batch
                if (last16 == 0x416e67696f546f6fL && last8 == 0x6c5f5f4261746368L)
                    cur.flags |= 1 << 31;

                if (prev2 == '-' && prev == '-' && c == '>')
                    isComment = false;

                prev2 = prev;
                prev = c;
                continue;
            }

            boolean isTokenChar = c == '-' || c == '_' || c == '.' || c == ':' || c == '\'' || c == '"' ||
                (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');

            if (c == '"' || c == '\'') {
                if (quoteChar == 0) {
                    quoteChar = c;
                }
                else if (prev != '\\' && c == quoteChar) {
                    quoteChar = 0;
                }
            }

            if (waitUntilEndTagChar != 0) {
                if (c == '>' && quoteChar == 0) {
                    if (waitUntilEndTagChar == '/')
                        break;
                    waitUntilEndTagChar = 0;
                }
                prev2 = prev;
                prev = c;
                continue;
            }

            if (quoteChar == 0 && tagLenNonWs == 0) {
                if (c == '!') {
                    isComment = true;
                }
                else if (c == '?' || c == '/') {
                    waitUntilEndTagChar = c;
                    prev2 = prev;
                    prev = c;
                    continue;
                }
                else if (isTokenChar) {
                    
                }
            }

            if (tagLenNonWs >= 0) {
                if (isTokenChar) {
                    if (cur.nameStart == 0) {
                        cur.nameStart = i;
                    }
                    else if (cur.nameLen > 0 && attrStart == 0) {
                        attrStart = i;
                    }
                    else if (attrLen > 0 && valueStart == 0) {
                        valueStart = i;
                    }
                }
                else if (quoteChar == 0) {
                    if (cur.nameStart > 0 && cur.nameLen == 0) {
                        cur.nameLen = i - cur.nameStart;
                    }
                    else if (attrStart > 0 && attrLen == 0) {
                        attrLen = i - attrStart;
                    }
                    else if (valueStart > 0 && valueLen == 0) {
                        if (cur.attrSpans == null)
                            cur.attrSpans = new PointVectorInt();

                        cur.attrSpans.add(attrStart, attrLen);
                        cur.attrSpans.add(valueStart, i - valueStart);
                        attrStart = attrLen = valueStart = valueLen = 0;
                    }

                    if (c == '/' && tagLenNonWs > 0) {
                        isSelfContained = true;
                    }
                    if (c == '>') {
                        if (attrLen > 0) {
                            if (cur.attrSpans == null)
                                cur.attrSpans = new PointVectorInt();
                            cur.attrSpans.add(attrStart, attrLen);
                            cur.attrSpans.add(0, 0);
                        }

                        //String tagName = cur.nameStart > 0 && cur.nameLen > 0 ? new String(buf, cur.nameStart, cur.nameLen) : "";
                        //System.out.println(tagName);

                        if (isSelfContained) {
                            //System.out.println(tagName + " is self-contained");
                            break;
                        }

                        while (i < off+len) {
                            Node inner = parseFromBuffer(buf, i + 1, len - (i+1) + off);

                            cur.flags |= inner.flags;
                            if (inner.lastIdx > i)
                                i = inner.lastIdx;

                            if (inner.nameStart > 0 && inner.nameLen > 0) {
                                if (cur.children == null)
                                    cur.children = new ArrayList<>();
                                cur.children.add(inner);
                                continue;
                            }
                            else if (inner.innerStart > 0 && inner.innerLen > 0) {
                                /*
                                System.out.println(
                                    tagName + ": " + new String(buf, inner.innerStart, inner.innerLen) +
                                    " (" + cur.nameStart + ", " + cur.nameLen + ", " + inner.innerStart + ", " + inner.innerLen + ")"
                                );
                                */
                                cur.innerStart = inner.innerStart;
                                cur.innerLen = inner.innerLen;
                            }

                            break;
                        }

                        break;
                    }
                }
                if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                    tagLenNonWs++;
                }
            }
            else if (isTokenChar) {
                if (cur.innerStart == 0) {
                    cur.innerStart = i;
                }
            }

            if (quoteChar == 0) {
                if (c == '<') {
                    tagLenNonWs = 0;
                    if (cur.innerStart > 0)
                        cur.innerLen = i - cur.innerStart;
                }
                else if (c == '>') {
                    tagLenNonWs = -1;
                }
            }

            prev2 = prev;
            prev = c;
        }

        cur.lastIdx = i;
        return cur;
    }

    public static void printXml(Node node, StringBuilder sb, byte[] originalBuf, int levels) {
        for (int i = 0; i < levels; i++)
            sb.append("\t");

        String name = node.nameStart != 0 && node.nameLen != 0 ? new String(originalBuf, node.nameStart, node.nameLen) : "";
        sb.append("<");
        sb.append(name);

        if (node.attrSpans != null) {
            for (int i = 0; i < node.attrSpans.size; i += 4) {
                sb.append(" ");
                sb.append(new String(originalBuf, node.attrSpans.buf[i], node.attrSpans.buf[i+1]));

                int valueStart = node.attrSpans.buf[i+2];
                int valueLen = node.attrSpans.buf[i+3];
                if (valueStart != 0 && valueLen != 0) {
                    sb.append("=");
                    sb.append(new String(originalBuf, valueStart, valueLen));
                }
            }
        }

        if (node.children != null) {
            sb.append(">");
            sb.append("\n");

            for (Node child : node.children)
                printXml(child, sb, originalBuf, levels + 1);

            for (int i = 0; i < levels; i++)
                sb.append("\t");

            sb.append("</");
            sb.append(name);
            sb.append(">");
        }
        else if (node.innerStart != 0 && node.innerLen != 0) {
            sb.append(">");
            sb.append(new String(originalBuf, node.innerStart, node.innerLen));
            sb.append("</");
            sb.append(name);
            sb.append(">");
        }
        else {
            sb.append("/>");
        }

        sb.append("\n");
    }
}
