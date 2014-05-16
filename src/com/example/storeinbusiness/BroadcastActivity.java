package com.example.storeinbusiness;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

public class BroadcastActivity extends ActionBarActivity {
	private static final String TAG = BroadcastActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_broadcast);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.broadcast, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		// UI
		Button mBroadcastButton;
		EditText mBroadcastEditTextMessage;

		// Fixed Variables
		private String placeID;
		private String placeName;
		private String message;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_broadcast,
					container, false);
			// get intent
			getPlaceId();

			// get information
			getInformation();

			// UI Declaration
			mBroadcastEditTextMessage = (EditText) rootView
					.findViewById(R.id.broadcastEditTextMessage);
			mBroadcastButton = (Button) rootView
					.findViewById(R.id.broadcastSendButton);
			mBroadcastButton.setOnClickListener(sendButtonListener);

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
		}

		/*
		 * added function
		 */

		private void getPlaceId() {
			Intent intent = getActivity().getIntent();
			placeID = intent.getStringExtra(ParseConstants.KEY_PLACE_ID);
		}

		private void getInformation() {
			if (placeID == null) {
				getPlaceId();
			}
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_PLACE);
			query.getInBackground(placeID, new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject object, ParseException e) {
					if (e == null) {
						// success
						placeName = object.getString(ParseConstants.KEY_NAME);
					} else {
						// failed
						parseErrorDialog(e);
					}
				}
			});
		}

		/*
		 * On Click Listener
		 */

		OnClickListener sendButtonListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendNotification();
			}
		};

		/*
		 * Send notification to users
		 */

		private void sendNotification() {
			message = mBroadcastEditTextMessage.getText().toString();
			ParsePush push = new ParsePush();
			push.setChannel(placeID);
			push.setMessage(message + "- Send by" + placeName);
			push.sendInBackground();
		}

		/*
		 * Debug if ParseException throw the alert dialog
		 */

		protected void parseErrorDialog(ParseException e) {
			Log.e(TAG, e.getMessage());
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(e.getMessage()).setTitle(R.string.error_title)
					.setPositiveButton(android.R.string.ok, null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}

	}

}
