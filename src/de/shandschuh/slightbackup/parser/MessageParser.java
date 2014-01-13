/**
 * Slight backup - a simple backup tool
 *
 * Copyright (c) 2011-2014 Stefan Handschuh
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

package de.shandschuh.slightbackup.parser;

import java.util.Vector;

import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.Toast;
import de.shandschuh.slightbackup.BackupActivity;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public class MessageParser extends SimpleParser {
	public static final String NAME = Strings.MESSAGES;
	
	public static final int NAMEID = R.string.smsmessages;
	
	/**
	 * We have to trigger MmsSmsDatabaseHelper.updateAllThreads which itself
	 * is called from the MmsSmsDatabaseHelper.updateThread method, if the
	 * thread id < 0.
	 *
	 * MmsSmsDatabaseHelper.updateThread is called from
	 * MmsSmsProvider.delete by using ContentResolver.delete with this
	 * content uri.
	 *
	 * The classes MmsSmsDatabaseHelper and MmsSmsProvider are part of the
	 * android libraries but they are not an official api such that they can
	 * change or even vanish in some android versions.
	 */
	private static final Uri SMSCONVERSATIONSUPDATE_URI = Uri.parse("content://sms/conversations/-1");
	
	private StringBuilder messageStringBuilder;
	
	public MessageParser(Context context, ImportTask importTask) {
		super(context, Strings.TAG_MESSAGE, determineFields(context), BackupActivity.SMS_URI, importTask, Strings.SMS_FIELDS);
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (tagEntered) {
			messageStringBuilder.append(ch, start, length);
		}
	}
	
	@Override
	public void startMainElement() {
		messageStringBuilder = new StringBuilder();
	}
	
	@Override
	public void addExtraContentValues(ContentValues contentValues) {
		contentValues.put(Strings.BODY, messageStringBuilder.toString());
	}

	@Override
	public void endDocument() throws SAXException {
		context.getContentResolver().delete(SMSCONVERSATIONSUPDATE_URI, null, null);
	}
	
	@Override
	public boolean isPrepared() {
		if (BackupActivity.API_LEVEL > 18) {
			try {
				final String defaultSmsPackage = (String) Class.forName("android.provider.Telephony$Sms").getMethod("getDefaultSmsPackage", Context.class).invoke(null, context);
				
				if (defaultSmsPackage.equalsIgnoreCase(context.getPackageName())) {
					if ((Integer) Class.forName("android.provider.Settings$Global").getMethod("getInt", ContentResolver.class, String.class, int.class).invoke(null, context.getContentResolver(), "airplane_mode_on", 1) == 0) {
						BackupActivity.showWarningDialog(context, R.string.warning_enableairplanemode, null);
					}
					return true;
				} else {
					if ((Integer) Class.forName("android.provider.Settings$Global").getMethod("getInt", ContentResolver.class, String.class, int.class).invoke(null, context.getContentResolver(), "airplane_mode_on", 1) == 1) {
						// only offer the change if the airplane mode is active
						BackupActivity.showWarningDialog(context, R.string.warning_aftersmsimport, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								BackupActivity.INSTANCE.addProperty("defaultSmsPackage", defaultSmsPackage);
								BackupActivity.INSTANCE.startActivity(new Intent("android.provider.Telephony.ACTION_CHANGE_DEFAULT").putExtra("package", context.getPackageName()));
							}
						});
					} else {
						Toast.makeText(BackupActivity.INSTANCE, R.string.hint_smsairplanemode, Toast.LENGTH_LONG).show();
					}
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		} else {
			return true;
		}
	}

	private static String[] determineFields(Context context) {
		Cursor cursor = context.getContentResolver().query(SMSCONVERSATIONSUPDATE_URI, null, null, null, BaseColumns._ID+" DESC LIMIT 0");
		
		String[] availableFields = cursor.getColumnNames();
		
		cursor.close();
		
		Vector<String> fields = new Vector<String>();
		
		for (String field : Strings.SMS_FIELDS) {
			if (Strings.indexOf(availableFields, field) == -1) {
				throw new IllegalArgumentException();
			} else {
				fields.add(field);
			}
		}
		for (String field : Strings.SMS_FIELDS_OPTIONAL) {
			if (Strings.indexOf(availableFields, field) > -1) {
				fields.add(field);
			}
		}
		
		return fields.toArray(new String[0]);
	}

	@Override
	public void cleanup() {
		if (BackupActivity.API_LEVEL > 18) {
			try {
				String defaultSmsPackage = BackupActivity.INSTANCE.getProperty("defaultSmsPackage");
				
				String currentDefaultSmsPackage = (String) Class.forName("android.provider.Telephony$Sms").getMethod("getDefaultSmsPackage", Context.class).invoke(null, context);
				
				if (defaultSmsPackage!= null && !currentDefaultSmsPackage.equals(defaultSmsPackage)) {
					BackupActivity.INSTANCE.startActivityForResult(new Intent("android.provider.Telephony.ACTION_CHANGE_DEFAULT").putExtra("package", defaultSmsPackage), BackupActivity.REQUEST_CHANGEDEFAULTSMSAPPLICATIONTOORIGINAL);
				}
			} catch (Exception e) {
				
			}
		}
		super.cleanup();
	}
	
	
}
