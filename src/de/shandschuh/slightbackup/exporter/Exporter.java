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

import java.util.Vector;

import android.content.Context;
import de.shandschuh.slightbackup.BackupActivity;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public abstract class Exporter {
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
	
	protected ExportTask exportTask;
	
	private byte[] password;
	
	public Exporter(ExportTask exportTask) {
		this.exportTask = exportTask;
	}
	
	protected abstract int export(String filename) throws Exception;
	
	public int export() throws Exception {
		if (!BackupActivity.DIR.exists() && !BackupActivity.DIR.mkdir()) {
			throw new Exception(exportTask.getContext().getString(R.string.error_couldnotcreatebackupfolder, BackupActivity.DIR.toString()));
		}
		return export(new StringBuilder(BackupActivity.DIR.toString()).append('/').append(getContentName()).append(Strings.FILE_SUFFIX).append(System.currentTimeMillis()).append(Strings.FILE_EXTENSION).toString());
	}
	
	public abstract String getContentName();
	
	public abstract int getTranslatedContentName();
	
	public abstract void cancel();
	
	public abstract String[] getExportedFilenames();
	
	public abstract int getId();
	
	public boolean isEncrypted() {
		return false;
	}
	
	public static Exporter getById(int id, ExportTask exportTask) {
		switch (id) {
			case BookmarkExporter.ID: 
				return new BookmarkExporter(exportTask);
			case CallLogExporter.ID: 
				return new CallLogExporter(exportTask);
			case MessageExporter.ID: 
				return new MessageExporter(exportTask);
			case UserDictionaryExporter.ID: 
				return new UserDictionaryExporter(exportTask);
			case PlaylistExporter.ID: 
				return new PlaylistExporter(exportTask);
			case SettingsExporter.ID: 
				return new SettingsExporter(exportTask);
			case WifiSettingsExporter.ID:
				return new WifiSettingsExporter(exportTask);
			case EverythingExporter.ID: 
				return new EverythingExporter(exportTask);
		}
		return null;
	}
	
	public static ExporterInfos getExporterInfos(Context context) {
		Vector<Integer> ids = new Vector<Integer>(10);
		
		Vector<String> names = new Vector<String>(10);
		
		ids.add(BookmarkExporter.ID);
		names.add(context.getString(BookmarkExporter.NAMEID));
		ids.add(CallLogExporter.ID);
		names.add(context.getString(CallLogExporter.NAMEID));
		ids.add(MessageExporter.ID);
		names.add(context.getString(MessageExporter.NAMEID));
		ids.add(UserDictionaryExporter.ID);
		names.add(context.getString(UserDictionaryExporter.NAMEID));
		ids.add(PlaylistExporter.ID);
		names.add(context.getString(PlaylistExporter.NAMEID));
		ids.add(SettingsExporter.ID);
		names.add(context.getString(SettingsExporter.NAMEID));
		if (BackupActivity.CANHAVEROOT) {
			ids.add(WifiSettingsExporter.ID);
			names.add(context.getString(WifiSettingsExporter.NAMEID));
		}
		
		int[] intIds = new int[ids.size()];
		
		for (int n = 0, i = ids.size(); n < i; n++) {
			intIds[n] = ids.get(n);
		}
		return new ExporterInfos(intIds, names.toArray(new String[0]));
	}
	
	public static Vector<Exporter> getAllExporters(ExportTask exportTask) {
		Vector<Exporter> result = new Vector<Exporter>(10);
		
		result.add(new BookmarkExporter(exportTask));
		result.add(new CallLogExporter(exportTask));
		result.add(new MessageExporter(exportTask));
		result.add(new UserDictionaryExporter(exportTask));
		result.add(new PlaylistExporter(exportTask));
		result.add(new SettingsExporter(exportTask));
		
		if (BackupActivity.CANHAVEROOT) {
			result.add(new WifiSettingsExporter(exportTask));
		}
		return result;
	}
	
}
