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

package de.shandschuh.slightbackup.parser;

import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public abstract class Parser extends DefaultHandler {
	protected static final String COUNT = "count";
	
	protected Context context;
	
	protected ImportTask importTask;
	
	protected boolean canceled;
	
	private StringBuilder hintStringBuilder;
	
	public Parser(Context context, ImportTask importTask) {
		this.context = context;
		this.importTask = importTask;
	}

	public final void cancel() {
		canceled = true;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public static Parser createParserByFilename(String filename, Context context, ImportTask importTask) {
		filename = filename.substring(filename.lastIndexOf('/')+1);
		
		if (filename.startsWith(Strings.CALLLOGS)) {
			return new CallLogParser(context, importTask);
		} else if (filename.startsWith(Strings.MESSAGES)) {
			return new MessageParser(context, importTask);
		} else if (filename.startsWith(Strings.BOOKMARKS)) {
			return new BookmarkParser(context, importTask);
		} else if (filename.startsWith(Strings.USERDICTIONARY)) {
			return new UserDictionaryParser(context, importTask);
		} else if (filename.startsWith(Strings.PLAYLISTS)) {
			return new PlaylistParser(context, importTask);
		} else if (filename.startsWith(Strings.SETTINGS)) {
			return new SettingsParser(context, importTask);
		}
		return null;
	}

	public static int getTranslatedParserName(String filename) {
		filename = filename.substring(filename.lastIndexOf('/')+1);
		
		if (filename.startsWith(Strings.CALLLOGS)) {
			return R.string.calllogs;
		} else if (filename.startsWith(Strings.MESSAGES)) {
			return R.string.messages;
		} else if (filename.startsWith(Strings.BOOKMARKS)) {
			return R.string.bookmarks;
		} else if (filename.startsWith(Strings.USERDICTIONARY)) {
			return R.string.userdictionary;
		} else if (filename.startsWith(Strings.PLAYLISTS)) {
			return R.string.playlists;
		} else if (filename.startsWith(Strings.SETTINGS)) {
			return R.string.settings;
		}
		return android.R.string.unknownName;
	}
	
	public void addHint(CharSequence charSequence) {
		if (hintStringBuilder == null) {
			hintStringBuilder = new StringBuilder(charSequence);
		} else {
			hintStringBuilder.append('\n');
			hintStringBuilder.append(charSequence);
		}
	}
	
	public StringBuilder getHints() {
		return hintStringBuilder;
	}
	
	public boolean hasHints() {
		return hintStringBuilder != null && hintStringBuilder.length() > 0;
	}
	
}
