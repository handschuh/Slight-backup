/**
 * Slight backup - a simple backup tool
 *
 * Copyright (c) 2011, 2012 Stefan Handschuh
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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
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
	
}
