package com.example.storeinbusiness;

import java.io.ByteArrayOutputStream;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ItemDetail extends Fragment {
	private static final String TAG = ItemDetail.class.getSimpleName();

	// UI Variable Declaration

	EditText mItemDetailNameText;
	EditText mItemDetailDescriptionEditText;
	Button mItemDetailEditButton;
	Button mItemDetailDeleteButton;
	Button mItemDetailUploadImage;
	Button mItemDetailGetImage;

	// Fixed Variables
	public final static int REQ_CODE_PICK_IMAGE = 1;
	String itemTitle;
	String itemDesc;
	Bitmap yourSelectedImage;
	byte[] scaledData;
	ParseFile image;

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
		mItemDetailNameText = (EditText) rootView
				.findViewById(R.id.itemDetailNameText);
		mItemDetailDescriptionEditText = (EditText) rootView
				.findViewById(R.id.itemDetailDescriptionEditText);
		mItemDetailEditButton = (Button) rootView
				.findViewById(R.id.itemDetailEditButton);
		mItemDetailDeleteButton = (Button) rootView
				.findViewById(R.id.itemDetailDeleteButton);
		mItemDetailUploadImage = (Button) rootView
				.findViewById(R.id.itemDetailUploadImage);
		mItemDetailGetImage = (Button) rootView
				.findViewById(R.id.itemDetailGetImage);

		// set on click listener
		mItemDetailGetImage.setOnClickListener(getImage);
		mItemDetailEditButton.setOnClickListener(updateInfo);
		mItemDetailUploadImage.setOnClickListener(updateImage);
		mItemDetailDeleteButton.setOnClickListener(onDeleteButton);

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

				yourSelectedImage = BitmapFactory.decodeFile(filePath);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				yourSelectedImage
						.compress(Bitmap.CompressFormat.JPEG, 100, bos);

				scaledData = bos.toByteArray();

				image = new ParseFile(itemTitle.trim() + ".jpg", scaledData);
				image.saveInBackground();
				Toast.makeText(getActivity(), "image ready", Toast.LENGTH_SHORT)
						.show();
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
		itemId = args.getString(ParseConstants.KEY_ITEM_ID);
		return itemId;
	}

	/*
	 * Find the detail of an item including description and rating
	 */
	public void findItemDetail() {
		// set progress bar
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

					mItemDetailNameText.setText(itemTitle);
					mItemDetailDescriptionEditText.setText(itemDesc);
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
	 * remove all loved
	 */

	private void removeAllLoved() {
		String itemId = getItemId();
		removeRelation();
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ITEM_LOVED);
		query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> loves, ParseException e) {
				if (e == null) {
					for (ParseObject love : loves) {
						love.deleteInBackground();
					}
				} else {
					parseErrorDialog(e);
				}
			}
		});
	}

	/*
	 * remove relation
	 */

	private void removeRelation() {
		String itemId = getItemId();
		ParseObject currentItem = ParseObject.createWithoutData(
				ParseConstants.TABLE_ITEM, itemId);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_PLACE_ITEM);
		query.whereEqualTo(ParseConstants.KEY_ITEM_ID, currentItem);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject item, ParseException e) {
				if (e == null) {
					item.deleteInBackground();
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

						// get latest input
						itemTitle = mItemDetailNameText.getText().toString();
						itemDesc = mItemDetailDescriptionEditText.getText()
								.toString();

						if (item.getString(ParseConstants.KEY_NAME).equals(
								itemTitle)) {
							item.put(ParseConstants.KEY_DESCRIPTION, itemDesc);
						} else {
							item.put(ParseConstants.KEY_NAME, itemTitle);
							item.put(ParseConstants.KEY_DESCRIPTION, itemDesc);
							item.put(ParseConstants.KEY_RATING, 0);
							item.put(ParseConstants.KEY_TOTAL_LOVED, 0);
							removeAllComments();
							removeAllLoved();
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

	OnClickListener getImage = new OnClickListener() {

		@Override
		public void onClick(View v) {
			getImage();

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
						item.put(ParseConstants.KEY_IMAGE, image);
						item.saveInBackground();
						Toast.makeText(getActivity(), "Image Updated",
								Toast.LENGTH_SHORT).show();
					} else {
						parseErrorDialog(e);
					}

				}
			});

		}
	};

	OnClickListener onDeleteButton = new OnClickListener() {

		@Override
		public void onClick(View v) {
			itemId = getItemId();
			removeAllComments();
			removeAllLoved();
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM);
			query.getInBackground(itemId, new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject item, ParseException e) {
					if (e == null) {
						item.deleteInBackground(new DeleteCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null) {
									Intent intent = new Intent(getActivity(),
											MainActivity.class);
									startActivity(intent);
								} else {
									parseErrorDialog(e);
								}
							}
						});
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
