package com.example.storeinbusiness;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.parse.SendCallback;

public class AddItem extends ActionBarActivity {

	private static final String TAG = AddItem.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_item);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_item, menu);
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

		// UI Variables
		EditText mAddItemNameField;
		EditText mAddItemDescriptionField;
		Button mAddItemSubmitButton;
		Button mAddItemGetImageButton;

		// Variables
		private String placeId;
		private String placeName;
		String itemTitle;
		String itemDesc;
		String itemId;
		public final static int REQ_CODE_PICK_IMAGE = 1;
		Bitmap yourSelectedImage;
		byte[] scaledData;
		ParseFile image;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_add_item,
					container, false);

			// Get Intent Variables
			getPlaceId();
			getPlaceName();

			// UI Declaration
			mAddItemNameField = (EditText) rootView
					.findViewById(R.id.addItemNameField);
			mAddItemDescriptionField = (EditText) rootView
					.findViewById(R.id.addItemDescriptionField);
			mAddItemGetImageButton = (Button) rootView
					.findViewById(R.id.addItemGetImageButton);
			mAddItemSubmitButton = (Button) rootView
					.findViewById(R.id.addItemSubmitButton);

			mAddItemGetImageButton.setOnClickListener(getImageButton);
			mAddItemSubmitButton.setOnClickListener(submitButton);

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
					yourSelectedImage.compress(Bitmap.CompressFormat.JPEG, 100,
							bos);

					scaledData = bos.toByteArray();
					itemTitle = mAddItemNameField.getText().toString().trim();

					image = new ParseFile(itemTitle + ".jpg", scaledData);
					image.saveInBackground();
					Toast.makeText(getActivity(), "image ready",
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		/*
		 * add function
		 */

		/*
		 * Create new Item
		 */

		private void createItem() {
			if (placeId == null) {
				getPlaceId();
			}
			// get values
			itemTitle = mAddItemNameField.getText().toString();
			itemDesc = mAddItemDescriptionField.getText().toString();

			final ParseObject item = new ParseObject(ParseConstants.TABLE_ITEM);
			item.put(ParseConstants.KEY_NAME, itemTitle);
			item.put(ParseConstants.KEY_DESCRIPTION, itemDesc);
			item.put(ParseConstants.KEY_RATING, 0);
			item.put(ParseConstants.KEY_TOTAL_LOVED, 0);
			
			if (image == null) {
				Toast.makeText(
						getActivity(),
						"You have not put any image, please select image by clicking get image button",
						Toast.LENGTH_SHORT).show();
			}
			if (image != null) {
				item.put(ParseConstants.KEY_IMAGE, image);
			}

			item.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						itemId = item.getObjectId();
						createRelPlaceItem(itemId);
					} else {
						errorAlertDialog(e);
					}
				}
			});

		}

		/*
		 * Create new Relation
		 */

		private void createRelPlaceItem(String itemId) {
			if (placeId == null) {
				getPlaceId();
			}
			ParseObject currentItem = ParseObject.createWithoutData(
					ParseConstants.TABLE_ITEM, itemId);
			ParseObject currentPlace = ParseObject.createWithoutData(
					ParseConstants.TABLE_PLACE, placeId);
			ParseObject relItemPlace = new ParseObject(
					ParseConstants.TABLE_REL_PLACE_ITEM);
			relItemPlace.put(ParseConstants.KEY_PLACE_ID, currentPlace);
			relItemPlace.put(ParseConstants.KEY_ITEM_ID, currentItem);
			relItemPlace.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						String message = "Creating this " + itemTitle;
						builder.setMessage(message)
								.setPositiveButton("Ok", dialogAddItemListener)
								.setNeutralButton("Share",
										dialogAddItemListener)
								.setNegativeButton("No", dialogAddItemListener)
								.show();
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

		private void goToMainActivity() {
			Toast.makeText(getActivity(), "Save item Success",
					Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(getActivity(), MainActivity.class);
			intent.putExtra(ParseConstants.KEY_PLACE_ID, placeId);
			startActivity(intent);
		}

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
		 * On Click Listener
		 */

		OnClickListener getImageButton = new OnClickListener() {

			@Override
			public void onClick(View v) {
				getImage();
			}
		};

		OnClickListener submitButton = new OnClickListener() {

			@Override
			public void onClick(View v) {
				createItem();
			}
		};

		/*
		 * Alert Dialog For Adding Item
		 */

		DialogInterface.OnClickListener dialogAddItemListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					goToMainActivity();
					break;

				case DialogInterface.BUTTON_NEUTRAL:
					if (placeName == null) {
						getPlaceName();
					}
					String message = "Check Out " + itemTitle;
					ParsePush push = new ParsePush();
					push.setChannel(placeId);
					push.setMessage(message + "- Only Available at "
							+ placeName);
					push.sendInBackground(new SendCallback() {

						@Override
						public void done(ParseException e) {
							if (e == null) {
								Toast.makeText(getActivity(),
										"Send messasge successfully",
										Toast.LENGTH_SHORT).show();
								goToMainActivity();
							} else {
								errorAlertDialog(e);
							}
						}
					});
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// do nothing
					break;
				}
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
