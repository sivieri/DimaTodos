package me.sivieri.dimatodos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotesOpenHelper extends SQLiteOpenHelper {

	static final String TABLE_NAME = "dimanotes";
	static final int TABLE_VERSION = 4;
	static final String ID = "_id";
	static final String KEY = "title";
	static final String VALUE = "content";
	static final String TIMESTAMP = "ts";
	static final String LAT = "lat";
	static final String LNG = "lng";
	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY + " TEXT, " + VALUE + " TEXT," + TIMESTAMP
	        + "DATETIME DEFAULT CURRENT_TIMESTAMP, " + LAT + " double DEFAULT 0, " + LNG + " double DEFAULT 0);";
	private static final String WELCOME_MSG = "INSERT INTO " + TABLE_NAME + "(" + KEY + ", " + VALUE
	        + ") VALUES ('Welcome', 'Welcome to the todo/notes app for Design and Implementation of Mobile Applications (DIMA) course!')";
	private static final String UPGRADE_3_TO_4 = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + LAT + " double DEFAULT 0;" + "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + LNG + " double DEFAULT 0;";

	public NotesOpenHelper(Context context) {
		super(context, TABLE_NAME, null, TABLE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL(TABLE_CREATE);
		arg0.execSQL(WELCOME_MSG);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		if (arg1 == 3 && arg2 == 4) {
			arg0.execSQL(UPGRADE_3_TO_4);
		}
		else {
			arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(arg0);

		}
	}

}
