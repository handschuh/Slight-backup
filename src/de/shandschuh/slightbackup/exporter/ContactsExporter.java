/**
 * Slight backup - a simple backup tool
 *
 * Copyright (c) 2012, 2013 Stefan Handschuh
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package de.shandschuh.slightbackup.exporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public class ContactsExporter extends SimpleExporter {
	public static final int ID = 8;
	
	public static final int NAMEID = R.string.contacts;
	
	public static final String NAME = Strings.CONTACTS;
	
	public static String LOOKUP_FIELDNAME;
	
	public static Uri CONTACTS_URI;
	
	public static Uri VCARD_URI;
	
	static {
		try {
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("android.provider.ContactsContract$Contacts");

			LOOKUP_FIELDNAME = (String) clazz.getField("LOOKUP_KEY").get(null);
			CONTACTS_URI = (Uri) clazz.getDeclaredField("CONTENT_URI").get(null);
			VCARD_URI = (Uri) clazz.getDeclaredField("CONTENT_VCARD_URI").get(null);
		} catch (Exception e) {
			
		}
	}
	
	private int lookupKeyColumn = -1;
	
	public ContactsExporter(ExportTask exportTask) {
		super(Strings.TAG_CONTACT, new String[] {LOOKUP_FIELDNAME}, CONTACTS_URI, false, exportTask);
	}
	
	@Override
	public void addText(Cursor cursor, Writer writer) throws IOException {
		if (lookupKeyColumn == -1) {
			lookupKeyColumn = cursor.getColumnIndex(LOOKUP_FIELDNAME);
		}
		writer.append(TextUtils.htmlEncode(new String(getVcardBytes(context, cursor.getString(lookupKeyColumn)))));
	}
	
	@Override
	public boolean maybeIncomplete() {
		return true;
	}

	public static byte[] getVcardBytes(Context context, String lookupKey) throws IOException {
		Uri contactUri = Uri.withAppendedPath(VCARD_URI, lookupKey);
		
		AssetFileDescriptor assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(contactUri, "r");
		
		InputStream fileInputStream = assetFileDescriptor.createInputStream();
		
		byte[] buffer = new byte[(int) assetFileDescriptor.getDeclaredLength()];
		
		fileInputStream.read(buffer);
		fileInputStream.close();
		
		return buffer;
	}

}
