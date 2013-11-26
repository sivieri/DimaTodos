package me.sivieri.dimatodos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NotesOpenHelper extends SQLiteOpenHelper {

	static final String TABLE_NAME = "dimanotes";
	static final int TABLE_VERSION = 7;
	static final String ID = "_id";
	static final String KEY = "title";
	static final String VALUE = "content";
	static final String TIMESTAMP = "ts";
	static final String LAT = "lat";
	static final String LNG = "lng";
	static final String LOCATION = "location";

	static final String TABLE_IMG = "dimacamera";
	static final String IMG_ID = "_id";
	static final String IMG_NAME = "filename";
	static final String IMG_NOTE_ID = "noteid";
	static final String IMG_TITLE = "title";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY + " TEXT, " + VALUE + " TEXT," + TIMESTAMP
	        + " DATETIME DEFAULT CURRENT_TIMESTAMP, " + LAT + " double DEFAULT 0, " + LNG + " double DEFAULT 0, " + LOCATION + " TEXT);";
	private static final String IMG_TABLE_CREATE = "CREATE TABLE " + TABLE_IMG + " (" + IMG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + IMG_NAME + " TEXT, " + IMG_NOTE_ID + " INTEGER, " + IMG_TITLE
	        + " TEXT, FOREIGN KEY (" + IMG_NOTE_ID + ")  REFERENCES " + TABLE_NAME + " (" + ID + "));";
	private static final String WELCOME_MSG = "INSERT INTO " + TABLE_NAME + "(" + KEY + ", " + VALUE
	        + ") VALUES ('Welcome', 'Welcome to the todo/notes app for Design and Implementation of Mobile Applications (DIMA) course!')";
	private static final String UPGRADE_3_TO_4_P1 = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + LAT + " double DEFAULT 0;";
	private static final String UPGRADE_3_TO_4_P2 = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + LNG + " double DEFAULT 0;";
	private static final String UPGRADE_6_TO_7_P1 = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + LOCATION + " TEXT;";
	private static final String UPGRADE_6_TO_7_P2 = "ALTER TABLE " + TABLE_IMG + " ADD COLUMN " + IMG_TITLE + " TEXT;";

	public NotesOpenHelper(Context context) {
		super(context, TABLE_NAME, null, TABLE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		Log.i(MainActivity.TAG, "Creating the db");
		arg0.execSQL(TABLE_CREATE);
		arg0.execSQL(IMG_TABLE_CREATE);
		arg0.execSQL(WELCOME_MSG);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		Log.i(MainActivity.TAG, "Upgrade from " + arg1 + " to " + arg2);
		if (arg1 == 3 && arg2 == 4) {
			arg0.execSQL(UPGRADE_3_TO_4_P1);
			arg0.execSQL(UPGRADE_3_TO_4_P2);
		}
		else if (arg1 == 4 && (arg2 == 5 || arg2 == 6)) {
			arg0.execSQL(IMG_TABLE_CREATE);
		}
		else if (arg1 == 5 && arg2 == 6) {
			arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_IMG);
			arg0.execSQL(IMG_TABLE_CREATE);
		}
		else if (arg1 == 6 && arg2 == 7) {
			arg0.execSQL(UPGRADE_6_TO_7_P1);
			arg0.execSQL(UPGRADE_6_TO_7_P2);
		}
		else {
			arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(arg0);
		}
	}

}
