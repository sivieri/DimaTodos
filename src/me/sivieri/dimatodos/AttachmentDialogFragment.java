package me.sivieri.dimatodos;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.webkit.MimeTypeMap;

public class AttachmentDialogFragment extends DialogFragment {

	private static final String NOTEID = "noteid";

	public static DialogFragment newInstance(String lastPathSegment) {
		AttachmentDialogFragment dialog = new AttachmentDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString(NOTEID, lastPathSegment);
		dialog.setArguments(bundle);

		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String id = getArguments().getString(NOTEID);
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(getActivity());
		String[] projection = { NotesOpenHelper.IMG_ID, NotesOpenHelper.IMG_NAME, NotesOpenHelper.IMG_NOTE_ID };
		String selection = NotesOpenHelper.IMG_NOTE_ID + " = ?";
		String[] selectionArgs = { id };
		final Cursor c = getActivity().getContentResolver().query(CameraContentProvider.CONTENT_URI, projection, selection, selectionArgs, null);
		builder.setCursor(c, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				c.moveToPosition(which);
				String name = c.getString(c.getColumnIndexOrThrow(NotesOpenHelper.IMG_NAME));
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				File file = new File(name);
				MimeTypeMap mime = MimeTypeMap.getSingleton();
				String ext = file.getName().substring(file.getName().indexOf(".") + 1);
				String type = mime.getMimeTypeFromExtension(ext);
				intent.setDataAndType(Uri.fromFile(file), type);
				startActivity(intent);
			}

		}, NotesOpenHelper.IMG_NAME);
		return builder.create();
	}
}
