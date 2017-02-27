package com.example.androidlbs;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class MainActivity extends Activity {
	
	private LocationManager locationManager;
	private TextView geoInformation;
	private String locationProvider;
	private Location location;
	
	private static final int GET_DECODE_GEO_INFORMATION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		geoInformation = (TextView) findViewById(R.id.show_geo_information);
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(true);
		if(providers.contains(LocationManager.GPS_PROVIDER)){
			locationProvider = LocationManager.GPS_PROVIDER;
			Toast.makeText(this, "The provider for location service is " + LocationManager.GPS_PROVIDER, Toast.LENGTH_SHORT).show();
		}else if(providers.contains(LocationManager.NETWORK_PROVIDER)){
			locationProvider = LocationManager.NETWORK_PROVIDER;
			Toast.makeText(this, "The provider for location service is " + LocationManager.NETWORK_PROVIDER, Toast.LENGTH_SHORT).show();
		}else {
			Toast.makeText(this, "There is no provider for location service", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//location = locationManager.getLastKnownLocation(locationProvider);
		Location bestLocation = null;
	    for (String provider : providers) {
	        Location l = locationManager.getLastKnownLocation(provider);
	        if (l == null) {
	            continue;
	        }
	        if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
	            // Found best last known location: %s", l);
	            bestLocation = l;
	        }
	    }
	    location = bestLocation;
	    
		if(location != null){
			Toast.makeText(this, "The current latitude is: " + location.getLatitude() + "; Current longitude is: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
			showLocation(location);
		}else{
			Toast.makeText(this, "No location information get", Toast.LENGTH_SHORT).show();
		}
		
		
		locationManager.requestLocationUpdates(locationProvider, 5000, 1, listener);
		
	}
	
	LocationListener listener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			showLocation(location);
		}
	};
	
	private void showLocation(final Location location) {
		geoInformation.setText("The current latitude is: " + location.getLatitude() + "; Current longitude is: " + location.getLongitude());
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Log.d("New thread", "I am in new thread now");
				HttpClient httpClient = new DefaultHttpClient();
				StringBuilder httpUrlString = new StringBuilder();
				httpUrlString.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
				httpUrlString.append(location.getLatitude()).append(",").append(location.getLongitude()).append("&sensor=false");
				HttpGet httpGet = new HttpGet(httpUrlString.toString());
				httpGet.addHeader("Accept-Language", "zh-CN");
				try {
					HttpResponse responsedDecodeGeo = httpClient.execute(httpGet);
					if(responsedDecodeGeo.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = responsedDecodeGeo.getEntity();
						String geoInformation = EntityUtils.toString(entity, "utf-8");
						JSONObject jsonObject = new JSONObject(geoInformation);
						JSONArray jsonArray = jsonObject.getJSONArray("results");
						if(jsonArray.length() > 0){
							JSONObject jsonSubObject = jsonArray.getJSONObject(0);
							String decodedInformation = jsonSubObject.getString("formatted_address");
							Message message = new Message();
							message.what = GET_DECODE_GEO_INFORMATION;
							message.obj = decodedInformation;
							geoInformatonHandler.sendMessage(message);
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	private Handler geoInformatonHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_DECODE_GEO_INFORMATION:
				if(location != null){
					Log.d("Location Service", "get google decoded information");
					geoInformation.setText("The current latitude is: " + location.getLatitude() + "; Current longitude is: " + location.getLongitude() + "and specific address is " + msg.obj.toString());
				}else{
					Log.d("Location Service", "did not get google decoded information");
					geoInformation.setText(msg.obj.toString());
				}
				
				break;

			default:
				break;
			}
		}
		
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(locationManager != null){
			locationManager.removeUpdates(listener);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
