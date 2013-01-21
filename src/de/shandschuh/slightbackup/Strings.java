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

package de.shandschuh.slightbackup;

/**
 * This class is just used as a "storage" for strings that are available to the
 * whole program. By having them as "static final" objects, memory for these
 * strings is only allocated once.
 */
public final class Strings {
	public static final String BODY = "body";
	
	public static final String TAG_MESSAGE = "message";
	
	public static final String TAG_CALL = "call";
	
	public static final String FILE_EXTENSION = ".xml";
	
	public static final String FILE_SUFFIX = "_export_";
	
	public static final String CALLLOGS = "calllogs";
	
	public static final String MESSAGES = "messages";
	
	public static final String PLAYLISTS = "playlists";
	
	public static final String SETTINGS = "settings";
	
	public static final String COUNT = "count=\"";
	
	public static final String DATE = "date=\"";

	public static final String TAG_BOOKMARK = "bookmark";
	
	public static final String BOOKMARKS = "bookmarks";
	
	public static final String TAG_WORD = "word";
	
	public static final String TAG_SETTING = "setting";
	
	public static final String USERDICTIONARY = "userdictionary";
	
	public static final String EXPORTTYPE = "exporttype";
	
	public static final String PREFERENCE_LICENSEACCEPTED = "license.accepted";
	
	public static final String PREFERENCE_CYCLECOUNT = "cyclecount";
	
	public static final String PREFERENCE_STORAGELOCATION = "storage.location";
	
	public static final String PREFERENCE_HIDEDATAWARNINGS = "hideincompletedatawarnings";
	
	public static final String THREENEWLINES = "\n\n\n";
	
	public static final String EMPTY = "";
	
	public static final String COMMA = ", ";
	
	public static final String TAG_PLAYLIST = "playlist";
	
	public static final String TAG_FILE = "file";
	
	public static final String DB_ARG = "=?";
	
	public static final String AND = " and ";
	
	public static final String EXTERNAL = "external";
	
	public static final String ZERO = "0";
	
	public static final String NOT_FOUR = "!= 4";
	
	public static final String FOUR = "4";
	
	public static final String WIFISETTINGS = "wifisettings";
	
	public static final String FIELD_ID = "ID";
	
	public static final String FIELD_NAMEID = "NAMEID";
	
	public static final String FIELD_NAME = "NAME";
	
	public static final String CONTACTS = "contacts";
	
	public static final String TAG_CONTACT = "contact";
	
	public static final String[] SMS_FIELDS = new String[] {
		"date",
		"address",
		"type",
		"read",
		"status",
	};

	public static final String[] SMS_FIELDS_OPTIONAL = new String[] {
		"locked",
		"date_sent",
		"seen",
		"error_code",
		"service_center",
	};

	public static final String UTF8 = "utf-8";

	public static int indexOf(String[] array, String string) {
		for (int n = 0, i = array != null ? array.length : 0; n < i; n++) {
			if (array[n] != null && array[n].equals(string)) {
				return n;
			}
		}
		return -1;
	}
	
}
