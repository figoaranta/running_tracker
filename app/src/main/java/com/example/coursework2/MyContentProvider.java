package com.example.coursework2;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class MyContentProvider extends ContentProvider {

    private static final UriMatcher uriMatcher;
    private MyRoomDatabase db;
    public final static String TAG = "mobile";

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MyContentProviderContract.AUTHORITY, "records", 1);
        uriMatcher.addURI(MyContentProviderContract.AUTHORITY, "records/#", 2);
    }

    @Override
    public boolean onCreate() {
        db = MyRoomDatabase.getDatabase(this.getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        RecordDao recordDao = db.recordDao();
        final int count;
        switch (uriMatcher.match(uri)) {
            case 1:
                count = recordDao.deleteAllRecordFromContentProvider();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case 2:
                count = recordDao.deleteRecordFromContentProvider((int) ContentUris.parseId(uri));
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        String contentType;

        if(uri.getLastPathSegment() == null){
            contentType = MyContentProviderContract.CONTENT_TYPE_MULTIPLE;
        }else{
            contentType = MyContentProviderContract.CONTENT_TYPE_SINGLE;
        }

        return contentType;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        RecordDao recordDao = db.recordDao();
        switch (uriMatcher.match(uri)) {
            case 1:
                final long id = recordDao.insertRecordFromContentProvider(Record.fromContentValues(values));
                Uri nu = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(nu, null);
                return nu;
            case 2:
                throw new IllegalArgumentException("Invalid URI, cannot insert with ID: " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        RecordDao recordDao = db.recordDao();
        Cursor cursor;
        switch (uriMatcher.match(uri)){
            case 1:
                cursor = recordDao.getRecordAllFromContentProvider();
                break;
            case 2:
                int id = (int) ContentUris.parseId(uri);
                cursor = recordDao.getRecordByIdFromContentProvider(id);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + uriMatcher.match(uri));
        }
        return cursor;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int update(Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        RecordDao recordDao = db.recordDao();
        switch (uriMatcher.match(uri)) {
            case 1:
                throw new IllegalArgumentException("Invalid URI, cannot update without ID" + uri);
            case 2:
                Record record = recordDao.getRecord((int) ContentUris.parseId(uri));

                Record newRecord = Record.fromContentValues(values);

                record.setName(newRecord.getName());
                record.setImage(newRecord.getImage());

                final int count = recordDao.updateRecordFromContentProvider(record);
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}