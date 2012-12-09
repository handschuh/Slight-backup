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

package de.shandschuh.slightbackup.exporter;

import java.io.IOException;
import java.io.Writer;

import android.database.Cursor;
import android.text.TextUtils;
import de.shandschuh.slightbackup.BackupActivity;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public class SMSExporter extends SimpleExporter {
	public static final int ID = 3;
	
	public static final int NAMEID = R.string.smsmessages;
	
	public static final String NAME = Strings.MESSAGES;
	
	private int bodyPosition;

	public SMSExporter(ExportTask exportTask) {
		super(Strings.TAG_MESSAGE, Strings.SMS_FIELDS, BackupActivity.SMS_URI, true, null, "date", exportTask, Strings.SMS_FIELDS_OPTIONAL);
		bodyPosition = -1;
	}
	
	@Override
	public void addText(Cursor cursor, Writer writer) throws IOException {
		if (bodyPosition == -1) {
			bodyPosition = cursor.getColumnIndex(Strings.BODY);
		}
		
		String body = cursor.getString(bodyPosition);
		
		if (body != null) {
			writer.write(TextUtils.htmlEncode(body));
		}
	}

	@Override
	public boolean checkFieldNames(String[] availableFieldNames, String[] neededFieldNames) {
		return super.checkFieldNames(availableFieldNames, neededFieldNames)
				&& Strings.indexOf(availableFieldNames, Strings.BODY) > -1;
	}

}
