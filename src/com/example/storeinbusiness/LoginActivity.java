package com.example.storeinbusiness;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
		Button mLoginButton;
		EditText mUserNameField;
		EditText mPasswordField;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_login,
					container, false);
			// Set up the login form.
			mUserNameField = (EditText) rootView
					.findViewById(R.id.usernameField);
			mPasswordField = (EditText) rootView
					.findViewById(R.id.passwordField);
			mLoginButton = (Button) rootView.findViewById(R.id.loginButton);
			return rootView;
		}

		/*
		 * Added Function
		 */

		/*
		 * Navigate to main Activity function
		 */

		private void navigateToMainActivity() {

			Intent intent = new Intent(getActivity(), MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}

		/*
		 * On click login button listener
		 */
		OnClickListener loginButtonListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				String username = mUserNameField.getText().toString();
				String password = mPasswordField.getText().toString();

				username = username.trim();
				password = password.trim();

				if (username.isEmpty() || password.isEmpty()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setMessage("username or password field cannot be empty !");
					builder.setTitle("Error");
					builder.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					// Login
					ParseUser.logInInBackground(username, password,
							new LogInCallback() {

								@Override
								public void done(ParseUser user,
										ParseException e) {
									if (e == null) {
										navigateToMainActivity();
									} else {
										errorAlertDialog(e);
									}
								}
							});
				}

			}
		};

		/*
		 * Parse Error handling
		 */

		private void errorAlertDialog(ParseException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(e.getMessage()).setTitle("Error")
					.setPositiveButton(android.R.string.ok, null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

}
