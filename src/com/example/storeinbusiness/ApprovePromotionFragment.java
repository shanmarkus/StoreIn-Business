package com.example.storeinbusiness;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ApprovePromotionFragment extends Fragment {

	protected final static String TAG = ApprovePromotionFragment.class
			.getSimpleName().toString();

	// UI Variable
	EditText mApprovePromotionalText;
	Button mApproveButton;

	// Variables
	private String placeId;
	ProgressDialog progressDialog;

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

		// get PlaceId
		getPlaceId();
		Toast.makeText(getActivity(), placeId, Toast.LENGTH_SHORT).show();
		// Declare UI
		mApprovePromotionalText = (EditText) view
				.findViewById(R.id.approvePromotionalText);
		mApproveButton = (Button) view.findViewById(R.id.approveButton);

		// set on click listener
		mApproveButton.setOnClickListener(approvePromotionListener);

		return view;
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
	 * Progress Dialog init
	 */

	private void initProgressDialog() {
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Loading");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	/*
	 * OnClickButton
	 */

	OnClickListener approvePromotionListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// get objectId
			String objectId = mApprovePromotionalText.getText().toString()
					.trim();
			approvePromotion(objectId);
		}
	};

	/*
	 * Checking Promotion
	 */

	private void approvePromotion(String objectId) {
		// objectID = promotionRewardnya
		// placeId dapet dari pas tenant register
		initProgressDialog();

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
					ParseObject tempPlace = promotion
							.getParseObject(ParseConstants.KEY_PLACE_ID);

					// get IDs
					String userId = tempUser.getObjectId();
					String tempPlaceId = tempPlace.getObjectId();

					// get variables
					boolean status = promotion
							.getBoolean(ParseConstants.KEY_IS_CLAIMED);
					final Integer tempPromotionReward = tempPromotion
							.getInt(ParseConstants.KEY_REWARD_POINT);
					placeId = getPlaceId();

					// if is claimed is false then return false
					// else change it to true

					if (placeId.equals(tempPlaceId) && status == false) {
						promotion.put(ParseConstants.KEY_IS_CLAIMED, true);
						promotion.saveInBackground();
						Toast.makeText(getActivity(), "Promotion Claimed",
								Toast.LENGTH_SHORT).show();

						/*
						 * updating user rewards
						 */
						updateUserReward(userId, tempPromotionReward);

					} else {
						Toast.makeText(getActivity(),
								"User already claimed this promotion",
								Toast.LENGTH_SHORT).show();
						progressDialog.dismiss();
					}

				} else {
					progressDialog.dismiss();
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * updating user rewards
	 */

	private void updateUserReward(String userId,
			final Integer tempPromotionReward) {
		ParseObject currentUser = ParseObject.createWithoutData(
				ParseConstants.TABLE_USER, userId);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_USER_REWARD);
		query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject object, ParseException e) {
				if (e == null) {
					Integer currentReward = object
							.getInt(ParseConstants.KEY_REWARD_POINT);
					currentReward = currentReward + tempPromotionReward;
					object.put(ParseConstants.KEY_REWARD_POINT, currentReward);
					object.saveInBackground();
					progressDialog.dismiss();
					Toast.makeText(getActivity(), "Reward Updated",
							Toast.LENGTH_SHORT).show();
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