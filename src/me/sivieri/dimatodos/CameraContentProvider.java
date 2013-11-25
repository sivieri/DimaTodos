package me.sivieri.dimatodos;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class CameraContentProvider extends DimaContentProvider {

	static final String AUTHORITY = "me.sivieri.dimatodos.cameracontentprovider";
	private static final String BASE_PATH = "picture";
	private static final int PICTURES = 10;
	private static final int PICTURE_ID = 20;

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/pictures";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/picture";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, PICTURES);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PICTURE_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = this.database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
			case PICTURES:
				rowsDeleted = sqlDB.delete(NotesOpenHelper.TABLE_IMG, selection, selectionArgs);
				break;
			case PICTURE_ID:
				String id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					rowsDeleted = sqlDB.delete(NotesOpenHelper.TABLE_IMG, NotesOpenHelper.IMG_ID + "=" + id, null);
				}
				else {
					rowsDeleted = sqlDB.delete(NotesOpenHelper.TABLE_IMG, NotesOpenHelper.IMG_ID + "=" + id + " and " + selection, selectionArgs);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);

		return rowsDeleted;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = this.database.getWritableDatabase();
		long id = 0;
		switch (uriType) {
			case PICTURES:
				id = sqlDB.insert(NotesOpenHelper.TABLE_IMG, null, values);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		checkColumns(projection);
		queryBuilder.setTables(NotesOpenHelper.TABLE_IMG);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
			case PICTURES:
				break;
			case PICTURE_ID:
				queryBuilder.appendWhere(NotesOpenHelper.IMG_ID + "=" + uri.getLastPathSegment());
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = this.database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = this.database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
			case PICTURES:
				rowsUpdated = sqlDB.update(NotesOpenHelper.TABLE_IMG, values, selection, selectionArgs);
				break;
			case PICTURE_ID:
				String id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection)) {
					rowsUpdated = sqlDB.update(NotesOpenHelper.TABLE_IMG, values, NotesOpenHelper.IMG_ID + "=" + id, null);
				}
				else {
					rowsUpdated = sqlDB.update(NotesOpenHelper.TABLE_IMG, values, NotesOpenHelper.IMG_ID + "=" + id + " and " + selection, selectionArgs);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);

		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = { NotesOpenHelper.IMG_ID, NotesOpenHelper.IMG_NAME, NotesOpenHelper.IMG_NOTE_ID };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}

}
