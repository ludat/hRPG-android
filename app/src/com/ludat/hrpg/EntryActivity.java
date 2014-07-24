package com.ludat.hrpg;

import com.ludat.hrpg.api.ApiService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class EntryActivity extends Activity {
	
	private static final String TAG = "LUCAS";//EntryActivity.class.getName();
	
    /** Messenger for communicating with the service. */
    Messenger mServiceMessenger = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mBound = false;
    
    @SuppressLint("HandlerLeak")
	class EntryIncomingHandler extends Handler {

    	@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
				case ApiService.LOGIN:
					Log.d(TAG, "respuesta despues de logueado:" + msg.getData().getBoolean("result"));
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};
	final Messenger mActivityMessenger = new Messenger(new EntryIncomingHandler());
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mServiceMessenger = new Messenger(service);
            mBound = true;
            login();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mServiceMessenger = null;
            mBound = false;
        }
    };
	
	public void login() {
		if (!mBound)
			return;
		// Create and send a message to the service, using a supported 'what'
		// value
		Message msg = Message.obtain(null, ApiService.LOGIN, 0, 0);
		msg.replyTo = mActivityMessenger;

		Bundle mBundle = new Bundle();
		mBundle.putString("username", "hRPG_test");
		mBundle.putString("password", "58963214789654123");
		msg.setData(mBundle);
		try {
			mServiceMessenger.send(msg);
		} catch (RemoteException e) {
			Log.e(TAG, "Algo salio muy mal");
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// Bind to the service
        bindService(new Intent(this, ApiService.class), mConnection,
                Context.BIND_AUTO_CREATE);

	}
	
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
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	};
}
