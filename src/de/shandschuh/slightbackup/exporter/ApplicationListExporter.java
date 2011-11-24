/**
 * Slight backup - a simple backup tool
 * 
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
import java.util.List;
import java.util.Vector;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import de.shandschuh.slightbackup.BackupTask;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public class ApplicationListExporter extends Exporter {
	public static final int ID = 8;
	
	public static final int NAMEID = R.string.applicationlist;
	
	private String filename;
	
	public ApplicationListExporter(ExportTask exportTask) {
		super(exportTask);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	protected int export(String filename) throws Exception {
		this.filename = filename;
		
		List<ApplicationInfo> applications = exportTask.getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
		
		Vector<ApplicationInfo> resultVector = new Vector<ApplicationInfo>();
		
		for (ApplicationInfo application : applications) {
			if ((application.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				resultVector.add(application);
			}
		}
		
		int count = resultVector.size();
		
		if (count > 0) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			writeXmlStart(writer, Strings.TAG_APPLICATION, count);
			
			exportTask.progress(BackupTask.MESSAGE_COUNT, count);
			exportTask.progress(BackupTask.MESSAGE_PROGRESS, 0);
			for (int n = 0; n < count; n++) {
				writer.write('<');
				writer.write(Strings.TAG_APPLICATION);
				writer.write('>');
				writer.write(resultVector.get(n).packageName);
				writer.write(ENDTAG_START);
				writer.write(Strings.TAG_APPLICATION);
				writer.write(TAG_END);
				exportTask.progress(BackupTask.MESSAGE_PROGRESS, n+1);
			}
			writeXmlEnd(writer, Strings.TAG_APPLICATION);
			writer.close();
		}
		return count;
	}

	@Override
	public String getContentName() {
		return Strings.APPLICATIONLIST;
	}

	@Override
	public String[] getExportedFilenames() {
		return new String[] {filename};
	}

	@Override
	public int getId() {
		return ID;
	}

	@Override
	public int getTranslatedContentName() {
		return NAMEID;
	}

}
