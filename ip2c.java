import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.*;
import java.util.*;


class ip2c {

    static File dbFile() {
        return assertFileExists(new File("IP2LOCATION-LITE-DB1.CSV"));
    }

    public static void main(final String[] args) throws Exception {
        if (empty(args))
        { print("Please supply an IP number as the argument to get the country name."); return; }

        String ip = args[0];
        long ipNum = ipToInt(ip);

        String line = pairB(binarySearchForLineInTextFile(dbFile(), s -> {
            List<String> l = tok_splitAtComma_unquote(s);
            long a = parseLongOpt(first(l)), b = parseLongOpt(second(l));
            return ipNum > b ? 1 : ipNum < a ? -1 : 0;
        }));

        print(or2(get(tok_splitAtComma_unquote(line), 2), "unknown"));
    }
    static File assertFileExists(File f) {
        return assertExists(f);
    }
    static boolean empty(Collection c) { return c == null || c.isEmpty(); }
    static boolean empty(CharSequence s) { return s == null || s.length() == 0; }
    static boolean empty(Map map) { return map == null || map.isEmpty(); }
    static boolean empty(Object[] o) { return o == null || o.length == 0; }
    static boolean empty(Object o) {
        if (o instanceof Collection) return empty((Collection) o);
        if (o instanceof String) return empty((String) o);
        if (o instanceof Map) return empty((Map) o);
        if (o instanceof Object[]) return empty((Object[]) o);
        if (o instanceof byte[]) return empty((byte[]) o);
        if (o == null) return true;
        throw fail("unknown type for 'empty': " + getType(o));
    }

    static boolean empty(Iterator i) { return i == null || !i.hasNext(); }

    static boolean empty(double[] a) { return a == null || a.length == 0; }
    static boolean empty(float[] a) { return a == null || a.length == 0; }
    static boolean empty(int[] a) { return a == null || a.length == 0; }
    static boolean empty(long[] a) { return a == null || a.length == 0; }
    static boolean empty(byte[] a) { return a == null || a.length == 0; }
    static boolean empty(short[] a) { return a == null || a.length == 0; }





    static boolean empty(File f) { return getFileSize(f) == 0; }
    static volatile StringBuffer local_log = new StringBuffer(); // not redirected


    static volatile Appendable print_log = local_log; // might be redirected, e.g. to main bot

    // in bytes - will cut to half that
    static volatile int print_log_max = 1024*1024;
    static volatile int local_log_max = 100*1024;

    static boolean print_silent = false; // total mute if set

    static Object print_byThread_lock = new Object();
    static volatile ThreadLocal<Object> print_byThread; // special handling by thread - prefers F1<S, Bool>
    static volatile Object print_allThreads;
    static volatile Object print_preprocess;

    static void print() {
        print("");
    }

    static <A> A print(String s, A o) {
        print((endsWithLetterOrDigit(s) ? s + ": " : s) + o);
        return o;
    }

    // slightly overblown signature to return original object...
    static <A> A print(A o) {
        ping_okInCleanUp();
        if (print_silent) return o;
        String s = String.valueOf(o) + "\n";
        print_noNewLine(s);
        return o;
    }

    static void print_noNewLine(String s) {


        print_raw(s);
    }

    static void print_raw(String s) {

        Appendable loc = local_log;
        Appendable buf = print_log;
        int loc_max = print_log_max;
        if (buf != loc && buf != null) {
            print_append(buf, s, print_log_max);
            loc_max = local_log_max;
        }
        if (loc != null)
            print_append(loc, s, loc_max);
        System.out.print(s);
    }

    static void print_autoRotate() {

    }
    static long ipToInt(String ip) {
        Matches m = new Matches();
        assertTrue(jmatch("*.*.*.*", ip, m));
        return parseLong(m.unq(3))
                | parseLong(m.unq(2)) << 8
                | parseLong(m.unq(1)) << 16
                | parseLong(m.unq(0)) << 24;
    }
    static <A, B> B pairB(Pair<A, B> p) {
        return p == null ? null : p.b;
    }
    // returns pair(character range, line) or null if not found
// if no exact match found, return line above
// nav takes a line and returns -1 (move up), 0 (found) or 1 (move down)
    static Pair<LongRange, String> binarySearchForLineInTextFile(File file, IF1<String, Integer> nav) {
        long length = l(file);
        int bufSize = 1024;
        RandomAccessFile raf = randomAccessFileForReading(file); try {
            long min = 0, max = length;
            int direction = 0;
            Pair<LongRange, String> possibleResult = null;
            while  (min < max) { ping();
                long middle = (min+max)/2;
                long lineStart = raf_findBeginningOfLine(raf, middle, bufSize);
                long lineEnd = raf_findEndOfLine(raf, middle, bufSize);

                String line = fromUtf8(raf_readFilePart(raf, lineStart, (int) (lineEnd-1-lineStart)));
                direction = nav.get(line);
                possibleResult = pair(new LongRange(lineStart, lineEnd), line);

                if (direction == 0) return possibleResult;
                // asserts are to assure that loop terminates
                if (direction < 0) max = assertLessThan(max, lineStart);
                else min = assertBiggerThan(min, lineEnd);

            }


            if (direction >= 0) return possibleResult;

            long lineStart = raf_findBeginningOfLine(raf, min-1, bufSize);
            String line = fromUtf8(raf_readFilePart(raf, lineStart, (int) (min-1-lineStart)));

            return pair(new LongRange(lineStart, min), line);
        } finally { _close(raf); }}
    static List<String> tok_splitAtComma_unquote(String s) {
        List<String> tok = javaTok(s);
        List<String> out = new ArrayList();
        for (int i = 0; i < l(tok); i++) {
            int j = smartIndexOf(tok, ",", i);
            out.add(unquote(trimJoinSubList(tok, i, j)));
            i = j;
        }
        return out;
    }
    static long parseLongOpt(String s) {
        return isInteger(s) ? parseLong(s) : 0;
    }

    static Object first(Object list) {
        return first((Iterable) list);
    }


    static <A> A first(List<A> list) {
        return empty(list) ? null : list.get(0);
    }

    static <A> A first(A[] bla) {
        return bla == null || bla.length == 0 ? null : bla[0];
    }



    static <A> A first(Iterator<A> i) {
        return i == null || !i.hasNext() ? null : i.next();
    }

    static <A> A first(Iterable<A> i) {
        if (i == null) return null;
        Iterator<A> it = i.iterator();
        return it.hasNext() ? it.next() : null;
    }

    static Character first(String s) { return empty(s) ? null : s.charAt(0); }


    static <A, B> A first(Pair<A, B> p) {
        return p == null ? null : p.a;
    }




    static Byte first(byte[] l) {
        return empty(l) ? null : l[0];
    }
    static <A> A second(List<A> l) {
        return get(l, 1);
    }

    static <A> A second(Iterable<A> l) {
        if (l == null) return null;
        Iterator<A> it = iterator(l);
        if (!it.hasNext()) return null;
        it.next();
        return it.hasNext() ? it.next() : null;
    }

    static <A> A second(A[] bla) {
        return bla == null || bla.length <= 1 ? null : bla[1];
    }


    static <A, B> B second(Pair<A, B> p) {
        return p == null ? null : p.b;
    }






    static char second(String s) {
        return charAt(s, 1);
    }


    static String or2(String a, String b) {
        return nempty(a) ? a : b;
    }

    static String or2(String a, String b, String c) {
        return or2(or2(a, b), c);
    }
// get purpose 1: access a list/array/map (safer version of x.get(y))

    static <A> A get(List<A> l, int idx) {
        return l != null && idx >= 0 && idx < l(l) ? l.get(idx) : null;
    }

// seems to conflict with other signatures
/*static <A, B> B get(Map<A, B> map, A key) {
  ret map != null ? map.get(key) : null;
}*/

    static <A> A get(A[] l, int idx) {
        return idx >= 0 && idx < l(l) ? l[idx] : null;
    }

    // default to false
    static boolean get(boolean[] l, int idx) {
        return idx >= 0 && idx < l(l) ? l[idx] : false;
    }

// get purpose 2: access a field by reflection or a map

