package org.pravin.android.sendcontact;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
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

	public ContactInfo loadContactFromVCard(ContentResolver contentResolver, Uri uri) {
		ContactInfo cinfo = new ContactInfo();
		try {
			AssetFileDescriptor afd = contentResolver.openAssetFileDescriptor(uri, "r");
			FileInputStream fi = afd.createInputStream();
			byte[] buffer = new byte[(int) afd.getLength()];
			fi.read(buffer);
			
			String s = new String(buffer);
			// http://en.wikipedia.org/wiki/VCard  
			// http://tools.ietf.org/html/rfc2425 
			// http://tools.ietf.org/html/rfc2426
			// very basic vcard parser
			// ~ each line is terminated by "\r\n".
			// somtimes long lines may have "\r\n " or "\r\n\t"
			// to support wrap around. a sound solution
			// needs to eventually take care of this for a full-blown
			// vcard processor.
			String lines[] = s.split("\r\n", -1);
			
			// Since RFC says that FN (Full Name) and TEL MUST
			// exist, assume the Android devices implement it!
			// no need to parse through the entire vcard.
			// loop till the first FN and TEL are found.
			for (int i = 0; 
					(i < lines.length) && 
					((cinfo.getName() == null) || 
					(cinfo.getPhoneNumber() == null)); i++) {
				// records consist of key value pairs separated by :
				// additionally, the keys can have params separated
				// by ;
				// even if a pattern is not found, split will 
				// return the entire string, so no need to check for null?
				String tokens[] = lines[i].split(":", -1);
				String params[] = tokens[0].split(";", -1);
				if (params[0].equals("FN")) {
					cinfo.setName(tokens[1]);
				}
				else if (params[0].equals("TEL")) {
					cinfo.setPhoneNumber(tokens[1]);
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
