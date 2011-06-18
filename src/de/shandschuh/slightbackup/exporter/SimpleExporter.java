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
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.ProgressBar;
import de.shandschuh.slightbackup.Strings;

public abstract class SimpleExporter {
	protected Context context;
	
	private String tag;
	
	private String[] fields;
	
	private Uri contentUri;
	
	private boolean checkFields;
	
	private String selection;
	
	ProgressBar progressBar;
	
	private boolean canceled;
	
	private Runnable runnable;
	
	public SimpleExporter(Context context, String tag, String[] fields, Uri contentUri, boolean checkFields, String selection, ProgressBar progressBar) {
		this.context = context;
		this.tag = tag;
		this.fields = fields;
		this.contentUri = contentUri;
		this.selection = selection;
		this.progressBar = progressBar;
		canceled = false;
		runnable = new Runnable() {
			public void run() {
				SimpleExporter.this.progressBar.setProgress(SimpleExporter.this.progressBar.getProgress()+1);
			}
		};
	}
	
	public SimpleExporter(Context context, String tag, String[] fields, Uri contentUri, boolean checkFields, ProgressBar progressBar) {
		this(context, tag, fields, contentUri, checkFields, null, progressBar);
	}
	
	public SimpleExporter(Context context, String tag, Uri contentUri, String selection, ProgressBar progressBar) {
		this(context, tag, null, contentUri, false, selection, progressBar);
	}
	
	public SimpleExporter(Context context, String tag, Uri contentUri, ProgressBar progressBar) {
		this(context, tag, contentUri, null, progressBar);
	}
	
	public final int export(String filename) throws IOException {
		Cursor cursor = context.getContentResolver().query(contentUri, null, selection, null, null);
		
		if (checkFields && fields != null) {
			checkFieldNames(cursor.getColumnNames(), fields);
		} else if (fields == null) {
			fields = cursor.getColumnNames();
		}
		
		int count = cursor.getCount();
		
		if (count == 0) {
			return 0;
		}
		
		progressBar.setMax(count);
		int length = fields.length;
    	
    	int[] positions = new int[length];
    	
    	for (int n = 0; n < length; n++) {
    		positions[n] = cursor.getColumnIndex(fields[n]);
    	}
    	
    	int index1, index2;
    	
    	StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    	
    	builder.append('<');
    	builder.append(tag);
    	builder.append("s date=\"");
    	builder.append(System.currentTimeMillis());
    	builder.append("\" count=\"");
    	builder.append(count);
    	builder.append("\">\n");
    	
    	while (!canceled && cursor.moveToNext()) {
    		builder.append('<');
    		builder.append(tag);
    		for (int n = 0; n < length; n++) {
    			try {
    				String string = cursor.getString(positions[n]);
        			
        			if (string != null && !BaseColumns._ID.equals(fields[n])) {
        				builder.append(' ');
            			builder.append(fields[n]);
            			builder.append("=\"");
            			
            			index1 = string.indexOf('<');
            			index2 = string.indexOf('>');
            			
            			if (index1 > -1 && index2 > index1) {
            				builder.append(string.substring(index1+1, index2));
            			} else {
            				builder.append(string.replace("&", "&amp;"));
            			}
            			builder.append('"');
        			}
    			} catch (Exception e) {
    				// if there is blob data
    			}
    		}
    		builder.append('>');
    		addText(cursor, builder);
    		builder.append("</");
    		builder.append(tag);
    		builder.append(">\n");
    		progressBar.post(runnable);
    	}
    	cursor.close();
    	
    	if (!canceled) {
    		builder.append("</");
        	builder.append(tag);
        	builder.append("s>\n");
        	
        	BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

    		writer.write(builder.toString());
    		writer.close();
    		return count;
    	} else {
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
	public void addText(Cursor cursor, StringBuilder builder) {
		
	}
	
	public abstract String getContentName();
	
	public void cancel() {
		canceled = true;
	}
	
}
