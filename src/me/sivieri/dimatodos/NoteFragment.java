package me.sivieri.dimatodos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.common.io.ByteStreams;

public class NoteFragment extends Fragment implements CurrentEventLocationResult, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

	public static final String LOCATION = "location";
	public static final String EDIT = "edit";

	private static final int ADD_IMAGE_INTENT = 100;
	private static final int SHARED_TITLE_LIMIT = 15;

	private Uri uri = null;
	private boolean edit = false;
	private boolean getLocation = false;
	private LocationClient locationClient = null;
	private double latitude = 0;
	private double longitude = 0;
	private URI location = null;
	private ShareActionProvider shareActionProvider = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		this.locationClient = new LocationClient(getActivity(), this, this);
	}

	public void updateContent(String action, String type, Bundle extras) {
		TextView titleText = (TextView) getActivity().findViewById(R.id.titleText);
		TextView contentText = (TextView) getActivity().findViewById(R.id.contentText);
		TextView locationText = (TextView) getActivity().findViewById(R.id.locationText);
		EditText titleTextEdit = (EditText) getActivity().findViewById(R.id.titleTextEdit);
		EditText contentTextEdit = (EditText) getActivity().findViewById(R.id.contentTextEdit);
		EditText locationTextEdit = (EditText) getActivity().findViewById(R.id.locationTextEdit);
		contentText.setMovementMethod(new ScrollingMovementMethod());
		contentTextEdit.setMovementMethod(new ScrollingMovementMethod());
		if (extras != null) {
			if (action != null && action.equals(Intent.ACTION_SEND) && type != null && type.startsWith("text")) {
				// coming from external sources
				String sharedText = extras.getString(Intent.EXTRA_TEXT);
				if (sharedText != null) {
					String sharedTitle = sharedText.substring(0, sharedText.length() >= SHARED_TITLE_LIMIT ? SHARED_TITLE_LIMIT : sharedText.length()).concat("...");
					titleText.setText(sharedTitle);
					contentText.setText(sharedText);
					titleTextEdit.setText(sharedTitle);
					contentTextEdit.setText(sharedText);
					this.edit = true;
					moveToEdit();
					launchCalendarSearch();
				}
			}
			else {
				this.uri = (Uri) extras.getParcelable(MainFragment.NOTE_ID);
				this.edit = extras.getBoolean(EDIT);
				this.getLocation = extras.getBoolean(LOCATION);
				if (this.uri != null) {
					String[] projection = { NotesOpenHelper.KEY, NotesOpenHelper.VALUE, NotesOpenHelper.LAT, NotesOpenHelper.LNG, NotesOpenHelper.LOCATION };
					Cursor cursor = getActivity().getContentResolver().query(this.uri, projection, null, null, null);
					if (cursor != null) {
						cursor.moveToFirst();
						String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesOpenHelper.KEY));
						String content = cursor.getString(cursor.getColumnIndexOrThrow(NotesOpenHelper.VALUE));
						String location = cursor.getString(cursor.getColumnIndexOrThrow(NotesOpenHelper.LOCATION));
						this.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesOpenHelper.LAT));
						this.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesOpenHelper.LNG));
						titleText.setText(title);
						contentText.setText(content);
						locationText.setText(location);
						titleTextEdit.setText(title);
						contentTextEdit.setText(content);
						locationTextEdit.setText(location);
						cursor.close();
					}
				}
				if (this.edit) {
					titleText.setText("");
					contentText.setText("");
					titleTextEdit.setText("");
					contentTextEdit.setText("");
					this.latitude = 0;
					this.longitude = 0;
					moveToEdit();
					launchCalendarSearch();
				}
			}
		}
		ImageButton button = (ImageButton) getActivity().findViewById(R.id.editButton);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				NoteFragment.this.edit = true;
				moveToEdit();
			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (this.getLocation) {
			this.locationClient.connect();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.editor_fragment, container);

		return view;
	}

	private void launchCalendarSearch() {
		if (this.uri == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			Log.i(MainActivity.TAG, "Launching the calendar task...");
			Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			long current = utc.getTimeInMillis();
			CurrentEventLocationTask task = new CurrentEventLocationTask();
			task.setDelegate(this);
			task.setContentResolver(getActivity().getContentResolver());
			task.execute(current);
		}
	}

	public boolean exitingEdit() {
		if (this.edit) {
			EditText titleTextEdit = (EditText) getActivity().findViewById(R.id.titleTextEdit);
			EditText contentTextEdit = (EditText) getActivity().findViewById(R.id.contentTextEdit);
			EditText locationTextEdit = (EditText) getActivity().findViewById(R.id.locationTextEdit);
			String title = titleTextEdit.getText().toString();
			String content = contentTextEdit.getText().toString();
			String location = locationTextEdit.getText().toString();
			if (title.length() == 0 && content.length() == 0) {
				if (this.uri == null) {
					Toast.makeText(getActivity().getApplicationContext(), getString(R.string.note_discarded), Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getActivity().getApplicationContext(), getString(R.string.note_restored), Toast.LENGTH_SHORT).show();
				}
				return true;
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
				if (location.length() != 0) {
					values.put(NotesOpenHelper.LOCATION, location);
				}
				if (this.uri == null) {
					Uri partial = getActivity().getContentResolver().insert(NotesContentProvider.CONTENT_URI, values);
					this.uri = Uri.parse("content://" + NotesContentProvider.AUTHORITY + "/" + partial);
					Log.d(MainActivity.TAG, this.uri.toString());
				}
				else {
					getActivity().getContentResolver().update(this.uri, values, null, null);
				}
				TextView titleText = (TextView) getActivity().findViewById(R.id.titleText);
				TextView contentText = (TextView) getActivity().findViewById(R.id.contentText);
				TextView locationText = (TextView) getActivity().findViewById(R.id.locationText);
				titleText.setText(title);
				contentText.setText(content);
				locationText.setText(location);
				moveToView();
				this.edit = false;
				Toast.makeText(getActivity().getApplicationContext(), getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		else {
			return true;
		}
	}

	@Override
	public void onPause() {
		if (this.getLocation) {
			this.locationClient.disconnect();
		}

		super.onPause();
	}

	private void moveToEdit() {
		ViewSwitcher titleView = (ViewSwitcher) getActivity().findViewById(R.id.titleView);
		ViewSwitcher contentView = (ViewSwitcher) getActivity().findViewById(R.id.contentView);
		ViewSwitcher locationView = (ViewSwitcher) getActivity().findViewById(R.id.locationView);
		titleView.showNext();
		contentView.showNext();
		locationView.showNext();
		getActivity().setTitle(getString(R.string.note_activity_edit));
	}

	private void moveToView() {
		ViewSwitcher titleView = (ViewSwitcher) getActivity().findViewById(R.id.titleView);
		ViewSwitcher contentView = (ViewSwitcher) getActivity().findViewById(R.id.contentView);
		ViewSwitcher locationView = (ViewSwitcher) getActivity().findViewById(R.id.locationView);
		titleView.showPrevious();
		contentView.showPrevious();
		locationView.showPrevious();
		getActivity().setTitle(getString(R.string.note_activity_view));
		prepareIntent();
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.editor, menu);
		MenuItem menuItem = menu.findItem(R.id.share_note);
		this.shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
		prepareIntent();
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
			case R.id.show_attachments:
				showAttachments();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void prepareIntent() {
		TextView titleText = (TextView) getActivity().findViewById(R.id.titleText);
		TextView contentText = (TextView) getActivity().findViewById(R.id.contentText);
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, titleText.getText().toString() + "\n" + contentText.getText().toString());
		sendIntent.setType("text/plain");
		this.shareActionProvider.setShareIntent(sendIntent);
	}

	private void showAttachments() {
		DialogFragment dialog = AttachmentDialogFragment.newInstance(this.uri.getLastPathSegment());
		dialog.show(getActivity().getSupportFragmentManager(), "dialog");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_IMAGE_INTENT) {
			if (resultCode == Activity.RESULT_OK) {
				if (data != null && data.getData() != null) {
					/*
					 * If so, then the image is NOT where it is supposed to be:
					 * it looks like those phones that respect the ExtraOutput
					 * field give us NULL, while the others don't... So we copy
					 * the file where we want it
					 */
					try {
						InputStream input = getActivity().getContentResolver().openInputStream(data.getData());
						FileOutputStream output = new FileOutputStream(new File(this.location));
						ByteStreams.copy(input, output);
						input.close();
						output.close();
					}
					catch (Exception e) {
						Toast.makeText(getActivity(), "Image not saved where we wanted it - sorry", Toast.LENGTH_LONG).show();
					}
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(getString(R.string.picture_dialog_title));
				builder.setMessage(getString(R.string.picture_dialog_message));
				final EditText titleText = new EditText(getActivity());
				titleText.setText(getString(R.string.picture__dialog_default));
				builder.setView(titleText);
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String title = titleText.getText().toString();
						ContentValues values = new ContentValues();
						values.put(NotesOpenHelper.IMG_NAME, NoteFragment.this.location.toString());
						values.put(NotesOpenHelper.IMG_NOTE_ID, NoteFragment.this.uri.getLastPathSegment());
						values.put(NotesOpenHelper.IMG_TITLE, title);
						Log.d(MainActivity.TAG, NoteFragment.this.uri.getLastPathSegment());
						Uri partial = getActivity().getContentResolver().insert(CameraContentProvider.CONTENT_URI, values);
						NoteFragment.this.uri = Uri.parse("content://" + CameraContentProvider.AUTHORITY + "/" + partial);
						Log.d(MainActivity.TAG, NoteFragment.this.uri.toString());
						Toast.makeText(getActivity(), "Image added", Toast.LENGTH_SHORT).show();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						File file = new File(NoteFragment.this.location);
						file.delete();
					}
				});
				builder.show();
			}
			else if (resultCode == Activity.RESULT_CANCELED) {
				// Cancelled: ok, no more stuff to do
			}
			else {
				Log.e(MainActivity.TAG, "Error in taking the picute");
				Toast.makeText(getActivity(), "Error in taking the camera", Toast.LENGTH_LONG).show();
			}
		}
	}

	private void addImage() {
		// Prepare environment to save the file
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Log.w(MainActivity.TAG, "Unable to use external storage: " + Environment.getExternalStorageState());
			Toast.makeText(getActivity(), "External storage not mounted: cannot take picture", Toast.LENGTH_LONG).show();
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
							Toast.makeText(getActivity(), "Unable to write in picture directory", Toast.LENGTH_LONG).show();
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

	@Override
	public void processResult(String result) {
		EditText locationTextEdit = (EditText) getActivity().findViewById(R.id.locationTextEdit);
		locationTextEdit.setText(result);
	}

}
