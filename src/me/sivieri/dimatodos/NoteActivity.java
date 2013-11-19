package me.sivieri.dimatodos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

public class NoteActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

	public static final String LOCATION = "location";
	public static final String EDIT = "edit";

	private Uri uri = null;
	private boolean edit = false;
	private boolean getLocation = false;
	private LocationClient locationClient = null;
	private Location location = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.locationClient = new LocationClient(this, this, this);
		setContentView(R.layout.editor);
		TextView titleText = (TextView) findViewById(R.id.titleText);
		TextView contentText = (TextView) findViewById(R.id.contentText);
		EditText titleTextEdit = (EditText) findViewById(R.id.titleTextEdit);
		EditText contentTextEdit = (EditText) findViewById(R.id.contentTextEdit);
		final ViewSwitcher titleView = (ViewSwitcher) findViewById(R.id.titleView);
		final ViewSwitcher contentView = (ViewSwitcher) findViewById(R.id.contentView);
		titleText.setScroller(new Scroller(this));
		titleText.setVerticalScrollBarEnabled(true);
		titleText.setMovementMethod(new ScrollingMovementMethod());
		contentText.setScroller(new Scroller(this));
		contentText.setVerticalScrollBarEnabled(true);
		contentText.setMovementMethod(new ScrollingMovementMethod());
		titleTextEdit.setScroller(new Scroller(this));
		titleTextEdit.setVerticalScrollBarEnabled(true);
		titleTextEdit.setMovementMethod(new ScrollingMovementMethod());
		contentTextEdit.setScroller(new Scroller(this));
		contentTextEdit.setVerticalScrollBarEnabled(true);
		contentTextEdit.setMovementMethod(new ScrollingMovementMethod());
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			this.uri = (Uri) extras.getParcelable(NoteListFragment.NOTE_ID);
			this.edit = extras.getBoolean(EDIT);
			this.getLocation = extras.getBoolean(LOCATION);
			if (this.uri != null) {
				String[] projection = { NotesOpenHelper.KEY, NotesOpenHelper.VALUE };
				Cursor cursor = getContentResolver().query(this.uri, projection, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();
					String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesOpenHelper.KEY));
					String content = cursor.getString(cursor.getColumnIndexOrThrow(NotesOpenHelper.VALUE));
					titleText.setText(title);
					contentText.setText(content);
					titleTextEdit.setText(title);
					contentTextEdit.setText(content);
					cursor.close();
				}
			}
			if (this.edit) {
				titleView.showNext();
				contentView.showNext();
			}
		}
		ImageButton button = (ImageButton) findViewById(R.id.editButton);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				NoteActivity.this.edit = true;
				titleView.showNext();
				contentView.showNext();
			}

		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (this.getLocation) {
			this.locationClient.connect();
		}
	}

	@Override
	protected void onStop() {
		if (this.getLocation) {
			this.locationClient.disconnect();
		}
		super.onStop();
	}

	@Override
	public void finish() {
		if (!this.edit) {
			super.finish();
		}
		else {
			EditText titleTextEdit = (EditText) findViewById(R.id.titleTextEdit);
			EditText contentTextEdit = (EditText) findViewById(R.id.contentTextEdit);
			String title = titleTextEdit.getText().toString();
			String content = contentTextEdit.getText().toString();
			if (title.length() == 0 && content.length() == 0) {
				if (this.uri == null) {
					Toast.makeText(getApplicationContext(), getString(R.string.note_discarded), Toast.LENGTH_SHORT).show();
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(getString(R.string.delete_dialog_title));
					builder.setMessage(getString(R.string.delete_dialog_content));
					builder.setPositiveButton(getString(R.string.delete_dialog_yes), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							getContentResolver().delete(NoteActivity.this.uri, null, null);
							NoteActivity.super.finish();
						}

					});
					builder.setNegativeButton(getString(R.string.delete_dialog_no), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ViewSwitcher titleView = (ViewSwitcher) findViewById(R.id.titleView);
							ViewSwitcher contentView = (ViewSwitcher) findViewById(R.id.contentView);
							titleView.showPrevious();
							contentView.showPrevious();
							NoteActivity.this.edit = false;
						}

					});
					builder.show();
				}
			}
			else {
				if (title.length() == 0 && content.length() != 0) {
					title = substring(content, 10);
				}
				ContentValues values = new ContentValues();
				values.put(NotesOpenHelper.KEY, title);
				values.put(NotesOpenHelper.VALUE, content);
				if (this.location != null) {
					values.put(NotesOpenHelper.LAT, this.location.getLatitude());
					values.put(NotesOpenHelper.LNG, this.location.getLongitude());
				}
				if (this.uri == null) {
					Uri partial = getContentResolver().insert(NotesContentProvider.CONTENT_URI, values);
					this.uri = Uri.parse("content://" + NotesContentProvider.AUTHORITY + "/" + partial);
					Log.d(MainActivity.TAG, this.uri.toString());
				}
				else {
					getContentResolver().update(this.uri, values, null, null);
				}
				TextView titleText = (TextView) findViewById(R.id.titleText);
				TextView contentText = (TextView) findViewById(R.id.contentText);
				titleText.setText(title);
				contentText.setText(content);
				ViewSwitcher titleView = (ViewSwitcher) findViewById(R.id.titleView);
				ViewSwitcher contentView = (ViewSwitcher) findViewById(R.id.contentView);
				titleView.showPrevious();
				contentView.showPrevious();
				this.edit = false;
				Toast.makeText(getApplicationContext(), getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private String substring(String string, int length) {
		if (string.length() >= length) {
			return string.substring(0, length);
		}
		else {
			return string;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(MainActivity.TAG, "Location error " + result.getErrorCode());
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		this.location = this.locationClient.getLastLocation();
		this.locationClient.disconnect();
	}

	@Override
	public void onDisconnected() {
		Log.i(MainActivity.TAG, "Location disconnected");
	}

}
