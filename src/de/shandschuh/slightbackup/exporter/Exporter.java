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
import java.util.Vector;

import android.content.Context;
import de.shandschuh.slightbackup.BackupActivity;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public abstract class Exporter {
	private static final String XML_START = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	
	private static final String S_DATE = "s date=\"";
	
	private static final String _COUNT = "\" count=\"";
	
	protected static final String TAG_END_QUOTE = "\">\n";
	
	private static final String TAGS_END = "s>\n";
	
	protected static final String ENDTAG_START = "</";
	
	protected static final String TAG_END = ">\n";
	
	public static class ExporterInfos {
		public int[] ids;
		
		public String[] names;
		
		public ExporterInfos(int[] ids, String[] names) {
			this.ids = ids;
			this.names = names;
		}
		
		public int length() {
			return ids != null ? ids.length : 0;
		}
		
	}
	
	public static Vector<Class<?>> EXPORTERS;
	
	/**
	 * This is the list of all exporters that are available to this backup
	 * program.
	 * Each new exporter has to be added here in order to be recognized by
	 * this program.
	 * All menu items and so on are created dynamically.
	 */
	static {
		EXPORTERS = new Vector<Class<?>>();

		EXPORTERS.add(BookmarkExporter.class);
		EXPORTERS.add(CallLogExporter.class);
		EXPORTERS.add(SMSExporter.class);
		EXPORTERS.add(UserDictionaryExporter.class);
		EXPORTERS.add(PlaylistExporter.class);
		EXPORTERS.add(SettingsExporter.class);
		if (BackupActivity.CANHAVEROOT) {
			EXPORTERS.add(WifiSettingsExporter.class);
		}
		if (BackupActivity.API_LEVEL > 4) {
			EXPORTERS.add(ContactsExporter.class);
		}
		// don't add the "everything"-exporter here
	}
	
	protected ExportTask exportTask;
	
	protected boolean canceled;
	
	public Exporter(ExportTask exportTask) {
		this.exportTask = exportTask;
		canceled = false;
	}
	
	protected abstract int export(String filename) throws Exception;
	
	public int export() throws Exception {
		if (!BackupActivity.DIR.exists() && !BackupActivity.DIR.mkdir()) {
			throw new Exception(exportTask.getContext().getString(R.string.error_couldnotcreatebackupfolder, BackupActivity.DIR.toString()));
		}
		return export(new StringBuilder(BackupActivity.DIR.toString()).append('/').append(getContentName()).append(Strings.FILE_SUFFIX).append(System.currentTimeMillis()).append(Strings.FILE_EXTENSION).toString());
	}
	
	public int getTranslatedContentName() {
		try {
			return getClass().getDeclaredField(Strings.FIELD_NAMEID).getInt(null);
		} catch (Exception e) {
			return android.R.string.unknownName;
		}
	}
	
	public String getContentName() {
		try {
			return (String) getClass().getDeclaredField(Strings.FIELD_NAME).get(null);
		} catch (Exception e) {
			return Strings.EMPTY;
		}
	}
	
	public int getId() {
		try {
			return getClass().getDeclaredField(Strings.FIELD_ID).getInt(null);
		} catch (Exception e) {
			return -1;
		}
	}
	
	public void cancel() {
		canceled = true;
	}
	
	public abstract String[] getExportedFilenames();
	
	public boolean isEncrypted() {
		return false;
	}
	
	/**
	 * By overriding this method, the program will show a warning before the
	 * export that the resulting exported data may be incomplete.
	 */
	public boolean maybeIncomplete() {
		return false;
	}
	
	public String getIncompleteDataNames(Context context) {
		return context.getString(getTranslatedContentName());
	}
	
	public static Exporter getById(int id, ExportTask exportTask) {
		if (id == EverythingExporter.ID) {
			return new EverythingExporter(exportTask);
		} else {
			for (Class<?> clazz : EXPORTERS) {
				try {
					if (clazz.getDeclaredField(Strings.FIELD_ID).getInt(null) == id) {
						return (Exporter) clazz.getConstructor(ExportTask.class).newInstance(exportTask);
					}
				} catch (Exception e) {
					
				}
			}
		}
		
		return null;
	}
	
	public static ExporterInfos getExporterInfos(Context context) {
		int length = EXPORTERS.size();
		
		int ids[] = new int[length];
		
		String[] names = new String[length];
		
		try {
			for (int n = 0; n < length; n++) {
				Class<?> clazz = EXPORTERS.get(n);
				
				ids[n] = clazz.getDeclaredField(Strings.FIELD_ID).getInt(null);
				names[n] = context.getString(clazz.getDeclaredField(Strings.FIELD_NAMEID).getInt(null));
			}
		} catch (Exception e) {
			
		}
		
		return new ExporterInfos(ids, names);
	}
	
	public static void writeXmlStart(Writer writer, String tag, int count) throws IOException {
		writer.write(XML_START);
		
		writer.write('<');
		writer.write(tag);
		writer.write(S_DATE);
		writer.write(Long.toString(System.currentTimeMillis()));
		writer.write(_COUNT);
		writer.write(Integer.toString(count));
		writer.write(TAG_END_QUOTE);
	}
	
	public static void writeXmlEnd(Writer writer, String tag) throws IOException {
		writer.write(ENDTAG_START);
		writer.write(tag);
		writer.write(TAGS_END);
	}

}
