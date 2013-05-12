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

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebClient extends WebViewClient {
	private Context context;
	
	final String defaultPage =
			"<html><body><h1>TiltScroll:</h1><br>" +
			"<h3>Please be sure to hold the device in a comfortable and normal" +
			" postion and press the calibrate button before continuing." +
			"</h3><br><br><h2>TIP:</h2><br><br><h3>On many devices, it's better" +
			" to tilt in a diagonal fashion from corner to corner(or side" +
			" to side) than straight vertically. EXPERIMENT</h3></body></html>";
	
	public WebClient(Context con) {
		context = con;
	}
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
		Log.d("page start", url);
		//Toast.makeText(context, "Start: " + url, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onReceivedError(WebView view, int error, String desc, String url)
	{
		super.onReceivedError(view, error, desc, url);
		Log.d("error received", desc);
		
		Toast.makeText(context, "Error loading page",
				Toast.LENGTH_SHORT).show();
	}
	
	public String sanitizeURL(String URL) {
		// WebView seems to dislike not having the "HTTP://" part
		// so we're going to force it into every URL..
		if(!(URL.substring(0, 7).equalsIgnoreCase("http://")) ||
				URL.length() <= 7)
		{
			URL = "http://" + URL;
		}
		
		// Java is silly sometimes.
		return URL;
	}
	
	public String getDefaultPage() {
		return defaultPage;
	}
}
