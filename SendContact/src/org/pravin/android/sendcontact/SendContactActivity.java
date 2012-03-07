package org.pravin.android.sendcontact;

import java.util.List;
import java.util.Set;

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
        
//        Button pickContactButton = (Button) findViewById(R.id.pickContactButton);
//        pickContactButton.setOnClickListener(this);
        
        contentAccessor = new ContentAccessor();
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
        	if (extras.containsKey(Intent.EXTRA_STREAM)) {
        		// convert uri from as-vcard to lookup
        		// for example, from
        		// content://com.android.contacts/contacts/as_vcard/0r1-5349475D454947532D472D
        		// content://com.android.contacts/contacts/lookup/0r1-5349475D454947532D472D/1
        		// this is a workaround to get the launchSMSActivity 
        		// treat the Uri identically between when launch via Share
        		// and via pickContactButton.
        		Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
        		Uri.Builder ub = new Uri.Builder();
        		List<String> l = uri.getPathSegments();
        		ub.scheme(uri.getScheme());
        		ub.authority(uri.getAuthority());
        		ub.appendPath(l.get(0));
        		ub.appendPath("lookup");
        		ub.appendPath(l.get(2));
        		ub.appendPath("1");
        		launchSMSActivity(ub.build());
        	}
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