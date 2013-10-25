package me.sivieri.dimatodos;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class NoteActivity extends Activity {

	private Uri uri = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.uri = (Uri) extras.getParcelable(NoteListFragment.NOTE_ID);
			String[] projection = { NotesOpenHelper.KEY, NotesOpenHelper.VALUE };
			Cursor cursor = getContentResolver().query(this.uri, projection, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesOpenHelper.KEY));
				String content = cursor.getString(cursor.getColumnIndexOrThrow(NotesOpenHelper.VALUE));
				EditText titleText = (EditText) findViewById(R.id.titleText);
				EditText contentText = (EditText) findViewById(R.id.contentText);
				titleText.setText(title);
				contentText.setText(content);
				cursor.close();
			}
		}
	}

	@Override
	public void finish() {
		EditText titleText = (EditText) findViewById(R.id.titleText);
		EditText contentText = (EditText) findViewById(R.id.contentText);
		String title = titleText.getText().toString();
		String content = contentText.getText().toString();
		if (title.length() == 0 && content.length() == 0) {
			if (this.uri == null) {
				Toast.makeText(getApplicationContext(), getString(R.string.note_discarded), Toast.LENGTH_SHORT).show();
			}
			else {
				// note deleted maybe?
			}
		}
		else {
			if (title.length() == 0 && content.length() != 0) {
				title = substring(content, 10);
			}
			ContentValues values = new ContentValues();
			values.put(NotesOpenHelper.KEY, title);
			values.put(NotesOpenHelper.VALUE, content);
			if (this.uri == null) {
				getContentResolver().insert(NotesContentProvider.CONTENT_URI, values);
			}
			else {
				getContentResolver().update(this.uri, values, null, null);
			}
			Toast.makeText(getApplicationContext(), getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
		}
		super.finish();
	}

	String substring(String string, int length) {
		if (string.length() >= length) {
			return string.substring(0, length);
		}
		else {
			return string;
		}
	}

}
