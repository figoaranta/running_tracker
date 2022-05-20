package com.example.coursework2;

import android.net.Uri;

public class MyContentProviderContract {
    public static final String AUTHORITY = "com.example.coursework2.MyContentProvider";
    public static final Uri RECORD_URI = Uri.parse("content://"+AUTHORITY+"/records");

    public static final String NAME = "name";
    public static final String IMAGE = "image";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/MyContentProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/MyContentProvider.data.text";
}
