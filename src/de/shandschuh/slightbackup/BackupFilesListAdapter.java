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

package de.shandschuh.slightbackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BackupFilesListAdapter extends ArrayAdapter<File> {
	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
	
	private LayoutInflater layoutInflater;

	public BackupFilesListAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_2, getFileList(BackupActivity.DIR));
		layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView == null ? layoutInflater.inflate(android.R.layout.simple_list_item_2, null) : convertView;
		
		TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		
		TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		
		File file = getItem(position);
		
		String filename = file.toString();
		
		long date = file.lastModified();
		
		text2.setText(filename);
		
		int count = -1;
		
		try {
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
			
			char[] buffer = new char[100]; // the info should be within the first 100 characters
			
			reader.read(buffer);
			reader.close();
			
			String string = new String(buffer);
			
			int index = string.indexOf(Strings.COUNT);
			
			if (index > 0) {
				count = Integer.parseInt(string.substring(index+7, string.indexOf('"', index+8)));
				text2.setText(new StringBuilder(String.format(getContext().getString(R.string.listentry_items), count)).append(' ').append(filename));
			} 
			index = string.indexOf(Strings.DATE);
			if (index > 0) {
				date = Long.parseLong(string.substring(index+6, string.indexOf('"', index+7)));
			} else {
				date = Long.parseLong(filename.substring(filename.lastIndexOf('_')+1, filename.lastIndexOf(Strings.FILE_EXTENSION)));
			}
			
		} catch (Exception e) {

		}

		view.setTag(count);
		text1.setText(new StringBuilder(filename.substring(filename.lastIndexOf('/')+1, filename.indexOf('_'))).append('@').append(dateFormat.format(new Date(date))));
		
		return view;
	}
	
	/*
	 * We have to use a vector since plain arrays would cause the "BackupFilesListAdapter.add"-command to fail
	 */
	private static List<File> getFileList(File dir) {
		Vector<File> vector = new Vector<File>();
		
		File[] files = dir.listFiles(new BackupFileNameFilter());
		
		for (int n = 0, i = files != null ? files.length : 0; n < i; n++) {
			vector.add(files[n]);
		}
		return vector;
	}
	
	private static final class BackupFileNameFilter implements FilenameFilter {

		public boolean accept(File dir, String filename) {
			return filename.endsWith(Strings.FILE_EXTENSION) && filename.indexOf(Strings.FILE_SUFFIX) > 0;
		}
		
	}
	
}
