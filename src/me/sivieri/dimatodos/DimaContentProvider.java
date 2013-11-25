package me.sivieri.dimatodos;

import android.content.ContentProvider;

public abstract class DimaContentProvider extends ContentProvider {

	static final String TAG = "dimatodos";
	protected NotesOpenHelper database;

	@Override
	public boolean onCreate() {
		this.database = new NotesOpenHelper(getContext());

		return false;
	}

}
