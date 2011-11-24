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

import de.shandschuh.slightbackup.R;
import android.content.Context;

public class WifiSettingsExporter extends Exporter {
	public static final int ID = 7;
	
	public static final int NAMEID = R.string.wifisettings;
	
	private Context context;
	
	private String filename;
	
	public WifiSettingsExporter(ExportTask exportTask) {
		super(exportTask);
		this.context = exportTask.getContext();
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public int export(String filename) throws Exception {
		// TODO Auto-generated method stub
		this.filename = filename;
		
		return 0;
	}

	@Override
	public String getContentName() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEncrypted() {
		return true;
	}

}
