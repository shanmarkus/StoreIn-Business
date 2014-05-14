package com.example.storeinbusiness;

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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ItemDetail extends Fragment {
	private static final String TAG = ItemDetail.class.getSimpleName();

	// UI Variable Declaration
	ParseImageView mImageView;

	// Fixed Variables
	private Integer totalLoved;
	public final static int REQ_CODE_PICK_IMAGE = 1;
	String itemTitle;
	String itemDesc;

	// Parse Variables
	ParseUser user = ParseUser.getCurrentUser();
	String userId = user.getObjectId();

	// Intent Variables
	protected static String itemId;
	protected static String isLoved;

	public ItemDetail() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail,
				container, false);

		// Declare UI Variables

		// Intent Variables
		// Setup Variable from the previous intents
		getItemId();

		// Find Item Details
		findItemDetail();

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

				Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
			}
		}
	}

	/*
	 * Added function
	 */

	/*
	 * Getter for placeId variables
	 */
	public String getItemId() {
		Bundle args = getArguments();
		itemId = args.getString(ParseConstants.KEY_OBJECT_ID);
		return itemId;
	}

	/*
	 * Find the detail of an item including description and rating
	 */
	public void findItemDetail() {
		// set progress bar
		getActivity().setProgressBarIndeterminate(true);

		// do the query
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ITEM);
		query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, itemId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject item, ParseException e) {
				// set progress bar
				getActivity().setProgressBarIndeterminate(false);
				if (e == null) {
					// success
					itemTitle = item.getString(ParseConstants.KEY_NAME);
					itemDesc = item.getString(ParseConstants.KEY_DESCRIPTION);

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
						item.put(ParseConstants.KEY_NAME, itemTitle);
						item.put(ParseConstants.KEY_DESCRIPTION, itemDesc);
						item.saveInBackground();
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
			ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseCo)

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
