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

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class BackupActivity extends ListActivity {
	public static final Uri SMS_URI = Uri.parse("content://sms");
	
	public static final int MENU_EXPORTSMS_ID = 1;
	
	public static final int MENU_EXPORTCALLLOG_ID = 2;
	
	public static final int MENU_EXPORTBOOKMARKS_ID = 3;
	
	public static final int MENU_EXPORTUSERDICTIONARY = 4;
	
	private static final int MENU_ABOUT_ID = 9;
	
	private static final int EXPORTACTIVITY_ID = 1;
	
	private static final int DIALOG_LICENSEAGREEMENT = 1;
	
	private static final int DIALOG_ABOUT = 2;
	
	public static final String DIR_NAME = new StringBuilder(Environment.getExternalStorageDirectory().toString()).append("/backup/").toString();
	
	public final static File DIR = new File(DIR_NAME);
	
	public BackupFilesListAdapter listAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getPreferences(MODE_PRIVATE).getBoolean(Strings.PREFERENCE_LICENSEACCEPTED, false)) {
        	setContent();
        } else {
        	showDialog(DIALOG_LICENSEAGREEMENT);
        }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_EXPORTSMS_ID, Menu.NONE, R.string.menu_exportsms).setIcon(android.R.drawable.ic_menu_save);
		menu.add(0, MENU_EXPORTCALLLOG_ID, Menu.NONE, R.string.menu_exportcalllog).setIcon(android.R.drawable.ic_menu_call);
		menu.add(0, MENU_EXPORTBOOKMARKS_ID, Menu.NONE, R.string.menu_exportbookmarks).setIcon(android.R.drawable.ic_menu_myplaces);
		menu.add(0, MENU_EXPORTUSERDICTIONARY, Menu.NONE, R.string.menu_exportuserdictionary).setIcon(android.R.drawable.ic_menu_my_calendar);
		menu.add(0, MENU_ABOUT_ID, Menu.NONE, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
		
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ABOUT_ID: {
				showDialog(DIALOG_ABOUT);
				break;
			}
			default: {
				startActivityForResult(new Intent(this, ExportActivity.class).putExtra(Strings.EXPORTTYPE, item.getItemId()), EXPORTACTIVITY_ID);
			}
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == EXPORTACTIVITY_ID) {
			listAdapter.add(new File(data.getStringExtra(Strings.EXTRA_FILE)));
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ImportActivity.class);
		
		intent.putExtra(Strings.EXTRA_FILE, listAdapter.getItem(position).toString());
		intent.putExtra(Strings.EXTRA_COUNT, (Integer) v.getTag());
		startActivity(intent);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_LICENSEAGREEMENT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.dialog_licenseagreement);
			builder.setNegativeButton(R.string.button_decline, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					finish();
				}
			});
			builder.setPositiveButton(R.string.button_accept, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					Editor editor = getPreferences(MODE_PRIVATE).edit();
					
					editor.putBoolean(Strings.PREFERENCE_LICENSEACCEPTED, true);
					editor.commit();
					
					/* we only want to invoke actions if the license is accepted */
					setContent();
				}
			});
			builder.setMessage(new StringBuilder(getString(R.string.license_intro)).append(Strings.THREENEWLINES).append(getString(R.string.license)));
			builder.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						dialog.cancel();
						finish();
					}
					return true;
				}
			});
			return builder.create();
		} else if (id == DIALOG_ABOUT) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setTitle(R.string.menu_about);
			builder.setMessage(new StringBuilder(getString(R.string.license_intro)).append(Strings.THREENEWLINES).append(getString(R.string.license)));
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			return builder.create();
		}
		return null;
	}
	
	private void setContent() {
		setContentView(R.layout.main);
        listAdapter = new BackupFilesListAdapter(this);
        setListAdapter(listAdapter);
	}
	
}
