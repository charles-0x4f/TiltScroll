/* 
	Copyright 2013 Charles O.
	charles.0x4f@gmail.com
	Github: https://github.com/charles-0x4f

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.charleso.tiltscroll;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.SearchView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.widget.ScrollView;
import edu.charleso.tiltscroll.R;

public class Main extends Activity implements SensorEventListener {
	WebView webview;
	WebClient webclient;
	ScrollView scrollview;
	SensorManager mSensorManager;
	Sensor mAccelerometer, mMagnet;
	
	private boolean calibrated = false;
	float inclination = 1;
	float standardLow = 0;
	float standardHigh = 0;
	
	float[] tilt = { -99,-99,-99 }, accel = { 0,0,0 }, magnet = { 0,0,0 };
	float[] prevR = { 0,0,0,0,0,0,0,0,0 };
	
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the progress bar
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        
        // Setup the ScrollView
        scrollview = (ScrollView)findViewById(R.id.scrollview);
        scrollview.setSmoothScrollingEnabled(true);
        
        // Setup sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        // Setup the WebView and the WebViewClient
        webview = (WebView)findViewById(R.id.webview);
        webclient = new WebClient(getBaseContext());
        webview.setWebViewClient(webclient);
        
        // Setup WebChromeClient to allow progress bar
        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient() {
        	public void onProgressChanged(WebView view, int progress) {
        		// This progress int goes from 0 to 10000
        		activity.setProgress(progress * 1000);
        		
        		if(progress == 100) {
        			activity.setProgressBarVisibility(false);
        	        activity.setProgressBarIndeterminateVisibility(false);
        		}
        	}
        });
        
        // !BROWSER NOT SECURE!
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.loadData(webclient.getDefaultPage(), "text/html", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar, menu);
        
        // Setup the SearchView/"URL" EditText
        // SearchView allows us to have a pre-made EditText with "enter button"
        // capabilities.
        final SearchView searchView = 
        		(SearchView)menu.findItem(R.id.searchview).getActionView();
        
        final MenuItem searchItem = menu.findItem(R.id.searchview);
        
        // Set keyboard to display "Go" as enter
        // TODO: find the file that defines this hex value
        searchView.setImeOptions(0x00000002);
        
        // This will be called when the user presses the "Go" button on the
        // keyboard
        final Activity activity = this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				String URL = searchView.getQuery().toString();
				
				// Close SearchView after "Go" is pressed; clear text
				searchItem.collapseActionView();
				searchView.setQuery("", false);
				
				// Start progress bar
				activity.setProgressBarVisibility(true);
		        activity.setProgressBarIndeterminateVisibility(true);
				URL = webclient.sanitizeURL(URL);
				webview.loadUrl(URL);
				
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
    	
    	// Calibrate button
    	if(menu.getItemId() == R.id.calibrate) {
    		// This will be used to set the tilt angles and a center "dead zone"
    		// Get 20% of inclination
    		float offset = (float)((20.0f/100.0f) * inclination);
    		// Inclination level - 20%
    		standardLow = inclination - offset;
    		// Inclination + 20%
    		standardHigh = inclination + offset;
    		
    		calibrated = true;
    		Toast.makeText(getBaseContext(), "Calibrated",
    				Toast.LENGTH_SHORT).show();
    		
    		return true;
    	}
    	
    	// Back button
    	if(menu.getItemId() == R.id.backButton) {
    		webview.goBack();
    	}
    	
    	// Forward button
    	if(menu.getItemId() == R.id.forwardButton) {
    		webview.goForward();
    	}
    	
    	return false;
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	// Release sensor to save battery
    	mSensorManager.unregisterListener(this);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Reinstate sensor
    	mSensorManager.registerListener(this, mAccelerometer, 
    			SensorManager.SENSOR_DELAY_GAME);
    	mSensorManager.registerListener(this, mMagnet, 
    			SensorManager.SENSOR_DELAY_GAME);
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	
    }
    
    public void onSensorChanged(SensorEvent event) {
    	float[] R = { 0,0,0,0,0,0,0,0,0 };
    	float[] I = { 0,0,0,0,0,0,0,0,0 };
    	
    	switch(event.sensor.getType()) {
    	
    	// Copy sensor values to our Sensor objects
    	case Sensor.TYPE_ACCELEROMETER:
    		accel = event.values.clone();
    	case Sensor.TYPE_MAGNETIC_FIELD:
    		magnet = event.values.clone();
    		break;
    	default:
    			return;
    	}
    	
    	// Convert sensor values into an inclination we can use
    	if(SensorManager.getRotationMatrix(R, I, accel, magnet)) {
    		SensorManager.getOrientation(R, tilt);
    		inclination = SensorManager.getInclination(I);
    	}
    	
    	// Don't scroll if our dead zone hasn't been set
    	if(!calibrated)
    		return;
    	
    	// If our current inclination is above or below
    	// calibrated level, scroll
    	if(inclination < standardLow)
    		scrollview.smoothScrollBy(0, -5);
    	else if(inclination > standardHigh)
    		scrollview.smoothScrollBy(0, 5);
    	
    }
}
