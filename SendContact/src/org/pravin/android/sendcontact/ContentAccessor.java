package org.pravin.android.sendcontact;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

public class ContentAccessor {

	public Intent getPickContactIntent(){
		return new Intent(Intent.ACTION_PICK,Contacts.CONTENT_URI);
	}

	public ContactInfo loadContact(ContentResolver contentResolver, Uri uri) {
		// try loading the name
		// if not found, return blank contact
		// if found, try loading the phone
		// if not found, return blank contact
		// else return both
		ContactInfo cinfo = new ContactInfo();
		loadName(contentResolver, uri,cinfo);
		if(cinfo.getId() != -1){
			loadPhoneNumber(contentResolver,uri,cinfo);
		}
		return cinfo;
	}


	private void loadName(ContentResolver contentResolver, Uri uri,
			ContactInfo cinfo) {
		Cursor csr = contentResolver.query(uri, new String[]{Contacts._ID,Contacts.DISPLAY_NAME}, null, null, null);
		try{
			if(csr.moveToFirst()){
				cinfo.setId(csr.getLong(0));
				cinfo.setName(csr.getString(1));
			}
		}
		finally{
			csr.close();
		}
	}
	private void loadPhoneNumber(ContentResolver contentResolver, Uri uri,
			ContactInfo cinfo) {
		Cursor csr = contentResolver.query(Phone.CONTENT_URI, new String[]{Phone.NUMBER}, Phone.CONTACT_ID + "=" + cinfo.getId(), null, Phone.IS_SUPER_PRIMARY + " DESC");
		try{
			if(csr.moveToFirst()){
				cinfo.setPhoneNumber(csr.getString(0));
			}
		}
		finally{
			csr.close();
		}
	}

}
