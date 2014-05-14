package com.example.storeinbusiness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TopItemsFragment extends Fragment {

	protected final static String TAG = TopItemsFragment.class.getSimpleName()
			.toString();

	// UI Variable
	Button mDiscoverButtonCheckIn;
	Button mDiscoverButtonBrowse;
	Button mDiscoverButtonReccomendation;

	// Variables
	ArrayList<HashMap<String, String>> itemsInfo = new ArrayList<HashMap<String, String>>();
	protected ArrayList<String> itemId = new ArrayList<String>();
	HashMap<String, String> itemInfo = new HashMap<String, String>();

	// Parse Constants

	public static TopItemsFragment newInstance(String param1, String param2) {
		TopItemsFragment fragment = new TopItemsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public TopItemsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_top_items, container,
				false);

		return view;
	}

	/*
	 * Function Added
	 */

	/*
	 * Checking Promotion
	 */

	private void getItemList() {
		String placeId = null;
		ParseObject currentPlace = ParseObject.createWithoutData(
				ParseConstants.TABLE_PLACE, placeId);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_PLACE_ITEM);
		query.include(ParseConstants.KEY_ITEM_ID);
		query.setLimit(3);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> items, ParseException e) {
				if (e == null) {
					for (ParseObject object : items) {
						HashMap<String, String> itemInfo = new HashMap<String, String>();
						ParseObject currentItem = object
								.getParseObject(ParseConstants.KEY_ITEM_ID);
						String itemName = currentItem
								.getString(ParseConstants.KEY_NAME);
						Number totalLoved = currentItem
								.getNumber(ParseConstants.KEY_TOTAL_LOVED);
						String temp = totalLoved.toString();

						itemInfo.put(ParseConstants.KEY_NAME, itemName);
						itemInfo.put(ParseConstants.KEY_TOTAL_LOVED, temp);

						itemId.add(currentItem.getObjectId());
						itemsInfo.add(itemInfo);
					}
				} else {
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * Error Dialog
	 */
	private void errorAlertDialog(ParseException e) {
		// failed
		Log.e(TAG, e.getMessage());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(e.getMessage()).setTitle("Error")
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}