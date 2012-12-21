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

package de.shandschuh.slightbackup;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.WindowManager;

public abstract class BackupTask<A, B> extends AsyncTask<A, Integer, B> {
	public static final int MESSAGE_TYPE = 0;
	
	public static final int MESSAGE_COUNT = 1;
	
	public static final int MESSAGE_PROGRESS = 2;
	
	protected ProgressDialog progressDialog;
	
	public BackupTask(final ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
		progressDialog.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					progressDialog.setProgress(0);
					cancel(true);
				}
				return true;
			}
		});
		progressDialog.setButton(Dialog.BUTTON_NEGATIVE, progressDialog.getContext().getString(android.R.string.cancel), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				progressDialog.setProgress(0);
				cancel(true);
			}
		});
	}
	
	public void progress(Integer... params) {
		publishProgress(params);
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values[0] == MESSAGE_COUNT) {
			progressDialog.setMax(values[1]);
		} else if (values[0] == MESSAGE_PROGRESS) {
			progressDialog.setProgress(values[1]);
		}
		super.onProgressUpdate(values);
	}
	
	@Override
	protected void onCancelled() {
		progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCancelled();
	}
	
	@Override
	protected void onPostExecute(B result) {
		progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onPostExecute(result);
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onPreExecute();
	}

}
