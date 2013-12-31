package me.sivieri.dimatodos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SidebarFragment extends Fragment {
	private static final String WIKITIONARY_ENDPOINT = "http://en.wiktionary.org/w/api.php";
	private static final String USER_AGENT = "DimaTodos/1.4 (PoliMi teaching app; alessandro.sivieri@polimi.it)";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.sidebar_fragment, container);
	}

	@Override
	public void onStart() {
		super.onStart();
		final Spinner spinner = (Spinner) getActivity().findViewById(R.id.languageSpinner);
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpParams httpParams = new BasicHttpParams();
				httpParams.setParameter("action", "query");
				httpParams.setParameter("meta", "siteinfo");
				httpParams.setParameter("siprop", "languages");
				httpParams.setParameter("format", "json");
				try {
					String result = makeHttpRequest(httpParams);
					JSONObject main = new JSONObject(result);
					JSONObject query = main.getJSONObject("query");
					JSONArray languages = query.getJSONArray("languages");
					Map<String, String> languagesMap = new HashMap<String, String>();
					for (int i = 0; i < languages.length(); ++i) {
						JSONObject language = languages.getJSONObject(i);
						languagesMap.put(language.getString("code"), language.getString("*"));
					}
					String currentCode = Locale.getDefault().getLanguage();
					final String currentLanguage = languagesMap.get(currentCode);
					List<String> values = new ArrayList<String>(languagesMap.values());
					final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, values);
					spinner.post(new Runnable() {

						@Override
						public void run() {
							spinner.setAdapter(adapter);
							spinner.setSelection(adapter.getPosition(currentLanguage));
						}

					});
				}
				catch (Exception e) {
					Log.e(MainActivity.TAG, e.getLocalizedMessage());
				}
			}

		}).start();
	}

	public String makeHttpRequest(HttpParams httpParams) throws ClientProtocolException, IOException {
		StringBuilder result = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(WIKITIONARY_ENDPOINT);
		httpGet.setParams(httpParams);
		httpGet.addHeader("User-Agent", USER_AGENT);
		HttpResponse httpResponse = httpClient.execute(httpGet);
		StatusLine statusLine = httpResponse.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			HttpEntity entity = httpResponse.getEntity();
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
		}
		else {
			Log.e(MainActivity.TAG, "Error: " + statusLine.getStatusCode() + ", " + statusLine.getReasonPhrase());
		}

		return result.toString();
	}

}
