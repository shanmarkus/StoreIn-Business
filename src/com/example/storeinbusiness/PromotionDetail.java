package com.example.storeinbusiness;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class PromotionDetail extends Fragment {
	private static final String TAG = PromotionDetail.class.getSimpleName();

	// UI Variable Declaration
	EditText mPromotionDetailStartDate;
	EditText mPromotionDetailNameField;
	EditText mPromotionDetailRequirement;
	EditText mPromotionDetailEndDate;
	EditText mPromotionDetailRewardPoint;
	EditText mPromotionDetailQuota;

	CheckBox mPromotionDetailClaimable;
	Button mPromotionDetailSubmit;
	Button mPromotionDetailGetImage;

	// Fixed Variables
	private Integer totalClaimed;
	public final static int REQ_CODE_PICK_IMAGE = 1;
	String promotionTitle;
	String promotionRequirement;
	Boolean promotionClaimable;
	Integer promotionRewardPoint;
	Date promotionEndDate;
	Date promotionStartDate;

	Bitmap yourSelectedImage;
	byte[] scaledData;

	// Parse Variables
	ParseUser user = ParseUser.getCurrentUser();
	String userId = user.getObjectId();

	// Intent Variables
	protected static String promotionId;

	public PromotionDetail() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_promotion_detail,
				container, false);

		// Declare UI Variables
		mPromotionDetailRewardPoint = (EditText) rootView
				.findViewById(R.id.promotionDetailRewardPoint);
		mPromotionDetailClaimable = (CheckBox) rootView
				.findViewById(R.id.promotionDetailClaimable);
		mPromotionDetailClaimable
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (mPromotionDetailClaimable.isChecked()) {
							mPromotionDetailQuota.setEnabled(true);
						} else {
							mPromotionDetailQuota.setEnabled(false);
							mPromotionDetailQuota.setText("");
						}
					}

				});

		mPromotionDetailNameField = (EditText) rootView
				.findViewById(R.id.promotionDetailNameField);
		mPromotionDetailQuota = (EditText) rootView
				.findViewById(R.id.promotionDetailQuota);
		mPromotionDetailRequirement = (EditText) rootView
				.findViewById(R.id.promotionDetailRequirement);

		mPromotionDetailSubmit = (Button) rootView
				.findViewById(R.id.promotionDetailSubmit);
		mPromotionDetailSubmit.setOnClickListener(submitButton);

		mPromotionDetailGetImage = (Button) rootView
				.findViewById(R.id.promotionDetailGetImage);
		mPromotionDetailGetImage.setOnClickListener(getImage);

		mPromotionDetailStartDate = (EditText) rootView
				.findViewById(R.id.promotionDetailStartDate);
		mPromotionDetailStartDate.addTextChangedListener(tw);
		mPromotionDetailEndDate = (EditText) rootView
				.findViewById(R.id.promotionDetailEndDate);
		mPromotionDetailEndDate.addTextChangedListener(tws);

		// Intent Variables
		// Setup Variable from the previous intents
		getPromotionId();

		// Find Promotion Details
		findPromotionDetail();

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

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
				yourSelectedImage
						.compress(Bitmap.CompressFormat.JPEG, 100, bos);

				scaledData = bos.toByteArray();
			}
		}
	}

	/*
	 * Added function
	 */

	/*
	 * Getter for placeId variables
	 */
	public String getPromotionId() {
		Bundle args = getArguments();
		promotionId = args.getString(ParseConstants.KEY_PROMOTION_ID);
		return promotionId;
	}

	/*
	 * Find the detail of an item including description and rating
	 */
	public void findPromotionDetail() {
		if (promotionId == null) {
			getPromotionId();
		}
		// do the query
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_PROMOTION);
		query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, promotionId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject promotion, ParseException e) {
				if (e == null) {
					// success
					promotionTitle = promotion
							.getString(ParseConstants.KEY_NAME);
					promotionRequirement = promotion
							.getString(ParseConstants.KEY_REQUIREMENT);
					promotionStartDate = promotion
							.getDate(ParseConstants.KEY_START_DATE);
					promotionEndDate = promotion
							.getDate(ParseConstants.KEY_END_DATE);
					promotionClaimable = promotion
							.getBoolean(ParseConstants.KEY_CLAIMABLE);
					promotionRewardPoint = promotion
							.getInt(ParseConstants.KEY_REWARD_POINT);

					mPromotionDetailNameField.setText(promotionTitle);
					mPromotionDetailRequirement.setText(promotionRequirement);

					DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					String temp = sdf.format(promotionStartDate);
					String tempEnd = sdf.format(promotionEndDate);

					mPromotionDetailStartDate.setText(temp);
					mPromotionDetailEndDate.setText(tempEnd);

					mPromotionDetailClaimable.setChecked(promotionClaimable);
					mPromotionDetailRewardPoint.setText(promotionRewardPoint
							.toString());
					if (promotionClaimable == true) {
						getQuota();
					} else {
						// do nothing
					}
				} else {
					// failed
					Toast.makeText(getActivity(), "here", Toast.LENGTH_SHORT)
							.show();
					parseErrorDialog(e);
				}
			}
		});

	}

	/*
	 * Find the number of quota
	 */

	private void getQuota() {
		if (promotionId == null) {
			getPromotionId();
		}
		ParseObject currentPromotion = ParseObject.createWithoutData(
				ParseConstants.TABLE_PROMOTION, promotionId);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_PROMOTION_QUOTA);
		query.whereEqualTo(ParseConstants.KEY_PROMOTION_ID, currentPromotion);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject promotionQuota, ParseException e) {
				if (e == null) {
					Integer temp = promotionQuota
							.getInt(ParseConstants.KEY_QUOTA);
					mPromotionDetailQuota.setText(temp.toString());
				} else {
					parseErrorDialog(e);
				}
			}
		});
	}

	/*
	 * Intent on finding image on gallery
	 */

	private void getImage() {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQ_CODE_PICK_IMAGE);
	}

	/*
	 * On Click Listener
	 */

	OnClickListener getImage = new OnClickListener() {

		@Override
		public void onClick(View v) {
			getImage();
		}
	};

	OnClickListener submitButton = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// createPromotion();
		}
	};

	/*
	 * update details promotion to the database on clickbutton
	 */

	OnClickListener updateInfo = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String promotionId = getPromotionId();
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_PROMOTION);
			query.getInBackground(promotionId, new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject promotion, ParseException e) {
					if (e == null) {
						if (promotion.getString(ParseConstants.KEY_NAME)
								.equals(promotionTitle)) {
							promotion.put(ParseConstants.KEY_NAME,
									promotionTitle);
							promotion.put(ParseConstants.KEY_REQUIREMENT,
									promotionRequirement);
							promotion.put(ParseConstants.KEY_START_DATE,
									promotionStartDate);
							promotion.put(ParseConstants.KEY_END_DATE,
									promotionEndDate);
						} else {
							promotion.put(ParseConstants.KEY_START_DATE,
									promotionStartDate);
							promotion.put(ParseConstants.KEY_END_DATE,
									promotionEndDate);
						}
						promotion.saveInBackground();
						Toast.makeText(getActivity(), "Item Updated",
								Toast.LENGTH_SHORT).show();
					} else {
						parseErrorDialog(e);
					}
				}
			});
		}
	};

	OnClickListener updateImage = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String promotionId = getPromotionId();
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM);
			query.getInBackground(promotionId, new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject item, ParseException e) {
					if (e == null) {
						ParseFile image = new ParseFile(
								promotionTitle + ".jpg", scaledData);
						image.saveInBackground();
						Toast.makeText(getActivity(), "Image Updated",
								Toast.LENGTH_SHORT).show();
					} else {
						parseErrorDialog(e);
					}

				}
			});

		}
	};

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
					year = (year < 1900) ? 1900 : (year > 2100) ? 2100 : year;
					clean = String.format("%02d%02d%02d", day, mon, year);
				}

				clean = String.format("%s/%s/%s", clean.substring(0, 2),
						clean.substring(2, 4), clean.substring(4, 8));
				current = clean;
				mPromotionDetailStartDate.setText(current);
				mPromotionDetailStartDate
						.setSelection(sel < current.length() ? sel : current
								.length());
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
					year = (year < 1900) ? 1900 : (year > 2100) ? 2100 : year;
					clean = String.format("%02d%02d%02d", day, mon, year);
				}

				clean = String.format("%s/%s/%s", clean.substring(0, 2),
						clean.substring(2, 4), clean.substring(4, 8));
				current = clean;
				mPromotionDetailEndDate.setText(current);
				mPromotionDetailEndDate
						.setSelection(sel < current.length() ? sel : current
								.length());
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
