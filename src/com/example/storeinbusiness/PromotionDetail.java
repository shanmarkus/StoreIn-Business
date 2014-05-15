package com.example.storeinbusiness;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class PromotionDetail extends Fragment {
	private static final String TAG = PromotionDetail.class.getSimpleName();

	// UI Variable Declaration
	ParseImageView mImageView;

	// Fixed Variables
	private Integer totalClaimed;
	public final static int REQ_CODE_PICK_IMAGE = 1;
	String promotionTitle;
	String promotionDesc;
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
		View rootView = inflater.inflate(R.layout.fragment_item_detail,
				container, false);

		// Declare UI Variables

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
		promotionId = args.getString(ParseConstants.KEY_OBJECT_ID);
		return promotionId;
	}

	/*
	 * Find the detail of an item including description and rating
	 */
	public void findPromotionDetail() {
		// set progress bar
		getActivity().setProgressBarIndeterminate(true);

		// do the query
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_PROMOTION);
		query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, promotionId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject promotion, ParseException e) {
				// set progress bar
				getActivity().setProgressBarIndeterminate(false);
				if (e == null) {
					// success
					promotionTitle = promotion
							.getString(ParseConstants.KEY_NAME);
					promotionDesc = promotion
							.getString(ParseConstants.KEY_DESCRIPTION);
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

				} else {
					// failed
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
	 * Remove User Review if the store change their item
	 */

	private void removeAllComments() {
		String itemId = getItemId();
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ITEM_REVIEW);
		query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					for (ParseObject object : objects) {
						object.deleteInBackground();
					}
					Toast.makeText(getActivity(), "delete comments successful",
							Toast.LENGTH_SHORT).show();
				} else {
					parseErrorDialog(e);
				}
			}
		});
	}

	/*
	 * update details item to the database on clickbutton
	 */

	OnClickListener updateInfo = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String itemId = getItemId();
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM);
			query.getInBackground(itemId, new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject item, ParseException e) {
					if (e == null) {
						if (item.getString(ParseConstants.KEY_NAME).equals(
								itemTitle)) {
							item.put(ParseConstants.KEY_NAME, itemTitle);
							item.put(ParseConstants.KEY_DESCRIPTION, itemDesc);
							item.put(ParseConstants.KEY_RATING, 0);
							item.put(ParseConstants.KEY_TOTAL_LOVED, 0);
							removeAllComments();
						} else {
							item.put(ParseConstants.KEY_DESCRIPTION, itemDesc);
						}
						item.saveInBackground();
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
			String itemId = getItemId();
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM);
			query.getInBackground(itemId, new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject item, ParseException e) {
					if (e == null) {
						ParseFile image = new ParseFile(itemTitle + ".jpg",
								scaledData);
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
