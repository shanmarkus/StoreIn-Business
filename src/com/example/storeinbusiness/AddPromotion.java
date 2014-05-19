package com.example.storeinbusiness;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class AddPromotion extends ActionBarActivity {

	private static final String TAG = AddPromotion.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_promotion);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_promotion, menu);
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
		// UI Declaration
		Spinner mAddPromotionSpinner;

		// Variables
		private String placeId;
		private String promotionId;
		private String promotionName;
		private String promotionDescription;
		private String promotionRequirement;
		private boolean promotionClaimable;
		private Integer promotionRewards;
		private Date promotionStartDate;
		private Date promotionEndDate;

		private String promotionCategoryId;
		Resources res = getResources();
		final TypedArray selectedValues = res
				.obtainTypedArray(R.array.category_id);

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_add_promotion,
					container, false);

			// Initiate the UI
			mAddPromotionSpinner = (Spinner) rootView
					.findViewById(R.id.addPromotionSpinner);
			mAddPromotionSpinner
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View view, int position, long id) {
							String selectedValue = selectedValues
									.getString(position);
							Toast.makeText(getActivity(), selectedValue,
									Toast.LENGTH_SHORT).show();

						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {
							// TODO Auto-generated method stub

						}
					});
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
		}

		/*
		 * Added Function
		 */

		/*
		 * Getter for placeId , placeName, and image variables
		 */
		public String getPlaceId() {
			Intent intent = getActivity().getIntent();
			placeId = intent.getStringExtra(ParseConstants.KEY_PLACE_ID);
			return placeId;
		}

		/*
		 * Create new Promotion
		 */

		private void createPromotion() {
			if (placeId == null) {
				getPlaceId();
			}

			// Get value from UI

			ParseObject promotion = new ParseObject(
					ParseConstants.TABLE_PROMOTION);

		}

		/*
		 * If Claimable is true the set the quota of the promotion
		 */

		private void createPromoQuota(final String promotionId) {
			if (placeId == null) {
				getPlaceId();
			}
			Integer promotionQuota = 0;
			ParseObject currentPlace = ParseObject.createWithoutData(
					ParseConstants.TABLE_PLACE, placeId);
			ParseObject currentPromotion = ParseObject.createWithoutData(
					ParseConstants.TABLE_PROMOTION, promotionId);
			ParseObject relPromotionQuota = new ParseObject(
					ParseConstants.TABLE_PROMOTION_QUOTA);
			relPromotionQuota.put(ParseConstants.KEY_PLACE_ID, placeId);
			relPromotionQuota.put(ParseConstants.KEY_PROMOTION_ID, promotionId);
			relPromotionQuota.put(ParseConstants.KEY_QUOTA, promotionQuota);
			relPromotionQuota.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						Toast.makeText(getActivity(),
								"Setting Quota Successfully",
								Toast.LENGTH_SHORT).show();
						createRelPlacePromo(promotionId);
					} else {
						errorAlertDialog(e);
					}
				}
			});

		}

		/*
		 * Create Relation
		 */

		private void createRelPlacePromo(String promotionId) {
			ParseObject currentPlace = ParseObject.createWithoutData(
					ParseConstants.TABLE_PLACE, placeId);
			ParseObject currentPromotion = ParseObject.createWithoutData(
					ParseConstants.TABLE_PROMOTION, promotionId);
			ParseObject relPlacePromo = new ParseObject(
					ParseConstants.TABLE_REL_PROMOTION_PLACE);
			relPlacePromo.put(ParseConstants.KEY_PLACE_ID, currentPlace);
			relPlacePromo.put(ParseConstants.KEY_PROMOTION_ID, promotionId);
			relPlacePromo.put(ParseConstants.KEY_TOTAL_CLAIMED, 0);
			relPlacePromo.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						Toast.makeText(getActivity(),
								"Saving Relation Success", Toast.LENGTH_SHORT)
								.show();
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

		/*
		 * Parse Error Method
		 */

		private void errorAlertDialog(ParseException e) {
			// failed
			Log.e(TAG, e.getMessage());
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(e.getMessage()).setTitle(R.string.error_title)
					.setPositiveButton(android.R.string.ok, null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

}
