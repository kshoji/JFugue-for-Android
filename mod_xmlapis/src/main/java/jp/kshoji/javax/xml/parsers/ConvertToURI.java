/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999-2001, Sun Microsystems,
 * Inc., http://www.sun.com.  For more information on the Apache Software
 * Foundation, please see <http://www.apache.org/>.
 */

package jp.kshoji.javax.xml.parsers;

import java.io.File;
/**
 * It escapes character sequences that doesn't correspond to printable character of US-ASCII coded character
 * set [00-1F and 7F hexadecimal] or any US-ASCII character that is disallowed like space ' '
 * As of right now this class doesn't not handle  non-US ASCII character. But it may be extended in future to
 * handle those cases. Most of the code in this class has been extracted from Sun JDK 1.4 java.net.URI.
 *
 * @author Neeraj Bajaj
 */



class ConvertToURI{

    // RFC2396 precisely specifies which characters in the US-ASCII charset are
    // permissible in the various components of a URI reference.  We here
    // define a set of mask pairs to aid in enforcing these restrictions.  Each
    // mask pair consists of two longs, a low mask and a high mask.  Taken
    // together they represent a 128-bit mask, where bit i is set iff the
    // character with value i is permitted.
    //
    // This approach is more efficient than sequentially searching arrays of
    // permitted characters.  It could be made still more efficient by
    // precompiling the mask information so that a character's presence in a
    // given mask could be determined by a single table lookup.

