package com.example.storeinbusiness;

import java.util.ArrayList;
import java.util.Date;
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

public class PromotionListFragment extends Fragment {

	protected final static String TAG = PromotionListFragment.class
			.getSimpleName().toString();

	// UI Variable
	ListView mListPromotions;
	Button mPromotionListAddPromotion;

	// Variables
	ArrayList<HashMap<String, String>> promotionsInfo = new ArrayList<HashMap<String, String>>();
	protected ArrayList<String> promotionId = new ArrayList<String>();
	HashMap<String, String> promotionInfo = new HashMap<String, String>();
	String placeId;
	String placeName;

	// Parse Constants

	public static PromotionListFragment newInstance(String param1, String param2) {
		PromotionListFragment fragment = new PromotionListFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public PromotionListFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		getItemList();
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
		View rootView = inflater.inflate(R.layout.fragment_promotion_list,
				container, false);
		getPlaceId();

		// Declare UI Variables
		mListPromotions = (ListView) rootView.findViewById(R.id.listPromotions);
		mPromotionListAddPromotion = (Button) rootView
				.findViewById(R.id.promotionListAddPromotion);

		mPromotionListAddPromotion.setOnClickListener(addButtonListener);
		mListPromotions.setOnItemClickListener(itemListener);
		return rootView;
	}

	/*
	 * Function Added
	 */

	/*
	 * Getter for placeId variables
	 */
	public String getPlaceId() {
		Bundle args = getArguments();
		placeId = args.getString(ParseConstants.KEY_PLACE_ID);
		return placeId;
	}

	/*
	 * Clear ArrayList
	 */

	private void clearArrayList() {
		promotionsInfo.clear();
		promotionInfo.clear();
		promotionId.clear();
	}

	/*
	 * Checking Promotion
	 */

	private void getItemList() {
		if (placeId == null) {
			getPlaceId();
		}
		clearArrayList();
		ParseObject currentPlace = ParseObject.createWithoutData(
				ParseConstants.TABLE_PLACE, placeId);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_PROMOTION_PLACE);
		query.whereEqualTo(ParseConstants.KEY_PLACE_ID, currentPlace);
		query.include(ParseConstants.KEY_PROMOTION_ID);
		query.include(ParseConstants.KEY_PLACE_ID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> promotions, ParseException e) {
				if (e == null) {
					for (ParseObject object : promotions) {
						HashMap<String, String> promotionInfo = new HashMap<String, String>();
						ParseObject currentPromotion = object
								.getParseObject(ParseConstants.KEY_PROMOTION_ID);
						ParseObject currentPlace = object
								.getParseObject(ParseConstants.KEY_PLACE_ID);

						placeName = currentPlace
								.getString(ParseConstants.KEY_NAME);

						String promotionName = currentPromotion
								.getString(ParseConstants.KEY_NAME);
						Date createdAt = currentPromotion.getCreatedAt();

						promotionInfo.put(ParseConstants.KEY_NAME,
								promotionName);
						promotionInfo.put(ParseConstants.KEY_CREATED_AT,
								createdAt.toString());

						promotionId.add(currentPromotion.getObjectId());
						promotionsInfo.add(promotionInfo);
					}
					// Setting adapter
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
				ParseConstants.KEY_CREATED_AT };
		int[] ids = { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter adapter = new SimpleAdapter(getActivity(),
				promotionsInfo, android.R.layout.simple_list_item_2, keys, ids);

		mListPromotions.setAdapter(adapter);
	}

	/*
	 * On Click Listener
	 */

	// for editing / deleting
	OnItemClickListener itemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(getActivity(),
					PromotionInformation.class);
			intent.putExtra(ParseConstants.KEY_PROMOTION_ID,
					promotionId.get(position));
			startActivity(intent);
		}
	};

	// for adding new promotion
	OnClickListener addButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), AddPromotion.class);
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