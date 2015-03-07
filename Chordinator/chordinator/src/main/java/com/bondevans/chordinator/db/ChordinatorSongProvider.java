package com.bondevans.chordinator.db;

import com.bondevans.chordinator.Log;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


public class ChordinatorSongProvider extends ContentProvider {
//	public static String AUTHORITY = "com.bondevans.chordinator";

	private static final String TAG = "SongProvider";
    private SongDB mDB;
    public static final int SONGS = 100;
    public static final int SONG_ID = 110;
    public static final int FAVOURITES = 120;
    public static final int RECENT = 130;
    public static final int SETS = 140;
    public static final int SET_ID = 150;
    public static final int SETITEMS = 155;// Set items
    public static final int SETITEMS_FOR_SET = 160;// Set items for given Set id
    public static final int SETITEM_ID = 170;// And individual Set item

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/song";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/song";

	public UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	protected void addUris(){
	}

    @Override
    public boolean onCreate() {
        mDB = new SongDB(getContext());
        addUris();
        return true;
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = mDB.getWritableDatabase();
	    int rowsAffected = 0;
	    String id ="";

	    switch (uriType) {
	    case SONGS:
	        rowsAffected = sqlDB.delete(SongDB.TABLE_SONG,
	                selection, selectionArgs);
	        break;
	    case SONG_ID:
	        id = uri.getLastPathSegment();
	        if (TextUtils.isEmpty(selection)) {
	            rowsAffected = sqlDB.delete(SongDB.TABLE_SONG,
	                    SongDB.COLUMN_ID + "=" + id, null);
	        } else {
	            rowsAffected = sqlDB.delete(SongDB.TABLE_SONG,
	                    selection + " and " + SongDB.COLUMN_ID + "=" + id,
	                    selectionArgs);
	        }
	        break;
	    case SETS:
	        rowsAffected = sqlDB.delete(SongDB.TABLE_SETLIST,
	                selection, selectionArgs);
	        break;
	    case SET_ID:
	        id = uri.getLastPathSegment();
	        if (TextUtils.isEmpty(selection)) {
	            rowsAffected = sqlDB.delete(SongDB.TABLE_SETLIST,
	                    SongDB.COLUMN_ID + "=" + id, null);
	        } else {
	            rowsAffected = sqlDB.delete(SongDB.TABLE_SETLIST,
	                    selection + " and " + SongDB.COLUMN_ID + "=" + id,
	                    selectionArgs);
	        }
	        break;
	    case SETITEMS_FOR_SET:
	    	// Delete setitems for given set
	        id = uri.getLastPathSegment();
	        if (TextUtils.isEmpty(selection)) {
	            rowsAffected = sqlDB.delete(SongDB.TABLE_SETITEM,
	                    SongDB.COLUMN_SETLIST_ID + "=" + id, null);
	        } else {
	            rowsAffected = sqlDB.delete(SongDB.TABLE_SETITEM,
	                    selection + " and " + SongDB.COLUMN_SETLIST_ID + "=" + id,
	                    selectionArgs);
	        }
	        break;
	    case SETITEM_ID:
	        id = uri.getLastPathSegment();
	        if (TextUtils.isEmpty(selection)) {
	            rowsAffected = sqlDB.delete(SongDB.TABLE_SETITEM,
	                    SongDB.COLUMN_ID + "=" + id, null);
	        } else {
	            rowsAffected = sqlDB.delete(SongDB.TABLE_SETITEM,
	                    selection + " and " + SongDB.COLUMN_ID + "=" + id,
	                    selectionArgs);
	        }
	        break;
	    default:
	        throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsAffected;
	}

	@Override
	public String getType(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case SONGS:
            return CONTENT_TYPE;
        case SONG_ID:
            return CONTENT_ITEM_TYPE;
        case SETS:
            return CONTENT_TYPE;
        case SET_ID:
            return CONTENT_ITEM_TYPE;
        case SETITEMS:
            return CONTENT_TYPE;
        case SETITEM_ID:
            return CONTENT_ITEM_TYPE;
        default:
            return null;
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		String table="";
		switch(uriType){
		case SONGS:
			table = SongDB.TABLE_SONG;
			break;
		case SETS:
			table = SongDB.TABLE_SETLIST;
			break;
		case SETITEMS:
			table = SongDB.TABLE_SETITEM;
			break;
		default:
			throw new IllegalArgumentException("Invalid URI for insert");
		}
		SQLiteDatabase sqlDB = mDB.getWritableDatabase();
		long newID = sqlDB.insert(table, null, values);
		if (newID > 0) {
			Uri newUri = ContentUris.withAppendedId(uri, newID);
			getContext().getContentResolver().notifyChange(uri, null);
			return newUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    int uriType = sURIMatcher.match(uri);
	    Log.d(TAG, "HELLO sortOrder:["+sortOrder+"] uriType=["+uriType+"] selection=["+selection+"]");
	    switch (uriType) {
	    case SONG_ID:
	    	// Get a particular Song by its ID
		    queryBuilder.setTables(SongDB.TABLE_SONG);
	        queryBuilder.appendWhere(SongDB.COLUMN_ID + "="
	                + uri.getLastPathSegment());
	        break;
	    case SONGS:
	    	// Get Songs
		    queryBuilder.setTables(SongDB.TABLE_SONG);
	        // no filter
	        break;
	    case FAVOURITES:
	    	//  Get favourite songs
		    queryBuilder.setTables(SongDB.TABLE_SONG);
	        queryBuilder.appendWhere(SongDB.COLUMN_FAV + "=1");
	        break;
	    case RECENT:
	    	// Get recent songs
		    queryBuilder.setTables(SongDB.TABLE_SONG);
	    	break;
	    case SETS:
		    queryBuilder.setTables(SongDB.TABLE_SETLIST);
	        // no filter
	        break;
	    case SET_ID:
	    	// Get a particular SET
		    queryBuilder.setTables(SongDB.TABLE_SETLIST);
	        queryBuilder.appendWhere(SongDB.COLUMN_ID + "="
	                + uri.getLastPathSegment());
	        break;
	    case SETITEMS_FOR_SET:
	    	// Get Set Items for a particular SET
	    	//		    queryBuilder.setTables(SongDB.TABLE_SETITEM);
		    queryBuilder.setTables(SongDB.TABLE_SETITEM+","+SongDB.TABLE_SONG);
		    String where = SongDB.COLUMN_SETLIST_ID + "="+ uri.getLastPathSegment()+
		    		" AND (" + SongDB.TABLE_SETITEM + "." +	SongDB.COLUMN_SONG_ID+"="+
		    		SongDB.TABLE_SONG+"."+SongDB.COLUMN_ID+")";
		    queryBuilder.appendWhere(where);
			//	        queryBuilder.appendWhere(" AND (" + SongDB.TABLE_SETITEM + "." +
			//	        		SongDB.COLUMN_SONG_ID+"="+SongDB.TABLE_SONG+"."+SongDB.COLUMN_ID+")");
	        Log.d(TAG, "HELLO SETITEMS_FOR_SET ["+uri.getLastPathSegment()+
			        "] tables["+queryBuilder.getTables()+"] where["+where+"]");
	        break;
	    case SETITEM_ID:
		    queryBuilder.setTables(SongDB.TABLE_SETITEM);
	        queryBuilder.appendWhere(SongDB.COLUMN_ID + "="
	                + uri.getLastPathSegment());
	        break;
	    case SETITEMS:
		    queryBuilder.setTables(SongDB.TABLE_SETITEM+","+SongDB.TABLE_SONG);
	        queryBuilder.appendWhere(SongDB.TABLE_SETITEM + "." + SongDB.COLUMN_SONG_ID+"="+SongDB.TABLE_SONG+"."+SongDB.COLUMN_ID);
	        break;
	    default:
	        throw new IllegalArgumentException("Unknown URI");
	    }

	    Cursor cursor = queryBuilder.query(mDB.getReadableDatabase(),
	            projection, selection, selectionArgs, null, null, sortOrder);
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);

	    return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDB.getWritableDatabase();
        String id="";
        StringBuilder modWhere = null;
        int rowsAffected;
        String tableName="";

        switch (uriType) {
        case SONG_ID:
            id = uri.getLastPathSegment();
            modWhere = new StringBuilder(SongDB.COLUMN_ID
                    + "=" + id);

            if (!TextUtils.isEmpty(whereClause)) {
                modWhere.append(" AND " + whereClause);
            }

            tableName = SongDB.TABLE_SONG;
            break;
        case SET_ID:
            id = uri.getLastPathSegment();
            modWhere = new StringBuilder(SongDB.COLUMN_ID
                    + "=" + id);

            if (!TextUtils.isEmpty(whereClause)) {
                modWhere.append(" AND " + whereClause);
            }

            tableName = SongDB.TABLE_SETLIST;
            break;
        case SETITEM_ID:
            id = uri.getLastPathSegment();
            modWhere = new StringBuilder(SongDB.COLUMN_ID
                    + "=" + id);

            if (!TextUtils.isEmpty(whereClause)) {
                modWhere.append(" AND " + whereClause);
            }

            tableName = SongDB.TABLE_SETITEM;
            break;
		case SETITEMS:
			tableName = SongDB.TABLE_SETITEM;
			modWhere = new StringBuilder(whereClause);
			break;
        case SETITEMS_FOR_SET:
	        // Update setitems for given set
	        tableName = SongDB.TABLE_SETITEM;
	        id = uri.getLastPathSegment();
	        modWhere = new StringBuilder(SongDB.COLUMN_SETLIST_ID
			        + "=" + id);

	        if (!TextUtils.isEmpty(whereClause)) {
		        modWhere.append(" AND " + whereClause);
	        }
	        break;
        case SONGS:
            tableName = SongDB.TABLE_SONG;
            modWhere = new StringBuilder(whereClause);
            break;
        case SETS:
            tableName = SongDB.TABLE_SETLIST;
            modWhere = new StringBuilder(whereClause);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI");
        }
        rowsAffected = sqlDB.update(tableName, values, modWhere.toString(), whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
	}
}
