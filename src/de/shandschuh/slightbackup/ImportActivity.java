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

import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.shandschuh.slightbackup.parser.SimpleParser;

public class ImportActivity extends Activity {
	SimpleParser parser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parse);
		
		LayoutParams layoutParams = getWindow().getAttributes(); 
		
		layoutParams.width = LayoutParams.FILL_PARENT; 
        getWindow().setAttributes(layoutParams); // this works around the not stretched dialog
        
		parser = null;

		final String filename = getIntent().getStringExtra(Strings.EXTRA_FILE);

		int count = getIntent().getIntExtra(Strings.EXTRA_COUNT, -1);

		((TextView) findViewById(R.id.text_file)).setText(filename);

		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.import_progressbar);

		if (count > -1) {
			progressBar.setMax(count);
		}
		((Button) findViewById(R.id.button_cancel)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (parser != null) {
					parser.cancel();
				}
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});

		final Button importButton = (Button) findViewById(R.id.button_import);
		
		final Handler handler = new Handler();

		importButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				parser = SimpleParser.createParserByFilename(filename, ImportActivity.this, progressBar);

				new Thread() {
					public void run() {
						handler.post(new Runnable() {
							public void run() {
								importButton.setEnabled(false);
							}
						});
						try {
							Xml.parse(new InputStreamReader(new FileInputStream(filename)), parser);
							finish();
							handler.post(new Runnable() {
								public void run() {
									Toast.makeText(ImportActivity.this,	R.string.message_importsuccessful, Toast.LENGTH_LONG).show();
								}
							});
						} catch (final Exception e) {
							handler.post(new Runnable() {
								public void run() {
									importButton.setEnabled(true);
									Toast.makeText(ImportActivity.this,	String.format(getString(R.string.error_somethingwentwrong), e.getMessage()), Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				}.start();
			}
		});
	}

}
