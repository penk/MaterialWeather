package net.penkia.materialweather;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.text.DecimalFormat;

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
    private TextView cond;
    private TextView temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temp = (TextView) findViewById(R.id.temp);
        cond = (TextView) findViewById(R.id.cond);
        //String locationProvider = LocationManager.NETWORK_PROVIDER;
        //Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

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

    private class WeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
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

                return buffer.toString();
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

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jObj = new JSONObject(result);
                JSONArray jArr = jObj.getJSONArray("weather");
                JSONObject weatherData = jArr.getJSONObject(0);

                JSONObject mainObj = jObj.getJSONObject("main");
                cond.setText(weatherData.getString("description"));
                Log.i(MainActivity.ACTIVITY_TAG, weatherData.getString("description"));
                Log.i(MainActivity.ACTIVITY_TAG, weatherData.getString("main"));
                Double c = new Double(mainObj.getDouble("temp") - 273.15);
                DecimalFormat df = new DecimalFormat("#.##");
                String degree = df.format(c).toString();
                temp.setText(degree + " °C");
                Log.i(MainActivity.ACTIVITY_TAG, mainObj.getString("temp"));
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
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
