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

package de.shandschuh.slightbackup;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.shandschuh.slightbackup.exporter.BookmarkExporter;
import de.shandschuh.slightbackup.exporter.CallLogExporter;
import de.shandschuh.slightbackup.exporter.MessageExporter;
import de.shandschuh.slightbackup.exporter.SimpleExporter;
import de.shandschuh.slightbackup.exporter.UserDictionaryExporter;

public class ExportActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parse);
		
		LayoutParams layoutParams = getWindow().getAttributes(); 
		
		layoutParams.width = LayoutParams.FILL_PARENT; 
        getWindow().setAttributes(layoutParams); // this works around the not stretched dialog
         
		((Button) findViewById(R.id.button_import)).setVisibility(View.INVISIBLE);
		
		TextView textView = (TextView) findViewById(R.id.text_file);
		
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.import_progressbar);
		
		switch (getIntent().getIntExtra(Strings.EXPORTTYPE, 0)) {
			case BackupActivity.MENU_EXPORTBOOKMARKS_ID: {
				textView.setText(String.format(getString(R.string.hint_exporting), getString(R.string.bookmarks)));
				export(new BookmarkExporter(this, progressBar));
				break;
			}
			case BackupActivity.MENU_EXPORTCALLLOG_ID: {
				textView.setText(String.format(getString(R.string.hint_exporting), getString(R.string.calllogs)));
				export(new CallLogExporter(this, progressBar));
				break;
			}
			case BackupActivity.MENU_EXPORTSMS_ID: {
				textView.setText(String.format(getString(R.string.hint_exporting), getString(R.string.messages)));
				export(new MessageExporter(this, progressBar));
				break;
			}
			case BackupActivity.MENU_EXPORTUSERDICTIONARY: {
				textView.setText(String.format(getString(R.string.hint_exporting), getString(R.string.userdictionary)));
				export(new UserDictionaryExporter(this, progressBar));
				break;
			}
			default: {
				setResult(RESULT_CANCELED);
				finish();
				break;
			}
		}
	}
	
	private void export(final SimpleExporter exporter) {
		setResult(RESULT_CANCELED);
		
		((Button) findViewById(R.id.button_cancel)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				exporter.cancel();
				finish();
			}
		});
		
		if (!BackupActivity.DIR.exists() && !BackupActivity.DIR.mkdir()) {
			finish();
			Toast.makeText(this, R.string.error_couldnotcreatebackupfolder, Toast.LENGTH_LONG).show();
		} else {
			final String filename = new StringBuilder(BackupActivity.DIR_NAME).append(exporter.getContentName()).append(Strings.FILE_SUFFIX).append(System.currentTimeMillis()).append(Strings.FILE_EXTENSION).toString();
			
			final Handler handler = new Handler();
			
			new Thread() {
				public void run() {
					try {
						int res = exporter.export(filename);
						
						if (res > 0) {
							handler.post(new Runnable() {
								public void run() {
									setResult(RESULT_OK, new Intent().putExtra(Strings.EXTRA_FILE, filename));
									finish();
									Toast.makeText(ExportActivity.this, String.format(getString(R.string.message_exportedto), filename), Toast.LENGTH_LONG).show();
								}
							});
							
						} else if (res == 0) {
							finish();
							handler.post(new Runnable() {
								public void run() {
									Toast.makeText(ExportActivity.this, R.string.hint_noexportdata, Toast.LENGTH_LONG).show();
								}
							});
						}
					} catch (final IOException e) {
						finish();
						handler.post(new Runnable() {
							public void run() {
								Toast.makeText(ExportActivity.this, String.format(getString(R.string.error_somethingwentwrong), e.getMessage()), Toast.LENGTH_LONG).show();
							}
						});
					}
				}
			}.start();
		}
	}
	
}
