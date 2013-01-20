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

import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import de.shandschuh.slightbackup.BackupActivity;
import de.shandschuh.slightbackup.Strings;

public abstract class Parser extends DefaultHandler {
	protected static final String COUNT = "count";
	
	private static Vector<Class<?>> PARSERS;
	
	/**
	 * This is the list of all importers that are available in this backup
	 * program.
	 * Each importer has to be added here to be registered by the this
	 * program.
	 * There is a file recognition that tries to find the importer that
	 * should be used to import a certain file.
	 */
	static {
		PARSERS = new Vector<Class<?>>();
		
		PARSERS.add(BookmarkParser.class);
		PARSERS.add(CallLogParser.class);
		PARSERS.add(MessageParser.class);
		PARSERS.add(PlaylistParser.class);
		PARSERS.add(SettingsParser.class);
		PARSERS.add(UserDictionaryParser.class);
		if (BackupActivity.API_LEVEL > 4) {
			PARSERS.add(ContactsParser.class);
		}
	}
	
	protected Context context;
	
	protected ImportTask importTask;
	
	protected boolean canceled;
	
	private StringBuilder hintStringBuilder;
	
	private int skipped;
	
	private int entryCount;
	
	public Parser(Context context, ImportTask importTask) {
		this.context = context;
		this.importTask = importTask;
		skipped = 0;
		entryCount = 0;
	}

	public final void cancel() {
		canceled = true;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public static Parser createParserByFilename(String filename, Context context, ImportTask importTask) {
		filename = filename.substring(filename.lastIndexOf('/')+1);
		
		for (Class<?> clazz : PARSERS) {
			try {
				if (filename.startsWith((String) clazz.getDeclaredField(Strings.FIELD_NAME).get(null))) {
					return (Parser) clazz.getConstructor(Context.class, ImportTask.class).newInstance(context, importTask);
				}
			} catch (Exception e) {
				
			}
		}
		return null;
	}

	public static int getTranslatedParserName(String filename) {
		filename = filename.substring(filename.lastIndexOf('/')+1);
		
		for (Class<?> clazz : PARSERS) {
			try {
				if (filename.startsWith((String) clazz.getDeclaredField(Strings.FIELD_NAME).get(null))) {
					return clazz.getDeclaredField(Strings.FIELD_NAMEID).getInt(null);
				}
			} catch (Exception e) {
				
			}
		}
		return android.R.string.unknownName;
	}
	
	public int getTranslatedContentName() {
		try {
			return getClass().getDeclaredField(Strings.FIELD_NAMEID).getInt(null);
		} catch (Exception e) {
			return android.R.string.unknownName;
		}
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
	
	public void addSkippedEntry() {
		skipped++;
	}
	
	public int getSkippedEntryCount() {
		return skipped;
	}
	
	protected void setEntryCount(int entryCount) {
		this.entryCount = entryCount;
	}
	
	public int getEntryCount() {
		return entryCount;
	}

	/**
	 * By overriding this method and returning "true", the program will give
	 * a warning before the import that the data that should be imported,
	 * may be incomplete.
	 */
	public boolean maybeIncomplete() {
		return false;
	}

}
