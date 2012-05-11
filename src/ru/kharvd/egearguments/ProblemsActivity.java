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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ProblemsActivity extends ListActivity {
    JSONObject mProblemGroup;
    JSONArray mProblems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent intent = getIntent();
        String json = intent.getStringExtra(EGEArguments.EXTRA_JSON);

        try {
            mProblemGroup = new JSONObject(json);
            
            String problemGroupName = mProblemGroup.getString("problemGroupName");
            
            TextView title = (TextView) findViewById(R.id.title);
            
            if (title != null) {
                title.setText(problemGroupName);
                title.setSelected(true);
            } else {
                setTitle(problemGroupName);
            }

            mProblems = getProblems(mProblemGroup);
            String[] strings = getProblemsList(mProblems);

            setListAdapter(new ArrayAdapter<String>(this,
                    R.layout.list_item, strings));
        } catch (JSONException e) {
            Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Intent intent = new Intent(view.getContext(),
                        ArgumentsActivity.class);
                try {
                    intent.putExtra(EGEArguments.EXTRA_JSON, mProblems
                            .getJSONObject(position).toString());
                } catch (JSONException e) {
                    Toast.makeText(view.getContext(), R.string.json_error,
                            Toast.LENGTH_SHORT).show();
                }

                startActivity(intent);
            }
        });
    }

    private JSONArray getProblems(JSONObject problemGroup) throws JSONException {
        return problemGroup.getJSONArray("problems");
    }

    private String[] getProblemsList(JSONArray problems) throws JSONException {
        String[] strings = new String[problems.length()];

        for (int i = 0; i < problems.length(); i++) {
            strings[i] = problems.getJSONObject(i).getString("problemName");
        }

        return strings;
    }
}
