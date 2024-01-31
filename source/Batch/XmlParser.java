package Batch;

public class XmlParser {
    public static class Node {
        public int flags = 0; // |= child.flags
        public int lastIdx = 0;
        public int nameStart = 0;
        public int nameLen = 0;
        public int innerStart = 0;
        public int innerLen = 0;
        public IntVector attrSpans = null;
        public RefVector<Node> children = null;

        public Node getChild(int idx) {
            return children != null ? children.getOr(idx, null) : null;
        }

        public String getName(byte[] originalBuf) {
            return makeString(originalBuf, nameStart, nameLen);
        }

        public String getInnerValue(byte[] originalBuf) {
            return makeString(originalBuf, innerStart, innerLen);
        }

        public boolean getAttribute(String[] nameValuePair, int idx, byte[] originalBuf) {
            if (nameValuePair == null || nameValuePair.length < 2)
                return false;
            if (attrSpans == null)
                return false;
            if (idx < 0 || idx * 4 + 3 >= attrSpans.size)
                return false;

            int attrNameStart  = attrSpans.buf[idx*4];
            int attrNameLen    = attrSpans.buf[idx*4+1];
            int attrValueStart = attrSpans.buf[idx*4+2];
            int attrValueLen   = attrSpans.buf[idx*4+3];

            if (attrNameStart <= 0 || attrNameLen <= 0)
                return false;

            getNameValuePair(nameValuePair, originalBuf, attrNameStart, attrNameLen, attrValueStart, attrValueLen);
            return true;
        }
    }

    public static class FlatNode {
        public int nameStart = 0;
        public int nameLen = 0;
        public int innerStart = 0;
        public int innerLen = 0;
        public int[] attrSpans = null;
        public int[] children = null;

        public FlatNode() {}

        public FlatNode(Node node) {
            this.nameStart = node.nameStart;
            this.nameLen = node.nameLen;
            this.innerStart = node.innerStart;
            this.innerLen = node.innerLen;

            int nAttrs = node.attrSpans != null ? node.attrSpans.size / 4 : 0;
            if (nAttrs > 0) {
                this.attrSpans = new int[nAttrs * 4];
                System.arraycopy(node.attrSpans.buf, 0, this.attrSpans, 0, nAttrs * 4);
            }

            int nChildren = node.children != null ? node.children.size : 0;
            if (nChildren > 0)
                this.children = new int[nChildren];
        }

        public FlatNode getChild(FlatNode[] nodes, int idx) {
            if (children == null)
                return null;
            if (idx < 0 || idx >= children.length)
                return null;

            int listIdx = children[idx];
            return listIdx >= 0 && listIdx < nodes.length ? nodes[listIdx] : null;
        }

        public String getName(byte[] originalBuf) {
            return makeString(originalBuf, nameStart, nameLen);
        }

        public String getInnerValue(byte[] originalBuf) {
            return makeString(originalBuf, innerStart, innerLen);
        }

        public boolean getAttribute(String[] nameValuePair, int idx, byte[] originalBuf) {
            if (nameValuePair == null || nameValuePair.length < 2)
                return false;
            if (attrSpans == null)
                return false;
            if (idx < 0 || idx >= attrSpans.length / 4)
                return false;

            int attrNameStart  = attrSpans[idx*4];
            int attrNameLen    = attrSpans[idx*4+1];
            int attrValueStart = attrSpans[idx*4+2];
            int attrValueLen   = attrSpans[idx*4+3];

            if (attrNameStart <= 0 || attrNameLen <= 0)
                return false;

            getNameValuePair(nameValuePair, originalBuf, attrNameStart, attrNameLen, attrValueStart, attrValueLen);
            return true;
        }
    };

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
                            cur.attrSpans = new IntVector();

