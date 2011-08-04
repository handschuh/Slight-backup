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

package de.shandschuh.slightbackup.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import de.shandschuh.slightbackup.BackupTask;
import de.shandschuh.slightbackup.R;

public class ImportTask extends BackupTask<Void, Exception> {
	private File file;
	
	private Parser parser;
	
	private Button importButton;

	public ImportTask(ProgressDialog progressDialog, File file, int count) {
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
		importButton = progressDialog.getButton(Dialog.BUTTON_POSITIVE);
		importButton.setEnabled(true);
		importButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				execute();
			}
		}); // we cannot use progressDialog.setButton(Dialog.BUTTON_POSITIVE, ...) since this would cause the dialog to close
	}
	
	@Override
	protected void onPreExecute() {
		importButton.setEnabled(false);
		super.onPreExecute();
	}

	@Override
	protected Exception doInBackground(Void... params) {
		parser = SimpleParser.createParserByFilename(file.toString(), progressDialog.getContext(), this);
		
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
		if (result == null) {
			progressDialog.dismiss();
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
				Toast.makeText(progressDialog.getContext(),	R.string.message_importsuccessful, Toast.LENGTH_LONG).show();
			}
		} else {
			importButton.setEnabled(true);
			progressDialog.setProgress(0);
			Toast.makeText(progressDialog.getContext(),	String.format(progressDialog.getContext().getString(R.string.error_somethingwentwrong), result.getMessage()), Toast.LENGTH_LONG).show();
		}
		super.onPostExecute(result);
	}
	

}
