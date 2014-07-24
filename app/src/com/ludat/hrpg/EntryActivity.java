package com.ludat.hrpg;

import com.ludat.hrpg.api.ApiService;
import com.ludat.hrpg.api.ApiService.ApiBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class EntryActivity extends Activity {
	
	ApiService mService;
	public boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	new AsyncTask<String, void, String>(){

				@Override
				protected void onPostExecute(String result) {
					// TODO Auto-generated method stub
					super.onPostExecute(result);
				}

				@Override
				protected String doInBackground(String... arg0) {
					// TODO Auto-generated method stub
					return null;
				}
				
				
        	};
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//startService(new Intent(getApplicationContext(), ApiService.class));
		
		Intent bindService = new Intent(this, ApiService.class);
		bindService(bindService, mConnection, Context.BIND_AUTO_CREATE);

	};
	

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
	
	public boolean isConnected(){
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) 
			return true;
		else
			return false;
	};
}
