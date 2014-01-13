/**
 * Slight backup - a simple backup tool
 *
 * Copyright (c) 2011-2014 Stefan Handschuh
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import de.shandschuh.slightbackup.BackupActivity;
import de.shandschuh.slightbackup.BackupTask;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public class ImportTask extends BackupTask<Void, Exception> {
	private File file;
	
	private Parser parser;
	
	private Button importButton;
	
	public ImportTask(final ProgressDialog progressDialog, final File file, int count) {
		super(progressDialog);
		this.file = file;
		
		progressDialog.setTitle(R.string.button_import);
		progressDialog.setMessage(file.toString());
		progressDialog.setMax(Math.max(0, count));
		progressDialog.setButton(Dialog.BUTTON_POSITIVE, progressDialog.getContext().getString(R.string.button_import), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// we have to define this since otherwise there would be no positive button
			}
		});
		progressDialog.show();
		
		parser = SimpleParser.createParserByFilename(file.toString(), progressDialog.getContext(), ImportTask.this);
		
		if (!parser.isPrepared()) {
			cancel(true);
		} else {
			importButton = progressDialog.getButton(Dialog.BUTTON_POSITIVE);
			importButton.setEnabled(true);
			importButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (getStatus() == AsyncTask.Status.PENDING) {
						Context context = progressDialog.getContext();
						
						if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Strings.PREFERENCE_HIDEDATAWARNINGS, false) && parser.maybeIncomplete()) {
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							
							builder.setTitle(android.R.string.dialog_alert_title);
							builder.setMessage(context.getString(R.string.warning_incompletedata_import, context.getString(parser.getTranslatedContentName())));
							builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
									execute();
								}
							});
							builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
							builder.setCancelable(true);
							builder.show();
						} else {
							execute();
						}
					}
				}
			}); // we cannot use progressDialog.setButton(Dialog.BUTTON_POSITIVE, ...) since this would cause the dialog to close
		}
	}
	
	@Override
	protected void onPreExecute() {
		importButton.setEnabled(false);
		super.onPreExecute();
	}

	@Override
	protected Exception doInBackground(Void... params) {
		try {
			Xml.parse(new InputStreamReader(new FileInputStream(file.toString())), parser);
			return null;
		} catch (Exception e) {
			return e;
		}
	}
	
	@Override
	protected void onCancelled() {
		if (parser != null) {
			parser.cancel();
		}
		progressDialog.cancel();
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(Exception result) {
		progressDialog.setProgress(0);
		progressDialog.dismiss();
		parser.cleanup();
		if (result == null) {
			if (parser.hasHints()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(progressDialog.getContext());
				
				builder.setTitle(R.string.message_importsuccessful);
				builder.setMessage(parser.getHints());
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			} else {
				int skipped = parser.getSkippedEntryCount();
				
				if (skipped > 0) {
					if (skipped == parser.getEntryCount()) {
						Toast.makeText(progressDialog.getContext(), R.string.message_skippedallentries, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(progressDialog.getContext(), progressDialog.getContext().getResources().getQuantityString(R.plurals.message_importsuccessful_skipped, skipped, skipped), Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(progressDialog.getContext(), R.string.message_importsuccessful, Toast.LENGTH_LONG).show();
				}
			}
		} else {
			Toast.makeText(progressDialog.getContext(),	String.format(progressDialog.getContext().getString(R.string.error_somethingwentwrong), result.getMessage()), Toast.LENGTH_LONG).show();
		}
		super.onPostExecute(result);
	}

}
