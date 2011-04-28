package aefc.QrScanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import la.droid.qr.services.Services;
import aefc.QrScanner.R;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
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

		//Get Places Data from server with JSON
		String result = "";

		//Initialization stuffs
		InputStream is = new InputStream() {

			public int read() throws IOException {
				return 0;
			}
		};; ;

		//the year data to send
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("year","1980"));

		//http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			//TODO: Set te addres from the server service.
			HttpPost httppost = new HttpPost("http://example.com/getAllPeopleBornAfter.php");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}catch(Exception e){
			Log.e("log_tag", "Error in http connection "+e.toString());
		}


		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();

			result=sb.toString();
		}catch(Exception e){
			Log.e("log_tag", "Error converting result "+e.toString());
		}

		//TODO: MODIFIE THIS PART
		//parse json data 
		try{
			JSONArray jArray = new JSONArray(result);
			for(int i=0;i<jArray.length();i++){
				JSONObject json_data = jArray.getJSONObject(i);
				Log.i("log_tag","id: "+json_data.getInt("id")+
						", name: "+json_data.getString("name")+
						", sex: "+json_data.getInt("sex")+
						", birthyear: "+json_data.getInt("birthyear")
				);
			}
		}
		catch(JSONException e){
			Log.e("log_tag", "Error parsing data "+e.toString());
		}

		//TODO: ARRANGE DATA IN A DROPDOWN
		
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