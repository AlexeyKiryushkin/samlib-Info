/*
 * Copyright 2013 Dmitry Monakhov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package monakhv.android.samlib.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.exception.SamlibParseException;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.samlib.http.HttpClientController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.SamLibConfig;

/**
 * The task to load Author into data base.
 * Take the array of full Authors URL
 * 
 * @author monakhv
 */
public class AddAuthor extends AsyncTask<String, Void, Boolean> {

    private static final String DEBUG_TAG = "AddAuthor";    
   
    private Context context = null;
    private int numberOfAdded;
    private int doubleAdd = 0;
    private final SettingsHelper settings;

    public AddAuthor(Context c) {
        context = c;
        numberOfAdded = 0;
        settings = new SettingsHelper(context);
    }

    @Override
    protected Boolean doInBackground(String... texts) {

        HttpClientController http = HttpClientController.getInstance(context);
        AuthorController sql = new AuthorController(context);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
        wl.acquire();
        for (String url : texts) {
            Author a = loadAuthor(http, sql, url);
            if (a != null) {
                sql.insert(a);
                ++numberOfAdded;
            }
        }
        wl.release();
        return true;
    }
    //

    @Override
    protected void onPostExecute(Boolean result) {
        int duration = Toast.LENGTH_SHORT;
        CharSequence msg="";
        
        if (numberOfAdded ==0){
             if (doubleAdd != 0) {//double is here
                 msg = context.getText(R.string.add_error_double);
             }
             else {
                 msg = context.getText(R.string.add_error);
             }
            
           
        }
        else if (numberOfAdded ==1 ) {
            msg = context.getText(R.string.add_success);
            settings.requestBackup();

        } else if (numberOfAdded >1){
            msg = context.getText(R.string.add_success_multi)+" "+numberOfAdded;
            settings.requestBackup();
        }

        Toast toast = Toast.makeText(context, msg, duration);
        toast.show();

    }

    private Author loadAuthor(HttpClientController http, AuthorController sql, String url) {
        Author a;
        String text;

        
        text = testURL(url);
        if (text == null){
            Log.e(DEBUG_TAG, "URL syntax error: "+url);
            settings.log(DEBUG_TAG, "URL syntax error: "+url);
            return null;
        }
        
        Author ta = sql.getByUrl(text);
        if (ta != null) {
            Log.i(DEBUG_TAG, "Ignore Double entries: "+text);
            settings.log(DEBUG_TAG, "Ignore Double entries: "+text);
            ++doubleAdd;
            return null;
        }
        try {
            a = http.addAuthor(text);
        } catch (IOException ex) {
            Log.e(DEBUG_TAG, "DownLoad Error for URL: " + text, ex);
            settings.log(DEBUG_TAG, "DownLoad Error for URL: " + text, ex);
            return null;

        } catch (SamlibParseException ex) {
            Log.e(DEBUG_TAG, "Author parsing Error: " + text, ex);
            settings.log(DEBUG_TAG, "Author parsing Error: " + text, ex);
            return null;
        } catch (IllegalArgumentException ex) {
            Log.e(DEBUG_TAG, "URL Parsing exception: " + text, ex);
            settings.log(DEBUG_TAG, "URL Parsing exception: " + text, ex);
            return null;
        }

        return a;
    }

    /**
     * URL syntax checkout
     * 
     * @param url original URL
     * @return reduced URL without host prefix or NULL if the syntax is wrong
     * 
     */
    private String testURL(String url)   {
        Log.d(DEBUG_TAG, "Got text: " + url);

        return SamLibConfig.reduceUrl(url);

    }
}
