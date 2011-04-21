package aefc.QrScanner;

import la.droid.qr.services.Services;
import aefc.QrScanner.R;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;

public class QrScaner extends Activity {
	/** Called when the activity is first created. */
	@SuppressWarnings("unused")
	private String hashRut;
	private static final int ACTIVITY_RESULT_QR_DRDROID = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//ALERT DIALOG

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage( getString(R.string.warning) )
		.setCancelable(true)
		.setNegativeButton( R.string.close, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.create().show();


		//"CallScanner" button
		final Button button = (Button) findViewById(R.id.CallScanner);

		//Set action to button
		button.setOnClickListener( new OnClickListener() {

			public void onClick(View v) {
				//Create a new Intent to send to QR Droid
				Intent qrDroid = new Intent( Services.SCAN ); //Set action "la.droid.qr.scan"

				//Notify we want complete results (default is FALSE)
				qrDroid.putExtra( Services.COMPLETE , true);

				//Send intent and wait result
				try {
					startActivityForResult(qrDroid, ACTIVITY_RESULT_QR_DRDROID);
				} catch (ActivityNotFoundException activity) {
					Services.qrDroidRequired(QrScaner.this);
				}
			}
		});
	}

	/**
	 * Reads data scanned by user and returned by QR Droid
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if( ACTIVITY_RESULT_QR_DRDROID==requestCode && null!=data && data.getExtras()!=null ) {
			//Read result from QR Droid (it's stored in la.droid.qr.result)
			String result = data.getExtras().getString(Services.RESULT);
			//Just set result to EditText to be able to view it
			EditText editText = ( EditText ) findViewById(R.id.resultTxt);
			editText.setText( result );
			editText.setVisibility(View.VISIBLE);
			this.hashRut = result;
		}
	}

	public static void qrDroidRequired( final Activity activity ) {
		//Apparently, QR Droid is not installed, or it's previous to version 3.5
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage( activity.getString(R.string.qrdroid_missing) )
		.setCancelable(true)
		.setNegativeButton( activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		})
		.setPositiveButton( activity.getString(R.string.from_market), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				activity.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( activity.getString(R.string.url_market) ) ) );
			}
		})
		.setNeutralButton(activity.getString(R.string.direct), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				activity.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( activity.getString(R.string.url_direct) ) ) );
			}
		});
		builder.create().show();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//Nothing
	}
}