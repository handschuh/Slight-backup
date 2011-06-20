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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import de.shandschuh.slightbackup.parser.SimpleParser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class BackupFilesListAdapter extends BaseExpandableListAdapter {
	private static final String BRACKED = " (";
	
	private static final String DASH = " - ";
	
	private DateFormat dateFormat = DateFormat.getDateInstance();
	
	private DateFormat timeFormat = DateFormat.getTimeInstance();
	
	private LayoutInflater layoutInflater;
	
	private Vector<Date> dates;
	
	private Map<Date, Vector<File>> data;
	
	private Context context;

	public BackupFilesListAdapter(Context context) {
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dates = new Vector<Date>();
		data = new HashMap<Date, Vector<File>>();
		
		File[] files = BackupActivity.DIR.listFiles(new BackupFileNameFilter());
		
		
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File object1, File object2) { // not the fastest choice
				return getFileDate(object1) > getFileDate(object2) ? 1 : -1;
			}
		});
		
		for (int n = 0, i = files != null ? files.length : 0; n < i; n++) {
			add(files[n], false);
		}
	}
	
	private static final class BackupFileNameFilter implements FilenameFilter {
		public boolean accept(File dir, String filename) {
			return filename.endsWith(Strings.FILE_EXTENSION) && filename.indexOf(Strings.FILE_SUFFIX) > 0;
		}
	}

	public File getChild(int groupPosition, int childPosition) {
		return data.get(dates.get(groupPosition)).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		View view = convertView == null ? layoutInflater.inflate(android.R.layout.simple_expandable_list_item_2, null) : convertView;
		
		File file = getChild(groupPosition, childPosition);
		
		long date = file.lastModified();
		
		String filename = file.toString();
		
		TextView text2 = (TextView) view.findViewById(android.R.id.text2);

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
				text2.setText(new StringBuilder(String.format(context.getString(R.string.listentry_items), count)).append(' ').append(filename));
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
		((TextView) view.findViewById(android.R.id.text1)).setText(new StringBuilder(timeFormat.format(new Date(date))).append(DASH).append(context.getString(SimpleParser.getTranslatedParserName(filename))));
		return view;
	}

	public int getChildrenCount(int groupPosition) {
		return data.get(dates.get(groupPosition)).size();
	}

	public Date getGroup(int groupPosition) {
		return dates.get(groupPosition);
	}

	public int getGroupCount() {
		return data.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View view = convertView == null ? layoutInflater.inflate(android.R.layout.simple_expandable_list_item_1, null) : convertView;

		if (isExpanded) {
			((TextView) view.findViewById(android.R.id.text1)).setText(dateFormat.format(getGroup(groupPosition)));
		} else {
			((TextView) view.findViewById(android.R.id.text1)).setText(new StringBuilder(dateFormat.format(getGroup(groupPosition))).append(BRACKED).append(getChildrenCount(groupPosition)).append(')'));
		}
		return view;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public void add(File file, boolean notify) {
		long longDate = getFileDate(file);
		
		longDate += TimeZone.getDefault().getOffset(longDate);
		
		Date date = new Date(longDate - (longDate % 86400000l)); // 86400000 == one day in milliseconds
		
		if (!dates.contains(date)) {
			dates.add(date);
			
			Vector<File> vector = new Vector<File>();
			
			vector.add(file);
			data.put(date, vector);
		} else {
			data.get(date).add(file);
		}
		if (notify) {
			notifyDataSetChanged();
		}
	}

	public void add(File file) {
		add(file, true);
	}
	
	public void remove(File file) {
		long longDate = getFileDate(file);
		
		longDate += TimeZone.getDefault().getOffset(longDate);
		
		Date date = new Date(longDate - (longDate % 86400000l));
		
		Vector<File> vector = data.get(date);
			
		if (vector != null && vector.remove(file)) {
			if (vector.size() == 0) {
				data.remove(date);
				dates.remove(date);
			}
			notifyDataSetChanged();
		}
	}
	
	private static long getFileDate(File file) {
		try {
			String filename = file.toString();
			
			return Long.parseLong(filename.substring(filename.lastIndexOf('_')+1, filename.indexOf(Strings.FILE_EXTENSION)));
		} catch (Exception e) {
			return 0;
		}
	}
	
}
