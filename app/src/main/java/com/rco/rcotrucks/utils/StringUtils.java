package com.rco.rcotrucks.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;

import com.rco.rcotrucks.businesslogic.rms.Rms;

import java.util.Locale;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Pattern;

public class StringUtils {
    public static String substrLastDigits(String str, int digits) {
        return str.substring(str.length() - digits);
    }

    public static String mask(String value, char maskChar) {
        if (value == null)
            return null;

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++)
            result.append(maskChar);

        return result.toString();
    }

    public static String toLowerCase(String value) {
        if (isNullOrWhitespaces(value))
            return "";

        return value.toLowerCase(Locale.US);
    }

    public static String spaceTab(String text, int tabSize) {
        if (isNullOrWhitespaces(text))
            return spaceTab(tabSize);

        return text.length() < tabSize ?
                text + spaceTab(tabSize - text.length()) : text;
    }

    public static String spaceTab(int tabSize) {
        if (tabSize <= 0)
            return "";

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < tabSize; i++)
            result.append(' ');

        return result.toString();
    }

    public static String dashIfEmpty(String text) {
        return isNullOrWhitespaces(text) ? "-" : text;
    }

    public static boolean equalsIgnoreCase(String text1, String text2) {
        if (text1 == null && text2 == null)
            return true;

        if (text1 == null || text2 == null) // One of them isn't null for sure
            return false;

        return text1.compareToIgnoreCase(text2) == 0;
    }

    public static boolean equalsIgnoreCaseAny(String src, String[] values) {
        for (String v : values)
            if (equalsIgnoreCase(src, v))
                return true;

        return false;
    }

    public static boolean equalsIgnoreCaseAll(String src, String[] values) {
        for (String v : values)
            if (!equalsIgnoreCase(src, v))
                return false;

        return true;
    }

    public static String valueOrDash(String text) {
        if (isNullOrWhitespaces(text))
            return "-";

        return text;
    }

    public static String substrWithEndingFlag(String text, int length) {
        if (text == null || text.length() < length)
            return text;

        return text.substring(0, length) + "...";
    }

    public static String substr(String text, int length) {
        if (text == null || text.length() < length)
            return text;

        return text.substring(0, length);
    }

    public static String substrWithPadding(String text, int length, char padChar) {
        if (text == null)
            return text;

        if (text.length() < length)
            return text + getPadding(length - text.length(), padChar);

        return text.substring(0, length);
    }

    public static String getPadding(int paddingLength, char paddingChar) {
        if (paddingLength <= 0)
            return "";

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < paddingLength; i++)
            result.append(paddingChar);

        return result.toString();
    }

    public static boolean isNullOrWhitespacesAll(String[] values) {
        for (String v : values)
            if (!isNullOrWhitespaces(v))
                return false;

        return true;
    }

    public static boolean isNullOrWhitespacesAny(String[] values) {
        for (String v : values)
            if (isNullOrWhitespaces(v))
                return true;

        return false;
    }

    public static boolean isNullOrWhitespaces(String text) {
        if (text == null)
            return true;

        if (text.trim().compareTo("") == 0)
            return true;

        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) != ' ')
                return false;

        return true;
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().compareTo("") == 0;
    }

    public static boolean isFloatingPoint(String text) {
        for (int i = 0; i < text.length(); i++)
            if (!java.lang.Character.isDigit(text.charAt(i)) && text.charAt(i) != '.')
                return false;

        return true;
    }

    public static boolean isNumeric(String text) {
        for (int i = 0; i < text.length(); i++)
            if (!java.lang.Character.isDigit(text.charAt(i)))
                return false;

        return true;
    }

    public static boolean isDouble(String text) {
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex =
                ("[\\x00-\\x20]*" +
                        "[+-]?(" +
                        "NaN|" +
                        "Infinity|" +
                        "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +
                        "(\\.(" + Digits + ")(" + Exp + ")?)|" +
                        "((" +
                        "(0[xX]" + HexDigits + "(\\.)?)|" +
                        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +
                        ")[pP][+-]?" + Digits + "))" +
                        "[fFdD]?))" +
                        "[\\x00-\\x20]*");

        if (Pattern.matches(fpRegex, text)) {
            Double.valueOf(text);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAlphabetic(String text) {
        for (int i = 0; i < text.length(); i++)
            if (!isAlphabetic(text.charAt(i)))
                return false;

        return true;
    }

    public static boolean isAlphabetic(char ch) {
        char[] alphabet = new char[]{
                'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd',
                'f', 'g', 'h', 'j', 'k', 'l', 'A', 'A', 'A', 'z', 'x', 'c', 'v',
                'b', 'n', 'm', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P',
                'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'A', 'A', 'A', 'Z',
                'X', 'C', 'V', 'B', 'N', 'M'
        };

        for (int i = 0; i < alphabet.length; i++)
            if (alphabet[i] == ch)
                return true;

        return false;
    }

    public static final boolean isTrue(String booleanString) {
        if (booleanString == null) return false;
        String s = booleanString.trim().toLowerCase();
        return "true".equals(s) || "yes".equals(s) || "1".equals(s);
    }

    public static int countWords(String source, String word) {
        if (source.indexOf(word) == -1)
            return 0;

        int counter = 0;

        while (source.indexOf(word) != -1) {
            counter++;

            if (source.indexOf(word) + word.length() > source.length())
                break;

            source = source.substring(source.indexOf(word) + word.length());
        }

        return counter;
    }

    public static String[] split(String original, String separator) {
        return split(original, separator, false);
    }

    public static String[] split(String original, String separator, boolean trimLines) {
        if (original == null)
            return null;

        Vector nodes = new Vector();

        int index = original.indexOf(separator);

        while (index >= 0) {
            String line = original.substring(0, index);

            if (trimLines && line.trim().compareTo(separator) == 0)
                continue;

            nodes.addElement(line);
            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
        }

        if (!trimLines || (trimLines && original.trim().compareTo(separator) == 0))
            nodes.addElement(original);

        String[] result = new String[nodes.size()];

        if (nodes.size() > 0)
            for (int loop = 0; loop < nodes.size(); loop++)
                result[loop] = (String) nodes.elementAt(loop);

        return result;
    }

    public static String concat(String[] original, String needle) {
        String result = "";

        for (int i = 0; i < original.length; i++)
            result += original[i] + needle;

        return trimEnd(result, needle);
    }

    /**
     * This method basically just concatenates a list of string parameters into a single string.  However,
     * semantically it treats every even parameter (starting from zero) as a "name" component and
     * the following odd parameter as a "separator" component, such as "-" or "--", or ",", etc.  Each separator
     * component is semantically paired with its preceding name component.  If a name component is blank (null,
     * empty, or all blanks), it and its separator will be ignored.  A separator component will not
     * be added to the emerging string until a following non-blank name component is found.  This
     * prevents the returned string from ending with a separator component.
     *
     * @param arParams
     * @return
     */
    public static String getCompoundName(String... arParams) {
        StringBuilder sbuf = new StringBuilder();
        Stack<String> stack = new Stack<String>();

        for (int i = 0; i < arParams.length; i += 2) {
            String name = arParams[i];

            // If the name component is not blank, add it to the name string
            // and get it's following separator and put it on pending stack.
            // Otherwise, if trimmed name is blank, discard it and its following separator.

            if (name != null && name.trim().length() > 0) {
                // If there was a pending separator, get it and add to name string.

                if (!stack.empty())
                    sbuf.append(stack.pop());

                sbuf.append(name); // Add name to name string.

                // Get next separator (unless at end of parameters) and put it on pending stack.

                if (i + 1 < arParams.length)
                    stack.push(arParams[i + 1]);
            }
        }

        return sbuf.toString();
    }

    public static boolean contains(String source, String text) {
        return contains(source, text, true);
    }

    public static boolean contains(String source, String text, boolean isCaseInsensitive) {
        if (source == null && text == null)
            return true;

        if (source == null || text == null)
            return false;

        return isCaseInsensitive ?
                source.toLowerCase().indexOf(text.toLowerCase()) != -1 : source.indexOf(text) != -1;
    }

    public static boolean containsAnyWord(String source, String query) {
        String[] words = split(query, " ");

        for (int i = 0; i < words.length; i++)
            if (source.indexOf(words[i]) != -1)
                return true;

        return false;
    }

    public static boolean containsAnyWord(String source, String[] words) {
        for (int i = 0; i < words.length; i++)
            if (source.indexOf(words[i]) != -1)
                return true;

        return false;
    }

    public static String trim(String text, String needle) {
        if (text.length() < needle.length())
            return text;

        while (text.substring(0, needle.length()).compareTo(needle) == 0)
            text = text.substring(needle.length());

        while (text.substring(text.length() - needle.length()).compareTo(needle) == 0)
            text = text.substring(0, text.length() - needle.length());

        return text;
    }

    public static String trimEnd(String text, String needle) {
        return
                text != null &&
                        text.length() > needle.length() &&
                        text.substring(text.length() - needle.length()).compareTo(needle) == 0 ?

                        text.substring(0, text.length() - needle.length()) : text;
    }

    public static String nvl(String s1, String s2) {
        if (s1 == null || s1.trim().length() == 0)
            return s2;
        else
            return s1;
    }

    public static String replace(String text, String searchString, String newString) {
        StringBuffer sb = new StringBuffer();

        int searchStringPosition = text.indexOf(searchString);
        int startPosition = 0;
        int searchStringLength = searchString.length();

        while (searchStringPosition != -1) {
            sb.append(text.substring(startPosition, searchStringPosition)).append(newString);
            startPosition = searchStringPosition + searchStringLength;
            searchStringPosition = text.indexOf(searchString, startPosition);
        }

        sb.append(text.substring(startPosition, text.length()));

        return sb.toString();
    }

    public static String replaceOneOf(String text, char[] chars, char newChar) {
        for (int i = 0; i < chars.length; i++)
            text = text.replace(chars[i], newChar);

        return text;
    }

    public static String getStringEnumeration(String[] enumeration, int startIndex, int length) {
        String result = "";

        for (int i = startIndex; i < startIndex + length; i++)
            if (!isNullOrEmpty(enumeration[i]))
                result += ", " + enumeration[i];

        return trim(result, ", ");
    }

//    Aug 08, 2022  -
//    public static String capitalize(String text) {
//        return text.substring(0, 1).toUpperCase() + (text.length() > 1 ? text.substring(1).toLowerCase() : "");
//    }

    public static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + (text.length() > 1 ? text.substring(1) : "");
    }

    public static String camel(String text) {
        if (text == null)
            return null;

        String needle = " ";
        String[] splitStr = split(text, needle);
        StringBuilder result = new StringBuilder();

        for (String s : splitStr)
            result.append(capitalize(s)).append(needle);

        return result.toString().trim();
    }

    public static String println(String[] text) {
        if (text == null || text.length == 0)
            return null;

        String result = "";

        for (int i = 0; i < text.length; i++)
            result += text[i] + "\n";

        return result;
    }

    public static final boolean isEquiv(String str1, String str2, boolean isCaseInsensitive) {
        if (str1 == null && str2 == null) return true;
        else if (str1 != null && str2 != null) {
            if (isCaseInsensitive) return str1.equalsIgnoreCase(str2);
            else return str1.equals(str2);
        } else if (str1 == null) return str2.length() == 0;
        else return str1.length() == 0;
    }

    public static final boolean isEquivStrictBlanks(String str1, String str2, boolean isCaseInsensitive) {
        if (str1 == null && str2 == null) return true;
        else if (str1 != null && str2 != null) {
            if (isCaseInsensitive) return str1.equalsIgnoreCase(str2);
            else return str1.equals(str2);
        } else return false;
    }

    public static final boolean isSearchMatchEquiv(String searchable, String searchText, boolean isCaseInsensitive) {
        if (searchable == null && searchText == null) return true;
        else if (searchable != null && searchText != null) {
            if (isCaseInsensitive)
                return searchable.toLowerCase().contains(searchText.toLowerCase());
            else return searchable.contains(searchText);
        } else return false;
    }

    public static String getResolvedTemplate(String template, String pattern, String value) {
        return getResolvedTemplate(template, new String[]{pattern}, new String[]{value});
    }

    public static String getResolvedTemplate(String template, String[] arPatterns, String[] arValues) {
        StringBuilder sbuf = new StringBuilder(template);

        for (int i = 0; i < arPatterns.length; i++) replaceAll(sbuf, arPatterns[i], arValues[i]);

        return sbuf.toString();
    }

    public static void replaceAll(StringBuilder sbuf, String pattern, String value) {
        int i = sbuf.indexOf(pattern);
        while (i >= 0) {
            sbuf.replace(i, i + pattern.length(), value);
            i = sbuf.indexOf(pattern, i + value.length());
        }
    }

    public static String dumpArray(Object[] arObj) {
        return dumpArray(arObj, ", ", null, null, 0, (arObj != null ? arObj.length : 0),
                true, true);
    }

    /**
     * @param arObj
     * @param strSeparator
     * @param strPrefix
     * @param strPostfix
     * @param iStartIndex
     * @param iMaxElements
     * @param bIsIncludePrivateFields
     * @param bIsIncludeInherited
     * @return
     */
    public static String dumpArray(Object[] arObj, String strSeparator,
                                   String strPrefix, String strPostfix, int iStartIndex, int iMaxElements,
                                   boolean bIsIncludePrivateFields, boolean bIsIncludeInherited
    ) {
        if (strSeparator == null)
            strSeparator = ",";
        if (strPrefix == null)
            strPrefix = "\"";
        if (strPostfix == null)
            strPostfix = "\"";

        StringBuilder sbuf = new StringBuilder();

        if (arObj == null)
            sbuf.append("(NULL)");
        else {
            int iLim = Math.min(iMaxElements, arObj.length);

            for (int i = iStartIndex; i < iLim; i++) {
                if (sbuf.length() > 0)
                    sbuf.append(strSeparator);
                Object obj = arObj[i];

                if (obj instanceof String)
                    sbuf.append(strPrefix + obj.toString() + strPostfix);
                else if (obj instanceof Object[])
                    sbuf.append(dumpArray((Object[]) obj, strSeparator, strPrefix,
                            strPostfix, 0, iMaxElements, bIsIncludePrivateFields, bIsIncludeInherited));
                else
                    sbuf.append("{" + String.valueOf(obj) + "}");
            }
        }

        return "[" + sbuf.toString() + "]";
    }

    public static String memberValuesToString(Object objSource) {
        return memberValuesToString(objSource, true, true, ", ");
    }

    /**
     * @param objSource
     * @param bIsIncludePrivateFields
     * @param bIsIncludeInherited
     * @param strFieldDelimiter
     * @return
     */
    public static String memberValuesToString(Object objSource,
                                              boolean bIsIncludePrivateFields, boolean bIsIncludeInherited,
                                              String strFieldDelimiter
                                              // String strDelimiterEquals
    ) {
        java.lang.reflect.Field fld;
        int i;

        if (objSource == null)
            return "(object is null)";

        if (strFieldDelimiter == null || strFieldDelimiter.length() == 0)
            strFieldDelimiter = "\r\n";

        StringBuilder sbuf = new StringBuilder();
        java.lang.reflect.Field[] flds = null;

        Class objClass = objSource.getClass();

        while (objClass != null) {
            if (bIsIncludePrivateFields)
                flds = objClass.getDeclaredFields();
            else
                flds = objClass.getFields(); // This gets inherited fields I think. Yes,

            for (i = 0; i < flds.length; i++) {
                fld = flds[i];

                if (bIsIncludePrivateFields)
                    fld.setAccessible(true);

                if (sbuf.length() > 0)
                    sbuf.append(strFieldDelimiter);

                sbuf.append(fld.getName() + "=");

                try {
                    Object obj = fld.get(objSource);

                    if (obj instanceof Object[]) {
                        sbuf.append(dumpArray(
                                (Object[]) obj, strFieldDelimiter, "\"",
                                "\"", 0, 20, true, true)
                        );
                    } else if (obj instanceof String) {
                        sbuf.append("\"" + obj + "\"");
                    } else {
                        sbuf.append(String.valueOf(obj));
                    }
                } catch (Exception e) {
                    sbuf.append(e.getMessage() + "\r\n");
                }
            }

            if (!bIsIncludePrivateFields
            )
                break; // getFields() gets superclass fields so don't need loop for this case. -RAN 8/6/09


            if (!bIsIncludeInherited) break; // -RAN 9/30/12
            objClass = objClass.getSuperclass();
        }

        return "{" + sbuf.toString() + "}";
    }

    public static String getDelimited(String[] values, String delimiter, Rms.IPostParser parser) {
        StringBuilder sbuf = new StringBuilder();

        for (String v : values) {
            String field = parser.parse(v);
            sbuf.append(field).append(delimiter);
        }

        if (sbuf.length() > 0) sbuf.setLength(sbuf.length() - delimiter.length());

        return sbuf.toString();
    }

    public static SpannableString refineSpannableString(String str1, String str2) {
        String source = str1 + " " + str2;
        SpannableString spannableString = new SpannableString(source);
        spannableString.setSpan(new AbsoluteSizeSpan(20, true), 0, str1.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(10, true),
                spannableString.length() - str2.length(), spannableString.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableString;
    }
}