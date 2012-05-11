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

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
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
            case R.id.about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void populateList() {
        try {
            mProblemGroups = new JSONArray(getJSON());
            String[] strings = getProblemGroupList(mProblemGroups);

            setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
                    strings));
        } catch (IOException e) {
            ioErrorToast(e.getLocalizedMessage());
        } catch (JSONException e) {
            parseErrorToast(e.getLocalizedMessage());
        }
    }

    private String getJSON() throws IOException {
        InputStream is = getAssets().open(mJSONAssetName);

        int size = is.available();

        // Read the entire asset into a local byte buffer.
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        // Convert the buffer into a string.
        return new String(buffer);
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

    private void ioErrorToast(String msg) {
        Toast.makeText(this, R.string.file_error + ": " + msg,
                Toast.LENGTH_SHORT).show();
    }

    private void parseErrorToast(String msg) {
        Toast.makeText(this, R.string.json_error + ": " + msg,
                Toast.LENGTH_SHORT).show();
    }
}