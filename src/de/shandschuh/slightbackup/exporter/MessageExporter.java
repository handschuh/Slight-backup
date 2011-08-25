/**
 * Copyright (c) 2011 Stefan Handschuh
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
import de.shandschuh.slightbackup.BackupActivity;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public class MessageExporter extends SimpleExporter {
	public static final int ID = 3;
	
	public static final int NAMEID = R.string.messages;
	
	private int bodyPosition;

	public MessageExporter(ExportTask exportTask) {
		super(Strings.TAG_MESSAGE, Strings.SMS_FIELDS, BackupActivity.SMS_URI, true, "1 order by date", exportTask);
		bodyPosition = -1;
	}
	
	@Override
	public void addText(Cursor cursor, Writer writer) throws IOException {
		if (bodyPosition == -1) {
			bodyPosition = cursor.getColumnIndex(Strings.BODY);
		}
		writer.write(cursor.getString(bodyPosition).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
	}

	@Override
	public boolean checkFieldNames(String[] availableFieldNames, String[] neededFieldNames) {
		return super.checkFieldNames(availableFieldNames, neededFieldNames) 
		       && Strings.indexOf(availableFieldNames, Strings.BODY) > -1;
	}

	@Override
	public String getContentName() {
		return Strings.MESSAGES;
	}
	
	@Override
	public int getId() {
		return ID;
	}

	@Override
	public int getTranslatedContentName() {
		return NAMEID;
	}

}
