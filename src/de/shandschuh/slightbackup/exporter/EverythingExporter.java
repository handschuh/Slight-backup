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

import java.util.Vector;

import de.shandschuh.slightbackup.Strings;

public class EverythingExporter extends Exporter {	
	public static final int ID = 0;
	
	private int currentNameId;
	
	private Vector<Exporter> exporters;
	
	private Vector<Exporter> failedExporters;
	
	public EverythingExporter(ExportTask exportTask) {
		super(exportTask);
		exporters = Exporter.getAllExporters(exportTask);
		failedExporters = new Vector<Exporter>();
	}

	@Override
	public void cancel() {
		// do not care about who is running
		for (Exporter exporter : exporters) {
			exporter.cancel();
		}
	}

	@Override
	public int export(String filename) throws Exception {
		int result = 0;
		
		for (Exporter exporter : exporters) {
			currentNameId = exporter.getTranslatedContentName();
			exportTask.progress(ExportTask.MESSAGE_TYPE, exporter.getId()); 
			try {
				result += export(exporter);
			} catch (Throwable t) {
				failedExporters.add(exporter);
			}
		}
		
		return result;
	}
	
	private int export(Exporter exporter) throws Exception {
		return exporter.export();
	}

	@Override
	public String getContentName() {
		return Strings.EMPTY; // since we do not use the filename at all
	}

	@Override
	public String[] getExportedFilenames() {
		Vector<String> exportedFilenames = new Vector<String>();
		
		for (Exporter exporter : exporters) {
			if (!failedExporters.contains(exporter)) {
				String[] filenames = exporter.getExportedFilenames();
				
				for (String filename : filenames) {
					exportedFilenames.add(filename);
				}
			}
		}
		return exportedFilenames.toArray(new String[0]);
	}
	
	@Override
	public int getId() {
		return ID;
	}

	@Override
	public int getTranslatedContentName() {
		return currentNameId;
	}

}
