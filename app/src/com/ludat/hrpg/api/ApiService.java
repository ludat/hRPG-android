package com.ludat.hrpg.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.net.ParseException;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ApiService extends Service {

	private static String apiKey;
	private static String apiUser;
	
	private final IBinder mBinder = new ApiBinder();
	
	public ApiService() {
	}

	public class ApiBinder extends Binder {
		public ApiService getService() {
			return ApiService.this;
		}
	}
	
	@Override
	public void onCreate(){
		apiKey = null;
		apiUser = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public boolean login(String username, String password){
		InputStream is = null;
		final String myurl = "https://habitrpg.com/api/v2/user/auth/local";
		try {
			URL url = new URL(myurl);
	    	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("POST");
	        conn.setDoInput(true);
	        conn.setDoOutput(true);

	        conn.addRequestProperty("Accept", "application/json");
			conn.addRequestProperty("Content-type", "application/json");

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("username", username);
			jsonObject.put("password", password);

			Log.e("LUCAS", "1");
			OutputStream os = conn.getOutputStream();
			Log.e("LUCAS", "3");
			os.write(jsonObject.toString().getBytes("UTF-8"));
			os.close();

			int response = conn.getResponseCode();
			Log.e("LUCAS", "The response is: " + response);
			is = conn.getInputStream();
	
	        // Convert the InputStream into a string
	        String contentAsString = readIt(is);
	        
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
			JSONObject responseJson = new JSONObject(contentAsString);
			if (! responseJson.has("err") && responseJson.has("username") && responseJson.has("password")){
				apiUser = responseJson.getString("username");
				apiKey = responseJson.getString("password");
				Log.e("LUCAS", "HOLY SHIT YES");
				return true;
			}
			return false;
	    } catch (Exception e) {
	    	Log.e("LUCAS", "ALGO PASO CON LA CONEXION:\"" + e.toString() + "\"");
	    	Log.e("LUCAS", "ALGO PASO CON LA CONEXION:\"" + e.getLocalizedMessage() + "\"");
			e.printStackTrace();
			return false;
		} finally {
	    	apiUser = null;
	    	apiKey = null;
	    	if (is != null) {
	    		try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
		}
	}
	    
	    // Reads an InputStream and converts it to a String.
	    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
	    	int len = 500;
	        Reader reader = null;
	        reader = new InputStreamReader(stream, "UTF-8");    
	        char[] buffer = new char[len];
	        reader.read(buffer);
	        return new String(buffer);
	    }
}
