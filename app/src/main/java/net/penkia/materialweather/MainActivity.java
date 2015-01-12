package net.penkia.materialweather;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {

    private static final String ACTIVITY_TAG = "MaterialWeather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WeatherTask task = new WeatherTask();
        String city = "taipei,taiwan";
        task.execute(new String[]{city});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private class WeatherTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(MainActivity.ACTIVITY_TAG, params[0]);

            HttpURLConnection con = null;
            InputStream is = null;

            try {
                con = (HttpURLConnection) ( new URL("http://api.openweathermap.org/data/2.5/weather?q=" + params[0])).openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.connect();

                StringBuffer buffer = new StringBuffer();
                is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                String line = null;
                while (  (line = br.readLine()) != null )
                    buffer.append(line + "\r\n");

                is.close();
                con.disconnect();

                JSONObject jObj = new JSONObject(buffer.toString());
                JSONArray jArr = jObj.getJSONArray("weather");
                JSONObject weatherData = jArr.getJSONObject(0);

                JSONObject mainObj = jObj.getJSONObject("main");
                Log.i(MainActivity.ACTIVITY_TAG, weatherData.getString("description"));
                Log.i(MainActivity.ACTIVITY_TAG, weatherData.getString("main"));

                Log.i(MainActivity.ACTIVITY_TAG, mainObj.getString("temp"));
            } 
            catch(Throwable t) {
                t.printStackTrace();
            }
            finally {
                try { is.close(); } catch(Throwable t) {}
                try { con.disconnect(); } catch(Throwable t) {}
            }
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
