package me.sivieri.dimatodos;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class NoteActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
	}

	@Override
	public void finish() {
		EditText titleText = (EditText) findViewById(R.id.titleText);
		EditText contentText = (EditText) findViewById(R.id.contentText);
		String title = titleText.getText().toString();
		String content = contentText.getText().toString();
		if (title.length() == 0 && content.length() == 0) {
			Toast.makeText(getApplicationContext(), getString(R.string.note_discarded), Toast.LENGTH_SHORT).show();
		}
		else {
			if (title.length() == 0 && content.length() != 0) {
				title = substring(content, 10);
			}
			ContentValues values = new ContentValues();
			values.put(NotesOpenHelper.KEY, title);
			values.put(NotesOpenHelper.VALUE, content);
			getContentResolver().insert(NotesContentProvider.CONTENT_URI, values);
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
