package me.sivieri.dimatodos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.common.io.ByteStreams;

public class NoteActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

	public static final String LOCATION = "location";
	public static final String EDIT = "edit";

	private static final int ADD_IMAGE_INTENT = 100;

	private Uri uri = null;
	private boolean edit = false;
	private boolean getLocation = false;
	private LocationClient locationClient = null;
	private double latitude = 0;
	private double longitude = 0;
	private URI location = null;

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
				String[] projection = { NotesOpenHelper.KEY, NotesOpenHelper.VALUE, NotesOpenHelper.LAT, NotesOpenHelper.LNG };
				Cursor cursor = getContentResolver().query(this.uri, projection, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();
					String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesOpenHelper.KEY));
					String content = cursor.getString(cursor.getColumnIndexOrThrow(NotesOpenHelper.VALUE));
					this.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesOpenHelper.LAT));
					this.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesOpenHelper.LNG));
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
					super.finish();
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
				values.put(NotesOpenHelper.LAT, this.latitude);
				values.put(NotesOpenHelper.LNG, this.longitude);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.editor, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.show_location:
				showLocation();
				return true;
			case R.id.add_image:
				addImage();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_IMAGE_INTENT) {
			if (resultCode == RESULT_OK) {
				if (data != null && data.getData() != null) {
					/*
					 * If so, then the image is NOT where it is supposed to be:
					 * it looks like those phones that respect the ExtraOutput
					 * field give us NULL, while the others don't... So we copy
					 * the file where we want it
					 */
					try {
						InputStream input = getContentResolver().openInputStream(data.getData());
						FileOutputStream output = new FileOutputStream(new File(this.location));
						ByteStreams.copy(input, output);
						input.close();
						output.close();
					}
					catch (Exception e) {
						Toast.makeText(this, "Image not saved where we want it - sorry", Toast.LENGTH_LONG).show();
					}
				}
				ContentValues values = new ContentValues();
				values.put(NotesOpenHelper.IMG_NAME, this.location.toString());
				values.put(NotesOpenHelper.IMG_NOTE_ID, this.uri.getLastPathSegment());
				Log.d(MainActivity.TAG, this.uri.getLastPathSegment());
				Uri partial = getContentResolver().insert(CameraContentProvider.CONTENT_URI, values);
				this.uri = Uri.parse("content://" + CameraContentProvider.AUTHORITY + "/" + partial);
				Log.d(MainActivity.TAG, this.uri.toString());
				Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show();
			}
			else if (resultCode == RESULT_CANCELED) {
				// Cancelled: ok, no more stuff to do
			}
			else {
				Log.e(MainActivity.TAG, "Error in taking the picute");
				Toast.makeText(this, "Error in taking the camera", Toast.LENGTH_LONG).show();
			}
		}
	}

	private void addImage() {
		// Prepare environment to save the file
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Log.w(MainActivity.TAG, "Unable to use external storage: " + Environment.getExternalStorageState());
			Toast.makeText(this, "External storage not mounted: cannot take picture", Toast.LENGTH_LONG).show();
		}
		else {
			File locationDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DimaTodos");
			if (!locationDir.exists()) {
				if (!locationDir.mkdir()) {
					// crap: fallback...
					Log.w(MainActivity.TAG, "Unable to create image directory " + locationDir.toURI());
					locationDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DimaTodos");
					if (!locationDir.exists()) {
						if (!locationDir.mkdir()) {
							Log.e(MainActivity.TAG, "Unable to create secondary image directory " + locationDir.toURI());
							Toast.makeText(this, "Unable to write in picture directory", Toast.LENGTH_LONG).show();
							return;
						}
					}
				}
			}
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			this.location = new File(locationDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg").toURI();
			Log.i(MainActivity.TAG, "Picture will be saved at: " + this.location);

			// Intent
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, this.location);
			startActivityForResult(intent, ADD_IMAGE_INTENT);
		}
	}

	private void showLocation() {
		String uri = String.format(Locale.ENGLISH, "geo:%f,%f", this.latitude, this.longitude);
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		startActivity(intent);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(MainActivity.TAG, "Location error " + result.getErrorCode());
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Location location = this.locationClient.getLastLocation();
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
		Log.i(MainActivity.TAG, "Got a location at " + this.latitude + ", " + this.longitude);
		this.locationClient.disconnect();
	}

	@Override
	public void onDisconnected() {
		Log.i(MainActivity.TAG, "Location disconnected");
	}

}
