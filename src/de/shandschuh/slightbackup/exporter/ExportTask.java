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

import java.io.File;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;
import de.shandschuh.slightbackup.BackupFilesListAdapter;
import de.shandschuh.slightbackup.BackupTask;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public class ExportTask extends BackupTask<Void, Integer> {
	private Exporter exporter;
	
	private Exception exception;
	
	private BackupFilesListAdapter listAdapter;
	
	private int id;
	
	public ExportTask(ProgressDialog progressDialog, BackupFilesListAdapter listAdapter, int id) {
		super(progressDialog);
		this.listAdapter = listAdapter;
		
		progressDialog.setButton(Dialog.BUTTON_POSITIVE, null, (OnClickListener) null); // disables the positive button
		progressDialog.setTitle(R.string.dialog_export);
		exporter = Exporter.getById(id, this);
		this.id = id;
	}
	
	public Context getContext() {
		return progressDialog.getContext();
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		publishProgress(MESSAGE_TYPE, id);
		try {
			return exporter.export(); // checks itself for cancellation
		} catch (Exception e) {
			exception = e;
			return -1;
		}
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		progressDialog.setProgress(0);
		progressDialog.dismiss();
		if (result > 0) {
			String[] exportedFilenames = exporter.getExportedFilenames();
			
			Toast.makeText(progressDialog.getContext(), String.format(progressDialog.getContext().getString(R.string.message_exportedto), concat(exportedFilenames)), Toast.LENGTH_LONG).show();
			
			for (int n = 0, i = exportedFilenames.length; n < i; n++) {
				if (exportedFilenames[n] != null) {
					listAdapter.add(new File(exportedFilenames[n]));
				}
			}
			
		} else if (result == 0) {
			Toast.makeText(progressDialog.getContext(), R.string.hint_noexportdata, Toast.LENGTH_LONG).show();
		} else if (result == -1 && exception != null) {
			Toast.makeText(progressDialog.getContext(), String.format(progressDialog.getContext().getString(R.string.error_somethingwentwrong), exception.getMessage()), Toast.LENGTH_LONG).show();
		}
		super.onPostExecute(result);
	}
	
	@Override
	protected void onCancelled() {
		if (exporter != null) {
			exporter.cancel();
		}
		progressDialog.cancel();
		progressDialog.setProgress(0);
		super.onCancelled();
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog.show();
		super.onPreExecute();
	}
	
	public Exporter getExporter() {
		return exporter;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values[0] == MESSAGE_TYPE) {
			switch (values[1]) {
				case EverythingExporter.ID:{
					// the case for "everything" is not needed since this is just a set of all available exports
					break;
				}
				default: {
					progressDialog.setMessage(String.format(progressDialog.getContext().getString(R.string.hint_exporting), progressDialog.getContext().getString(exporter.getTranslatedContentName())));
					break;
				}
			}
			
		} else {
			super.onProgressUpdate(values);
		}
		
	}
	
	private static StringBuilder concat(String[] strings) {
		StringBuilder builder = new StringBuilder();
		
		boolean first = true;
		
		for (int n = 0, i = strings != null ? strings.length : 0; n < i; n++) {
			if (strings[n] != null) {
				if (first == true) {
					first = false;
				} else {
					builder.append(Strings.COMMA);
				}
				builder.append(strings[n]);
			}
			
		}
		return builder;
	}

}
