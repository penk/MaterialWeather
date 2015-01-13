package net.penkia.materialweather;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.content.DialogInterface;
import android.widget.EditText;
import android.view.KeyEvent;
import android.widget.TextView.OnEditorActionListener;
import android.view.View.OnKeyListener;

import android.widget.TextView;
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
    private TextView city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temp = (TextView) findViewById(R.id.temp);
        cond = (TextView) findViewById(R.id.cond);
        city = (TextView) findViewById(R.id.city);

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
                city.setText(jObj.getString("name"));
                JSONObject weatherData = jArr.getJSONObject(0);

                JSONObject mainObj = jObj.getJSONObject("main");
                Double c = new Double(mainObj.getDouble("temp") - 273.15);
                
                // FIXME: temperature range from openweathermap doesn't seem to work 
                //Double temp_min = new Double(mainObj.getDouble("temp_min") - 273.15);
                //Double temp_max = new Double(mainObj.getDouble("temp_max") - 273.15);

                DecimalFormat df = new DecimalFormat("#.#");
                cond.setText( 
                        //df.format(temp_max).toString() + " - " + df.format(temp_min) + "\n" + 
                        weatherData.getString("main") + "\n" + weatherData.getString("description") + "\n"
                        + "Humidity: " + mainObj.getString("humidity") + " %"
                );

                String degree = df.format(c).toString();
                temp.setText(degree + " °C");
                Log.i(MainActivity.ACTIVITY_TAG, mainObj.getString("temp"));
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void onClick(View v) {
        Dialog d = createDialog();
        d.show();
    }

    private Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.city_dialog, null);
        builder.setView(v);
        final EditText edit = (EditText) v.findViewById(R.id.ptnEdit);

        // handle enter key
        edit.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    WeatherTask task = new WeatherTask();
                    task.execute(new String[]{edit.getText().toString()});
                    return true;
                }
                return false;
            }
        });
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                WeatherTask task = new WeatherTask();
                String city = edit.getText().toString();
                task.execute(new String[]{city});
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
