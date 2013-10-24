package me.sivieri.dimatodos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotesOpenHelper extends SQLiteOpenHelper {

	static final String DATABASE_NAME = "dimanotes";
	static final int DATABASE_VERSION = 1;
	static final String ID = "_id";
	static final String KEY = "title";
	static final String VALUE = "content";
	static final String TIMESTAMP = "ts";
	static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_NAME + " (" + ID + " INTEGER PRIMARY KEY," + KEY + " TEXT, " + VALUE + " TEXT," + TIMESTAMP
	        + "DATETIME DEFAULT CURRENT_TIMESTAMP);";

	public NotesOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// still first version...
	}

}
