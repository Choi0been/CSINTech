package com.example.csintech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private MapView mapView;
    private static final String API_URL = "http://cuser.dothome.co.kr/jdata.php";

    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        web = findViewById(R.id.mainWeb);
        web.setWebViewClient(new WebViewClient());
        web.getSettings().setJavaScriptEnabled(true);

        web.loadUrl("http://cuser.dothome.co.kr/index.php"); // Url 삽입

        findViewById(R.id.reloadBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "새로운 정보를 불러옵니다.", Toast.LENGTH_SHORT).show();
                web.reload();
                mapView.getMapAsync(MainActivity.this::onMapReady);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng myloc = new LatLng(37.445, 126.649);
        new MarkerTask().execute();
        CameraPosition.Builder builder = new CameraPosition.Builder();
        builder.target(myloc);
        builder.zoom(15);
        CameraPosition position = builder.build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    private class MarkerTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... args) {
            HttpURLConnection connection = null;
            final StringBuilder jsondata = new StringBuilder();
            try {
                URL url = new URL(API_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    int read_result;
                    char[] buff = new char[1024];
                    while ((read_result = inputStreamReader.read(buff)) != -1) {
                        jsondata.append(buff, 0, read_result);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return jsondata.toString();
        }

        @Override
        protected void onPostExecute(String json) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    double latitude = jsonObject.getDouble("latitude");
                    double longitude = jsonObject.getDouble("longitude");
                    String car_num = jsonObject.getString("car_num");
                    String error_name = jsonObject.getString("error_name");
                    String error_code = jsonObject.getString("error_code");

                    StringBuilder strbuilder = new StringBuilder();
                    strbuilder.append(error_name).append("(").append(error_code).append(")");
                    String error_content = strbuilder.toString();

                    LatLng location = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(location)
                            .title(car_num)
                            .snippet(error_content);
                    googleMap.addMarker(markerOptions);
                }
            } catch (JSONException e) {
                    e.printStackTrace();
            }
            mapView.onResume();
        }

        private String readStream(InputStream inputStream) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            return stringBuilder.toString();
        }
    }
}