    static Object get(Object o, String field) {
        try {
            if (o == null) return null;
            if (o instanceof Class) return get((Class) o, field);

            if (o instanceof Map)
                return ((Map) o).get(field);

            Field f = getOpt_findField(o.getClass(), field);
            if (f != null) {
                makeAccessible(f);
                return f.get(o);
            }


        } catch (Exception e) {
            throw asRuntimeException(e);
        }
        throw new RuntimeException("Field '" + field + "' not found in " + o.getClass().getName());
    }

    static Object get_raw(String field, Object o) {
        return get_raw(o, field);
    }

    static Object get_raw(Object o, String field) { try {
        if (o == null) return null;
        Field f = get_findField(o.getClass(), field);
        makeAccessible(f);
        return f.get(o);
    } catch (Exception __e) { throw rethrow(__e); } }

    static Object get(Class c, String field) {
        try {
            Field f = get_findStaticField(c, field);
            makeAccessible(f);
            return f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Field get_findStaticField(Class<?> c, String field) {
        Class _c = c;
        do {
            for (Field f : _c.getDeclaredFields())
                if (f.getName().equals(field) && (f.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0)
                    return f;
            _c = _c.getSuperclass();
        } while (_c != null);
        throw new RuntimeException("Static field '" + field + "' not found in " + c.getName());
    }

    static Field get_findField(Class<?> c, String field) {
        Class _c = c;
        do {
            for (Field f : _c.getDeclaredFields())
                if (f.getName().equals(field))
                    return f;
            _c = _c.getSuperclass();
        } while (_c != null);
        throw new RuntimeException("Field '" + field + "' not found in " + c.getName());
    }

    static Object get(String field, Object o) {
        return get(o, field);
    }


    static AutoCloseable tempInterceptPrintIfNotIntercepted(F1<String, Boolean> f) {
        return print_byThread().get() == null ? tempInterceptPrint(f) : null;
    }
    static File assertExists(File f) {
        if (!fileExists(f)) throw fail("File not found: " + f);
        return f;
    }
    static RuntimeException fail() { throw new RuntimeException("fail"); }
    static RuntimeException fail(Throwable e) { throw asRuntimeException(e); }
    static RuntimeException fail(Object msg) { throw new RuntimeException(String.valueOf(msg)); }
    static RuntimeException fail(String msg) { throw new RuntimeException(msg == null ? "" : msg); }
    static RuntimeException fail(String msg, Throwable innerException) { throw new RuntimeException(msg, innerException); }

    static String getType(Object o) {
        return getClassName(o);
    }
    static long getFileSize(String path) {
        return path == null ? 0 : new File(path).length();
    }

    static long getFileSize(File f) {
        return f == null ? 0 : f.length();
    }
    static boolean endsWithLetterOrDigit(String s) {
        return s != null && s.length() > 0 && Character.isLetterOrDigit(s.charAt(s.length()-1));
    }
    static void ping_okInCleanUp() {

    }
    static void print_append(Appendable buf, String s, int max) { try {
        synchronized(buf) {
            buf.append(s);
            if (buf instanceof StringBuffer)
                rotateStringBuffer(((StringBuffer) buf), max);
            else if (buf instanceof StringBuilder)
                rotateStringBuilder(((StringBuilder) buf), max);
        }
    } catch (Exception __e) { throw rethrow(__e); } }
    static void assertTrue(Object o) {
        if (!(eq(o, true) /*|| isTrue(pcallF(o))*/))
            throw fail(str(o));
    }

    static boolean assertTrue(String msg, boolean b) {
        if (!b)
            throw fail(msg);
        return b;
    }

    static boolean assertTrue(boolean b) {
        if (!b)
            throw fail("oops");
        return b;
    }
    static boolean jmatch(String pat, String s) {
        return jmatch(pat, s, null);
    }

    static boolean jmatch(String pat, String s, Matches matches) {
        if (s == null) return false;
        return jmatch(pat, javaTok(s), matches);
    }

    static boolean jmatch(String pat, List<String> toks) {
        return jmatch(pat, toks, null);
    }

    static boolean jmatch(String pat, List<String> toks, Matches matches) {
        List<String> tokpat = javaTok(pat);
        String[] m = match2(tokpat, toks);
        //print(structure(tokpat) + " on " + structure(toks) + " => " + structure(m));
        if (m == null)
            return false;
        else {
            if (matches != null) matches.m = m;
            return true;
        }
    }
    static long parseLong(String s) {
        if (empty(s)) return 0;
        return Long.parseLong(dropSuffix("L", s));
    }

    static long parseLong(Object s) {
        return Long.parseLong((String) s);
    }
    static int l(Object[] a) { return a == null ? 0 : a.length; }
    static int l(boolean[] a) { return a == null ? 0 : a.length; }
    static int l(byte[] a) { return a == null ? 0 : a.length; }
    static int l(short[] a) { return a == null ? 0 : a.length; }
    static int l(long[] a) { return a == null ? 0 : a.length; }
    static int l(int[] a) { return a == null ? 0 : a.length; }
    static int l(float[] a) { return a == null ? 0 : a.length; }
    static int l(double[] a) { return a == null ? 0 : a.length; }
    static int l(char[] a) { return a == null ? 0 : a.length; }
    static int l(Collection c) { return c == null ? 0 : c.size(); }

    static int l(Iterator i) { return iteratorCount_int_close(i); } // consumes the iterator && closes it if possible

    static int l(Map m) { return m == null ? 0 : m.size(); }
    static int l(CharSequence s) { return s == null ? 0 : s.length(); }
    static long l(File f) { return f == null ? 0 : f.length(); }



    static int l(Object o) {
        return o == null ? 0
                : o instanceof String ? l((String) o)
                : o instanceof Map ? l((Map) o)
                : o instanceof Collection ? l((Collection) o)
                : o instanceof Object[] ? l((Object[]) o)
                : o instanceof boolean[] ? l((boolean[]) o)
                : o instanceof byte[] ? l((byte[]) o)
                : o instanceof char[] ? l((char[]) o)
                : o instanceof short[] ? l((short[]) o)
                : o instanceof int[] ? l((int[]) o)
                : o instanceof float[] ? l((float[]) o)
                : o instanceof double[] ? l((double[]) o)
                : o instanceof long[] ? l((long[]) o)
                : (Integer) call(o, "size");
    }









    static long l(LongRange r) { return r == null ? 0 : r.length(); }

    static RandomAccessFile randomAccessFileForReading(File path) { try {

        return new RandomAccessFile(path, "r");


    } catch (Exception __e) { throw rethrow(__e); } }

    // you can change this function to allow interrupting long calculations from the outside. just throw a RuntimeException.
    static boolean ping() { return true; }
    static boolean ping_impl(boolean okInCleanUp) { return true; }



    static long raf_findBeginningOfLine(RandomAccessFile raf, long pos, int bufSize) { try {
        byte[] buf = new byte[bufSize];
        while (pos > 0) {
            long start = Math.max(pos-bufSize, 0);
            raf.seek(start);
            raf.readFully(buf, 0, (int) Math.min(pos-start, bufSize));
            int idx = lastIndexOf_byteArray(buf, (byte) '\n');
            if (idx >= 0) return start+idx+1;
            pos = start;
        }
        return 0;
    } catch (Exception __e) { throw rethrow(__e); } }
    static long raf_findEndOfLine(RandomAccessFile raf, long pos, int bufSize) { try {
        byte[] buf = new byte[bufSize];
        long length = raf.length();
        while (pos < length) {
            raf.seek(pos);
            raf.readFully(buf, 0, (int) Math.min(length-pos, bufSize));
            int idx = indexOf_byteArray(buf, (byte) '\n');
            if (idx >= 0) return pos+idx+1;
            pos += bufSize;
        }
        return length;
    } catch (Exception __e) { throw rethrow(__e); } }
    static String fromUtf8(byte[] bytes) { try {
        return bytes == null ? null : new String(bytes, "UTF-8");
    } catch (Exception __e) { throw rethrow(__e); } }
    static byte[] raf_readFilePart(RandomAccessFile raf, long start, int l) { try {
        byte[] buf = new byte[l];
        raf.seek(start);
        raf.readFully(buf);
        return buf;
    } catch (Exception __e) { throw rethrow(__e); } }
    static <A, B> Pair<A, B> pair(A a, B b) {
        return new Pair(a, b);
    }

    static <A> Pair<A, A> pair(A a) {
        return new Pair(a, a);
    }
    static <A> A assertLessThan(A a, A b) {
        assertTrue(cmp(b, a) < 0);
        return b;
    }
    static <A> A assertBiggerThan(A a, A b) {
        assertTrue(cmp(b, a) > 0);
        return b;
    }
    static void _close(AutoCloseable c) {
        if (c != null) try {
            c.close();
        } catch (Throwable e) {
            // Some classes stupidly throw an exception on double-closing
            if (c instanceof javax.imageio.stream.ImageOutputStream)
                return;
            else throw rethrow(e);
        }
    }
// TODO: extended multi-line strings

    static int javaTok_n, javaTok_elements;
    static boolean javaTok_opt = false;

    static List<String> javaTok(String s) {
        ++javaTok_n;
        ArrayList<String> tok = new ArrayList();
        int l = s == null ? 0 : s.length();

        int i = 0, n = 0;
        while (i < l) {
            int j = i;
            char c, d;

            // scan for whitespace
            while (j < l) {
                c = s.charAt(j);
                d = j+1 >= l ? '\0' : s.charAt(j+1);
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
                    ++j;
                else if (c == '/' && d == '*') {
                    do ++j; while (j < l && !s.substring(j, Math.min(j+2, l)).equals("*/"));
                    j = Math.min(j+2, l);
                } else if (c == '/' && d == '/') {
                    do ++j; while (j < l && "\r\n".indexOf(s.charAt(j)) < 0);
                } else
                    break;
            }

            tok.add(javaTok_substringN(s, i, j));
            ++n;
            i = j;
            if (i >= l) break;
            c = s.charAt(i);
            d = i+1 >= l ? '\0' : s.charAt(i+1);

            // scan for non-whitespace

            // Special JavaX syntax: 'identifier
            if (c == '\'' && Character.isJavaIdentifierStart(d) && i+2 < l && "'\\".indexOf(s.charAt(i+2)) < 0) {
                j += 2;
                while (j < l && Character.isJavaIdentifierPart(s.charAt(j)))
                    ++j;
            } else if (c == '\'' || c == '"') {
                char opener = c;
                ++j;
                while (j < l) {
                    int c2 = s.charAt(j);
                    if (c2 == opener || c2 == '\n' && opener == '\'') { // allow multi-line strings, but not for '
                        ++j;
                        break;
                    } else if (c2 == '\\' && j+1 < l)
                        j += 2;
                    else
                        ++j;
                }
            } else if (Character.isJavaIdentifierStart(c))
                do ++j; while (j < l && (Character.isJavaIdentifierPart(s.charAt(j)) || s.charAt(j) == '\'')); // for stuff like "don't"
            else if (Character.isDigit(c)) {
                do ++j; while (j < l && Character.isDigit(s.charAt(j)));
                if (j < l && s.charAt(j) == 'L') ++j; // Long constants like 1L
            } else if (c == '[' && d == '[') {
                do ++j; while (j+1 < l && !s.substring(j, j+2).equals("]]"));
                j = Math.min(j+2, l);
            } else if (c == '[' && d == '=' && i+2 < l && s.charAt(i+2) == '[') {
                do ++j; while (j+2 < l && !s.substring(j, j+3).equals("]=]"));
                j = Math.min(j+3, l);
            } else
                ++j;

            tok.add(javaTok_substringC(s, i, j));
            ++n;
            i = j;
        }

        if ((tok.size() % 2) == 0) tok.add("");
        javaTok_elements += tok.size();
        return tok;
    }

    static List<String> javaTok(List<String> tok) {
        return javaTokWithExisting(join(tok), tok);
    }
    // returns l(s) if not found
    static int smartIndexOf(String s, String sub, int i) {
        if (s == null) return 0;
        i = s.indexOf(sub, min(i, l(s)));
        return i >= 0 ? i : l(s);
    }

    static int smartIndexOf(String s, int i, char c) {
        return smartIndexOf(s, c, i);
    }

    static int smartIndexOf(String s, char c, int i) {
        if (s == null) return 0;
        i = s.indexOf(c, min(i, l(s)));
        return i >= 0 ? i : l(s);
    }

    static int smartIndexOf(String s, String sub) {
        return smartIndexOf(s, sub, 0);
    }

    static int smartIndexOf(String s, char c) {
        return smartIndexOf(s, c, 0);
    }

    static <A> int smartIndexOf(List<A> l, A sub) {
        return smartIndexOf(l, sub, 0);
    }

    static <A> int smartIndexOf(List<A> l, int start, A sub) {
        return smartIndexOf(l, sub, start);
    }

    static <A> int smartIndexOf(List<A> l, A sub, int start) {
        int i = indexOf(l, sub, start);
        return i < 0 ? l(l) : i;
    }
    static String unquote(String s) {
        if (s == null) return null;
        if (startsWith(s, '[')) {
            int i = 1;
            while (i < s.length() && s.charAt(i) == '=') ++i;
            if (i < s.length() && s.charAt(i) == '[') {
                String m = s.substring(1, i);
                if (s.endsWith("]" + m + "]"))
                    return s.substring(i+1, s.length()-i-1);
            }
        }

        if (s.length() > 1) {
            char c = s.charAt(0);
            if (c == '\"' || c == '\'') {
                int l = endsWith(s, c) ? s.length()-1 : s.length();
                StringBuilder sb = new StringBuilder(l-1);

                for (int i = 1; i < l; i++) {
                    char ch = s.charAt(i);
                    if (ch == '\\') {
                        char nextChar = (i == l - 1) ? '\\' : s.charAt(i + 1);
                        // Octal escape?
                        if (nextChar >= '0' && nextChar <= '7') {
                            String code = "" + nextChar;
                            i++;
                            if ((i < l - 1) && s.charAt(i + 1) >= '0'
                                    && s.charAt(i + 1) <= '7') {
                                code += s.charAt(i + 1);
                                i++;
                                if ((i < l - 1) && s.charAt(i + 1) >= '0'
                                        && s.charAt(i + 1) <= '7') {
                                    code += s.charAt(i + 1);
                                    i++;
                                }
                            }
                            sb.append((char) Integer.parseInt(code, 8));
                            continue;
                        }
                        switch (nextChar) {
                            case '\"': ch = '\"'; break;
                            case '\\': ch = '\\'; break;
                            case 'b': ch = '\b'; break;
                            case 'f': ch = '\f'; break;
                            case 'n': ch = '\n'; break;
                            case 'r': ch = '\r'; break;
                            case 't': ch = '\t'; break;
                            case '\'': ch = '\''; break;
                            // Hex Unicode: u????
                            case 'u':
                                if (i >= l - 5) {
                                    ch = 'u';
                                    break;
                                }
                                int code = Integer.parseInt(
                                        "" + s.charAt(i + 2) + s.charAt(i + 3)
                                                + s.charAt(i + 4) + s.charAt(i + 5), 16);
                                sb.append(Character.toChars(code));
                                i += 5;
                                continue;
                            default:
                                ch = nextChar; // added by Stefan
                        }
                        i++;
                    }
                    sb.append(ch);
                }
                return sb.toString();
            }
        }

        return s; // not quoted - return original
    }
    static String trimJoinSubList(List<String> l, int i, int j) {
        return trim(join(subList(l, i, j)));
    }

    static String trimJoinSubList(List<String> l, int i) {
        return trim(join(subList(l, i)));
    }
    static boolean isInteger(String s) {
        int n = l(s);
        if (n == 0) return false;
        int i = 0;
        if (s.charAt(0) == '-')
            if (++i >= n) return false;
        while (i < n) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
            ++i;
        }
        return true;
    }
    static <A> Iterator<A> iterator(Iterable<A> c) {
        return c == null ? emptyIterator() : c.iterator();
    }
    static char charAt(String s, int i) {
        return s != null && i >= 0 && i < s.length() ? s.charAt(i) : '\0';
    }
    static boolean nempty(Collection c) {
        return !empty(c);
    }

    static boolean nempty(CharSequence s) {
        return !empty(s);
    }

    static boolean nempty(Object[] o) { return !empty(o); }
    static boolean nempty(byte[] o) { return !empty(o); }
    static boolean nempty(int[] o) { return !empty(o); }

    static boolean nempty(Map m) {
        return !empty(m);
    }

    static boolean nempty(Iterator i) {
        return i != null && i.hasNext();
    }

    static boolean nempty(Object o) { return !empty(o); }
    static Field getOpt_findField(Class<?> c, String field) {
        Class _c = c;
        do {
            for (Field f : _c.getDeclaredFields())
                if (f.getName().equals(field))
                    return f;
            _c = _c.getSuperclass();
        } while (_c != null);
        return null;
    }
    static Field makeAccessible(Field f) {
        try {
            f.setAccessible(true);
        } catch (Throwable e) {
            // Note: The error reporting only works with Java VM option --illegal-access=deny

        }
        return f;
    }

    static Method makeAccessible(Method m) {
        try {
            m.setAccessible(true);
        } catch (Throwable e) {

        }
        return m;
    }

    static Constructor makeAccessible(Constructor c) {
        try {
            c.setAccessible(true);
        } catch (Throwable e) {

        }
        return c;
    }
    static RuntimeException asRuntimeException(Throwable t) {

        return t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
    }
    static RuntimeException rethrow(Throwable t) {

        throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
    }

    static RuntimeException rethrow(String msg, Throwable t) {
        throw new RuntimeException(msg, t);
    }


    static ThreadLocal<Object> print_byThread() {
        synchronized(print_byThread_lock) {
            if (print_byThread == null)
                print_byThread = new ThreadLocal();
        }
        return print_byThread;
    }
    // f can return false to suppress regular printing
// call print_raw within f to actually print something
    static AutoCloseable tempInterceptPrint(F1<String, Boolean> f) {
        return tempSetThreadLocal(print_byThread(), f);
    }
    static boolean fileExists(String path) {
        return path != null && new File(path).exists();
    }

    static boolean fileExists(File f) {
        return f != null && f.exists();
    }
    static String getClassName(Object o) {
        return o == null ? "null" : o instanceof Class ? ((Class) o).getName() : o.getClass().getName();
    }
    static void rotateStringBuffer(StringBuffer buf, int max) { try {
        if (buf == null) return;
        synchronized(buf) {
            if (buf.length() <= max) return;

            try {
                int newLength = max/2;
                int ofs = buf.length()-newLength;
                String newString = buf.substring(ofs);
                buf.setLength(0);
                buf.append("[...] ").append(newString);
            } catch (Exception e) {
                buf.setLength(0);
            }
            buf.trimToSize();
        }
    } catch (Exception __e) { throw rethrow(__e); } }
    static void rotateStringBuilder(StringBuilder buf, int max) { try {
        if (buf == null) return;
        synchronized(buf) {
            if (buf.length() <= max) return;

            try {
                int newLength = max/2;
                int ofs = buf.length()-newLength;
                String newString = buf.substring(ofs);
                buf.setLength(0);
                buf.append("[...] ").append(newString);
            } catch (Exception e) {
                buf.setLength(0);
            }
            buf.trimToSize();
        }
    } catch (Exception __e) { throw rethrow(__e); } }
    static boolean eq(Object a, Object b) {
        return a == b || a != null && b != null && a.equals(b);
    }


    static String str(Object o) {
        return o == null ? "null" : o.toString();
    }

    static String str(char[] c) {
        return new String(c);
    }
// match2 matches multiple "*" (matches a single token) wildcards and zero or one "..." wildcards (matches multiple tokens)

    static String[] match2(List<String> pat, List<String> tok) {
        // standard case (no ...)
        int i = pat.indexOf("...");
        if (i < 0) return match2_match(pat, tok);

        pat = new ArrayList<String>(pat); // We're modifying it, so copy first
        pat.set(i, "*");
        while (pat.size() < tok.size()) {
            pat.add(i, "*");
            pat.add(i+1, ""); // doesn't matter
        }

        return match2_match(pat, tok);
    }

    static String[] match2_match(List<String> pat, List<String> tok) {
        List<String> result = new ArrayList<String>();
        if (pat.size() != tok.size()) {

            return null;
        }
        for (int i = 1; i < pat.size(); i += 2) {
            String p = pat.get(i), t = tok.get(i);

            if (eq(p, "*"))
                result.add(t);
            else if (!equalsIgnoreCase(unquote(p), unquote(t))) // bold change - match quoted and unquoted now. TODO: should remove
                return null;
        }
        return result.toArray(new String[result.size()]);
    }

    static String dropSuffix(String suffix, String s) {
        return s.endsWith(suffix) ? s.substring(0, l(s)-l(suffix)) : s;
    }
    static <A> int iteratorCount_int_close(Iterator<A> i) { try {
        int n = 0;
        if (i != null) while (i.hasNext()) { i.next(); ++n; }
        if (i instanceof AutoCloseable) ((AutoCloseable) i).close();
        return n;
    } catch (Exception __e) { throw rethrow(__e); } }
    static Object call(Object o) {
        return callF(o);
    }

    // varargs assignment fixer for a single string array argument
    static Object call(Object o, String method, String[] arg) {
        return call(o, method, new Object[] {arg});
    }

    static Object call(Object o, String method, Object... args) {
        //ret call_cached(o, method, args);
        return call_withVarargs(o, method, args);
    }
    static int lastIndexOf_byteArray(byte[] a, byte b) {
        for (int i = l(a)-1; i >=0; i--)
            if (a[i] == b)
                return i;
        return -1;
    }
    static int indexOf_byteArray(byte[] a, byte b) {
        int n = l(a);
        for (int i = 0; i < n; i++)
            if (a[i] == b)
                return i;
        return -1;
    }
    static int cmp(Number a, Number b) {
        return a == null ? b == null ? 0 : -1 : cmp(a.doubleValue(), b.doubleValue());
    }

    static int cmp(double a, double b) {
        return a < b ? -1 : a == b ? 0 : 1;
    }

    static int cmp(Object a, Object b) {
        if (a == null) return b == null ? 0 : -1;
        if (b == null) return 1;
        return ((Comparable) a).compareTo(b);
    }
    static String javaTok_substringN(String s, int i, int j) {
        if (i == j) return "";
        if (j == i+1 && s.charAt(i) == ' ') return " ";
        return s.substring(i, j);
    }
    static String javaTok_substringC(String s, int i, int j) {
        return s.substring(i, j);
    }
    static List<String> javaTokWithExisting(String s, List<String> existing) {
        ++javaTok_n;
        int nExisting = javaTok_opt && existing != null ? existing.size() : 0;
        ArrayList<String> tok = existing != null ? new ArrayList(nExisting) : new ArrayList();
        int l = s.length();

        int i = 0, n = 0;
        while (i < l) {
            int j = i;
            char c, d;

            // scan for whitespace
            while (j < l) {
                c = s.charAt(j);
                d = j+1 >= l ? '\0' : s.charAt(j+1);
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
                    ++j;
                else if (c == '/' && d == '*') {
                    do ++j; while (j < l && !s.substring(j, Math.min(j+2, l)).equals("*/"));
                    j = Math.min(j+2, l);
                } else if (c == '/' && d == '/') {
                    do ++j; while (j < l && "\r\n".indexOf(s.charAt(j)) < 0);
                } else
                    break;
            }

            if (n < nExisting && javaTokWithExisting_isCopyable(existing.get(n), s, i, j))
                tok.add(existing.get(n));
            else
                tok.add(javaTok_substringN(s, i, j));
            ++n;
            i = j;
            if (i >= l) break;
            c = s.charAt(i);
            d = i+1 >= l ? '\0' : s.charAt(i+1);

            // scan for non-whitespace

            // Special JavaX syntax: 'identifier
            if (c == '\'' && Character.isJavaIdentifierStart(d) && i+2 < l && "'\\".indexOf(s.charAt(i+2)) < 0) {
                j += 2;
                while (j < l && Character.isJavaIdentifierPart(s.charAt(j)))
                    ++j;
            } else if (c == '\'' || c == '"') {
                char opener = c;
                ++j;
                while (j < l) {
                    if (s.charAt(j) == opener /*|| s.charAt(j) == '\n'*/) { // allow multi-line strings
                        ++j;
                        break;
                    } else if (s.charAt(j) == '\\' && j+1 < l)
                        j += 2;
                    else
                        ++j;
                }
            } else if (Character.isJavaIdentifierStart(c))
                do ++j; while (j < l && (Character.isJavaIdentifierPart(s.charAt(j)) || "'".indexOf(s.charAt(j)) >= 0)); // for stuff like "don't"
            else if (Character.isDigit(c)) {
                do ++j; while (j < l && Character.isDigit(s.charAt(j)));
                if (j < l && s.charAt(j) == 'L') ++j; // Long constants like 1L
            } else if (c == '[' && d == '[') {
                do ++j; while (j+1 < l && !s.substring(j, j+2).equals("]]"));
                j = Math.min(j+2, l);
            } else if (c == '[' && d == '=' && i+2 < l && s.charAt(i+2) == '[') {
                do ++j; while (j+2 < l && !s.substring(j, j+3).equals("]=]"));
                j = Math.min(j+3, l);
            } else
                ++j;

            if (n < nExisting && javaTokWithExisting_isCopyable(existing.get(n), s, i, j))
                tok.add(existing.get(n));
            else
                tok.add(javaTok_substringC(s, i, j));
            ++n;
            i = j;
        }

        if ((tok.size() % 2) == 0) tok.add("");
        javaTok_elements += tok.size();
        return tok;
    }

    static boolean javaTokWithExisting_isCopyable(String t, String s, int i, int j) {
        return t.length() == j-i
                && s.regionMatches(i, t, 0, j-i); // << could be left out, but that's brave
    }
    public static <A> String join(String glue, Iterable<A> strings) {
        if (strings == null) return "";
        if (strings instanceof Collection) {
            if (((Collection) strings).size() == 1) return str(first(((Collection) strings)));
        }
        StringBuilder buf = new StringBuilder();
        Iterator<A> i = strings.iterator();
        if (i.hasNext()) {
            buf.append(i.next());
            while (i.hasNext())
                buf.append(glue).append(i.next());
        }
        return buf.toString();
    }

    public static String join(String glue, String... strings) {
        return join(glue, Arrays.asList(strings));
    }

    static <A> String join(Iterable<A> strings) {
        return join("", strings);
    }

    static <A> String join(Iterable<A> strings, String glue) {
        return join(glue, strings);
    }

    public static String join(String[] strings) {
        return join("", strings);
    }


    static String join(String glue, Pair p) {
        return p == null ? "" : str(p.a) + glue + str(p.b);
    }

    static int min(int a, int b) {
        return Math.min(a, b);
    }

    static long min(long a, long b) {
        return Math.min(a, b);
    }

    static float min(float a, float b) { return Math.min(a, b); }
    static float min(float a, float b, float c) { return min(min(a, b), c); }

    static double min(double a, double b) {
        return Math.min(a, b);
    }

    static double min(double[] c) {
        double x = Double.MAX_VALUE;
        for (double d : c) x = Math.min(x, d);
        return x;
    }

    static float min(float[] c) {
        float x = Float.MAX_VALUE;
        for (float d : c) x = Math.min(x, d);
        return x;
    }

    static byte min(byte[] c) {
        byte x = 127;
        for (byte d : c) if (d < x) x = d;
        return x;
    }

    static short min(short[] c) {
        short x = 0x7FFF;
        for (short d : c) if (d < x) x = d;
        return x;
    }

    static int min(int[] c) {
        int x = Integer.MAX_VALUE;
        for (int d : c) if (d < x) x = d;
        return x;
    }
    static <A> int indexOf(List<A> l, A a, int startIndex) {
        if (l == null) return -1;
        int n = l(l);
        for (int i = startIndex; i < n; i++)
            if (eq(l.get(i), a))
                return i;
        return -1;
    }

    static <A> int indexOf(List<A> l, int startIndex, A a) {
        return indexOf(l, a, startIndex);
    }

    static <A> int indexOf(List<A> l, A a) {
        if (l == null) return -1;
        return l.indexOf(a);
    }

    static int indexOf(String a, String b) {
        return a == null || b == null ? -1 : a.indexOf(b);
    }

    static int indexOf(String a, String b, int i) {
        return a == null || b == null ? -1 : a.indexOf(b, i);
    }

    static int indexOf(String a, char b) {
        return a == null ? -1 : a.indexOf(b);
    }

    static int indexOf(String a, int i, char b) {
        return indexOf(a, b, i);
    }

    static int indexOf(String a, char b, int i) {
        return a == null ? -1 : a.indexOf(b, i);
    }

    static int indexOf(String a, int i, String b) {
        return a == null || b == null ? -1 : a.indexOf(b, i);
    }

    static <A> int indexOf(A[] x, A a) {
        int n = l(x);
        for (int i = 0; i < n; i++)
            if (eq(x[i], a))
                return i;
        return -1;
    }
    static boolean startsWith(String a, String b) {
        return a != null && a.startsWith(b);
    }

    static boolean startsWith(String a, char c) {
        return nemptyString(a) && a.charAt(0) == c;
    }


    static boolean startsWith(String a, String b, Matches m) {
        if (!startsWith(a, b)) return false;
        m.m = new String[] {substring(a, strL(b))};
        return true;
    }


    static boolean startsWith(List a, List b) {
        if (a == null || listL(b) > listL(a)) return false;
        for (int i = 0; i < listL(b); i++)
            if (neq(a.get(i), b.get(i)))
                return false;
        return true;
    }


    static boolean endsWith(String a, String b) {
        return a != null && a.endsWith(b);
    }

    static boolean endsWith(String a, char c) {
        return nempty(a) && lastChar(a) == c;
    }


    static boolean endsWith(String a, String b, Matches m) {
        if (!endsWith(a, b)) return false;
        m.m = new String[] {dropLast(l(b), a)};
        return true;
    }


    static String trim(String s) { return s == null ? null : s.trim(); }
    static String trim(StringBuilder buf) { return buf.toString().trim(); }
    static String trim(StringBuffer buf) { return buf.toString().trim(); }
    static <A> List<A> subList(List<A> l, int startIndex) {
        return subList(l, startIndex, l(l));
    }

    static <A> List<A> subList(int startIndex, int endIndex, List<A> l) {
        return subList(l, startIndex, endIndex);
    }

    static <A> List<A> subList(List<A> l, int startIndex, int endIndex) {
        if (l == null) return null;
        int n = l(l);
        startIndex = Math.max(0, startIndex);
        endIndex = Math.min(n, endIndex);
        if (startIndex >= endIndex) return ll();
        if (startIndex == 0 && endIndex == n) return l;
        return l.subList(startIndex, endIndex);
    }


    static Iterator emptyIterator() {
        return Collections.emptyIterator();
    }


    static <A> AutoCloseable tempSetThreadLocal(final ThreadLocal<A> tl, A a) {
        if (tl == null) return null;
        final A prev = setThreadLocal(tl, a);
        return new AutoCloseable() { public String toString() { return "tl.set(prev);"; } public void close() throws Exception { tl.set(prev); }};
    }
    static boolean equalsIgnoreCase(String a, String b) {
        return eqic(a, b);
    }

    static boolean equalsIgnoreCase(char a, char b) {
        return eqic(a, b);
    }
    static Map<Class, ArrayList<Method>> callF_cache = newDangerousWeakHashMap();




    static <A, B> B callF(F1<A, B> f, A a) {
        return f == null ? null : f.get(a);
    }





    static <A, B> B callF(IF1<A, B> f, A a) {
        return f == null ? null : f.get(a);
    }








    static Object callF(Object f, Object... args) { try {
        if (f instanceof String)
            return callMCWithVarArgs((String) f, args); // possible SLOWDOWN over callMC
        if (f instanceof Runnable) {
            ((Runnable) f).run();
            return null;
        }
        if (f == null) return null;

        Class c = f.getClass();
        ArrayList<Method> methods;
        synchronized(callF_cache) {
            methods = callF_cache.get(c);
            if (methods == null)
                methods = callF_makeCache(c);
        }

        int n = l(methods);
        if (n == 0) {

            throw fail("No get method in " + getClassName(c));
        }
        if (n == 1) return invokeMethod(methods.get(0), f, args);
        for (int i = 0; i < n; i++) {
            Method m = methods.get(i);
            if (call_checkArgs(m, args, false))
                return invokeMethod(m, f, args);
        }
        throw fail("No matching get method in " + getClassName(c));
    } catch (Exception __e) { throw rethrow(__e); } }

    // used internally
    static ArrayList<Method> callF_makeCache(Class c) {
        ArrayList<Method> l = new ArrayList();
        Class _c = c;
        do {
            for (Method m : _c.getDeclaredMethods())
                if (m.getName().equals("get")) {
                    makeAccessible(m);
                    l.add(m);
                }
            if (!l.isEmpty()) break;
            _c = _c.getSuperclass();
        } while (_c != null);
        callF_cache.put(c, l);
        return l;
    }
    static Object call_withVarargs(Object o, String method, Object... args) { try {
        if (o == null) return null;

        if (o instanceof Class) {
            Class c = (Class) o;
            _MethodCache cache = callOpt_getCache(c);

            Method me = cache.findStaticMethod(method, args);
            if (me != null)
                return invokeMethod(me, null, args);

            // try varargs
            List<Method> methods = cache.cache.get(method);
            if (methods != null) methodSearch: for (Method m : methods) {
                { if (!(m.isVarArgs())) continue; }
                { if (!(isStaticMethod(m))) continue; }
                Object[] newArgs = massageArgsForVarArgsCall(m, args);
                if (newArgs != null)
                    return invokeMethod(m, null, newArgs);
            }

            throw fail("Method " + c.getName() + "." + method + "(" + joinWithComma(classNames(args)) + ") not found");
        } else {
            Class c = o.getClass();
            _MethodCache cache = callOpt_getCache(c);

            Method me = cache.findMethod(method, args);
            if (me != null)
                return invokeMethod(me, o, args);

            // try varargs
            List<Method> methods = cache.cache.get(method);
            if (methods != null) methodSearch: for (Method m : methods) {
                { if (!(m.isVarArgs())) continue; }
                Object[] newArgs = massageArgsForVarArgsCall(m, args);
                if (newArgs != null)
                    return invokeMethod(m, o, newArgs);
            }

            throw fail("Method " + c.getName() + "." + method + "(" + joinWithComma(classNames(args)) + ") not found");
        }
    } catch (Exception __e) { throw rethrow(__e); } }
    static boolean nemptyString(String s) {
        return s != null && s.length() > 0;
    }
    static String substring(String s, int x) {
        return substring(s, x, strL(s));
    }

    static String substring(String s, int x, int y) {
        if (s == null) return null;
        if (x < 0) x = 0;
        if (x >= s.length()) return "";
        if (y < x) y = x;
        if (y > s.length()) y = s.length();
        return s.substring(x, y);
    }



    // convenience method for quickly dropping a prefix
    static String substring(String s, CharSequence l) {
        return substring(s, l(l));
    }
    static int strL(String s) {
        return s == null ? 0 : s.length();
    }
    static int listL(Collection l) {
        return l == null ? 0 : l.size();
    }
    static boolean neq(Object a, Object b) {
        return !eq(a, b);
    }
    static char lastChar(String s) {
        return empty(s) ? '\0' : s.charAt(l(s)-1);
    }
    static String[] dropLast(String[] a, int n) {
        n = Math.min(n, a.length);
        String[] b = new String[a.length-n];
        System.arraycopy(a, 0, b, 0, b.length);
        return b;
    }

    static <A> List<A> dropLast(List<A> l) {
        return subList(l, 0, l(l)-1);
    }

    static <A> List<A> dropLast(int n, List<A> l) {
        return subList(l, 0, l(l)-n);
    }

    static <A> List<A> dropLast(Iterable<A> l) {
        return dropLast(asList(l));
    }

    static String dropLast(String s) {
        return substring(s, 0, l(s)-1);
    }

    static String dropLast(String s, int n) {
        return substring(s, 0, l(s)-n);
    }

    static String dropLast(int n, String s) {
        return dropLast(s, n);
    }

    static <A> List<A> ll(A... a) {
        ArrayList l = new ArrayList(a.length);
        if (a != null) for (A x : a) l.add(x);
        return l;
    }


    static <A> A setThreadLocal(ThreadLocal<A> tl, A value) {
        if (tl == null) return null;
        A old = tl.get();
        tl.set(value);
        return old;
    }
    static boolean eqic(String a, String b) {


        if ((a == null) != (b == null)) return false;
        if (a == null) return true;
        return a.equalsIgnoreCase(b);

    }



    static boolean eqic(char a, char b) {
        if (a == b) return true;

        char u1 = Character.toUpperCase(a);
        char u2 = Character.toUpperCase(b);
        if (u1 == u2) return true;

        return Character.toLowerCase(u1) == Character.toLowerCase(u2);
    }
    static <A, B> Map<A, B> newDangerousWeakHashMap() {
        return _registerDangerousWeakMap(synchroMap(new WeakHashMap()));
    }

    // initFunction: voidfunc(Map) - is called initially, and after clearing the map
    static <A, B> Map<A, B> newDangerousWeakHashMap(Object initFunction) {
        return _registerDangerousWeakMap(synchroMap(new WeakHashMap()), initFunction);
    }
    static Object callMCWithVarArgs(String method, Object... args) {
        return call_withVarargs(mc(), method, args);
    }
    static Object invokeMethod(Method m, Object o, Object... args) { try {
        try {
            return m.invoke(o, args);
        } catch (InvocationTargetException e) {
            throw rethrow(getExceptionCause(e));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage() + " - was calling: " + m + ", args: " + joinWithSpace(classNames(args)));
        }
    } catch (Exception __e) { throw rethrow(__e); } }
    static boolean call_checkArgs(Method m, Object[] args, boolean debug) {
        Class<?>[] types = m.getParameterTypes();
        if (types.length != args.length) {
            if (debug)
                print("Bad parameter length: " + args.length + " vs " + types.length);
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            Object arg = args[i];
            if (!(arg == null ? !types[i].isPrimitive()
                    : isInstanceX(types[i], arg))) {
                if (debug)
                    print("Bad parameter " + i + ": " + arg + " vs " + types[i]);
                return false;
            }
        }
        return true;
    }
    static final Map<Class, _MethodCache> callOpt_cache = newDangerousWeakHashMap();

    static Object callOpt_cached(Object o, String methodName, Object... args) { try {
        if (o == null) return null;

        if (o instanceof Class) {
            Class c = (Class) o;
            _MethodCache cache = callOpt_getCache(c);

            // TODO: (super-rare) case where method exists static and non-static
            // with different args

            Method me = cache.findMethod(methodName, args);
            if (me == null || (me.getModifiers() & Modifier.STATIC) == 0) return null;
            return invokeMethod(me, null, args);
        } else {
            Class c = o.getClass();
            _MethodCache cache = callOpt_getCache(c);

            Method me = cache.findMethod(methodName, args);
            if (me == null) return null;
            return invokeMethod(me, o, args);
        }
    } catch (Exception __e) { throw rethrow(__e); } }

    static _MethodCache callOpt_getCache(Class c) {
        synchronized(callOpt_cache) {
            _MethodCache cache = callOpt_cache.get(c);
            if (cache == null)
                callOpt_cache.put(c, cache = new _MethodCache(c));
            return cache;
        }
    }
    static boolean isStaticMethod(Method m) {
        return methodIsStatic(m);
    }
    static Object[] massageArgsForVarArgsCall(Method m, Object[] args) {
        Class<?>[] types = m.getParameterTypes();
        int n = types.length-1, nArgs = args.length;
        if (nArgs < n) return null;
        for (int i = 0; i < n; i++)
            if (!argumentCompatibleWithType(args[i], types[i]))
                return null;
        Class varArgType = types[n].getComponentType();
        for (int i = n; i < nArgs; i++)
            if (!argumentCompatibleWithType(args[i], varArgType))
                return null;
        Object[] newArgs = new Object[n+1];
        arraycopy(args, 0, newArgs, 0, n);
        Object[] varArgs = arrayOfType(varArgType, nArgs-n);
        arraycopy(args, n, varArgs, 0, nArgs-n);
        newArgs[n] = varArgs;
        return newArgs;
    }
    static <A> String joinWithComma(Collection<A> c) {
        return join(", ", c);
    }

    static String joinWithComma(String... c) {
        return join(", ", c);
    }


    static String joinWithComma(Pair p) {
        return p == null ? "" : joinWithComma(str(p.a), str(p.b));
    }

    static List<String> classNames(Collection l) {
        return getClassNames(l);
    }

    static List<String> classNames(Object[] l) {
        return getClassNames(Arrays.asList(l));
    }
// unclear semantics as to whether return null on null

    static <A> ArrayList<A> asList(A[] a) {
        return a == null ? new ArrayList<A>() : new ArrayList<A>(Arrays.asList(a));
    }

    static ArrayList<Integer> asList(int[] a) {
        if (a == null) return null;
        ArrayList<Integer> l = emptyList(a.length);
        for (int i : a) l.add(i);
        return l;
    }

    static ArrayList<Float> asList(float[] a) {
        if (a == null) return null;
        ArrayList<Float> l = emptyList(a.length);
        for (float i : a) l.add(i);
        return l;
    }

    static ArrayList<Double> asList(double[] a) {
        if (a == null) return null;
        ArrayList<Double> l = emptyList(a.length);
        for (double i : a) l.add(i);
        return l;
    }

    static <A> ArrayList<A> asList(Iterable<A> s) {
        if (s instanceof ArrayList) return (ArrayList) s;
        ArrayList l = new ArrayList();
        if (s != null)
            for (A a : s)
                l.add(a);
        return l;
    }



    static <A> ArrayList<A> asList(Enumeration<A> e) {
        ArrayList l = new ArrayList();
        if (e != null)
            while (e.hasMoreElements())
                l.add(e.nextElement());
        return l;
    }




    static String asString(Object o) {
        return o == null ? null : o.toString();
    }
    static List<Pair> _registerDangerousWeakMap_preList;

    static <A> A _registerDangerousWeakMap(A map) {
        return _registerDangerousWeakMap(map, null);
    }

    static <A> A _registerDangerousWeakMap(A map, Object init) {

        return map;
    }

    static void _onLoad_registerDangerousWeakMap() {

    }
    static Map synchroMap() {
        return synchroHashMap();
    }

    static <A, B> Map<A, B> synchroMap(Map<A, B> map) {
        return Collections.synchronizedMap(map);
    }
    static Class mc() {
        return ip2c.class;
    }
    static Throwable getExceptionCause(Throwable e) {
        Throwable c = e.getCause();
        return c != null ? c : e;
    }
    static String joinWithSpace(Iterable c) {
        return join(" ", c);
    }

    static String joinWithSpace(String... c) {
        return join(" ", c);
    }

    static boolean isInstanceX(Class type, Object arg) {
        if (type == boolean.class) return arg instanceof Boolean;
        if (type == int.class) return arg instanceof Integer;
        if (type == long.class) return arg instanceof Long;
        if (type == float.class) return arg instanceof Float;
        if (type == short.class) return arg instanceof Short;
        if (type == char.class) return arg instanceof Character;
        if (type == byte.class) return arg instanceof Byte;
        if (type == double.class) return arg instanceof Double;
        return type.isInstance(arg);
    }
    static boolean methodIsStatic(Method m) {
        return (m.getModifiers() & Modifier.STATIC) != 0;
    }
    static boolean argumentCompatibleWithType(Object arg, Class type) {
        return arg == null ? !type.isPrimitive() : isInstanceX(type, arg);
    }
    static void arraycopy(Object[] a, Object[] b) {
        if (a != null && b != null)
            arraycopy(a, 0, b, 0, min(a.length, b.length));
    }

    static void arraycopy(Object src, int srcPos, Object dest, int destPos, int n) {
        if (n != 0)
            System.arraycopy(src, srcPos, dest, destPos, n);
    }
    static <A> A[] arrayOfType(Class<A> type, int n) {
        return makeArray(type, n);
    }

    static <A> A[] arrayOfType(int n, Class<A> type) {
        return arrayOfType(type, n);
    }
    static List<String> getClassNames(Collection l) {
        List<String> out = new ArrayList();
        if (l != null) for (Object o : l)
            out.add(o == null ? null : getClassName(o));
        return out;
    }
    static ArrayList emptyList() {
        return new ArrayList();
        //ret Collections.emptyList();
    }

    static ArrayList emptyList(int capacity) {
        return new ArrayList(max(0, capacity));
    }

    // Try to match capacity
    static ArrayList emptyList(Iterable l) {
        return l instanceof Collection ? emptyList(((Collection) l).size()) : emptyList();
    }

    static ArrayList emptyList(Object[] l) {
        return emptyList(l(l));
    }

    // get correct type at once
    static <A> ArrayList<A> emptyList(Class<A> c) {
        return new ArrayList();
    }


    static Map synchroHashMap() {
        return Collections.synchronizedMap(new HashMap());
    }

    static <A> A[] makeArray(Class<A> type, int n) {
        return (A[]) Array.newInstance(type, n);
    }
    static int max(int a, int b) { return Math.max(a, b); }
    static int max(int a, int b, int c) { return max(max(a, b), c); }
    static long max(int a, long b) { return Math.max((long) a, b); }
    static long max(long a, long b) { return Math.max(a, b); }
    static double max(int a, double b) { return Math.max((double) a, b); }
    static float max(float a, float b) { return Math.max(a, b); }
    static double max(double a, double b) { return Math.max(a, b); }

    static int max(Collection<Integer> c) {
        int x = Integer.MIN_VALUE;
        for (int i : c) x = max(x, i);
        return x;
    }

    static double max(double[] c) {
        if (c.length == 0) return Double.MIN_VALUE;
        double x = c[0];
        for (int i = 1; i < c.length; i++) x = Math.max(x, c[i]);
        return x;
    }

    static float max(float[] c) {
        if (c.length == 0) return Float.MAX_VALUE;
        float x = c[0];
        for (int i = 1; i < c.length; i++) x = Math.max(x, c[i]);
        return x;
    }

    static byte max(byte[] c) {
        byte x = -128;
        for (byte d : c) if (d > x) x = d;
        return x;
    }

    static short max(short[] c) {
        short x = -0x8000;
        for (short d : c) if (d > x) x = d;
        return x;
    }

    static int max(int[] c) {
        int x = Integer.MIN_VALUE;
        for (int d : c) if (d > x) x = d;
        return x;
    }


    // immutable, has strong refs
    final static class _MethodCache {
        final Class c;
        final HashMap<String, List<Method>> cache = new HashMap();

        _MethodCache(Class c) {
            this.c = c; _init(); }

        void _init() {
            Class _c = c;
            while (_c != null) {
                for (Method m : _c.getDeclaredMethods())
                    if (!isAbstract(m) && !reflection_isForbiddenMethod(m))
                        multiMapPut(cache, m.getName(), makeAccessible(m));
                _c = _c.getSuperclass();
            }

            // add default methods - this might lead to a duplication
            // because the overridden method is also added, but it's not
            // a problem except for minimal performance loss.
            for (Class intf : allInterfacesImplementedBy(c))
                for (Method m : intf.getDeclaredMethods())
                    if (m.isDefault() && !reflection_isForbiddenMethod(m))
                        multiMapPut(cache, m.getName(), makeAccessible(m));


        }

        // Returns only matching methods
        Method findMethod(String method, Object[] args) { try {
            List<Method> m = cache.get(method);

            if (m == null) return null;
            int n = m.size();
            for (int i = 0; i < n; i++) {
                Method me = m.get(i);
                if (call_checkArgs(me, args, false))
                    return me;
            }
            return null;
        } catch (Exception __e) { throw rethrow(__e); } }

        Method findStaticMethod(String method, Object[] args) { try {
            List<Method> m = cache.get(method);
            if (m == null) return null;
            int n = m.size();
            for (int i = 0; i < n; i++) {
                Method me = m.get(i);
                if (isStaticMethod(me) && call_checkArgs(me, args, false))
                    return me;
            }
            return null;
        } catch (Exception __e) { throw rethrow(__e); } }
    }final static class LongRange {
        long start, end;

        LongRange() {}
        LongRange(long start, long end) {
            this.end = end;
            this.start = start;}

        public boolean equals(Object o) {
            if (o instanceof LongRange) return start == ((LongRange) o).start && end == ((LongRange) o).end;
            return false;
        }

        public int hashCode() {
            return boostHashCombine(hashOfLong(start), hashOfLong(end));
        }

        long length() { return end-start; }

        static String _fieldOrder = "start end";

        public String toString() { return "[" + start + ";" + end + "]"; }
    }static class Matches {
        String[] m;

        Matches() {}
        Matches(String... m) {
            this.m = m;}

        String get(int i) { return i < m.length ? m[i] : null; }
        String unq(int i) { return unquote(get(i)); }

        String tlc(int i) { return unq(i).toLowerCase(); }
        boolean bool(int i) { return "true".equals(unq(i)); }
        String rest() { return m[m.length-1]; } // for matchStart
        int psi(int i) { return Integer.parseInt(unq(i)); }

        public String toString() { return "Matches(" + joinWithComma(quoteAll(asList(m))) + ")"; }

        public int hashCode() { return _hashCode(toList(m)); }
        public boolean equals(Object o) { return o instanceof Matches && arraysEqual(m, ((Matches) o).m); }
    }
    static interface IF1<A, B> {
        B get(A a);
    }static abstract class F1<A, B> {
        abstract B get(A a);
    }static class Pair<A, B> implements Comparable<Pair<A, B>> {
        A a;
        B b;

        Pair() {}
        Pair(A a, B b) {
            this.b = b;
            this.a = a;}

        public int hashCode() {
            return hashCodeFor(a) + 2*hashCodeFor(b);
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Pair)) return false;
            Pair t = (Pair) o;
            return eq(a, t.a) && eq(b, t.b);
        }

