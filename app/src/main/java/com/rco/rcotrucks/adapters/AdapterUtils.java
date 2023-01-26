package com.rco.rcotrucks.adapters;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.rco.rcotrucks.businesslogic.PairList;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AdapterUtils {

    public static final String TAG = "AdapterUtils";

    // region: Adapter List Utils

    public static <K extends AdapterUtils.IMatchable> List<K> getItems(List<K> listMaster, String searchText) {
        if (listMaster == null || listMaster.size() == 0)
            return listMaster;

        if (StringUtils.isNullOrWhitespaces(searchText))
            return listMaster;

        List<K> result = new ArrayList();

        for (K item : listMaster)
            if (item.isMatch(searchText)) result.add(item);

        return result;
    }

    public static <K> List<K> getItems(List<K> listMaster, String searchText, IMatcher<K> matcher) {
        if (listMaster == null || listMaster.size() == 0)
            return null;

        if (StringUtils.isNullOrWhitespaces(searchText))
            return listMaster;

        List result = new ArrayList();

        for (K item : listMaster) {
            if (matcher.isMatch(item, searchText)) result.add(item);
        }

        return result;
    }

    public static <K extends AdapterUtils.IMatchable> List<K> filterItems(
            List<K> listMaster, List<K> listItems, String searchText) {
        if (listItems == null) return listItems;

        listItems.clear();

        if (!StringUtils.isNullOrWhitespaces(searchText)) {
            for (K item : listMaster)
                if (item.isMatch(searchText)) listItems.add(item);
        } else {
            if (listItems != null && listMaster!=null) {
                listItems.addAll(listMaster);
            }
        }

        return listItems;
    }

    public static boolean isMatch(String[] arText, String pattern, boolean isCaseInsensitive) {
        boolean isMatch = false;
        for (String text : arText) {
            isMatch = StringUtils.isSearchMatchEquiv(text, pattern, isCaseInsensitive);
            if (isMatch) break;
        }
        return isMatch;
    }

    public static <H, K extends IPairListInitable<H>> List<K> syncItems(String jsonGetRecordsResponse, List<K> listItems,
                                                                        Class<K> classObj, H helperObject) throws Exception {
        Log.d(TAG, "syncDvirItems: syncItems(), Start.  jsonGetRecordsResponse=" + jsonGetRecordsResponse);

        if (jsonGetRecordsResponse == null || jsonGetRecordsResponse.trim().length() == 0) {
            return null;
        }

        listItems.clear();

        JSONArray jsonArray = new JSONArray(jsonGetRecordsResponse);
        Log.d(TAG, "syncDvirItems: syncItems(), jsonArray.length()=" + jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject o = jsonArray.getJSONObject(i);
            PairList codingFields = Rms.parseJsonCodingFields(o);
            K item = classObj.newInstance();
            item.init(codingFields, helperObject);
            listItems.add(item);
        }

        Log.d(TAG, "syncDvirItems: syncItems(), End.  result.size()=" + listItems.size());
        return listItems;
    }

    public static <H, K extends AdapterUtils.ICursorInitable<H>> List<K> loadItemsFromDatabase(
            Cursor cursor, List<K> listItems, Class<K> classObj, H helperObject) throws Exception {
        Log.d(TAG, "loadItemsFromDatabase(), Start. classObj.getSimpleName()=" + classObj.getSimpleName());

        listItems.clear();

        while (cursor.moveToNext()) {
            K item = classObj.newInstance();
            item.init(cursor, helperObject);
            Log.d(TAG, "loadItemsFromDatabase(), after item.init(), item.toString()=" + item.toString());
            listItems.add(item);
        }

        Log.d(TAG, "loadItemsFromDatabase(), End.  result.size()=" + listItems.size());
        return listItems;
    }
//    public static <H, K extends IPairListInitable <H>> List<K> syncItems(String jsonGetRecordsResponse, List <K> listItems,
//                                                                         Class<K> classObj, H helperObject) throws Exception
//    {
//        Log.d(TAG, "syncItems(), Start.  jsonGetRecordsResponse=" + jsonGetRecordsResponse);
//
//        if (jsonGetRecordsResponse == null || jsonGetRecordsResponse.trim().length() == 0)
//        {
//            return null;
//        }
//
//        listItems.clear();
//
//        JSONArray jsonArray = new JSONArray(jsonGetRecordsResponse);
//        Log.d(TAG, "syncItems(), jsonArray.length()=" + jsonArray.length());
//
//        for (int i = 0; i < jsonArray.length(); i++)
//        {
//            JSONObject o = jsonArray.getJSONObject(i);
//            PairList codingFields = Rms.parseJsonCodingFields(o);
//            K item = classObj.newInstance();
//            item.init(codingFields, helperObject);
//            listItems.add(item);
//        }
//
//        Log.d(TAG, "syncItems(), Start.  result.size()=" + listItems.size());
//        return listItems;
//    }
    // endregion

    // region: Adapter Item Interfaces

    public interface ISimpleFilter {
        String get(String val);
    }

    public interface IMatchable {
        boolean isMatch(String pattern);
    }

    public interface IMapInitable<K, V> {
        public void init(Map<K, V> map);
    }

    public interface IPairListInitable<H> {
        public void init(PairList pairlist, H optionalHelper);
    }

    public interface ICursorInitable<H> {
        public void init(Cursor cursor, H optionalHelper);
    }

    public interface ISortable {
        public String getSortKey();
    }

    public interface ILabeled {
        public String getLabel();
    }

    public interface IAdapterItem<H, T> extends
            AdapterUtils.IPairListInitable<H>,
            AdapterUtils.ICursorInitable<H>,
            AdapterUtils.ISortable,
            AdapterUtils.IMatchable, Comparable<T>, ILabeled {
    }

    ;

//    public interface IAdapterItemCursor <H, T> extends AdapterUtils.ICursorInitable <H>,
//            AdapterUtils.ISortable,
//            AdapterUtils.IMatchable, Comparable <T>, ILabeled {};

    public interface ITagged<V> {
        public V getTag();

        public void setTag(V tag);
    }

    public interface IPairListInitableItemFactory<G> {
        public G newInstance(PairList list);
    }

    // endregion Adapter Item Interfaces

    // region Nested Classes

    // region: Comparators

    public static class SortKeyComparator implements Comparator<ISortable> {
        @Override
        public int compare(ISortable o1, ISortable o2) {
//            Log.d("SortKeyComparator", "compare()"
//                    + " o1.getSortKey()=" + (o1 != null ? o1.getSortKey() : "(NULL)")
//                    + ", o2.getSortKey()=" + (o2 != null ? o2.getSortKey() : "(NULL)")
//            );

            return o1.getSortKey().compareTo(o2.getSortKey());
        }
    }

    public static class SortKeyComparatorDesc implements Comparator<ISortable> {
        @Override
        public int compare(ISortable o1, ISortable o2) {
//            Log.d("SortKeyComparatorDesc", "compare()"
//                    + " o1.getSortKey()=" + (o1 != null ? o1.getSortKey() : "(NULL)")
//                    + ", o2.getSortKey()=" + (o2 != null ? o2.getSortKey() : "(NULL)")
//            );

            return -o1.getSortKey().compareTo(o2.getSortKey());
        }
    }

    public static Comparator sortKeyComparator = new SortKeyComparator();
    public static Comparator sortKeyComparatorDesc = new SortKeyComparatorDesc();

    // endregion Comparators

    // region: Matchers

    public interface IMatcher<T> {
        boolean isMatch(T o1, String searchText);
    }


    public static class LabelMatcher implements IMatcher<ILabeled> {
        public boolean isMatch(ILabeled o, String searchText) {
            return StringUtils.isSearchMatchEquiv(o.getLabel(), searchText, true);
        }
    }

    public static IMatcher labelMatcher = new LabelMatcher();

    // endregion Matchers

    public static class BitmapItem {
        public Bitmap bitmap;
        public int bitmapClass;
        public int bitmapType;
        public long idRmsRecords;
        public boolean isModified = false;
//        public ImageView.ScaleType scaleType = null;
//        public boolean isAdjustViewBounds = false;

        public BitmapItem(Bitmap bitmap, int bitmapClass, int bitmapType, long idRmsRecords) {
            this.bitmap = bitmap;
            this.bitmapClass = bitmapClass;
            this.bitmapType = bitmapType;
            this.idRmsRecords = idRmsRecords;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            isModified = true;
        }

        public void setIsUpdated(boolean isUpdated) {
            this.isModified = isUpdated;
        }

        public String toString() {
            return "{" + "bitmap bytecount: " + (bitmap != null ? bitmap.getByteCount() : "(NULL)") + ", idRmsRecords=" + idRmsRecords
                    + ", isModified=" + isModified + ", bitmapClass=" + bitmapClass + ", bitmapType=" + bitmapType + "}";
        }
    }

    // endregion Nested Classes
}