    // Compute the low-order mask for the characters in the given string
    private static long lowMask(String chars) {
        int n = chars.length();
        long m = 0;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if (c < 64)
            m |= (1L << c);
        }
        return m;

    }

    // Compute the high-order mask for the characters in the given string
    private static long highMask(String chars) {
        int n = chars.length();
        long m = 0;
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);
            if ((c >= 64) && (c < 128))
            m |= (1L << (c - 64));
        }
        return m;
    }

    // Compute a low-order mask for the characters
    // between first and last, inclusive
    private static long lowMask(char first, char last) {
        long m = 0;
        int f = Math.max(Math.min(first, 63), 0);
        int l = Math.max(Math.min(last, 63), 0);
        for (int i = f; i <= l; i++)
            m |= 1L << i;
        return m;
    }

    // Compute a high-order mask for the characters
    // between first and last, inclusive
    private static long highMask(char first, char last) {
        long m = 0;
        int f = Math.max(Math.min(first, 127), 64) - 64;
        int l = Math.max(Math.min(last, 127), 64) - 64;
        for (int i = f; i <= l; i++)
            m |= 1L << i;
        return m;
    }

    // Tell whether the given character is permitted by the given mask pair
    private static boolean match(char c, long lowMask, long highMask) {
        if (c < 64)
            return ((1L << c) & lowMask) != 0;
        if (c < 128)
            return ((1L << (c - 64)) & highMask) != 0;
        return false;
    }

    // Character-class masks, in reverse order from RFC2396 because
    // initializers for static fields cannot make forward references.

    // digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
    //            "8" | "9"
    private static final long L_DIGIT = lowMask('0', '9');
    private static final long H_DIGIT = 0L;

    // upalpha  = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" |
    //            "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" |
    //            "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
    private static final long L_UPALPHA = 0L;
    private static final long H_UPALPHA = highMask('A', 'Z');

    // lowalpha = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" |
    //            "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" |
    //            "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
    private static final long L_LOWALPHA = 0L;
    private static final long H_LOWALPHA = highMask('a', 'z');

    // alpha         = lowalpha | upalpha
    private static final long L_ALPHA = L_LOWALPHA | L_UPALPHA;
    private static final long H_ALPHA = H_LOWALPHA | H_UPALPHA;

    // alphanum      = alpha | digit
    private static final long L_ALPHANUM = L_DIGIT | L_ALPHA;
    private static final long H_ALPHANUM = H_DIGIT | H_ALPHA;

    // hex           = digit | "A" | "B" | "C" | "D" | "E" | "F" |
    //                         "a" | "b" | "c" | "d" | "e" | "f"
    private static final long L_HEX = L_DIGIT;
    private static final long H_HEX = highMask('A', 'F') | highMask('a', 'f');

    // mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" |
    //                 "(" | ")"
    private static final long L_MARK = lowMask("-_.!~*'()");
    private static final long H_MARK = highMask("-_.!~*'()");

    // unreserved    = alphanum | mark
    private static final long L_UNRESERVED = L_ALPHANUM | L_MARK;
    private static final long H_UNRESERVED = H_ALPHANUM | H_MARK;

    // reserved      = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" |
    //                 "$" | "," | "[" | "]"
    // Added per RFC2732: "[", "]"
    private static final long L_RESERVED = lowMask(";/?:@&=+$,[]");
    private static final long H_RESERVED = highMask(";/?:@&=+$,[]");

    // The zero'th bit is used to indicate that escape pairs and non-US-ASCII
    // characters are allowed; this is handled by the scanEscape method below.
    private static final long L_ESCAPED = 1L;
    private static final long H_ESCAPED = 0L;

    // uric          = reserved | unreserved | escaped
    private static final long L_URIC = L_RESERVED | L_UNRESERVED | L_ESCAPED;
    private static final long H_URIC = H_RESERVED | H_UNRESERVED | H_ESCAPED;

    // pchar         = unreserved | escaped |
    //                 ":" | "@" | "&" | "=" | "+" | "$" | ","
    private static final long L_PCHAR
    = L_UNRESERVED | L_ESCAPED | lowMask(":@&=+$,");
    private static final long H_PCHAR
    = H_UNRESERVED | H_ESCAPED | highMask(":@&=+$,");

    // All valid path characters
    private static final long L_PATH = L_PCHAR | lowMask(";/");
    private static final long H_PATH = H_PCHAR | highMask(";/");

    // Dash, for use in domainlabel and toplabel
    private static final long L_DASH = lowMask("-");
    private static final long H_DASH = highMask("-");

    // Dot, for use in hostnames
    private static final long L_DOT = lowMask(".");
    private static final long H_DOT = highMask(".");

    // userinfo      = *( unreserved | escaped |
    //                    ";" | ":" | "&" | "=" | "+" | "$" | "," )
    private static final long L_USERINFO
    = L_UNRESERVED | L_ESCAPED | lowMask(";:&=+$,");
    private static final long H_USERINFO
    = H_UNRESERVED | H_ESCAPED | highMask(";:&=+$,");

    // reg_name      = 1*( unreserved | escaped | "$" | "," |
    //                     ";" | ":" | "@" | "&" | "=" | "+" )
    private static final long L_REG_NAME
    = L_UNRESERVED | L_ESCAPED | lowMask("$,;:@&=+");
    private static final long H_REG_NAME
    = H_UNRESERVED | H_ESCAPED | highMask("$,;:@&=+");

    // All valid characters for server-based authorities
    private static final long L_SERVER
    = L_USERINFO | L_ALPHANUM | L_DASH | lowMask(".:@[]");
    private static final long H_SERVER
    = H_USERINFO | H_ALPHANUM | H_DASH | highMask(".:@[]");

    // scheme        = alpha *( alpha | digit | "+" | "-" | "." )
    private static final long L_SCHEME = L_ALPHA | L_DIGIT | lowMask("+-.");
    private static final long H_SCHEME = H_ALPHA | H_DIGIT | highMask("+-.");

    // uric_no_slash = unreserved | escaped | ";" | "?" | ":" | "@" |
    //                 "&" | "=" | "+" | "$" | ","
    private static final long L_URIC_NO_SLASH
    = L_UNRESERVED | L_ESCAPED | lowMask(";?:@&=+$,");
    private static final long H_URIC_NO_SLASH
    = H_UNRESERVED | H_ESCAPED | highMask(";?:@&=+$,");


    /**
     * Data must be escaped if it does not have a representation using an
     *  unreserved character; this includes data that does not correspond to
     *  a printable character of the US-ASCII coded character set, or that
     *  corresponds to any US-ASCII character that is disallowed.
     *
     *   unreserved  = alphanum | mark
     *   mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     *
     *  @param path string to be escaped
     *  @return string escaped string

     */

    public static String getEscapedURI(String path){
        return quote(normalize(slashify(path)), L_PATH, H_PATH) ;
    }


    // -- Escaping and encoding --

    private final static char[] hexDigits = {
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static void appendEscape(StringBuffer sb, byte b) {
        sb.append('%');
        sb.append(hexDigits[(b >> 4) & 0x0f]);
        sb.append(hexDigits[(b >> 0) & 0x0f]);
    }

    private static String slashify(String path) {
        String p = path;
        if (File.separatorChar != '/')
            p = p.replace(File.separatorChar, '/');
        if (!p.startsWith("/"))
            p = "/" + p;
        return p;
    }//slashify

    // Quote any characters in s that are not permitted
    // by the given mask pair
    //
    private static String quote(String s, long lowMask, long highMask) {

    int n = s.length();
    StringBuffer sb = null;

    boolean allowNonASCII = ((lowMask & L_ESCAPED) != 0);

    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c < '\u0080') {
            if (!match(c, lowMask, highMask)) {
                if (sb == null) {
                    sb = new StringBuffer();
                    sb.append(s.substring(0, i));
                }
                appendEscape(sb, (byte)c);
            }
            else {
                if (sb != null)
                    sb.append(c);
            }
        } else if (allowNonASCII
            && (Character.isSpaceChar(c)
            || Character.isISOControl(c))) {


            if (sb == null) {
                sb = new StringBuffer();
                sb.append(s.substring(0, i));
            }

            //appendEncoded(sb, c);

            //this case is not handled.
            throw new InternalError() ;

        }
        else {
        if (sb != null)
            sb.append(c);
        }
    }
    return (sb == null) ? s : sb.toString();

    }//quote


    private static int decode(char c) {
        if ((c >= '0') && (c <= '9'))
            return c - '0';
        if ((c >= 'a') && (c <= 'f'))
            return c - 'a' + 10;
        if ((c >= 'A') && (c <= 'F'))
            return c - 'A' + 10;
        return -1;
    }


    private static byte decode(char c1, char c2) {
        return (byte)(  ((decode(c1) & 0xf) << 4)
              | ((decode(c2) & 0xf) << 0));
    }



    // Split the given path into segments, replacing slashes with nulls and
    // filling in the given segment-index array.
    //
    // Preconditions:
    //   segs.length == Number of segments in path
    //
    // Postconditions:
    //   All slashes in path replaced by '\0'
    //   segs[i] == Index of first char in segment i (0 <= i < segs.length)
    //
    static private void split(char[] path, int[] segs) {

      int end = path.length - 1;	// Index of last char in path
      int p = 0;			// Index of next char in path
      int i = 0;			// Index of current segment

      // Skip initial slashes
      while (p <= end) {
          if (path[p] != '/') break;
          path[p] = '\0';
          p++;
      }

      while (p <= end) {

        // Note start of segment
        segs[i++] = p++;

        // Find beginning of next segment
        while (p <= end) {
            if (path[p++] != '/')
                continue;
            path[p - 1] = '\0';

            // Skip redundant slashes
            while (p <= end) {
                if (path[p] != '/') break;
                path[p++] = '\0';
            }

            break;

        }

      }

      if (i != segs.length)
        throw new InternalError();	// ASSERT

    }//split()


    // Normalize the given path string.  A normal path string has no empty
    // segments (i.e., occurrences of "//"), no segments equal to ".", and no
    // segments equal to ".." that are preceded by a segment not equal to "..".
    // In contrast to Unix-style pathname normalization, for URI paths we
    // always retain trailing slashes.
    //
    private static String normalize(String ps) {

      // Does this path need normalization?
      int ns = needsNormalization(ps);	// Number of segments
      if (ns < 0)
          // Nope -- just return it
          return ps;

      char[] path = ps.toCharArray();		// Path in char-array form

      // Split path into segments
      int[] segs = new int[ns];		// Segment-index array
      split(path, segs);

      // Remove dots
      removeDots(path, segs);

      // Prevent scheme-name confusion
      //maybeAddLeadingDot(path, segs);

      // Join the remaining segments and return the result
      return new String(path, 0, join(path, segs));
    }


    // Join the segments in the given path according to the given segment-index
    // array, ignoring those segments whose index entries have been set to -1,
    // and inserting slashes as needed.  Return the length of the resulting
    // path.
    //
    // Preconditions:
    //   segs[i] == -1 implies segment i is to be ignored
    //   path computed by split, as above, with '\0' having replaced '/'
    //
    // Postconditions:
    //   path[0] .. path[return value] == Resulting path
    //
    static private int join(char[] path, int[] segs) {
      int ns = segs.length;		// Number of segments
      int end = path.length - 1;	// Index of last char in path
      int p = 0;			// Index of next path char to write

      if (path[p] == '\0') {
          // Restore initial slash for absolute paths
          path[p++] = '/';
      }

      for (int i = 0; i < ns; i++) {
        int q = segs[i];		// Current segment
        if (q == -1)
        // Ignore this segment
        continue;

        if (p == q) {
            // We're already at this segment, so just skip to its end
            while ((p <= end) && (path[p] != '\0'))
                p++;
            if (p <= end) {
                // Preserve trailing slash
                path[p++] = '/';
            }
        } else if (p < q) {
            // Copy q down to p
            while ((q <= end) && (path[q] != '\0'))
                path[p++] = path[q++];
            if (q <= end) {
                // Preserve trailing slash
                path[p++] = '/';
            }
        } else
        throw new InternalError(); // ASSERT false
      }

      return p;
    }


    // Remove "." segments from the given path, and remove segment pairs
    // consisting of a non-".." segment followed by a ".." segment.
    //
    private static void removeDots(char[] path, int[] segs) {
      int ns = segs.length;
      int end = path.length - 1;

      for (int i = 0; i < ns; i++) {
          int dots = 0;		// Number of dots found (0, 1, or 2)

          // Find next occurrence of "." or ".."
          do {
        int p = segs[i];
        if (path[p] == '.') {
            if (p == end) {
          dots = 1;
          break;
            } else if (path[p + 1] == '\0') {
          dots = 1;
          break;
            } else if ((path[p + 1] == '.')
                 && ((p + 1 == end)
               || (path[p + 2] == '\0'))) {
          dots = 2;
          break;
            }
        }
        i++;
          } while (i < ns);
          if ((i > ns) || (dots == 0))
        break;

          if (dots == 1) {
        // Remove this occurrence of "."
        segs[i] = -1;
          } else {
        // If there is a preceding non-".." segment, remove both that
        // segment and this occurrence of ".."; otherwise, leave this
        // ".." segment as-is.
        int j;
        for (j = i - 1; j >= 0; j--) {
            if (segs[j] != -1) break;
        }
        if (j >= 0) {
            int q = segs[j];
            if (!((path[q] == '.')
            && (path[q + 1] == '.')
            && (path[q + 2] == '\0'))) {
          segs[i] = -1;
          segs[j] = -1;
            }
        }
          }
      }
    }


    // Check the given path to see if it might need normalization.  A path
    // might need normalization if it contains duplicate slashes, a "."
    // segment, or a ".." segment.  Return -1 if no further normalization is
    // possible, otherwise return the number of segments found.
    //
    // This method takes a string argument rather than a char array so that
    // this test can be performed without invoking path.toCharArray().
    //

    static private int needsNormalization(String path) {

      boolean normal = true;
      int ns = 0;			// Number of segments
      int end = path.length() - 1;	// Index of last char in path
      int p = 0;			// Index of next char in path

      // Skip initial slashes
      while (p <= end) {
          if (path.charAt(p) != '/') break;
          p++;
      }
      if (p > 1) normal = false;

      // Scan segments
      while (p <= end) {

          // Looking at "." or ".." ?
          if ((path.charAt(p) == '.')
        && ((p == end)
            || ((path.charAt(p + 1) == '/')
          || ((path.charAt(p + 1) == '.')
              && ((p + 1 == end)
            || (path.charAt(p + 2) == '/')))))) {
        normal = false;
          }
          ns++;

          // Find beginning of next segment
          while (p <= end) {
        if (path.charAt(p++) != '/')
            continue;

        // Skip redundant slashes
        while (p <= end) {
            if (path.charAt(p) != '/') break;
            normal = false;
            p++;
        }

        break;
          }
      }

      return normal ? -1 : ns;
    }

    
}//ConvertToURI

