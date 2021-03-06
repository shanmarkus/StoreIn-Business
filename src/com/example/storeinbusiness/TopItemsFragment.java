package com.example.storeinbusiness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TopItemsFragment extends Fragment {

	protected final static String TAG = TopItemsFragment.class.getSimpleName()
			.toString();

	// UI Variable
	ListView mListTopItems;
	Button mTopItemsAddButton;

	// Variables
	ArrayList<HashMap<String, String>> itemsInfo = new ArrayList<HashMap<String, String>>();
	protected ArrayList<String> itemId = new ArrayList<String>();
	HashMap<String, String> itemInfo = new HashMap<String, String>();

	private String placeId;
	private String placeName;

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
		getItemList();

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
		// Initial the UI
		mTopItemsAddButton = (Button) view.findViewById(R.id.topItemsAddButton);
		mListTopItems = (ListView) view.findViewById(R.id.listTopItems);

		// Set on Click Listener
		mListTopItems.setOnItemClickListener(itemListener);
		mTopItemsAddButton.setOnClickListener(addItemListener);
		return view;
	}

	/*
	 * Function Added
	 */

	/*
	 * Clear Arraylist
	 */

	private void clearArray() {
		itemsInfo.clear();
		itemInfo.clear();
		itemId.clear();
	}

	/*
	 * Getter for placeId variables
	 */
	public String getPlaceId() {
		Bundle args = getArguments();
		placeId = args.getString(ParseConstants.KEY_PLACE_ID);
		return placeId;
	}

	/*
	 * Checking Promotion
	 */

	private void getItemList() {
		if (placeId == null) {
			getPlaceId();
		}
		// clear array list
		clearArray();

		ParseObject currentPlace = ParseObject.createWithoutData(
				ParseConstants.TABLE_PLACE, placeId);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_PLACE_ITEM);
		query.whereEqualTo(ParseConstants.KEY_PLACE_ID, currentPlace);
		query.include(ParseConstants.KEY_ITEM_ID);
		query.include(ParseConstants.KEY_PLACE_ID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> items, ParseException e) {
				if (e == null) {
					for (ParseObject object : items) {
						HashMap<String, String> itemInfo = new HashMap<String, String>();
						ParseObject currentItem = object
								.getParseObject(ParseConstants.KEY_ITEM_ID);
						ParseObject currentPlace = object
								.getParseObject(ParseConstants.KEY_PLACE_ID);
						placeName = currentPlace
								.getString(ParseConstants.KEY_NAME);

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
					// setting up adapter
					setAdapter();
				} else {
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * Set Adapter
	 */

	private void setAdapter() {
		// dismiss the progress dialog
		String[] keys = { ParseConstants.KEY_NAME,
				ParseConstants.KEY_TOTAL_LOVED };
		int[] ids = { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter adapter = new SimpleAdapter(getActivity(), itemsInfo,
				android.R.layout.simple_list_item_2, keys, ids);
		mListTopItems.setAdapter(adapter);
	}

	/*
	 * On Click Listener
	 */

	OnItemClickListener itemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(getActivity(), ItemInformation.class);
			intent.putExtra(ParseConstants.KEY_ITEM_ID, itemId.get(position));
			startActivity(intent);

		}
	};

	OnClickListener addItemListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), AddItem.class);
			intent.putExtra(ParseConstants.KEY_PLACE_ID, placeId);
			intent.putExtra(ParseConstants.KEY_NAME, placeName);
			startActivity(intent);

		}
	};

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