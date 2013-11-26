package me.sivieri.dimatodos;

import android.os.AsyncTask;

public class CurrentEventLocationTask extends AsyncTask<Long, Void, String> {

	private CurrentEventLocationResult delegate = null;

	@Override
	protected String doInBackground(Long... arg0) {
		long current = arg0[0];

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (this.delegate != null && result != null) {
			this.delegate.processResult(result);
		}
	}

	public void setDelegate(CurrentEventLocationResult delegate) {
		this.delegate = delegate;
	}

}
