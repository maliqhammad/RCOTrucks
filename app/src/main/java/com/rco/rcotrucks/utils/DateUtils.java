package com.rco.rcotrucks.utils;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private static final String TAG = "DateUtils";

    public static String getYyyyMmDdStr(Date d) {
        int month = d.getMonth() + 1;
        String mm = month < 10 ? "0" + month : "" + month;

        int day = d.getDate();
        String dd = day < 10 ? "0" + day : "" + day;

        return (d.getYear() + 1900) + "-" + mm + "-" + dd;
    }

    public static String getMmDdYyyyStr(Date d) {
        int month = d.getMonth() + 1;
        String mm = month < 10 ? "0" + month : "" + month;

        int day = d.getDate();
        String dd = day < 10 ? "0" + day : "" + day;

        return mm + "-" + dd + "-" + (d.getYear() + 1900);
    }

    public static final String getMmDdYyyy(String yyyyxMmxDd, String returnSeparator) {
        String d = yyyyxMmxDd;
        String s = returnSeparator;

        return d.substring(5, 7) + s + d.substring(8, 10) + s + d.substring(0, 4);
    }

    public static String getHhmmssStr(Date d) {
        String hrs = d.getHours() < 10 ? "0" + d.getHours() : "" + d.getHours();
        String mins = d.getMinutes() < 10 ? "0" + d.getMinutes() : "" + d.getMinutes();
        String secs = d.getSeconds() < 10 ? "0" + d.getSeconds() : "" + d.getSeconds();

        return hrs + ":" + mins + ":" + secs;
    }

    public static Date getHhmmss(String hhmmss) {
        String hh = hhmmss.substring(0, 2);
        String mm = hhmmss.substring(3, 5);
        String ss = hhmmss.substring(6, 8);

        Date d = new Date();
        d.setHours(Integer.parseInt(hh));
        d.setMinutes(Integer.parseInt(mm));
        d.setSeconds(Integer.parseInt(ss));

        return d;
    }

    public static String getHhmmStr(Date d) {
        String hrs = d.getHours() < 10 ? "0" + d.getHours() : "" + d.getHours();
        String mins = d.getMinutes() < 10 ? "0" + d.getMinutes() : "" + d.getMinutes();

        return hrs + ":" + mins;
    }

    public static String getNowHhmmStr() {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        return dateFormat.format(new Date()).toString();
    }

    public static String getYyyyMmDdHhmmssStr(Date d) {
        int month = d.getMonth() + 1;
        String mm = month < 10 ? "0" + month : "" + month;

        int day = d.getDate() + 1;
        String dd = day < 10 ? "0" + day : "" + day;

        String hrs = d.getHours() < 10 ? "0" + d.getHours() : "" + d.getHours();
        String mins = d.getMinutes() < 10 ? "0" + d.getMinutes() : "" + d.getMinutes();
        String secs = d.getSeconds() < 10 ? "0" + d.getSeconds() : "" + d.getSeconds();

        return (d.getYear() + 1900) + "-" + mm + "-" + dd + " " + hrs + ":" + mins + ":" + secs + "";
    }

    public static String getMmDdYyyyHmmssStr(Date d) {
        int month = d.getMonth() + 1;
        String mm = month < 10 ? "0" + month : "" + month;

        int day = d.getDate();
        String dd = day < 10 ? "0" + day : "" + day;

        String hrs = d.getHours() < 10 ? "0" + d.getHours() : "" + d.getHours();
        String mins = d.getMinutes() < 10 ? "0" + d.getMinutes() : "" + d.getMinutes();
        String secs = d.getSeconds() < 10 ? "0" + d.getSeconds() : "" + d.getSeconds();

        return mm + "-" + dd + "-" + (d.getYear() + 1900) + " " + hrs + ":" + mins + ":" + secs;
    }

    public static String getIso8601NowDateStr() {
        Date d = new Date();

        int month = d.getMonth() + 1;
        String mm = month < 10 ? "0" + month : "" + month;

        int day = d.getDate();
        String dd = day < 10 ? "0" + day : "" + day;

        String hrs = d.getHours() < 10 ? "0" + d.getHours() : "" + d.getHours();
        String mins = d.getMinutes() < 10 ? "0" + d.getMinutes() : "" + d.getMinutes();
        String secs = d.getSeconds() < 10 ? "0" + d.getSeconds() : "" + d.getSeconds();

        return mm + "-" + dd + "-" + (d.getYear() + 1900) + " " + hrs + "." + mins + "." + secs;
    }

    public static String getNowYyyyMmDdHhmmss() {
        Date d = new Date();

        int month = d.getMonth() + 1;
        String mm = month < 10 ? "0" + month : "" + month;

        int day = d.getDate();
        String dd = day < 10 ? "0" + day : "" + day;

        String hrs = d.getHours() < 10 ? "0" + d.getHours() : "" + d.getHours();
        String mins = d.getMinutes() < 10 ? "0" + d.getMinutes() : "" + d.getMinutes();
        String secs = d.getSeconds() < 10 ? "0" + d.getSeconds() : "" + d.getSeconds();

        return (d.getYear() + 1900) + "-" + mm + "-" + dd + " " + hrs + ":" + mins + ":" + secs + "";
    }

    public static String getNowYyyyMmDd() {
        String now = getNowYyyyMmDdHhmmss();

        return now.substring(0, 10);
    }

    public static String getNowYyyyMm() {
        String now = getNowYyyyMmDdHhmmss();

        return now.substring(0, 9);
    }

    public static String getNowHhmmss() {
        String now = getNowYyyyMmDdHhmmss();

        return now.substring(11, 19);
    }

    public static String getNowMmDdYyyyHhmmssStr() {
        Date d = new Date();

        int month = d.getMonth() + 1;
        String mm = month < 10 ? "0" + month : "" + month;

        int day = d.getDate();
        String dd = day < 10 ? "0" + day : "" + day;

        String hrs = d.getHours() < 10 ? "0" + d.getHours() : "" + d.getHours();
        String mins = d.getMinutes() < 10 ? "0" + d.getMinutes() : "" + d.getMinutes();
        String secs = d.getSeconds() < 10 ? "0" + d.getSeconds() : "" + d.getSeconds();

        return mm + "-" + dd + "-" + (d.getYear() + 1900) + " " + hrs + ":" + mins + ":" + secs + "";
    }

    public static String getNowMmDdYyStr() {
        return getNowMmDdYyStr("-");
    }

    public static String getNowMmDdYyStr(String needle) {
        Date d = new Date();

        int month = d.getMonth() + 1;
        String mm = month < 10 ? "0" + month : "" + month;

        int day = d.getDate();
        String dd = day < 10 ? "0" + day : "" + day;

        return mm + needle + dd + needle + (d.getYear() - 100);
    }

    public static Long getTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    public static double getTimestampInDouble() {
        double timeStamp = ((double) System.currentTimeMillis()) / 1000;
        Log.d(TAG, "getTimestampInDouble: timeStamp: " + timeStamp);
        return timeStamp;
    }

    public static Date addHours(Date d, int hoursToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.HOUR_OF_DAY, hoursToAdd);

        return cal.getTime();
    }

    public static Date addSecs(Date d, int secsToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.SECOND, secsToAdd);

        return cal.getTime();
    }

    public static String getNowYear() {
        return getYear(getNowYyyyMmDdHhmmss());
    }

    public static String getYear(String dateStr) {
        if (dateStr == null || dateStr.length() < 4)
            return null;

        return dateStr.substring(0, 4);
    }

    // region Date formatting additional functions (RAN 8/1/2020)

    public static final String FORMAT_DATE_MM_DD_YYYY_HH_MM_SS_SSS = "MM/dd/yyyy HH:mm:ss.SSS";
    public static final String FORMAT_DATE_MM_DD_YYYY_HH_MM_SS = "MM/dd/yyyy HH:mm:ss";
    public static final String FORMAT_DATE_MM_DD_YYYY_HH_MM_AMPM = "MM/dd/yyyy hh:mm a";
    public static final String FORMAT_DATE_MM_DD_YY_HH_MM_SS_SSS = "MM/dd/yy HH:mm:ss.SSS";
    public static final String FORMAT_DATE_MM_DD_YY = "MM/dd/yy";
    public static final String FORMAT_DATE_MM_DD_YY_SIMPLE = "MMddyy";
    public static final String FORMAT_DATE_MM_DD_YYYY = "MM/dd/yyyy";
    public static final String FORMAT_DATE_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String FORMAT_DATE_YYYY_MMM_DD = "EEE MMM dd";
    public static final String FORMAT_DATE_DD_MMM_YY = "dd-MMM-yy";
    public static final String FORMAT_DATE_MMMM_DD_YYYY = "MMMM dd, yyyy";
    public static final String FORMAT_DATE_TIME_SEC = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE_TIME_SEC_AMPM = "yyyy-MM-dd hh:mm:ss a";
    public static final String FORMAT_TIME_SEC_AMPM = "hh:mm:ss a";
    public static final String FORMAT_DATE_TIME_SEC_FILE = "yyyy-MM-dd_HH.mm.ss";
    public static final String FORMAT_DATE_TIME_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FORMAT_DATE_TIME_MILLIS_FILE = "yyyy-MM-dd_HH.mm.ss.SSS";
    public static final String FORMAT_ISO_SSS_X = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final String FORMAT_ISO_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String FORMAT_ISO_SSS_A = "yyyy-MM-dd'T'HH:mm.SSSZ";
    public static String[] FROM_DATE_TIME_FORMAT_LIST = {FORMAT_DATE_TIME_MILLIS, FORMAT_ISO_SSS_Z};
    public static final String FORMAT_ISO_SSS_Z_FOR_NAME = "yyyy-MM-dd_HH.mm.ss.SSSZ";
    public static final String FORMAT_DATE_HH_MM = "HH:mm";
    public static final String FORMAT_DATE_HH_MM_A = "HH:mm a";
    public static final String FORMAT_DATE_hh_mm_a = "h:mm a";

    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

    public static String getDateTime(long LsystemTime, String format) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(LsystemTime);
        SimpleDateFormat dfmt = new SimpleDateFormat(format);

        return dfmt.format(cal.getTime());
    }

    public static String getDateTime(java.util.Date d, String format) {
        SimpleDateFormat dfmt = new SimpleDateFormat(format);
        return dfmt.format(d);
    }

    public static String getDate(long time, String format) {
        SimpleDateFormat dfmt = new SimpleDateFormat(format);
        return dfmt.format(time);
    }

    public static String getNowIsoZ() {
        return getDateTime(System.currentTimeMillis(), FORMAT_ISO_SSS_Z);
    }

    public static String convertDateTime(String date, String fromFormat, String toFormat) {
        String ret = null;

        try {
            SimpleDateFormat dfmtFrom = new SimpleDateFormat(fromFormat);
            if (date.length() == 19) {
                dfmtFrom = new SimpleDateFormat(FORMAT_DATE_TIME_SEC);
            }
            SimpleDateFormat dfmtTo = new SimpleDateFormat(toFormat);
            ret = convertDateTime(date, dfmtFrom, dfmtTo);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return ret;
    }

    public static final String convertDateTime(String date, SimpleDateFormat fromFormat, SimpleDateFormat toFormat) {
        return convertDateTime(date, fromFormat, toFormat, false);
    }

    public static final String convertDateTime(String date, SimpleDateFormat fromFormat, SimpleDateFormat toFormat, boolean isStackTraceOnError) {
        String ret = null;

        try {
            java.util.Date d = fromFormat.parse(date);
            return toFormat.format(d);
        } catch (Throwable throwable) {
            Log.d(TAG, "convertDateTime() cannot parse date: [" + date + "] with fromFormat: " + fromFormat.toPattern() + " and toFormat: " + toFormat.toPattern());
            if (isStackTraceOnError) throwable.printStackTrace();
        }

        return ret;
    }

    /**
     * inputs a date or date time and outputs a date or datetime
     */
    public interface IDateConverter {
        public String convert(String fromDate, boolean isIncludeTime);
    }

    public interface IDateConverterParser extends IDateConverter {
        public java.util.Date parse(String fromDate);
    }

    public static class DateConverterParser implements IDateConverterParser {
        public SimpleDateFormat[] arDfmtFrom;
        public SimpleDateFormat dfmtToDate;
        public SimpleDateFormat dfmtToDateTime;

        public DateConverterParser(String formatFrom, String formatToDate, String formatToDateTime) {
            this(new String[]{formatFrom}, formatToDate, formatToDateTime);
        }

        public DateConverterParser(String[] arFormatFrom, String formatToDate, String formatToDateTime) {
            arDfmtFrom = new SimpleDateFormat[arFormatFrom.length];

            for (int i = 0; i < arFormatFrom.length; i++) {
                arDfmtFrom[i] = new SimpleDateFormat(arFormatFrom[i]);
            }

            dfmtToDate = new SimpleDateFormat(formatToDate);
            dfmtToDateTime = new SimpleDateFormat(formatToDateTime);
        }

        @Override
        public String convert(String fromDate, boolean isIncludeTime) {

            if (StringUtils.isNullOrWhitespaces(fromDate)) return null;

            String strRet = null;
            SimpleDateFormat dfmtTo = null;

            if (isIncludeTime && dfmtToDateTime != null)
                dfmtTo = dfmtToDateTime;
            else dfmtTo = dfmtToDate;

            if (dfmtTo != null && arDfmtFrom != null && arDfmtFrom.length > 0) {
                for (SimpleDateFormat dfmtFrom : arDfmtFrom) {
                    if (dfmtFrom != null)
                        strRet = DateUtils.convertDateTime(fromDate, dfmtFrom, dfmtTo);
                    if (strRet != null) break;
                }
            }

            return strRet;
        }

        public String convert(String fromDate) {
            return convert(fromDate, false);
        }

        public java.util.Date parse(String fromDate) {
            java.util.Date d = null;

            if (fromDate != null) {
                for (SimpleDateFormat dfmtFrom : arDfmtFrom) {
                    if (dfmtFrom != null) {
                        try {
                            d = dfmtFrom.parse(fromDate);
                            Log.d(TAG, "**** DateConverterParser.parse(), parse of " + fromDate + " succeeded with format: " + dfmtFrom.toPattern());
                            break; // If not exception, date conversion succeeded.
                        } catch (ParseException e) {
                            Log.d(TAG, "**** DateConverterParser.parse(), parse of " + fromDate + " failed with format: " + dfmtFrom.toPattern());
//                            Log.d(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
            }

            return d;
        }
    }

    /**
     * Converts from local date or datetime to Ansi date or datetime.
     * MM/dd/yyyy or MM/dd/yyyy HH:... to yyyy-MM-dd or yyyy-MM-dd HH:mm:ss.SSS.
     * Under devel.
     */
    public static class AnyLocalDateToAnsiConverter implements IDateConverter {
        @Override
        public String convert(String fromDate, boolean isTimeIncluded) {
            return getAnsiDateFromLocal(fromDate, isTimeIncluded);
        }
    }

    public static String getAnsiDateFromLocal(String fromDate, boolean isTimeIncluded) {
        String strThis = "getAnsiDateFromLocal";
        String strRet = null;

        String[] arDateTimeParts = fromDate.split("\\s+|[/:\\.\\-]");
        int len = arDateTimeParts.length;

        if (len > 2) {
            String mon = arDateTimeParts[0];
            String day = arDateTimeParts[1];
            String year = arDateTimeParts[2];

            StringBuilder sbuf = new StringBuilder();

            if (year.length() < 4) {
                String yearCurrent = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                year = prepad(year, yearCurrent, 4);
            }

            sbuf.append(year).append("-").append(padz2(mon)).append("-").append(padz2(day));

            if (isTimeIncluded) {
                String hour = (len > 3 ? padz2(arDateTimeParts[3]) : "00");
                String min = (len > 4 ? padz2(arDateTimeParts[4]) : "00");
                String sec = (len > 5 ? padz2(arDateTimeParts[5]) : "00");
                String ms = (len > 6 ? prepad(arDateTimeParts[6], "000", 3) : "000");

                sbuf.append(" ").append(hour).append(":").append(min).append(":").append(sec).append(".").append(ms);
            }

            strRet = sbuf.toString();
        }

        return strRet;
    }

    public static final String prepad(String s, String padding, int length) {
        if (s.length() < length)
            return padding.substring(0, length - s.length()) + s;
        else
            return s;
    }

    public static final String postpad(String s, String padding, int length) {
        if (s.length() < length)
            return s + padding.substring(0, length - s.length());
        else
            return s;
    }

    public static final String padz2(String s) {
        return prepad(s, "0", 2);
    }

    // endregion

    //region NTP Time

    public static Long getNtpTime() throws Exception {
        DefaultHttpClient c = new DefaultHttpClient();
        HttpResponse response = c.execute(new HttpGet("http://worldclockapi.com/api/json/utc/now"));
        StatusLine line = response.getStatusLine();

        if (line.getStatusCode() == HttpStatus.SC_OK) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();

            String responseString = out.toString();
            responseString = responseString.substring(responseString.indexOf("currentDateTime") + 17);
            String dateStr = responseString.substring(1, responseString.indexOf("\",\"") - 1).replace("T", " ");

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = df.parse(dateStr);

            return d.getTime();
        }

        response.getEntity().getContent().close();
        throw new IOException(line.getReasonPhrase());
    }

    //endregion

    public static List<String> getDaysList(int monthNumber, String format) {
        List<String> daysList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MONTH, -monthNumber);

        while (calendar.getTime().before(todayCalendar.getTime())) {
            Date day = calendar.getTime();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            daysList.add(simpleDateFormat.format(day));
            calendar.add(Calendar.DATE, 1);
        }

        daysList.add(simpleDateFormat.format(todayCalendar.getTime()));

        return daysList;
    }

    public static boolean isToday(String date, String fromFormat) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromFormat);
            Date d = simpleDateFormat.parse(date);
            return android.text.format.DateUtils.isToday(d.getTime());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return false;
    }

    public static String getCurrentTime() {
        Date currentDay = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATE_HH_MM);
        return simpleDateFormat.format(currentDay);
    }

    public static String getCurrentDay() {
        Date currentDay = new Date();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATE_TIME_SEC);
        return simpleDateFormat.format(currentDay);
    }

    public static String ConvertDate(String date, String format) {
        try {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            if (date.length() == 19) {
                simpleDateFormat = new SimpleDateFormat(FORMAT_DATE_TIME_SEC);
            }

            Date d = simpleDateFormat.parse(date);
            Calendar now = Calendar.getInstance();
            now.setTime(d);
            now.set(Calendar.HOUR_OF_DAY, 0);
            return simpleDateFormat.format(now.getTime());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public static String getPreviousDay(String date, String fromFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromFormat);
        Date d = null;
        try {
            d = simpleDateFormat.parse(date);
        } catch (ParseException parseException) {
            Log.d(TAG, "getPreviousDay: parseException: " + parseException.getMessage());
            parseException.printStackTrace();
        }
        return simpleDateFormat.format(new Date(d.getTime() - MILLIS_IN_A_DAY));
    }

    public static long getDateDiff(String format, String oldDate, String newDate) {
        try {
            SimpleDateFormat dfmtFrom = new SimpleDateFormat(format);

            return TimeUnit.MILLISECONDS.toSeconds(dfmtFrom.parse(newDate).getTime() - dfmtFrom.parse(oldDate).getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static Date getDateDiff(String dateString) {
//        String dtStart = "2010-10-15T09:27:37Z";
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(dateString);
            return date;
        } catch (ParseException e) {
            Log.d(TAG, "MD: getDateDiff: ParseException: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static String convertMinutesToHours(long time) {
        float hours = (float) time / 60;

        return String.valueOf(MathUtils.roundFloat(hours, 1));
    }

    public static String convertSecondToMinutes(long time) {
        int minutes = (int) (time / 60);
        int seconds = (int) (time % 60);
        return String.format(minutes + " min" + " %02d s", seconds);
    }

    public static SpannableString calculateBreakTime(long time) {
        if (time < 60) {
            return StringUtils.refineSpannableString("" + time, "minutes");
        }

        int hours = (int) (time % 60);
        int minutes = (int) (time / 60);

        SpannableString formattedHours = StringUtils.refineSpannableString("" + hours, "hours & ");
        SpannableString formattedMinutes = StringUtils.refineSpannableString("" + minutes, "min");
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(formattedHours);
        spannableStringBuilder.append(formattedMinutes);
        return SpannableString.valueOf(spannableStringBuilder);
    }

    public static String convertMinutesToHours(long time, String hoursText, String minutesText) {
        int days = (int) (time / 60) / 24;
        int hours = (int) ((time % (24 * 60)) / 60);
        int minutes = (int) (time % 60);
        if (days > 0) {
            return String.format("%02d d %02d " + hoursText + " %02d " + minutesText, days, hours, minutes);
        }

        return String.format("%02d " + hoursText + " %02d " + minutesText, hours, minutes);
    }

    public static int convertToMinutes(double time) {
        return (int) (time / 60);
    }

    public static String getLastCycleDate(int hoursRoles, String format) {

        SimpleDateFormat dfmtFrom = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -hoursRoles);
        try {
            return dfmtFrom.format(calendar.getTime());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    public static long getTimeInSecond() {
        return new Date().getTime() / 1000;
    }

    public static long eliminateSecondsFromValue(long seconds) {
        long value = (seconds / 60) * 60;
        return value;
    }


    public static String getCreatedDate() {
        Date currentDay = new Date();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATE_YYYY_MM_DD);
        return simpleDateFormat.format(currentDay);
    }

    public static boolean isCreateDateLastWeek(String dateString) {
        try {
            SimpleDateFormat fromFormat = new SimpleDateFormat(FORMAT_DATE_YYYY_MM_DD);
            Date date = fromFormat.parse(dateString);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            Date lastweekDate = calendar.getTime();

            return date.after(lastweekDate);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return false;
    }

    public static Date parseDate(String str) throws Exception {
        return parseDate(str, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date parseDate(String str, String format) throws Exception {
        return new SimpleDateFormat(format).parse(str);
    }

    public static Long diffInSecs(Date end, Date start) {
        return (end.getTime() - start.getTime()) / 1000;
    }

    public static String formatSecsStr(Long value) {
        Double hrs = value / 60d / 60d;
        Double mins = value / 60d % 60d;
        Double secs = value.doubleValue() % 60d;

        return hrs.longValue() + "h " + mins.longValue() + "m " + secs.longValue() + "s";
    }

    public static String formatHourStr(long value) {
        double hrs = (double) (value / 60d);

        hrs = Double.parseDouble(clearSpecialChars("" + hrs));
//        Double rounded= new BigDecimal(myDouble).setScale(1, RoundingMode.HALF_UP).doubleValue();
        double rounded = Double.parseDouble(new DecimalFormat("##.#").format(hrs));
        return rounded + " hrs";
    }

    private static String clearSpecialChars(String value) {
        if (StringUtils.isNullOrWhitespaces(value))
            return "";

        return value.replace(",", "");
    }

    public static String formatHourStr(double hoursValue) {
        double rounded = Double.parseDouble(new DecimalFormat("##.#").format(hoursValue));
//        Todo =>   if time value is less than 1 hour, should we show time in mints
        return rounded + " hrs";
    }

    public static Date getCurrentDateTimeSevenDaysAgo(String date, String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        if (!date.isEmpty()) {
            try {
                cal.setTime(s.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cal.add(Calendar.DAY_OF_YEAR, days);
//        return s.format(new Date(cal.getTimeInMillis()));
        return new Date(cal.getTimeInMillis());
    }


    public static String getDateFromDateAndTime(String dateAndTime) {
//        2022-04-20 16:56:13
        if (!dateAndTime.isEmpty()) {
            String[] splitDate = dateAndTime.trim().split(" ");
            if (splitDate[0].length() > 0) {

                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat format2 = new SimpleDateFormat(DateUtils.FORMAT_DATE_MM_DD_YY_SIMPLE);
                Date date = null;
                try {
                    date = format1.parse(splitDate[0]);
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }

                String newDate = format2.format(date);
                return newDate;
            }
        }
        return "";
    }


    public static String getMonthDayYearDateFromDateAndTime(String dateAndTime) {
//        2022-04-20 16:56:13
        if (!dateAndTime.isEmpty()) {
            String[] splitDate = dateAndTime.trim().split(" ");
            if (splitDate[0].length() > 0) {

                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
//                SimpleDateFormat format2 = new SimpleDateFormat(DateUtils.FORMAT_DATE_MM_DD_YY_SIMPLE);
                SimpleDateFormat format2 = new SimpleDateFormat(DateUtils.FORMAT_DATE_MMMM_DD_YYYY);
                Date date = null;
                try {
                    date = format1.parse(splitDate[0]);
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }

                String newDate = format2.format(date);
                return newDate;
            }
        }
        return "";
    }

    public static String getTimeFromDateAndTime(String dateAndTime) {
//        2022-04-20 16:56:13
        if (!dateAndTime.isEmpty()) {
            String[] splitDate = dateAndTime.trim().split(" ");
            if (splitDate[1].length() > 1) {
                String time = splitDate[1].replace(":", "").replace(".000", "");
                return time;
            }
        }
        return "";
    }

    public static String removeMilliSecondsFromDateAndTime(String dateAndTime) {
        return dateAndTime.replace(".000", "");
    }


    public static long getTodayDateTimeInTimeStamp(boolean isStartingDateRequired) {

        Calendar calculate = Calendar.getInstance();
//        Dec 05, 2022  -   If we set these below params as 0 thats mean send us all timestamp of today starting time
//        else it will return us today's end dateTime in timestamp
        int hourOfTheDay = 0, minute = 0, second = 0;
        if (!isStartingDateRequired) {
            hourOfTheDay = 23;
            minute = 59;
            second = 59;
        }

        calculate.set(Calendar.MILLISECOND, 0);
        calculate.set(Calendar.HOUR_OF_DAY, hourOfTheDay); //set hours to 0 for start time of current day
        calculate.set(Calendar.MINUTE, minute); // set minutes to 0 for start time of current day
        calculate.set(Calendar.SECOND, second); //set seconds to 0 for start time of current day
        return calculate.getTimeInMillis();
    }

    public static long getYesterdayDateTimeInTimeStamp(boolean isStartingDateRequired) {

        Calendar calculate = Calendar.getInstance();
//        Dec 05, 2022  -   If we set these below params as 0 thats mean send us all timestamp of today starting time
//        else it will return us today's end dateTime in timestamp
        int hourOfTheDay = 0, minute = 0, second = 0;
        if (isStartingDateRequired) {
            hourOfTheDay = -24;
        } else {
            second = -1;
        }

        calculate.set(Calendar.MILLISECOND, 0);
        calculate.set(Calendar.HOUR_OF_DAY, hourOfTheDay); //set hours to 0 for start time of current day
        calculate.set(Calendar.MINUTE, minute); // set minutes to 0 for start time of current day
        calculate.set(Calendar.SECOND, second); //set seconds to 0 for start time of current day
        return calculate.getTimeInMillis();
    }

    public static long getCurrentWeek(android.icu.util.Calendar mCalendar, boolean isStartingDateRequired) {
        Date date = new Date();
        mCalendar.setTime(date);

        // 1 = Sunday, 2 = Monday, etc.
        int day_of_week = mCalendar.get(android.icu.util.Calendar.DAY_OF_WEEK);

        int monday_offset;
        if (day_of_week == 1) {
            monday_offset = -6;
        } else
            monday_offset = (2 - day_of_week); // need to minus back
        mCalendar.add(android.icu.util.Calendar.DAY_OF_YEAR, monday_offset);

        Date mDateMonday = mCalendar.getTime();

        // return 6 the next days of current day (object cal save current day)
        mCalendar.add(android.icu.util.Calendar.DAY_OF_YEAR, 6);
        Date mDateSunday = mCalendar.getTime();

        if (isStartingDateRequired) {
            return mDateMonday.getTime();
        } else {
            return mDateSunday.getTime();
        }

    }

    public static long getLastWeekStartTimeStamp() {
        Calendar lastWeekStartingTime = Calendar.getInstance();
        int currentWeekNumber = lastWeekStartingTime.get(Calendar.WEEK_OF_YEAR);
        lastWeekStartingTime.set(Calendar.WEEK_OF_YEAR, currentWeekNumber - 1);
        lastWeekStartingTime.set(Calendar.DAY_OF_WEEK, 2);
        lastWeekStartingTime.set(Calendar.MILLISECOND, 0);
        lastWeekStartingTime.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        lastWeekStartingTime.set(Calendar.MINUTE, 0); // set minutes to zero
        lastWeekStartingTime.set(Calendar.SECOND, 0); //set seconds to zero
        Log.d(TAG, "endDateTime: timeStamp: " + lastWeekStartingTime.getTimeInMillis());
        return lastWeekStartingTime.getTimeInMillis();
    }

    public static long getLastWeekEndTimeStamp() {
        Calendar lastWeekEndTime = Calendar.getInstance();
        int currentWeekNumber = lastWeekEndTime.get(Calendar.WEEK_OF_YEAR);
        lastWeekEndTime.set(Calendar.WEEK_OF_YEAR, currentWeekNumber - 1);
        lastWeekEndTime.set(Calendar.DAY_OF_WEEK, 1);
        lastWeekEndTime.set(Calendar.MILLISECOND, 59);
        lastWeekEndTime.set(Calendar.HOUR_OF_DAY, 23); //set hours to zero
        lastWeekEndTime.set(Calendar.MINUTE, 59); // set minutes to zero
        lastWeekEndTime.set(Calendar.SECOND, 59); //set seconds to zero
        Log.d(TAG, "endDateTime: timeStamp: " + lastWeekEndTime.getTimeInMillis());

        return lastWeekEndTime.getTimeInMillis();
    }


    public static long getLastWeek(android.icu.util.Calendar mCalendar, boolean isStartingDateRequired) {
        Log.d(TAG, "getLastWeek: ");
        Date date = new Date();
        mCalendar.setTime(date);

        // 1 = Sunday, 2 = Monday, etc.
        int day_of_week = mCalendar.get(android.icu.util.Calendar.DAY_OF_WEEK);

        int monday_offset;
        if (day_of_week == 1) {
            monday_offset = -6;
        } else
            monday_offset = (2 - day_of_week); // need to minus back

        monday_offset = monday_offset - 7;
        Log.d(TAG, "getLastWeek: monday_offset: " + monday_offset);
        mCalendar.add(android.icu.util.Calendar.DAY_OF_YEAR, monday_offset);

        Date mDateMonday = mCalendar.getTime();
        Log.d(TAG, "getLastWeek: mDateMonday: " + mDateMonday);

        // return 6 the next days of current day (object cal save current day)
        mCalendar.add(android.icu.util.Calendar.DAY_OF_YEAR, 6);
        Date mDateSunday = mCalendar.getTime();
        Log.d(TAG, "getLastWeek: mDateSunday: " + mDateSunday);

        //Get format date
        String strDateFormat = "dd MMM";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

        String MONDAY = sdf.format(mDateMonday);
        String SUNDAY = sdf.format(mDateSunday);

        // Sub String
        if ((MONDAY.substring(3, 6)).equals(SUNDAY.substring(3, 6))) {
            MONDAY = MONDAY.substring(0, 2);
        }
////
////        return MONDAY + " - " + SUNDAY;
        Log.d(TAG, "getLastWeek: " + MONDAY + " - " + SUNDAY);

        if (isStartingDateRequired) {
            return mDateMonday.getTime();
        } else {
            return mDateSunday.getTime();
        }

    }


    public static long getThisMonth(android.icu.util.Calendar mCalendar, boolean isStartingDateRequired) {
        Date date = new Date();
        mCalendar.setTime(date);
        mCalendar.add(android.icu.util.Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfCurrentMonth = mCalendar.getTime();

        mCalendar.add(android.icu.util.Calendar.DAY_OF_MONTH, mCalendar.getActualMaximum(android.icu.util.Calendar.DAY_OF_MONTH));
        Date lastDayOfCurrentMonth = mCalendar.getTime();
        if (isStartingDateRequired) {
            return firstDayOfCurrentMonth.getTime();
        } else {
            return lastDayOfCurrentMonth.getTime();
        }
    }

    public static long getThisMonthStart() {
        Calendar thisMonthStartTime = Calendar.getInstance();
        thisMonthStartTime.set(Calendar.DAY_OF_MONTH, 1);
        thisMonthStartTime.set(Calendar.MILLISECOND, 0);
        thisMonthStartTime.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        thisMonthStartTime.set(Calendar.MINUTE, 0); // set minutes to zero
        thisMonthStartTime.set(Calendar.SECOND, 0); //set seconds to zero
        Log.d(TAG, "start: startTime: " + thisMonthStartTime.getTimeInMillis());

        return thisMonthStartTime.getTimeInMillis();
    }

    public static long getThisMonthEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 23); //set hours to zero
        cal.set(Calendar.MINUTE, 59); // set minutes to zero
        cal.set(Calendar.SECOND, 59); //set seconds to zero

        Date lastDayOfMonth = cal.getTime();
        Log.d(TAG, "endDate: last: " + lastDayOfMonth);
        Log.d(TAG, "endDateTime: timeStamp: " + cal.getTimeInMillis());
        return cal.getTimeInMillis();
    }

    public static long getLastMonthStartTimeStamp() {
        Calendar lastMonthStartTime = Calendar.getInstance();
        lastMonthStartTime.add(Calendar.MONTH, -1);
        lastMonthStartTime.set(Calendar.DATE, 1);
        lastMonthStartTime.set(Calendar.MILLISECOND, 0);
        lastMonthStartTime.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        lastMonthStartTime.set(Calendar.MINUTE, 0); // set minutes to zero
        lastMonthStartTime.set(Calendar.SECOND, 0); //set seconds to zero
        Log.d(TAG, "start: startTime: " + lastMonthStartTime.getTimeInMillis());
        return lastMonthStartTime.getTimeInMillis();
    }

    public static long getLastMonthEndTimeStamp() {
        Calendar lastMonthEndingTimeStamp = Calendar.getInstance();
        lastMonthEndingTimeStamp.add(Calendar.MONTH, -1);

        int max = lastMonthEndingTimeStamp.getActualMaximum(Calendar.DAY_OF_MONTH);
        lastMonthEndingTimeStamp.set(Calendar.DAY_OF_MONTH, max);
        lastMonthEndingTimeStamp.set(Calendar.MILLISECOND, 59);
        lastMonthEndingTimeStamp.set(Calendar.HOUR_OF_DAY, 23); //set hours to zero
        lastMonthEndingTimeStamp.set(Calendar.MINUTE, 59); // set minutes to zero
        lastMonthEndingTimeStamp.set(Calendar.SECOND, 59); //set seconds to zero
        Log.d(TAG, "start: startTime: " + lastMonthEndingTimeStamp.getTimeInMillis());
        return lastMonthEndingTimeStamp.getTimeInMillis();
    }


    public static long getLastMonth(boolean isStartingDateRequired) {
        android.icu.util.Calendar aCalendar = android.icu.util.Calendar.getInstance();
        aCalendar.set(android.icu.util.Calendar.DATE, 1);
        aCalendar.add(android.icu.util.Calendar.DAY_OF_MONTH, -1);
        Date lastDateOfPreviousMonth = aCalendar.getTime();
        aCalendar.set(android.icu.util.Calendar.DATE, 1);
        Date firstDateOfPreviousMonth = aCalendar.getTime();

        if (isStartingDateRequired) {
            return firstDateOfPreviousMonth.getTime();
        } else {
            return lastDateOfPreviousMonth.getTime();
        }
    }

    public static long getThisYear(boolean isStartingDateRequired) {
        android.icu.util.Calendar firstDayOfCurrentYear = android.icu.util.Calendar.getInstance();
        firstDayOfCurrentYear.set(android.icu.util.Calendar.DATE, 1);
        firstDayOfCurrentYear.set(android.icu.util.Calendar.MONTH, 0);

        android.icu.util.Calendar lastDayOfCurrentYear = android.icu.util.Calendar.getInstance();
        lastDayOfCurrentYear.set(android.icu.util.Calendar.DATE, 31);
        lastDayOfCurrentYear.set(android.icu.util.Calendar.MONTH, 11);

        if (isStartingDateRequired) {
            return firstDayOfCurrentYear.getTime().getTime();
        } else {
            return lastDayOfCurrentYear.getTime().getTime();
        }
    }

    public static long getThisYearStartTimeStamp() {
        Calendar firstDayOfThisYear = Calendar.getInstance();
        firstDayOfThisYear.set(Calendar.DATE, 1);
        firstDayOfThisYear.set(Calendar.MONTH, 0);
        firstDayOfThisYear.set(Calendar.MILLISECOND, 0);
        firstDayOfThisYear.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        firstDayOfThisYear.set(Calendar.MINUTE, 0); // set minutes to zero
        firstDayOfThisYear.set(Calendar.SECOND, 0); //set seconds to zero
        Log.d(TAG, "endDateTime: timeStamp: " + firstDayOfThisYear.getTimeInMillis());

        return firstDayOfThisYear.getTimeInMillis();
    }

    public static long getThisYearEndTimeStamp() {
        Calendar lastDayOfThisYearTime = Calendar.getInstance();
        lastDayOfThisYearTime.add(Calendar.YEAR, 0);
        lastDayOfThisYearTime.set(Calendar.DATE, -1);
        lastDayOfThisYearTime.set(Calendar.MONTH, 12);
        lastDayOfThisYearTime.set(Calendar.MILLISECOND, 59);
        lastDayOfThisYearTime.set(Calendar.HOUR_OF_DAY, 23); //set hours to zero
        lastDayOfThisYearTime.set(Calendar.MINUTE, 59); // set minutes to zero
        lastDayOfThisYearTime.set(Calendar.SECOND, 59); //set seconds to zero
        Log.d(TAG, "endDateTime: timeStamp: " + lastDayOfThisYearTime.getTimeInMillis());

        return lastDayOfThisYearTime.getTimeInMillis();
    }

    public static long getLastYear(boolean isStartingDateRequired) {
        android.icu.util.Calendar firstDayOfCurrentYear = android.icu.util.Calendar.getInstance();
        firstDayOfCurrentYear.set(android.icu.util.Calendar.DATE, 1);
        firstDayOfCurrentYear.set(android.icu.util.Calendar.MONTH, 0);

        android.icu.util.Calendar lastDayOfCurrentYear = android.icu.util.Calendar.getInstance();
        lastDayOfCurrentYear.set(android.icu.util.Calendar.DATE, 31);
        lastDayOfCurrentYear.set(android.icu.util.Calendar.MONTH, 11);

        if (isStartingDateRequired) {
            return firstDayOfCurrentYear.getTime().getTime();
        } else {
            return lastDayOfCurrentYear.getTime().getTime();
        }
    }

    public static long getLastYearFirstDayInTimeStamp() {
        Calendar firstDayOfPreviousYear = Calendar.getInstance();
        firstDayOfPreviousYear.add(Calendar.YEAR, -1);
        firstDayOfPreviousYear.set(Calendar.DATE, 1);
        firstDayOfPreviousYear.set(Calendar.MONTH, 0);
        firstDayOfPreviousYear.set(Calendar.MILLISECOND, 0);
        firstDayOfPreviousYear.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        firstDayOfPreviousYear.set(Calendar.MINUTE, 0); // set minutes to zero
        firstDayOfPreviousYear.set(Calendar.SECOND, 0); //set seconds to zero
        Log.d(TAG, "endDateTime: timeStamp: " + firstDayOfPreviousYear.getTimeInMillis());
        return firstDayOfPreviousYear.getTimeInMillis();
    }

    public static long getLastYearLastDayInTimeStamp() {
        Calendar lastDayOfPreviousYear = Calendar.getInstance();
        lastDayOfPreviousYear.add(Calendar.YEAR, -1);
        lastDayOfPreviousYear.set(Calendar.DATE, 31);
        lastDayOfPreviousYear.set(Calendar.MONTH, 11);

        lastDayOfPreviousYear.set(Calendar.MILLISECOND, 59);
        lastDayOfPreviousYear.set(Calendar.HOUR_OF_DAY, 23); //set hours to zero
        lastDayOfPreviousYear.set(Calendar.MINUTE, 59); // set minutes to zero
        lastDayOfPreviousYear.set(Calendar.SECOND, 59); //set seconds to zero
        Log.d(TAG, "endDateTime: timeStamp: " + lastDayOfPreviousYear.getTimeInMillis());
        return lastDayOfPreviousYear.getTimeInMillis();
    }

    public static Date getFirstDayOfQuarter() {
        android.icu.util.Calendar cal = android.icu.util.Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        cal.set(android.icu.util.Calendar.DAY_OF_MONTH, 1);

        cal.set(Calendar.HOUR, -12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.set(android.icu.util.Calendar.MONTH, cal.get(android.icu.util.Calendar.MONTH) / 3 * 3);
        Log.d(TAG, "getFirstDayOfQuarter: ");
        return cal.getTime();
    }

    public static Date getLastDayOfQuarter() {
        android.icu.util.Calendar cal = android.icu.util.Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        cal.set(android.icu.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(android.icu.util.Calendar.MONTH, cal.get(android.icu.util.Calendar.MONTH) / 3 * 3 + 2);
        cal.set(android.icu.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(android.icu.util.Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public static Long convertToTimeStamp(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTimeInMillis();
    }

    public static long getStartOfThisWeekInMilli() {
        Calendar cal = initClearDayCal();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Log.d(TAG, "getStartOfThisWeekInMilli: cal: " + cal.getTimeInMillis());
        return cal.getTimeInMillis();
    }

    private static Calendar initClearDayCal() {
        Calendar cal = Calendar.getInstance();
        Log.d(TAG, "initClearDayCal: getFirstDayOfWeek: " + cal.getTimeInMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        return cal;
    }

    private final static SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat longSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Date getCurrentQuarterEndTime() {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH) + 1;
        Date now = null;/*  ww  w  . j ava  2 s.  c o m*/
        try {
            if (currentMonth >= 1 && currentMonth <= 3) {
                c.set(Calendar.MONTH, 2);
                c.set(Calendar.DATE, 31);
            } else if (currentMonth >= 4 && currentMonth <= 6) {
                c.set(Calendar.MONTH, 5);
                c.set(Calendar.DATE, 30);
            } else if (currentMonth >= 7 && currentMonth <= 9) {
                c.set(Calendar.MONTH, 8);
                c.set(Calendar.DATE, 30);
            } else if (currentMonth >= 10 && currentMonth <= 12) {
                c.set(Calendar.MONTH, 11);
                c.set(Calendar.DATE, 31);
            }
            now = longSdf.parse(shortSdf.format(c.getTime()) + " 23:59:59");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return now;
    }


    public static Date getCurrentQuarterStartTime() {
        Calendar c = Calendar.getInstance();
        int currentMonth = c.get(Calendar.MONTH) + 1;
        Date now = null;/*w  w  w  .  j a va 2 s . c  om*/
        try {
            if (currentMonth >= 1 && currentMonth <= 3)
                c.set(Calendar.MONTH, 0);
            else if (currentMonth >= 4 && currentMonth <= 6)
                c.set(Calendar.MONTH, 3);
            else if (currentMonth >= 7 && currentMonth <= 9)
                c.set(Calendar.MONTH, 4);
            else if (currentMonth >= 10 && currentMonth <= 12)
                c.set(Calendar.MONTH, 9);
            c.set(Calendar.DATE, 1);
            now = longSdf.parse(shortSdf.format(c.getTime()) + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return now;
    }

    public static Date getFirstDayOfPreviousQuarter() {
        android.icu.util.Calendar cal = android.icu.util.Calendar.getInstance();
        Date date = new Date();

        cal.setTime(date);
        cal.set(android.icu.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(android.icu.util.Calendar.MONTH, cal.get(android.icu.util.Calendar.MONTH) / 3 * 2);
        cal.set(Calendar.HOUR, -12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getLastDayOfPreviousQuarter() {
        android.icu.util.Calendar cal = android.icu.util.Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        cal.set(android.icu.util.Calendar.DAY_OF_MONTH, 1);

        cal.set(android.icu.util.Calendar.MONTH, cal.get(android.icu.util.Calendar.MONTH) / 3 * 2 + 2);
        cal.set(android.icu.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(android.icu.util.Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR, 11);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 59);
        return cal.getTime();
    }

    public static long getThisWeekStartTimeInTimeStamp() {
        Calendar thisWeekStartingTime = Calendar.getInstance();
        int currentWeekNumber = thisWeekStartingTime.get(Calendar.WEEK_OF_YEAR);
        thisWeekStartingTime .set(Calendar.WEEK_OF_YEAR, currentWeekNumber);
        thisWeekStartingTime .set(Calendar.DAY_OF_WEEK, 2);
        thisWeekStartingTime .set(Calendar.MILLISECOND, 0);
        thisWeekStartingTime .set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        thisWeekStartingTime .set(Calendar.MINUTE, 0); // set minutes to zero
        thisWeekStartingTime .set(Calendar.SECOND, 0); //set seconds to zero
        return thisWeekStartingTime.getTimeInMillis();
    }
    public static long getThisWeekEndTimeInTimeStamp() {
        Calendar thisWeekEnding = Calendar.getInstance();
        int currentWeekNumber = thisWeekEnding.get(Calendar.WEEK_OF_YEAR);
        thisWeekEnding.set(Calendar.WEEK_OF_YEAR, currentWeekNumber);
        thisWeekEnding.set(Calendar.DAY_OF_WEEK, 1);
        thisWeekEnding.set(Calendar.MILLISECOND, 59);
        thisWeekEnding.set(Calendar.HOUR_OF_DAY, 23); //set hours to zero
        thisWeekEnding.set(Calendar.MINUTE, 59); // set minutes to zero
        thisWeekEnding.set(Calendar.SECOND, 59); //set seconds to zero
        Log.d(TAG, "endDateTime: timeStamp: " + thisWeekEnding.getTimeInMillis());
        return thisWeekEnding.getTimeInMillis();
    }


    public static long getPreviousWeekEndTimeInTimeStamp() {
        Calendar thisWeekEndTime = Calendar.getInstance();
        int currentWeekNumber = thisWeekEndTime.get(Calendar.WEEK_OF_YEAR);
        thisWeekEndTime.set(Calendar.WEEK_OF_YEAR, currentWeekNumber );
        thisWeekEndTime.set(Calendar.DAY_OF_WEEK, 1);
        thisWeekEndTime.set(Calendar.MILLISECOND, 59);
        thisWeekEndTime.set(Calendar.HOUR_OF_DAY, 23); //set hours to zero
        thisWeekEndTime.set(Calendar.MINUTE, 59); // set minutes to zero
        thisWeekEndTime.set(Calendar.SECOND, 59); //set seconds to zero
        return thisWeekEndTime.getTimeInMillis();
    }



    public static long getCurrentWeekEndTimeInTimeStamp() {
        Calendar thisWeekEndTime = Calendar.getInstance();
        int currentWeekNo = thisWeekEndTime.get(Calendar.WEEK_OF_YEAR);
        thisWeekEndTime.set(Calendar.WEEK_OF_YEAR, currentWeekNo);
        thisWeekEndTime.set(Calendar.DAY_OF_WEEK,1);
        thisWeekEndTime.add(Calendar.DATE,7);
        thisWeekEndTime.set(Calendar.HOUR_OF_DAY, 23); //set hours to zero
        thisWeekEndTime.set(Calendar.MINUTE, 59); // set minutes to zero
        thisWeekEndTime.set(Calendar.SECOND, 59); //set seconds to zero
        thisWeekEndTime.set(Calendar.MILLISECOND, 59);
        Log.d(TAG, "endDateTime: timeStamp: " + thisWeekEndTime.getTimeInMillis());
        return thisWeekEndTime.getTimeInMillis();
    }






}
