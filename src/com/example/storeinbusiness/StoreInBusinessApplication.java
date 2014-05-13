package com.example.storeinbusiness;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class StoreInBusinessApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "mgDWPieWTeiy4SRWiefaJ5QFLTlj2XGJwVAVuiJ3",
				"g5j92J8jFzZXupil1EWT2Y0Sbo0ExCKvpdI1bRXV");

		// push notification testing
		PushService.setDefaultPushCallback(this, MainActivity.class);

		ParseInstallation.getCurrentInstallation().saveInBackground();
		Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

	}
}
