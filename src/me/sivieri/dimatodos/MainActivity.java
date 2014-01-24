package me.sivieri.dimatodos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.sivieri.dimatodos.noteendpoint.Noteendpoint;
import me.sivieri.dimatodos.noteendpoint.model.CollectionResponseNote;
import me.sivieri.dimatodos.noteendpoint.model.Note;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;

public class MainActivity extends ActionBarActivity implements OnNoteSelectedListener {

	public static final String TAG = "dimatodos";

	private static final int PLAY_ERROR = 1;

	private LocationState locationState = LocationState.FIRST;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListFragment listFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.notesfragment);
		this.progressBar = new ProgressBar(this);
		this.progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		this.progressBar.setIndeterminate(true);
		listFragment.getListView().setEmptyView(this.progressBar);
		ViewGroup root = (ViewGroup) this.findViewById(android.R.id.content);
		root.addView(this.progressBar);
	}

	@Override
	protected void onResume() {
		super.onResume();
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS && this.locationState == LocationState.FIRST) {
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_ERROR).show();
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		switch (arg0) {
			case PLAY_ERROR:
				switch (arg1) {
					case Activity.RESULT_OK:
						this.locationState = LocationState.SET;
						break;
					default:
						this.locationState = LocationState.UNSET;
						break;
				}
				break;
			default:
				// really?
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_note:
				NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentById(R.id.notefragment);
				if (noteFragment == null) {
					Intent i = new Intent(this, NoteActivity.class);
					i.putExtra(NoteActivity.EDIT, true);
					if (this.locationState == LocationState.SET) {
						i.putExtra(NoteActivity.LOCATION, true);
					}
					startActivity(i);
				}
				else {
					Bundle extras = new Bundle();
					extras.putBoolean(NoteActivity.EDIT, true);
					if (this.locationState == LocationState.SET) {
						extras.putBoolean(NoteActivity.LOCATION, true);
					}
					noteFragment.updateContent(null, null, extras);
				}
				return true;
			case R.id.notes_sync:
				new NotesCloudAsync().execute(NotesOpenHelper.ID, NotesOpenHelper.KEY, NotesOpenHelper.VALUE, NotesOpenHelper.TIMESTAMP, NotesOpenHelper.LOCATION, NotesOpenHelper.LAT,
				        NotesOpenHelper.LNG);
				return true;
			case R.id.settings:
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public LocationState getLocationState() {
		return this.locationState;
	}

	static enum LocationState {
		FIRST, SET, UNSET
	}

	@Override
	public void onNoteSelected(Uri uri) {
		NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentById(R.id.notefragment);
		if (noteFragment == null) {
			Intent i = new Intent(this, NoteActivity.class);
			i.putExtra(MainFragment.NOTE_ID, uri);
			if (this.locationState == LocationState.SET) {
				i.putExtra(NoteActivity.LOCATION, true);
			}
			startActivity(i);
		}
		else {
			Bundle extras = new Bundle();
			extras.putParcelable(MainFragment.NOTE_ID, uri);
			if (this.locationState == LocationState.SET) {
				extras.putBoolean(NoteActivity.LOCATION, true);
			}
			noteFragment.updateContent(null, null, extras);
		}
	}

	@Override
	public void finish() {
		NoteFragment noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentById(R.id.notefragment);
		if (noteFragment == null || noteFragment.exitingEdit()) {
			super.finish();
		}
	}

	private class NotesCloudAsync extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... arg0) {
			try {
				Noteendpoint.Builder builder = new Noteendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
				builder = CloudEndpointUtils.updateBuilder(builder);
				Noteendpoint endpoint = builder.build();
				// get the notes from local storage
				List<Note> notes = new ArrayList<Note>();
				Cursor notesdb = getContentResolver().query(NotesContentProvider.CONTENT_URI, arg0, null, null, null);
				if (notesdb.getCount() == 0) {
					return null;
				}
				for (notesdb.moveToFirst(); !notesdb.isAfterLast(); notesdb.moveToNext()) {
					Note note = new Note();
					note.setNoteId(notesdb.getInt(notesdb.getColumnIndexOrThrow(NotesOpenHelper.ID)));
					note.setDeviceId(Settings.Secure.ANDROID_ID);
					note.setTitle(notesdb.getString(notesdb.getColumnIndexOrThrow(NotesOpenHelper.KEY)));
					note.setContent(notesdb.getString(notesdb.getColumnIndexOrThrow(NotesOpenHelper.VALUE)));
					note.setTimestamp(notesdb.getString(notesdb.getColumnIndexOrThrow(NotesOpenHelper.TIMESTAMP)));
					note.setLocation(notesdb.getString(notesdb.getColumnIndexOrThrow(NotesOpenHelper.LOCATION)));
					note.setLat(notesdb.getDouble(notesdb.getColumnIndexOrThrow(NotesOpenHelper.LAT)));
					note.setLng(notesdb.getDouble(notesdb.getColumnIndexOrThrow(NotesOpenHelper.LNG)));
					notes.add(note);
				}
				System.out.println("Local notes: " + notes.size());
				// get the notes from the cloud
				CollectionResponseNote response = endpoint.listNote().execute();
				List<Note> remoteNotes = response.getItems();
				if (remoteNotes == null) {
					remoteNotes = new ArrayList<Note>();
				}
				System.out.println("Remote notes: " + remoteNotes.size());
				// insert locally the new remote notes
				List<Note> insertRemoteNotes = diff(remoteNotes, notes);
				System.out.println("Notes to be inserted locally: " + insertRemoteNotes.size());
				ContentValues[] values = new ContentValues[insertRemoteNotes.size()];
				for (int i = 0; i < insertRemoteNotes.size(); ++i) {
					ContentValues v = new ContentValues();
					Note n = insertRemoteNotes.get(i);
					v.put(NotesOpenHelper.KEY, n.getTitle());
					v.put(NotesOpenHelper.VALUE, n.getContent());
					v.put(NotesOpenHelper.TIMESTAMP, n.getTimestamp());
					v.put(NotesOpenHelper.LOCATION, n.getLocation());
					v.put(NotesOpenHelper.LAT, n.getLat());
					v.put(NotesOpenHelper.LNG, n.getLng());
					values[i] = v;
				}
				getContentResolver().bulkInsert(NotesContentProvider.CONTENT_URI, values);
				// upload the new local notes
				List<Note> insertLocalNotes = diff(notes, remoteNotes);
				System.out.println("Notes to be uploaded: " + insertLocalNotes.size());
				for (Note note : insertLocalNotes) {
					endpoint.insertNote(note).execute();
					System.out.println("Note uploaded");
				}
			}
			catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(MainActivity.this, getString(R.string.notes_uploaded), Toast.LENGTH_LONG).show();
		}

		private List<Note> diff(List<Note> list1, List<Note> list2) {
			List<Note> result = new ArrayList<Note>();
			boolean insert;

			for (Note note1 : list1) {
				insert = true;
				for (Note note2 : list2) {
					if (note1.getDeviceId().equals(note2.getDeviceId()) && note1.getNoteId().equals(note2.getNoteId())) {
						insert = false;
					}
				}
				if (insert) {
					result.add(note1);
				}
			}

			return result;
		}

	}

}
