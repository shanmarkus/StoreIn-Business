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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class PromotionListFragment extends Fragment {

	protected final static String TAG = PromotionListFragment.class
			.getSimpleName().toString();

	// UI Variable

	// Variables
	ArrayList<HashMap<String, String>> promotionsInfo = new ArrayList<HashMap<String, String>>();
	protected ArrayList<String> promotionId = new ArrayList<String>();
	HashMap<String, String> promotionInfo = new HashMap<String, String>();

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
		View view = inflater.inflate(R.layout.fragment_promotion_list,
				container, false);

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
				.getQuery(ParseConstants.TABLE_REL_PROMOTION_PLACE);
		query.whereEqualTo(ParseConstants.KEY_PLACE_ID, currentPlace);
		query.include(ParseConstants.KEY_PROMOTION_ID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> promotions, ParseException e) {
				if (e == null) {
					for (ParseObject object : promotions) {
						HashMap<String, String> promotionInfo = new HashMap<String, String>();
						ParseObject currentPromotion = object
								.getParseObject(ParseConstants.KEY_PROMOTION_ID);
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

		// mListUsersReview.setAdapter(adapter);
	}

	/*
	 * On Click Listener
	 */

	OnItemClickListener itemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(getActivity(), ItemInformation.class);
			intent.putExtra(ParseConstants.KEY_ITEM_ID,
					promotionId.get(position));
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