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
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.util.Linkify;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.TextView;
import de.shandschuh.slightbackup.exporter.ExportTask;
import de.shandschuh.slightbackup.parser.ImportTask;

public class BackupActivity extends ExpandableListActivity {
	public static final Uri SMS_URI = Uri.parse("content://sms");
	
	public static final int MENU_EXPORTSMS_ID = 101;
	
	public static final int MENU_EXPORTCALLLOG_ID = 102;
	
	public static final int MENU_EXPORTBOOKMARKS_ID = 103;
	
	public static final int MENU_EXPORTUSERDICTIONARY_ID = 104;
	
	public static final int MENU_EXPORTPLAYLIST_ID = 105;
	
	public static final int MENU_EXPORTEVERYTHING_ID = 200;
	
	private static final int CONTEXTMENU_IMPORT = 21;
	
	private static final int CONTEXTMENU_DELETEFILE = 22;
	
	private static final int MENU_ABOUT_ID = 9;
	
	private static final int DIALOG_LICENSEAGREEMENT = 1;
	
	private static final int DIALOG_ABOUT = 2;
	
	public static final int DIALOG_EXPORT = 4;
	
	public static final String DIR_NAME = new StringBuilder(Environment.getExternalStorageDirectory().toString()).append("/backup/").toString();
	
	public final static File DIR = new File(DIR_NAME);
	
	public BackupFilesListAdapter listAdapter;
	
	private AlertDialog deleteFileDialog;
	
	private ProgressDialog exportDialog;
	
	private ProgressDialog importDialog;
	
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
		menu.add(0, MENU_EXPORTEVERYTHING_ID, Menu.NONE, R.string.menu_exporteverything).setIcon(android.R.drawable.ic_menu_directions);
		menu.add(0, MENU_ABOUT_ID, Menu.NONE, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, MENU_EXPORTUSERDICTIONARY_ID, Menu.NONE, R.string.menu_exportuserdictionary).setIcon(android.R.drawable.ic_menu_my_calendar);
		menu.add(0, MENU_EXPORTPLAYLIST_ID, Menu.NONE, R.string.menu_exportplaylists).setIcon(android.R.drawable.ic_menu_recent_history);
		
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ABOUT_ID: {
				showDialog(DIALOG_ABOUT);
				break;
			}
			case CONTEXTMENU_DELETEFILE: {
				/* using "showDialog" with a Bundle is only available from api version 8 on, so we cannot directly use this. Lets impose this */
				
				long packedPosition = ((ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo()).packedPosition;
				
				if (ExpandableListView.getPackedPositionType(packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					break; 
				}
				
				final File file = listAdapter.getChild(ExpandableListView.getPackedPositionGroup(packedPosition), ExpandableListView.getPackedPositionChild(packedPosition));
					
				if (deleteFileDialog == null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					
					builder.setIcon(android.R.drawable.ic_dialog_alert);
					builder.setTitle(android.R.string.dialog_alert_title);
					builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (file.delete()) {
								listAdapter.remove(file);
							} else {
								// show error
							}
							dialog.dismiss();
						}
					});
					builder.setNegativeButton(android.R.string.no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					builder.setMessage(String.format(getString(R.string.question_deletefile), file.toString()));
					deleteFileDialog = builder.create();
				} else {
					deleteFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							if (file.delete() || !file.exists()) {
								listAdapter.remove(file);
							} else {
								// show error
							}
							deleteFileDialog.dismiss();
						}
					});
					deleteFileDialog.setMessage(String.format(getString(R.string.question_deletefile), file.toString()));
				}
				deleteFileDialog.show();
				break;
			}
			case CONTEXTMENU_IMPORT: {
				ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
				
				long packedPosition = menuInfo.packedPosition;
				
				if (ExpandableListView.getPackedPositionType(packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					break; 
				}
				if (importDialog == null) {
					importDialog = new ProgressDialog(this);
				}
				checkProgressDialog(importDialog);
				new ImportTask(importDialog, listAdapter.getChild(ExpandableListView.getPackedPositionGroup(packedPosition), ExpandableListView.getPackedPositionChild(packedPosition)), (Integer) menuInfo.targetView.getTag());
				break;
			}
			default: {
				if (exportDialog == null) {
					exportDialog = new ProgressDialog(this);
				}
				checkProgressDialog(exportDialog);
				new ExportTask(exportDialog, listAdapter).execute(item.getItemId());
				break;
			}
		}
		return true;
	}	
	
	private void checkProgressDialog(ProgressDialog dialog) {
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		dialog.setMax(100);
		dialog.setMessage(Strings.EMPTY); // we just have to set some non-null value to enable the title
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		if (importDialog == null) {
			importDialog = new ProgressDialog(this);
		}
		checkProgressDialog(importDialog);
		new ImportTask(importDialog, listAdapter.getChild(groupPosition, childPosition), (Integer) v.getTag());

		return true;
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
			setupLicenseText(builder);
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
			
			builder.setIcon(android.R.drawable.ic_dialog_info);		
			builder.setTitle(R.string.menu_about);
			setupLicenseText(builder);			
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}
	
	private void setupLicenseText(AlertDialog.Builder builder) {
		ScrollView scrollView = new ScrollView(this);
		
		TextView textView = new TextView(this);
		
		scrollView.addView(textView);
		scrollView.setPadding(0, 0, 2, 0);
		
		textView.setTextColor(textView.getTextColors().getDefaultColor()); // disables color change on selection
		textView.setPadding(5, 0, 5, 0);
		textView.setTextSize(15);
		textView.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
		textView.setText(new StringBuilder(getString(R.string.license_intro)).append(Strings.THREENEWLINES).append(getString(R.string.license)));
		builder.setView(scrollView);
	}
	
	private void setContent() {
		setContentView(R.layout.main);
        listAdapter = new BackupFilesListAdapter(this);
        setListAdapter(listAdapter);
        getExpandableListView().setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				ExpandableListView.ExpandableListContextMenuInfo expandableInfo = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
				
				if (ExpandableListView.getPackedPositionType(expandableInfo.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					menu.setHeaderTitle(((TextView) ((ExpandableListView.ExpandableListContextMenuInfo) menuInfo).targetView.findViewById(android.R.id.text1)).getText());
					menu.add(0, CONTEXTMENU_IMPORT, Menu.NONE, R.string.button_import);
					menu.add(0, CONTEXTMENU_DELETEFILE, Menu.NONE, R.string.contextmenu_deletefile);
				}
			}
        });
	}
}