                        cur.attrSpans.add(attrStart);
                        cur.attrSpans.add(attrLen);
                        cur.attrSpans.add(valueStart);
                        cur.attrSpans.add(i - valueStart);
                        attrStart = attrLen = valueStart = valueLen = 0;
                    }

                    if (c == '/' && tagLenNonWs > 0) {
                        isSelfContained = true;
                    }
                    if (c == '>') {
                        if (attrLen > 0) {
                            if (cur.attrSpans == null)
                                cur.attrSpans = new IntVector();
                            cur.attrSpans.add(attrStart);
                            cur.attrSpans.add(attrLen);
                            cur.attrSpans.add(0);
                            cur.attrSpans.add(0);
                        }

                        if (isSelfContained)
                            break;

                        while (i < off+len) {
                            int newLen = len - (i+1) + off;
                            Node inner = parseFromBuffer(buf, i + 1, newLen);

                            cur.flags |= inner.flags;
                            if (inner.lastIdx > i)
                                i = inner.lastIdx;

                            if (inner.nameStart > 0 && inner.nameLen > 0) {
                                if (cur.children == null)
                                    cur.children = new RefVector<>(Node.class);
                                cur.children.add(inner);
                                continue;
                            }
                            else if (inner.innerStart > 0 && inner.innerLen > 0) {
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

    public static FlatNode[] getFlattenedNodes(Node node) {
        if (node == null)
            return null;

        RefVector<FlatNode> flatNodes = new RefVector<>(FlatNode.class);
        getFlattenedNodes(flatNodes, node);
        if (flatNodes.size == 0)
            return null;

        return flatNodes.copy();
    }

    public static void getFlattenedNodes(RefVector<FlatNode> flatNodeList, Node node) {
        FlatNode flat = new FlatNode(node);
        flatNodeList.add(flat);
        if (node.children != null) {
            int idx = 0;
            for (Node n : node.children) {
                flat.children[idx++] = flatNodeList.size;
                getFlattenedNodes(flatNodeList, n);
            }
        }
    }

    public static void printXml(ByteVectorOutputStream sb, Node node, byte[] originalBuf, int levels) {
        sb.addMany('\t', levels);
        sb.add('<');
        sb.add(originalBuf, node.nameStart, node.nameLen);

        if (node.attrSpans != null) {
            for (int i = 0; i < node.attrSpans.size - 3; i += 4) {
                sb.add(' ');
                sb.add(originalBuf, node.attrSpans.buf[i], node.attrSpans.buf[i+1]);

                int valueStart = node.attrSpans.buf[i+2];
                int valueLen = node.attrSpans.buf[i+3];
                if (valueStart != 0 && valueLen != 0) {
                    sb.add('=');
                    sb.add(originalBuf, valueStart, valueLen);
                }
            }
        }

        if (node.children != null) {
            sb.add(">\n");

            for (Node child : node.children)
                printXml(sb, child, originalBuf, levels + 1);

            sb.addMany('\t', levels);
            sb.add("</");
            sb.add(originalBuf, node.nameStart, node.nameLen);
            sb.add('>');
        }
        else if (node.innerStart != 0 && node.innerLen != 0) {
            sb.add('>');
            sb.add(originalBuf, node.innerStart, node.innerLen);
            sb.add("</");
            sb.add(originalBuf, node.nameStart, node.nameLen);
            sb.add('>');
        }
        else {
            sb.add("/>");
        }

        sb.add('\n');
    }

    public static void printXml(ByteVectorOutputStream sb, FlatNode[] flatNodes, byte[] originalBuf) {
        IntVector parents = new IntVector();

        for (int i = 0; i < flatNodes.length; i++) {
            FlatNode node = flatNodes[i];

            sb.addMany('\t', parents.size);
            sb.add('<');
            sb.add(originalBuf, node.nameStart, node.nameLen);

            if (node.attrSpans != null) {
                for (int j = 0; j < node.attrSpans.length - 3; j += 4) {
                    sb.add(' ');
                    sb.add(originalBuf, node.attrSpans[j], node.attrSpans[j+1]);

                    int valueStart = node.attrSpans[j+2];
                    int valueLen = node.attrSpans[j+3];
                    if (valueStart != 0 && valueLen != 0) {
                        sb.add('=');
                        sb.add(originalBuf, valueStart, valueLen);
                    }
                }
            }

            if (node.children != null) {
                sb.add(">");

                parents.add(i);

                //for (Node child : node.children)
                    //printXml(sb, child, originalBuf, levels + 1);
            }
            else if (node.innerStart != 0 && node.innerLen != 0) {
                sb.add('>');
                sb.add(originalBuf, node.innerStart, node.innerLen);
                sb.add("</");
                sb.add(originalBuf, node.nameStart, node.nameLen);
                sb.add('>');
            }
            else {
                sb.add("/>");
            }

            sb.add('\n');

            int parent;
            int cur = i;
            while ((parent = parents.lastOr(-1)) >= 0) {
                int[] children = flatNodes[parent].children;
                if (children[children.length-1] == cur) {
                    parents.size--;
                    sb.addMany('\t', parents.size);
                    sb.add("</");
                    sb.add(originalBuf, flatNodes[parent].nameStart, flatNodes[parent].nameLen);
                    sb.add(">\n");
                }
                else {
                    break;
                }
                cur = parent;
            }
        }

        int parent;
        while ((parent = parents.lastOr(-1)) >= 0) {
            int[] children = flatNodes[parent].children;
            parents.size--;
            sb.addMany('\t', parents.size);
            sb.add("</");
            sb.add(originalBuf, flatNodes[parent].nameStart, flatNodes[parent].nameLen);
            sb.add(">\n");
        }
    }

    public static String makeString(byte[] buf, int start, int len) {
        return start > 0 && len > 0 ? new String(buf, start, len) : "";
    }

    public static void getNameValuePair(
        String[] nameValuePair,
        byte[] buf,
        int attrNameStart,
        int attrNameLen,
        int attrValueStart,
        int attrValueLen
    ) {
        if (attrValueStart > 0 && attrValueLen >= 2 &&
            (buf[attrValueStart] == '\'' || buf[attrValueStart] == '"') &&
            buf[attrValueStart] == buf[attrValueStart+attrValueLen-1]
        ) {
            attrValueStart++;
            attrValueLen -= 2;
        }

        nameValuePair[0] = new String(buf, attrNameStart, attrNameLen);
        nameValuePair[1] = attrValueStart > 0 && attrValueLen > 0 ? new String(buf, attrValueStart, attrValueLen) : "";
    }
}
