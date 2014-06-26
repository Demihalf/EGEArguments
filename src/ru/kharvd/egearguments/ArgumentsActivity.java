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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ArgumentsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        WebView webview = new WebView(this);
        setContentView(webview);

        Intent intent = getIntent();
        String json = intent.getStringExtra(EGEArguments.EXTRA_JSON);

        try {
            StringBuilder str = new StringBuilder(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");

            str.append("<html><body>");

            JSONObject problem = new JSONObject(json);
            str.append("<h3><b>").append(problem.getString("problemName"))
                    .append("</b></h3>");

            JSONArray arguments = problem.getJSONArray("arguments");

            for (int i = 0; i < arguments.length(); i++) {
                JSONObject arg = arguments.getJSONObject(i);

                String author = arg.getString("author");
                String book = arg.getString("book");
                String comment = arg.getString("comment");

                str.append("<h4>");

                if (author.length() > 0) {
                    str.append("<b>").append(author).append("</b>");
                }
                
                if (author.length() > 0 && book.length() > 0) {
                    str.append(", ");
                }
                
                str.append("<i>").append(book).append("</i></h4>");

                str.append("<p>").append(comment).append("</p>");
            }

            str.append("</body></html>");

            webview.loadData(str.toString(), "text/html; charset=UTF-8", "UTF-8");
        } catch (JSONException e) {
            Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT)
                    .show();
        }

    }
}
