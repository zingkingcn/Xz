package com.zice.xz.mvp.presenter;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.zice.xz.App;
import com.zice.xz.database.DataBaseHelper;
import com.zice.xz.database.DataBaseTable;
import com.zice.xz.database.ColumnName;
import com.zice.xz.mvp.contract.IMainActivityView;
import com.zice.xz.utils.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Copyright (c) 2017,xxxxxx All rights reserved.
 * author：Z.kai
 * date：2017/3/20
 * description：
 */

public class DBPresenter extends BasePresenter<IMainActivityView> {

    private DataBaseHelper dbh;

    /**
     * 自动关联view和presenter
     *
     * @param iMainActivityView 关联对象
     */
    public DBPresenter(IMainActivityView iMainActivityView) {
        super(iMainActivityView);
        if (dbh == null) {
            dbh = new DataBaseHelper(App.getAppContext(), DataBaseTable.DB_NAME, null, DataBaseHelper.DB_VERSION_INIT);
        }
    }

    public void initConsumeCategory() {
        Log.i(TAG, "onCreate() savedInstanceState -> ");
        List<HashMap<String, String>> listItems = new ArrayList<>();
        Cursor consumeCategory = dbh.queryTable(DataBaseTable.TABLE_CONSUME_CATEGORY).exec();
        while (consumeCategory.moveToNext()) {
            HashMap<String, String> map = new HashMap<>();
            String name = consumeCategory.getString(consumeCategory.getColumnIndex(ColumnName.COLUMN_NAME));
            String id = consumeCategory.getString(consumeCategory.getColumnIndex(ColumnName.COLUMN_CATEGORY_ID));
            map.put(ColumnName.COLUMN_CATEGORY_ID, id);
            map.put(ColumnName.COLUMN_NAME, name);
            listItems.add(map);
        }
        if (isAttach()) {
            getView().onFetchConsumeCategory(listItems);
        }
    }

    public void initConsumeType( HashMap<String, String> selectedItem) {
        String categoryId = selectedItem.get(ColumnName.COLUMN_CATEGORY_ID);
        Cursor query = dbh.query(DataBaseTable.TABLE_CONSUME_TYPE, ColumnName.COLUMN_CATEGORY_ID, categoryId);
        List<HashMap<String, String>> listItems = new ArrayList<>();
        while (query.moveToNext()) {
            HashMap<String, String> map = new HashMap<>();
            String name = query.getString(query.getColumnIndex(ColumnName.COLUMN_NAME));
            String typeId = query.getString(query.getColumnIndex(ColumnName.COLUMN_TYPE_ID));
            map.put(ColumnName.COLUMN_TYPE_ID, typeId);
            map.put(ColumnName.COLUMN_NAME, name);
            listItems.add(map);
        }
        if (isAttach()) {
            getView().onFetchConsumeType(listItems);
        }
    }

    public void insertConsume( HashMap<String, String> categoryItem, HashMap<String, String> typeItem, String money) {
        Double aDouble = Double.valueOf(money);
        money = String.valueOf(NumberUtils.formatDouble(aDouble, 2, true));
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String categoryId = categoryItem.get(ColumnName.COLUMN_CATEGORY_ID);
        String typeId = typeItem.get(ColumnName.COLUMN_TYPE_ID);
        boolean success = dbh.insertTable(DataBaseTable.TABLE_CONSUME_BILL)
                .where(ColumnName.COLUMN_CATEGORY_ID).is(categoryId)
                .where(ColumnName.COLUMN_TYPE_ID).is(typeId)
                .where(ColumnName.COLUMN_YEAR).is(String.valueOf(year))
                .where(ColumnName.COLUMN_MONTH).is(String.valueOf(month))
                .where(ColumnName.COLUMN_DAY).is(String.valueOf(day))
                .where(ColumnName.COLUMN_MONEY).is(money)
                .where(ColumnName.COLUMN_INSERT_TIME).is(String.valueOf(calendar.getTime().getTime()))
                .where(ColumnName.COLUMN_DESC).is("")
                .exec();
        if (isAttach()) {
            if (success) {
                getView().onFetchInsertSuccess();
            } else {
                getView().onFetchInsertFailed();
            }
        }
    }

    public void searchConsume(String... condition) {
        DataBaseHelper.DBQuery dbQuery = dbh.queryTable(DataBaseTable.TABLE_CONSUME_BILL);
        for (String s : condition) {
            
            if (TextUtils.isEmpty(s)){
                continue;    
            }
            String substring = s.substring(0, 3);
            switch (substring) {
                case "YER":
                    dbQuery.where(ColumnName.COLUMN_YEAR).is(s.substring(3));
                    break;
                case "MOH":
                    dbQuery.where(ColumnName.COLUMN_MONEY).is(s.substring(3));
                    break;
                case "DAY":
                    dbQuery.where(ColumnName.COLUMN_DAY).is(s.substring(3));
                    break;
                case "MOY":
                    dbQuery.where(ColumnName.COLUMN_MONEY).is(NumberUtils.formatDouble(s.substring(3),2,true));
                    break;
                case "TIE":
                    dbQuery.where(ColumnName.COLUMN_INSERT_TIME).is(s.substring(3));
                    break;
                case "TID":
                    dbQuery.where(ColumnName.COLUMN_TYPE_ID).is(s.substring(3));
                    break;
                case "CID":
                    dbQuery.where(ColumnName.COLUMN_CATEGORY_ID).is(s.substring(3));
                    break;
                case "DES":
                    dbQuery.where(ColumnName.COLUMN_DESC).is(s.substring(3));
                    break;
            }
        }
        Cursor cursor = dbQuery.exec();
        List<HashMap<String, String>> listItems = new ArrayList<>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (cursor.moveToNext()) {
            HashMap<String, String> map = new HashMap<>();
            String insertTime = cursor.getString(cursor.getColumnIndex(ColumnName.COLUMN_INSERT_TIME));
            String money = cursor.getString(cursor.getColumnIndex(ColumnName.COLUMN_MONEY));
            String time = simpleDateFormat.format(new Date(Long.valueOf(insertTime)));
            map.put("time", time);
            map.put("money", money);
            listItems.add(map);
            Log.i(TAG, "kai ---- searchConsume() insertTime ----> " + insertTime);
            Log.i(TAG, "kai ---- searchConsume() money ----> " + money);
        }
        if (isAttach()) {
                getView().onFetchUpdateConsume(listItems);
        }
        
    }
}
