package com.example.storeinbusiness;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
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

		EditText mAddPromotionStartDate;
		EditText mAddPromotionNameField;
		EditText mAddPromotionRequirement;
		EditText mAddPromotionEndDate;
		EditText mAddPromotionRewardPoint;
		EditText mAddPromotionQuota;

		CheckBox mAddPromotionClaimable;
		Button mAddPromotionSubmit;
		Button mAddPromotionGetImage;

		// Variables
		private String placeId;
		private String placeName;

		private String promotionId;
		private String promotionName;
		private String promotionRequirement;
		private boolean promotionClaimable;
		private Integer promotionRewards;
		private Date promotionStartDate;
		private Date promotionEndDate;

		private String promotionCategoryId;
		String[] categoriesId;
		public final static int REQ_CODE_PICK_IMAGE = 1;
		Bitmap yourSelectedImage;
		byte[] scaledData;
		ParseFile image;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_add_promotion,
					container, false);

			// Get values
			getPlaceId();
			categoriesId = getResources().getStringArray(R.array.category_id);

			// Initiate the UI
			mAddPromotionRewardPoint = (EditText) rootView
					.findViewById(R.id.addPromotionRewardPoint);
			mAddPromotionClaimable = (CheckBox) rootView
					.findViewById(R.id.addPromotionClaimable);
			mAddPromotionClaimable
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (mAddPromotionClaimable.isChecked()) {
								mAddPromotionQuota.setEnabled(true);
							} else {
								mAddPromotionQuota.setEnabled(false);
								mAddPromotionQuota.setText("");
							}
						}

					});

			mAddPromotionGetImage = (Button) rootView
					.findViewById(R.id.addPromotionGetImage);
			mAddPromotionNameField = (EditText) rootView
					.findViewById(R.id.addPromotionNameField);
			mAddPromotionQuota = (EditText) rootView
					.findViewById(R.id.addPromotionQuota);
			mAddPromotionRequirement = (EditText) rootView
					.findViewById(R.id.addPromotionRequirement);
			mAddPromotionSubmit = (Button) rootView
					.findViewById(R.id.addPromotionSubmit);

			mAddPromotionStartDate = (EditText) rootView
					.findViewById(R.id.addPromotionStartDate);
			mAddPromotionStartDate.addTextChangedListener(tw);
			mAddPromotionEndDate = (EditText) rootView
					.findViewById(R.id.addPromotionEndDate);
			mAddPromotionEndDate.addTextChangedListener(tws);

			mAddPromotionSpinner = (Spinner) rootView
					.findViewById(R.id.addPromotionSpinner);
			mAddPromotionSpinner
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View view, int position, long id) {
							int tempPosition = mAddPromotionSpinner
									.getSelectedItemPosition();
							promotionCategoryId = categoriesId[tempPosition];
							Toast.makeText(getActivity(), promotionCategoryId,
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

		@Override
		public void onPause() {
			super.onPause();
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode,
				Intent imageReturnedIntent) {
			super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

			switch (requestCode) {
			case REQ_CODE_PICK_IMAGE:
				if (resultCode == Activity.RESULT_OK) {
					Uri selectedImage = imageReturnedIntent.getData();
					String[] filePathColumn = { MediaStore.Images.Media.DATA };

					Cursor cursor = getActivity().getContentResolver().query(
							selectedImage, filePathColumn, null, null, null);
					cursor.moveToFirst();

					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String filePath = cursor.getString(columnIndex);
					cursor.close();

					yourSelectedImage = BitmapFactory.decodeFile(filePath);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					yourSelectedImage.compress(Bitmap.CompressFormat.JPEG, 100,
							bos);

					scaledData = bos.toByteArray();
					promotionName = mAddPromotionNameField.getText().toString()
							.trim();

					image = new ParseFile(promotionName + ".jpg", scaledData);
					image.saveInBackground();
					Toast.makeText(getActivity(), "image ready",
							Toast.LENGTH_SHORT).show();
				}
			}
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

		public String getPlaceName() {
			Intent intent = getActivity().getIntent();
			placeName = intent.getStringExtra(ParseConstants.KEY_NAME);
			return placeId;
		}

		private void getImage() {
			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, REQ_CODE_PICK_IMAGE);
		}

		/*
		 * Create new Promotion
		 */

		private void createPromotion() {
			if (placeId == null) {
				getPlaceId();
			}

			// Get value from UI
			promotionName = mAddPromotionNameField.getText().toString();
			promotionRequirement = mAddPromotionRequirement.getText()
					.toString();
			promotionStartDate = (Date) mAddPromotionStartDate.getText();
			promotionEndDate = (Date) mAddPromotionEndDate.getText();
			promotionClaimable = mAddPromotionClaimable.isChecked();
			promotionRewards = Integer.parseInt(mAddPromotionRewardPoint
					.getText().toString());
			ParseObject promotion = new ParseObject(
					ParseConstants.TABLE_PROMOTION);
			promotion.put(ParseConstants.KEY_NAME, promotionName);
			promotion.put(ParseConstants.KEY_REQUIREMENT, promotionRequirement);
			promotion.put(ParseConstants.KEY_PROMOTION_ID, promotionCategoryId);
			promotion.put(ParseConstants.KEY_REWARD_POINT, promotionRewards);
			promotion.put(ParseConstants.KEY_START_DATE, promotionStartDate);
			promotion.put(ParseConstants.KEY_END_DATE, promotionEndDate);
			promotion.put(ParseConstants.KEY_CLAIMABLE, promotionClaimable);
			promotion.put(ParseConstants.KEY_IMAGE, image);

			if (promotionClaimable == true) {
				promotion.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						createPromoQuota(promotion)

					}
				});
			}

		}

		/*
		 * If Claimable is true the set the quota of the promotion
		 */

		private void createPromoQuota(Integer promotionQuota,
				final String promotionId) {
			if (placeId == null) {
				getPlaceId();
			}
			ParseObject currentPlace = ParseObject.createWithoutData(
					ParseConstants.TABLE_PLACE, placeId);
			ParseObject currentPromotion = ParseObject.createWithoutData(
					ParseConstants.TABLE_PROMOTION, promotionId);
			ParseObject relPromotionQuota = new ParseObject(
					ParseConstants.TABLE_PROMOTION_QUOTA);
			relPromotionQuota.put(ParseConstants.KEY_PLACE_ID, currentPlace);
			relPromotionQuota
					.put(ParseConstants.KEY_PROMOTION_ID, currentPromotion);
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
		 * Text Watcher
		 */

		TextWatcher tw = new TextWatcher() {
			private String current = "";
			private String ddmmyyyy = "DDMMYYYY";

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!s.toString().equals(current)) {
					String clean = s.toString().replaceAll("[^\\d.]", "");
					String cleanC = current.replaceAll("[^\\d.]", "");

					int cl = clean.length();
					int sel = cl;
					for (int i = 2; i <= cl && i < 6; i += 2) {
						sel++;
					}
					// Fix for pressing delete next to a forward slash
					if (clean.equals(cleanC))
						sel--;

					if (clean.length() < 8) {
						clean = clean + ddmmyyyy.substring(clean.length());
					} else {
						// This part makes sure that when we finish entering
						// numbers
						// the date is correct, fixing it otherways
						int day = Integer.parseInt(clean.substring(0, 2));
						int mon = Integer.parseInt(clean.substring(2, 4));
						int year = Integer.parseInt(clean.substring(4, 8));

						if (mon > 12)
							mon = 12;
						Calendar cal = new GregorianCalendar();
						cal.set(Calendar.MONTH, mon - 1);
						day = (day > cal.getActualMaximum(Calendar.DATE)) ? cal
								.getActualMaximum(Calendar.DATE) : day;
						year = (year < 1900) ? 1900 : (year > 2100) ? 2100
								: year;
						clean = String.format("%02d%02d%02d", day, mon, year);
					}

					clean = String.format("%s/%s/%s", clean.substring(0, 2),
							clean.substring(2, 4), clean.substring(4, 8));
					current = clean;
					mAddPromotionStartDate.setText(current);
					mAddPromotionStartDate
							.setSelection(sel < current.length() ? sel
									: current.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};

		TextWatcher tws = new TextWatcher() {
			private String current = "";
			private String ddmmyyyy = "DDMMYYYY";

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!s.toString().equals(current)) {
					String clean = s.toString().replaceAll("[^\\d.]", "");
					String cleanC = current.replaceAll("[^\\d.]", "");

					int cl = clean.length();
					int sel = cl;
					for (int i = 2; i <= cl && i < 6; i += 2) {
						sel++;
					}
					// Fix for pressing delete next to a forward slash
					if (clean.equals(cleanC))
						sel--;

					if (clean.length() < 8) {
						clean = clean + ddmmyyyy.substring(clean.length());
					} else {
						// This part makes sure that when we finish entering
						// numbers
						// the date is correct, fixing it otherways
						int day = Integer.parseInt(clean.substring(0, 2));
						int mon = Integer.parseInt(clean.substring(2, 4));
						int year = Integer.parseInt(clean.substring(4, 8));

						if (mon > 12)
							mon = 12;
						Calendar cal = new GregorianCalendar();
						cal.set(Calendar.MONTH, mon - 1);
						day = (day > cal.getActualMaximum(Calendar.DATE)) ? cal
								.getActualMaximum(Calendar.DATE) : day;
						year = (year < 1900) ? 1900 : (year > 2100) ? 2100
								: year;
						clean = String.format("%02d%02d%02d", day, mon, year);
					}

					clean = String.format("%s/%s/%s", clean.substring(0, 2),
							clean.substring(2, 4), clean.substring(4, 8));
					current = clean;
					mAddPromotionEndDate.setText(current);
					mAddPromotionEndDate
							.setSelection(sel < current.length() ? sel
									: current.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};

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
