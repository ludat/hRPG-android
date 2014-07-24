package com.ludat.hrpg.api;

import java.io.BufferedOutputStream;
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
import java.util.Timer;
import java.util.TimerTask;

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

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class ApiService extends Service {

	private static final String TAG = "LUCAS";//ApiService.class.getName();
	
	public static final int LOGIN = 1;
	
	private static String apiKey = null;
	private static String apiUser = null;
	
	private Timer mTimer;
	
	private TimerTask updateTask = new TimerTask () {
		@Override
		public void run(){
			Log.i(TAG, "I'm working");
		}
	};
	
	@Override
	public void onCreate(){
		super.onCreate();
		Log.i(TAG, "Starting service");
		apiKey = null;
		apiUser = null;
		mTimer = new Timer("ApiServiceTimer");
		mTimer.schedule(updateTask, 1000L, 60 * 1000L);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Destroying service");
	     
		mTimer.cancel();
		mTimer = null;
	}

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			Bundle mData = msg.getData();
			switch (msg.what) {
			case LOGIN:
				Log.d(TAG, "quiero loguearme");
				Log.d(TAG, "complete-afuera:'" + msg.toString() + "'");
				new LoginTask(msg.replyTo, mData.getString("username"),
						mData.getString("password")).execute();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private class LoginTask extends AsyncTask<Message, Integer, Boolean> {
		private Messenger _replyTo;
		private String _username;
		private String _password;
		
		public LoginTask (Messenger replyTo, String username, String password) {
			super();
			_replyTo = replyTo;
			_username = username;
			_password = password;
		}
		
		protected Boolean doInBackground(Message... messages) {

			Log.d(TAG, "username:'" + _username + "'");
			Log.d(TAG, "password:'" + _password + "'");
			Message replyMsg = Message.obtain(null, ApiService.LOGIN, 0, 0);
			Bundle responseData = new Bundle();
			
			Boolean result = ApiService.this.login(_username, _password);
			
			responseData.putBoolean("result", result);
			replyMsg.setData(responseData);
			try {
				_replyTo.send(replyMsg);
			} catch (RemoteException e) {
				Log.e(TAG, "Algo salio muy mal");
				e.printStackTrace();
			}
			return result;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(Boolean result) {
		}
	}
	
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	
	// My functions
	public boolean login(String username, String password){
		InputStream is = null;
		final String myurl = "https://habitrpg.com/api/v2/user/auth/local";
		
		URL url = null;
		HttpsURLConnection conn = null;
		try {
			url = new URL(myurl);
			conn = (HttpsURLConnection) url.openConnection();
	        conn.setReadTimeout(100000 /* milliseconds */);
	        conn.setConnectTimeout(150000 /* milliseconds */);
	        conn.setRequestMethod("POST");
	        conn.setDoInput(true);

			conn.addRequestProperty("Content-type", "application/json");

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("username", username);
			jsonObject.put("password", password);
			Log.e(TAG, "JSON:" + jsonObject.toString());

			Log.e(TAG, "1");
			OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
			Log.e(TAG, "2");
			os.write(jsonObject.toString());
			Log.e(TAG, "3");

			os.close();
			Log.e(TAG, "4");
			
			conn.connect();
			
			// This is really weird
			is = conn.getInputStream();
			Log.e(TAG, "5");
	        // Convert the InputStream into a string
	        String contentAsString = readIt(is);
			Log.e(TAG, "Content: " + contentAsString);
	        
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
			
			JSONObject responseJson = new JSONObject(contentAsString);
			if (! responseJson.has("err") && responseJson.has("id") && responseJson.has("token")){
				apiUser = responseJson.getString("id");
				apiKey = responseJson.getString("token");
				Log.e(TAG, "HOLY SHIT YES");
				return true;
			}
			return false;
	    } catch (Exception e) {
	    	Log.e(TAG, "ALGO PASO CON LA CONEXION:\"" + e.toString() + "\"");
	    	try {
				Log.e(TAG, "CODIGO DE LA CONEXION:\"" + conn.getResponseCode() + "\"");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			return false;
		} finally {
	    	apiUser = null;
	    	apiKey = null;
	    	if (is != null) {
	    		try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
		}
	}
	    
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream) throws IOException {
    	BufferedReader r = new BufferedReader(new InputStreamReader(stream));
    	StringBuilder total = new StringBuilder();
    	String line;
    	while ((line = r.readLine()) != null) {
    	    total.append(line);
    	}
    	return total.toString();
    }
}
