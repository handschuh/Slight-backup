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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import de.shandschuh.slightbackup.BackupTask;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public abstract class SimpleExporter extends Exporter {
	private static final String XML_START = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	
	private static final String REALAMP = "&";
	
	private static final String ENDTAG_START = "</";
	
	protected static final String EQUALS = "=\"";
	
	private static final String TAG_END = ">\n";
	
	private static final String S_DATE = "s date=\"";
	
	private static final String _COUNT = "\" count=\"";
	
	protected static final String TAG_END_QUOTE = "\">\n";
	
	private static final String TAGS_END = "s>\n";
	
	protected Context context;
	
	private String tag;
	
	private String[] fields;
	
	private Uri contentUri;
	
	private boolean checkFields;
	
	private String selection;
	
	boolean canceled;
	
	private String filename;
	
	public SimpleExporter(String tag, String[] fields, Uri contentUri, boolean checkFields, String selection, ExportTask exportTask) {
		super(exportTask);
		this.context = exportTask.getContext();
		this.tag = tag;
		this.fields = fields;
		this.contentUri = contentUri;
		this.selection = selection;
		this.checkFields = checkFields;
		canceled = false;
	}
	
	public SimpleExporter(String tag, String[] fields, Uri contentUri, boolean checkFields, ExportTask exportTask) {
		this(tag, fields, contentUri, checkFields, null, exportTask);
	}
	
	public SimpleExporter(String tag, Uri contentUri, String selection, ExportTask exportTask) {
		this(tag, null, contentUri, false, selection, exportTask);
	}
	
	public SimpleExporter(String tag, Uri contentUri, ExportTask exportTask) {
		this(tag, contentUri, null, exportTask);
	}
	
	public final int export(String filename) throws Exception {
		this.filename = filename;
		
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, null);
		
		if (checkFields && fields != null) {
			if (cursor == null || !checkFieldNames(cursor.getColumnNames(), fields)) {
				throw new Exception(context.getString(R.string.error_unsupporteddatabasestructure));
			}
		} else if (fields == null) {
			if (cursor == null) {
				this.filename = null;
				return 0;
			} else {
				fields = cursor.getColumnNames();
			}
		}
		
		int count = cursor.getCount();
		
		if (count == 0) {
			this.filename = null;
			return 0;
		}
		
		exportTask.progress(BackupTask.MESSAGE_COUNT, count);
		exportTask.progress(BackupTask.MESSAGE_PROGRESS, 0);

		int length = fields.length;
    	
    	int[] positions = new int[length];
    	
    	for (int n = 0; n < length; n++) {
    		positions[n] = cursor.getColumnIndex(fields[n]);
    	}
    	
    	int index1, index2;
    	
    	BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
    	
    	writer.write(XML_START);
    	
    	writer.write('<');
    	writer.write(tag);
    	writer.write(S_DATE);
    	writer.write(Long.toString(System.currentTimeMillis()));
    	writer.write(_COUNT);
    	writer.write(Integer.toString(count));
    	writer.write(TAG_END_QUOTE);
    	
    	int position = 0;
    	
    	while (!canceled && cursor.moveToNext()) {
    		writer.write('<');
    		writer.write(tag);
    		for (int n = 0; n < length; n++) {
    			try {
    				String string = cursor.getString(positions[n]);
        			
        			if (string != null && !BaseColumns._ID.equals(fields[n])) {
        				writer.write(' ');
        				writer.write(fields[n]);
        				writer.write(EQUALS);
            			
            			index1 = string.indexOf('<');
            			index2 = string.indexOf('>');
            			
            			if (index1 > -1 && index2 > index1) {
            				writer.write(string.substring(index1+1, index2));
            			} else {
            				writer.write(string.replace(REALAMP, Strings.AMP));
            			}
            			writer.write('"');
        			}
    			} catch (Exception e) {
    				// if there is blob data
    			}
    		}
    		writer.write('>');
    		addText(cursor, writer);
    		writer.write(ENDTAG_START);
    		writer.write(tag);
    		writer.write(TAG_END);
    		
    		exportTask.progress(BackupTask.MESSAGE_PROGRESS, ++position);
    	}
    	cursor.close();
    	
    	writer.write(ENDTAG_START);
		writer.write(tag);
		writer.write(TAGS_END);
		writer.close();
    	
    	if (!canceled) {
    		return count;
    	} else {
    		new File(filename).delete();
    		this.filename = null;
    		return -1;
    	}
	}
	
	protected boolean checkFieldNames(String[] availableFieldNames, String[] neededFieldNames) {
		for (int n = 0, i = neededFieldNames != null ? neededFieldNames.length : 0; n < i; n++) {
    		if (Strings.indexOf(availableFieldNames, neededFieldNames[n]) == -1) {
    			return false;
    		}
    	}
    	return true;
	}
	
	/*
	 * Override to use
	 */
	public void addText(Cursor cursor, Writer writer) throws IOException {
		
	}
	
	public void cancel() {
		canceled = true;
	}

	@Override
	public String[] getExportedFilenames() {
		return new String[] {filename};
	}
	
	public String getFilename() {
		return filename;
	}
	
	
}