        public String toString() {
            return "<" + a + ", " + b + ">";
        }

        public int compareTo(Pair<A, B> p) {
            if (p == null) return 1;
            int i = ((Comparable<A>) a).compareTo(p.a);
            if (i != 0) return i;
            return ((Comparable<B>) b).compareTo(p.b);
        }
    }

    static boolean isAbstract(Class c) {
        return (c.getModifiers() & Modifier.ABSTRACT) != 0;
    }

    static boolean isAbstract(Method m) {
        return (m.getModifiers() & Modifier.ABSTRACT) != 0;
    }
    static boolean reflection_isForbiddenMethod(Method m) {
        return m.getDeclaringClass() == Object.class
                && eqOneOf(m.getName(), "finalize", "clone", "registerNatives");
    }
    static <A, B> void multiMapPut(Map<A, List<B>> map, A a, B b) {
        List<B> l = map.get(a);
        if (l == null)
            map.put(a, l = new ArrayList());
        l.add(b);
    }


    static Set<Class> allInterfacesImplementedBy(Class c) {
        if (c == null) return null;
        HashSet<Class> set = new HashSet();
        allInterfacesImplementedBy_find(c, set);
        return set;
    }

    static void allInterfacesImplementedBy_find(Class c, Set<Class> set) {
        if (c.isInterface() && !set.add(c)) return;
        do {
            for (Class intf : c.getInterfaces())
                allInterfacesImplementedBy_find(intf, set);
        } while ((c = c.getSuperclass()) != null);
    }
    static Method findMethod(Object o, String method, Object... args) {
        return findMethod_cached(o, method, args);
    }

    static boolean findMethod_checkArgs(Method m, Object[] args, boolean debug) {
        Class<?>[] types = m.getParameterTypes();
        if (types.length != args.length) {
            if (debug)
                System.out.println("Bad parameter length: " + args.length + " vs " + types.length);
            return false;
        }
        for (int i = 0; i < types.length; i++)
            if (!(args[i] == null || isInstanceX(types[i], args[i]))) {
                if (debug)
                    System.out.println("Bad parameter " + i + ": " + args[i] + " vs " + types[i]);
                return false;
            }
        return true;
    }
    static Method findStaticMethod(Class c, String method, Object... args) {
        Class _c = c;
        while (c != null) {
            for (Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(method))
                    continue;

                if ((m.getModifiers() & Modifier.STATIC) == 0 || !findStaticMethod_checkArgs(m, args))
                    continue;

                return m;
            }
            c = c.getSuperclass();
        }
        return null;
    }

    static boolean findStaticMethod_checkArgs(Method m, Object[] args) {
        Class<?>[] types = m.getParameterTypes();
        if (types.length != args.length)
            return false;
        for (int i = 0; i < types.length; i++)
            if (!(args[i] == null || isInstanceX(types[i], args[i])))
                return false;
        return true;
    }

    static int boostHashCombine(int a, int b) {
        return a ^ (b + 0x9e3779b9 + (a << 6) + (a >> 2));
    }
    static int hashOfLong(long l) {
        return Long.hashCode(l);
    }
    static int length(Object[] array) {
        return array == null ? 0 : array.length;
    }

    static int length(List list) {
        return list == null ? 0 : list.size();
    }

    static int length(String s) {
        return s == null ? 0 : s.length();
    }
    static List<String> quoteAll(Collection<String> l) {
        List<String> x = new ArrayList();
        for (String s : l)
            x.add(quote(s));
        return x;
    }
    static int _hashCode(Object a) {
        return a == null ? 0 : a.hashCode();
    }
    static <A> ArrayList<A> toList(A[] a) { return asList(a); }
    static ArrayList<Integer> toList(int[] a) { return asList(a); }
    static <A> ArrayList<A> toList(Set<A> s) { return asList(s); }
    static <A> ArrayList<A> toList(Iterable<A> s) { return asList(s); }
    static boolean arraysEqual(Object[] a, Object[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++)
            if (neq(a[i], b[i])) return false;
        return true;
    }
    static int hashCodeFor(Object a) {
        return a == null ? 0 : a.hashCode();
    }


    static boolean eqOneOf(Object o, Object... l) {
        for (Object x : l) if (eq(o, x)) return true; return false;
    }
    static Method findMethod_cached(Object o, String method, Object... args) { try {
        if (o == null) return null;
        if (o instanceof Class) {
            _MethodCache cache = callOpt_getCache(((Class) o));
            List<Method> methods = cache.cache.get(method);
            if (methods != null) for (Method m : methods)
                if (isStaticMethod(m) && findMethod_checkArgs(m, args, false))
                    return m;
            return null;
        } else {
            _MethodCache cache = callOpt_getCache(o.getClass());
            List<Method> methods = cache.cache.get(method);
            if (methods != null) for (Method m : methods)
                if (findMethod_checkArgs(m, args, false))
                    return m;
            return null;
        }
    } catch (Exception __e) { throw rethrow(__e); } }

    static String quote(Object o) {
        if (o == null) return "null";
        return quote(str(o));
    }

    static String quote(String s) {
        if (s == null) return "null";
        StringBuilder out = new StringBuilder((int) (l(s)*1.5+2));
        quote_impl(s, out);
        return out.toString();
    }

    static void quote_impl(String s, StringBuilder out) {
        out.append('"');
        int l = s.length();
        for (int i = 0; i < l; i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '"')
                out.append('\\').append(c);
            else if (c == '\r')
                out.append("\\r");
            else if (c == '\n')
                out.append("\\n");
            else if (c == '\0')
                out.append("\\0");
            else
                out.append(c);
        }
        out.append('"');
    }

}