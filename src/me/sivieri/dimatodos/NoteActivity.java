package me.sivieri.dimatodos;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class NoteActivity extends ActionBarActivity {

	public static final String LOCATION = "location";
	public static final String EDIT = "edit";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor_main);
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		Bundle extras = intent.getExtras();
		NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentById(R.id.notefragment);
		noteFragment.updateContent(action, type, extras);
	}

	@Override
	public void finish() {
		NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentById(R.id.notefragment);
		if (noteFragment.exitingEdit()) {
			super.finish();
		}
	}

}
