package org.pravin.android.sendcontact;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SendContactActivity extends Activity implements OnClickListener {
	private static final String TAG= "ListContactsActivity";
	private static final int PICK_CONTACT_REQUEST = 1;
	private ContentAccessor contentAccessor;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button pickContactButton = (Button) findViewById(R.id.pickContactButton);
        pickContactButton.setOnClickListener(this);
        
        contentAccessor = new ContentAccessor();
    }

	public void onClick(View v) {
		pickContact();
	}

	private void pickContact() {
		startActivityForResult(contentAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
	}

	@Override
	protected void onActivityResult(int reqCode, int resCode, Intent data){
		if(reqCode == PICK_CONTACT_REQUEST && resCode == RESULT_OK){
			launchSMSActivity(data.getData());
		}
	}

	private void launchSMSActivity(Uri data) {
		AsyncTask<Uri, Void, ContactInfo> task = new AsyncTask<Uri, Void, ContactInfo>(){

			@Override
			protected ContactInfo doInBackground(Uri... params) {
				return contentAccessor.loadContact(getContentResolver(),params[0]);
			}
			
			@Override
			protected void onPostExecute(ContactInfo result){
		        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		        sendIntent.putExtra("sms_body", result.getName() + ": Ph: "+ result.getPhoneNumber()); 
		        sendIntent.setType("vnd.android-dir/mms-sms");
		        startActivity(sendIntent);
			}
		};
		task.execute(data);
	}
}