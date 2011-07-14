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

public class EverythingExporter extends Exporter {
	private BookmarkExporter bookmarkExporter;
	
	private CallLogExporter callLogExporter;
	
	private MessageExporter messageExporter;
	
	private UserDictionaryExporter userDictionaryExporter;

	public EverythingExporter(Context context, ExportTask exportTask) {
		super(exportTask);
		bookmarkExporter = new BookmarkExporter(context, exportTask);
		callLogExporter = new CallLogExporter(context, exportTask);
		messageExporter = new MessageExporter(context, exportTask);
		userDictionaryExporter = new UserDictionaryExporter(context, exportTask);
	}

	@Override
	public void cancel() {
		// do not care about who is running
		bookmarkExporter.cancel();
		callLogExporter.cancel();
		messageExporter.cancel();
		userDictionaryExporter.cancel();
	}

	@Override
	public int export(String filename) throws Exception {
		exportTask.progress(ExportTask.MESSAGE_TYPE, BackupActivity.MENU_EXPORTBOOKMARKS_ID);
		
		int result = export(bookmarkExporter);
		
		exportTask.progress(ExportTask.MESSAGE_TYPE, BackupActivity.MENU_EXPORTCALLLOG_ID);
		result += export(callLogExporter);
		exportTask.progress(ExportTask.MESSAGE_TYPE, BackupActivity.MENU_EXPORTSMS_ID);
		result += export(messageExporter);
		exportTask.progress(ExportTask.MESSAGE_TYPE, BackupActivity.MENU_EXPORTUSERDICTIONARY_ID);
		return  result + export(userDictionaryExporter);
	}
	
	private int export(SimpleExporter exporter) throws Exception {
		return exporter.export(new StringBuilder(BackupActivity.DIR_NAME).append(exporter.getContentName()).append(Strings.FILE_SUFFIX).append(System.currentTimeMillis()).append(Strings.FILE_EXTENSION).toString());
	}

	@Override
	public String getContentName() {
		return Strings.EMPTY; // since we do not use the filename at all
	}

	@Override
	public String[] getExportedFilenames() {
		return new String[] {
				bookmarkExporter.getFilename(),
				callLogExporter.getFilename(),
				messageExporter.getFilename(),
				userDictionaryExporter.getFilename()
		};
	}

}
