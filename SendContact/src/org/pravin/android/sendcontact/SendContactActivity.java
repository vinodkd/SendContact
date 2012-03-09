package org.pravin.android.sendcontact;

import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SendContactActivity extends Activity implements OnClickListener {
	private static final String TAG= "ListContactsActivity";
	private static final int PICK_CONTACT_REQUEST = 1;
	private static final int CONTACT_SENT_REQUEST = 2;
	private ContentAccessor contentAccessor;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
//        Button pickContactButton = (Button) findViewById(R.id.pickContactButton);
//        pickContactButton.setOnClickListener(this);
        
        contentAccessor = new ContentAccessor();
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
        	if (extras.containsKey(Intent.EXTRA_STREAM)) {
        		// The URI returned from the Intent is 
        		// a vcard.
        		// for example
        		// content://com.android.contacts/contacts/as_vcard/0r1-5349475D454947532D472D
        		// or
        		// file:///sdcard/.blur/vcard/211.vcf
        		Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
        		launchSMSActivity(uri);
        	}
        }
        else {
        	Context context = getApplicationContext();
        	CharSequence text = 
        		"To launch this app, go to the Contact list, select \"Menu\" then \"Share\".";
        	int duration = Toast.LENGTH_LONG;

        	Toast toast = Toast.makeText(context, text, duration);
        	toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 
        			0, 0);
        	toast.show();
        	finish();
        }
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
		else if (reqCode == CONTACT_SENT_REQUEST){
			// once the SMS intent is complete (user either sends the SMS
			// or discards it, this callback will be invoked.
			// no need to look at resCode as we don't care about if the user 
			// sent or canceled it.
			finish();
		}
	}

	private void launchSMSActivity(Uri data) {
		AsyncTask<Uri, Void, ContactInfo> task = new AsyncTask<Uri, Void, ContactInfo>(){

			@Override
			protected ContactInfo doInBackground(Uri... params) {
				return contentAccessor.loadContactFromVCard(getContentResolver(),params[0]);
			}
			
			@Override
			protected void onPostExecute(ContactInfo result){
		        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		        sendIntent.putExtra("sms_body", result.getName() + ": Ph: "+ result.getPhoneNumber()); 
		        sendIntent.setType("vnd.android-dir/mms-sms");
		        startActivityForResult(sendIntent, CONTACT_SENT_REQUEST);
			}
		};
		task.execute(data);
	}
}