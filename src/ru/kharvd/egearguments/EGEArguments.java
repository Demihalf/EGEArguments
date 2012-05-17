/*
 Copyright (c) 2012        Valery Kharitonov <kharvd@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"),
 to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense,
 and/or sell copies of the Software, and to permit persons to whom the Software
 is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 DEALINGS IN THE SOFTWARE.
 */

package ru.kharvd.egearguments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EGEArguments extends ListActivity {
    private JSONArray mProblemGroups;
    private String mJSONAssetName;

    private boolean mExternalStorageAvailable;

    public final static String EXTRA_JSON = "ru.kharvd.egearguments.JSON";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TextView title = (TextView) findViewById(R.id.title);

        if (title != null) {
            title.setText(R.string.app_name);
            title.setSelected(true);
        }

        updateExternalStorageState();

        mJSONAssetName = getResources().getString(R.string.json_asset_name);

        populateList();

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                try {
                    Intent intent = new Intent(view.getContext(),
                            ProblemsActivity.class);
                    intent.putExtra(EXTRA_JSON,
                            mProblemGroups.getJSONObject(position).toString());
                    startActivity(intent);
                } catch (JSONException e) {
                    parseErrorToast(e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.refresh:
            populateList();
            return true;
        case R.id.settings:
            Intent prefsIntent = new Intent(this, PreferencesActivity.class);
            startActivity(prefsIntent);
            return true;
        case R.id.about:
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Loads JSON to mProblemGroups and populates the list with problem groups.
     */
    private void populateList() {
        try {
            mProblemGroups = new JSONArray(getJSONFromAsset());

            updateExternalStorageState();

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(this);

            if (prefs.getBoolean("user_arguments_preference", false)
                    && mExternalStorageAvailable) {
                loadUserArguments();
            }

            String[] strings = getProblemGroupList(mProblemGroups);

            setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
                    strings));
        } catch (IOException e) {
            ioErrorToast(e.getLocalizedMessage());
        } catch (JSONException e) {
            parseErrorToast(e.getLocalizedMessage());
        }
    }

    /**
     * Loads user problems from file and appends them to mProblemGroups
     */
    private void loadUserArguments() {
        try {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(this);

            String json = loadUserJSON(prefs.getString("user_arguments_path",
                    ""));
            JSONArray userProblems = new JSONArray(json);

            for (int i = 0; i < userProblems.length(); i++) {
                mProblemGroups.put(userProblems.get(i));
            }
        } catch (IOException e) {
            ioErrorToast(e.getLocalizedMessage());
        } catch (JSONException e) {
            parseErrorToast(e.getLocalizedMessage());
        }
    }

    /**
     * Loads user JSON raw string
     * 
     * @return Raw JSON string
     * @param fileName
     *            Name of the file, relative to
     *            Environment.getExternalStorageDirectory();
     */
    private String loadUserJSON(String fileName) throws IOException {
        updateExternalStorageState();
        File dir = Environment.getExternalStorageDirectory();
        File file = new File(dir, fileName);

        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        return getJSON(new FileInputStream(file));
    }

    /**
     * Loads raw JSON string with default arguments from assets.
     * 
     * @return Raw JSON string
     * @throws IOException
     */
    private String getJSONFromAsset() throws IOException {
        InputStream is = getAssets().open(mJSONAssetName);
        return getJSON(is);
    }

    /**
     * Loads text from {@code is} and returns a raw JSON string
     * 
     * @param is
     *            InputStream to read data from
     * @return Raw JSON string
     * @throws IOException
     */
    private String getJSON(InputStream is) throws IOException {
        InputStreamReader inputreader = new InputStreamReader(is);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        while ((line = buffreader.readLine()) != null) {
            text.append(line);
        }

        return text.toString();
    }

    private String[] getProblemGroupList(JSONArray problemGroups)
            throws JSONException {
        String[] strings = new String[problemGroups.length()];

        for (int i = 0; i < problemGroups.length(); i++) {
            JSONObject problemGroup = problemGroups.getJSONObject(i);
            strings[i] = problemGroup.getString("problemGroupName");
        }

        return strings;
    }

    private void updateExternalStorageState() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
        } else {
            mExternalStorageAvailable = false;
        }
    }

    private void ioErrorToast(String msg) {
        Toast.makeText(this, getString(R.string.file_error) + ": " + msg,
                Toast.LENGTH_LONG).show();
    }

    private void parseErrorToast(String msg) {
        Toast.makeText(this, getString(R.string.json_error) + ": " + msg,
                Toast.LENGTH_LONG).show();
    }
}