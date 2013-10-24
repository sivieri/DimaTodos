package me.sivieri.dimatodos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotesOpenHelper extends SQLiteOpenHelper {

	static final String TABLE_NAME = "dimanotes";
	static final int TABLE_VERSION = 2;
	static final String ID = "_id";
	static final String KEY = "title";
	static final String VALUE = "content";
	static final String TIMESTAMP = "ts";
	static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY + " TEXT, " + VALUE + " TEXT," + TIMESTAMP
	        + "DATETIME DEFAULT CURRENT_TIMESTAMP);";

	public NotesOpenHelper(Context context) {
		super(context, TABLE_NAME, null, TABLE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(arg0);
	}

}
