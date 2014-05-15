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

public class ApprovePromotionFragment extends Fragment {

	protected final static String TAG = ApprovePromotionFragment.class
			.getSimpleName().toString();

	// UI Variable
	Button mDiscoverButtonCheckIn;
	Button mDiscoverButtonBrowse;
	Button mDiscoverButtonReccomendation;

	// Variables
	private String placeId;

	// Parse Constants

	public static ApprovePromotionFragment newInstance(String param1,
			String param2) {
		ApprovePromotionFragment fragment = new ApprovePromotionFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public ApprovePromotionFragment() {
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
		View view = inflater.inflate(R.layout.fragment_approve_promotion,
				container, false);

		// Declare UI
		mDiscoverButtonBrowse = (Button) view
				.findViewById(R.id.discoverButtonBrowse);
		mDiscoverButtonCheckIn = (Button) view
				.findViewById(R.id.discoverButtonCheckIn);
		mDiscoverButtonReccomendation = (Button) view
				.findViewById(R.id.discoverButtonDiscover);

		return view;
	}

	/*
	 * Function Added
	 */

	/*
	 * Checking Promotion
	 */

	private void approvePromotion() {
		// objectID = promotionRewardnya
		// placeId dapet dari pas tenant register
		
		String objectId = null;
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ACTV_USER_CLAIM_PROMOTION);
		query.include(ParseConstants.KEY_PROMOTION_ID);
		query.include(ParseConstants.KEY_USER_ID);
		query.include(ParseConstants.KEY_PLACE_ID);
		
		query.getInBackground(objectId, new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject promotion, ParseException e) {
				if (e == null) {
					ParseObject tempUser = promotion
							.getParseObject(ParseConstants.KEY_USER_ID);
					ParseObject tempPromotion = promotion
							.getParseObject(ParseConstants.KEY_PROMOTION_ID);
					ParseObject tempPlace = promotion.getParseObject(ParseConstants.KEY_PLACE_ID);

					// get IDs
					String userId = tempUser.getObjectId();
					String tempPlaceId = tempPlace.getObjectId();
					
					// get variables
					boolean status = promotion
							.getBoolean(ParseConstants.KEY_IS_CLAIMED);
					Integer promotionReward = tempPromotion
							.getInt(ParseConstants.KEY_REWARD_POINT);
					
					if(placeId.equals(tempPlaceId) && )

					// if is claimed is false then return false
					// else change it to true

					
					if (status == false) {
						promotion.put(ParseConstants.KEY_IS_CLAIMED, true);
						promotion.saveInBackground();
						Toast.makeText(getActivity(), "Promotion Claimed",
								Toast.LENGTH_SHORT).show();

						// Run second query for adding rewards point
						ParseQuery<ParseObject> innerQuery = ParseQuery
								.getQuery(ParseConstants.TABLE_USER);
						innerQuery.getInBackground(userId,
								new GetCallback<ParseObject>() {

									@Override
									public void done(ParseObject user,
											ParseException e) {
										if (e == null) {

										} else {
											errorAlertDialog(e);
										}
									}
								});
					} else {
						Toast.makeText(getActivity(),
								"User already claimed this promotion",
								Toast.LENGTH_SHORT).show();
					}

				} else {
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * Adding reward to User
	 */

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