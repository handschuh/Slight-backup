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

import android.content.Context;
import de.shandschuh.slightbackup.BackupActivity;
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
	
	public Exporter(ExportTask exportTask) {
		this.exportTask = exportTask;
	}
	
	public abstract int export(String filename) throws Exception;
	
	public int export() throws Exception {
		return export(new StringBuilder(BackupActivity.DIR_NAME).append(getContentName()).append(Strings.FILE_SUFFIX).append(System.currentTimeMillis()).append(Strings.FILE_EXTENSION).toString());
	}
	
	public abstract String getContentName();
	
	public abstract int getTranslatedContentName();
	
	public abstract void cancel();
	
	public abstract String[] getExportedFilenames();
	
	public abstract int getId();
	
	public static Exporter getById(int id, Context context, ExportTask exportTask) {
		switch (id) {
			case BookmarkExporter.ID: 
				return new BookmarkExporter(context, exportTask);
			case CallLogExporter.ID: 
				return new CallLogExporter(context, exportTask);
			case MessageExporter.ID: 
				return new MessageExporter(context, exportTask);
			case UserDictionaryExporter.ID: 
				return new UserDictionaryExporter(context, exportTask);
			case PlaylistExporter.ID: 
				return new PlaylistExporter(context, exportTask);
			case SettingsExporter.ID: 
				return new SettingsExporter(context, exportTask);
			case EverythingExporter.ID: 
				return new EverythingExporter(context, exportTask);
		}
		return null;
	}
	
	public static ExporterInfos getExporterInfos(Context context) {
		return new ExporterInfos(new int[] {
				BookmarkExporter.ID,
				CallLogExporter.ID,
				MessageExporter.ID,
				UserDictionaryExporter.ID,
				PlaylistExporter.ID,
				SettingsExporter.ID
		}, new String[] {
				context.getString(BookmarkExporter.NAMEID),
				context.getString(CallLogExporter.NAMEID),
				context.getString(MessageExporter.NAMEID),
				context.getString(UserDictionaryExporter.NAMEID),
				context.getString(PlaylistExporter.NAMEID),
				context.getString(SettingsExporter.NAMEID)
		});
	}
	
	public static Exporter[] getAllExporters(Context context, ExportTask exportTask) {
		return new Exporter[] {
				new BookmarkExporter(context, exportTask),
				new CallLogExporter(context, exportTask),
				new MessageExporter(context, exportTask),
				new UserDictionaryExporter(context, exportTask),
				new PlaylistExporter(context, exportTask),
				new SettingsExporter(context, exportTask),
		};
	}
}
