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

import java.util.Vector;

import de.shandschuh.slightbackup.Strings;

import android.content.Context;

/**
 * This exporter simply bundles all the existing exporters by creating a dummy
 * exporter that performs a wrapping of the existing ones.
 *
 * The list of the exporters is Exporter.EXPORTERS.
 */
public class EverythingExporter extends Exporter {
	public static final int ID = 0;
	
	private int currentNameId;
	
	private Vector<Exporter> exporters;
	
	private Vector<Exporter> failedExporters;
	
	public EverythingExporter(ExportTask exportTask) {
		super(exportTask);
		exporters = new Vector<Exporter>();
		for (Class<?> clazz : Exporter.EXPORTERS) {
			if (clazz != null && !clazz.equals(EverythingExporter.class)) {
				try {
					exporters.add((Exporter) clazz.getConstructor(ExportTask.class).newInstance(exportTask));
				} catch (Exception e) {
					
				}
			}
		}
		failedExporters = new Vector<Exporter>();
	}

	@Override
	public void cancel() {
		// do not care about which exporter is running
		super.cancel();
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
	public int getTranslatedContentName() {
		return currentNameId;
	}

	@Override
	public boolean maybeIncomplete() {
		for (Exporter exporter : exporters) {
			if (exporter.maybeIncomplete()) {
				return true;
			}
		}
		return super.maybeIncomplete();
	}

	@Override
	public String getIncompleteDataNames(Context context) {
		StringBuilder stringBuilder = new StringBuilder();
		
		for (Exporter exporter : exporters) {
			if (exporter.maybeIncomplete()) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(Strings.COMMA);
				}
				stringBuilder.append(context.getString(exporter.getTranslatedContentName()));
			}
		}
		return stringBuilder.toString();
	}

}
