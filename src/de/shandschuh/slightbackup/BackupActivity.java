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

package de.shandschuh.slightbackup;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.TextView;
import de.shandschuh.slightbackup.exporter.EverythingExporter;
import de.shandschuh.slightbackup.exporter.ExportTask;
import de.shandschuh.slightbackup.exporter.Exporter;
import de.shandschuh.slightbackup.exporter.Exporter.ExporterInfos;
import de.shandschuh.slightbackup.parser.ImportTask;

/**
 * This class is the main activity of the backup program. From this view, the
 * user can trigger all imports and all exports.
 */
public class BackupActivity extends ExpandableListActivity {
	public static final Uri SMS_URI = Uri.parse("content://sms");
	
	private static final int CONTEXTMENU_IMPORT = 21;
	
	private static final int CONTEXTMENU_DELETEFILE = 22;
	
	private static final int CONTEXTMENU_DELETEDAY = 23;
	
	private static final int DIALOG_LICENSEAGREEMENT = 1;
	
	private static final int DIALOG_ABOUT = 2;
	
	public static final int DIALOG_EXPORT = 4;
	
	public static final String STANDARD_DIRNAME = new StringBuilder(Environment.getExternalStorageDirectory().toString()).append("/backup/").toString();
	
	public static File DIR;
	
	public static final boolean CANHAVEROOT = false && checkRoot(); // not yet ready, forgot to branch
	
	public static BackupActivity INSTANCE;
	
	public static final int API_LEVEL = Integer.parseInt(Build.VERSION.SDK);
	
	public static final int REQUEST_CHANGEDEFAULTSMSAPPLICATIONTOORIGINAL = 1;
	
	public BackupFilesListAdapter listAdapter;
	
	private AlertDialog deleteFileDialog;
	
	private AlertDialog deleteDayDialog;
	
	private ProgressDialog exportDialog;
	
	private ProgressDialog importDialog;
	
	private AlertDialog selectExportsDialog;
	
	private ExporterInfos exporterInfos;
	
	private Properties properties;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		INSTANCE = this;
		super.onCreate(savedInstanceState);
		properties = new Properties();
		/**
		 * Only show the content, if the license has been shown and
		 * accepted before.
		 */
		if (getPreferences(MODE_PRIVATE).getBoolean(Strings.PREFERENCE_LICENSEACCEPTED, false)) {
			setContent();
		} else {
			showDialog(DIALOG_LICENSEAGREEMENT);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onMenuItemSelected(int featureId, final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_about: {
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
							// just to enable the button
						}
					});
					builder.setNegativeButton(android.R.string.no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					builder.setMessage(Strings.EMPTY); // just so that the string is available
					deleteFileDialog = builder.create();
				}
				deleteFileDialog.show();
				deleteFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (!file.exists() || file.delete()) {
							listAdapter.remove(file);
						} else {
							// show error
						}
						deleteFileDialog.dismiss();
					}
				});
				deleteFileDialog.setMessage(String.format(getString(R.string.question_deletefile), file.toString()));
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
			case CONTEXTMENU_DELETEDAY: {
				long packedPosition = ((ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo()).packedPosition;
				
				if (ExpandableListView.getPackedPositionType(packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
					break;
				}
				
				final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
				
				Date date = listAdapter.getGroup(groupPosition);
				
				if (deleteDayDialog == null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					
					builder.setIcon(android.R.drawable.ic_dialog_alert);
					builder.setTitle(android.R.string.dialog_alert_title);
					builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// just to enable the button
						}
					});
					builder.setNegativeButton(android.R.string.no, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					builder.setMessage(Strings.EMPTY); // just so that the string is available
					deleteDayDialog = builder.create();
				}
				deleteDayDialog.show();
				deleteDayDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Vector<File> files = listAdapter.getChildren(groupPosition);
						
						Vector<File> deletedFiles = new Vector<File>();
						
						for (File file : files) {
							if (!file.exists() || file.delete()) {
								deletedFiles.add(file);
							} else {
								// show error
							}
						}
						listAdapter.remove(deletedFiles);
						deleteDayDialog.dismiss();
					}
				});
				deleteDayDialog.setMessage(String.format(getString(R.string.question_deletefile), DateFormat.getDateInstance().format(date)));
				break;
			}
			case R.id.menu_exporteverything: {
				if (exportDialog == null) {
					exportDialog = new ProgressDialog(BackupActivity.this);
				}
				checkProgressDialog(exportDialog);
				checkExportTaskForIncompleteData(new ExportTask(exportDialog, listAdapter, EverythingExporter.ID));
				break;
			}
			case R.id.menu_export: {
				if (selectExportsDialog == null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					
					builder.setIcon(android.R.drawable.ic_dialog_info);
					builder.setTitle(R.string.dialog_export);
					
					exporterInfos = Exporter.getExporterInfos(this);
					
					builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					
					builder.setItems(exporterInfos.names, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							if (exportDialog == null) {
								exportDialog = new ProgressDialog(BackupActivity.this);
							}
							checkProgressDialog(exportDialog);
							checkExportTaskForIncompleteData(new ExportTask(exportDialog, listAdapter, exporterInfos.ids[which]));
						}
					});
					selectExportsDialog = builder.create();
				}
				selectExportsDialog.show();
				break;
			}
			case R.id.menu_settings: {
				startActivity(new Intent(this, ApplicationPreferencesActivity.class));
				break;
			}
		}
		return true;
	}
	
	/**
	 * Checks if the exporter that is attached to the given ExportTask may
	 * produce incomplete data and shows a warning if this is the case and
	 * if the user wants to get notified. Note that the standard setting is
	 * to show the warning.
	 * The user may also cancel the warning dialog which results in the
	 * export to be not performed.
	 *
	 * @param exportTask task whose exporter is checked w.r.t. incomplete
	 *                   exports
	 */
	private void checkExportTaskForIncompleteData(final ExportTask exportTask) {
		Exporter exporter = exportTask.getExporter();
		
		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Strings.PREFERENCE_HIDEDATAWARNINGS, false) && exporter.maybeIncomplete()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setTitle(android.R.string.dialog_alert_title);
			builder.setMessage(getString(R.string.warning_incompletedata_export, exporter.getIncompleteDataNames(this)));
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					exportTask.execute();
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
			exportTask.execute();
		}
	}
	
	/**
	 * Here, the given progress dialog will be reset.
	 *
	 * @param dialog progress dialog to be reset
	 */
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
	
	@SuppressWarnings("deprecation")
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
	
	/**
	 * This method sets all the needed values in an AlertDialog.Builder that
	 * are needed to show the license.
	 * No buttons are set up such that we can use this method both for the
	 * agreement and the showing of the license.
	 *
	 * @param builder dialog builder that will be used to generate the
	 *                license text dialog
	 */
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
	
	/**
	 * Sets up the main content of the application (i.e. loads the list of
	 * available backups and generates the context menu).
	 */
	private void setContent() {
		setContentView(R.layout.main);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		String dirName = preferences.getString(Strings.PREFERENCE_STORAGELOCATION, STANDARD_DIRNAME);
		
		if (TextUtils.isEmpty(dirName)) {
			dirName = STANDARD_DIRNAME;
		}
		DIR = new File(dirName);
		
		listAdapter = new BackupFilesListAdapter(this, preferences);
		setListAdapter(listAdapter);
		getExpandableListView().setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				ExpandableListView.ExpandableListContextMenuInfo expandableInfo = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
				
				menu.setHeaderTitle(((TextView) ((ExpandableListView.ExpandableListContextMenuInfo) menuInfo).targetView.findViewById(android.R.id.text1)).getText());
				if (ExpandableListView.getPackedPositionType(expandableInfo.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					menu.add(0, CONTEXTMENU_IMPORT, Menu.NONE, R.string.button_import);
					menu.add(0, CONTEXTMENU_DELETEFILE, Menu.NONE, R.string.contextmenu_deletefile);
				} else {
					menu.add(0, CONTEXTMENU_DELETEDAY, Menu.NONE, R.string.contextmenu_deletedaydata);
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CHANGEDEFAULTSMSAPPLICATIONTOORIGINAL : {
				if (resultCode != RESULT_OK) {
					showWarningDialog(this, R.string.warning_defaultsmsapplication, null);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
		
	}

	
	/**
	 * In order to perform certain backups (such as the wifi settings), we
	 * need root to access the corresponding configuration files.
	 *
	 * @return true if <i>root</i> access can be obtained, <i>false</i>
	 *         otherwise
	 */
	private static boolean checkRoot() {
		try {
			Process process = Runtime.getRuntime().exec("/system/bin/ls -l /system/bin/su /system/xbin/su");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			String line = reader.readLine();
			
			reader.close();
			process.destroy();
			
			return line != null && line.length() > 9 && line.charAt(9) == 'x';
		} catch (Exception e) {
			return false;
		}
	}

	public void addProperty(String key, String value) {
		properties.put(key, value);
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public static Dialog showWarningDialog(Context context, int textId, OnClickListener onClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle(android.R.string.dialog_alert_title);
		builder.setMessage(textId);
		
		if (onClickListener == null) {
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
				
			});
		} else {
			builder.setPositiveButton(android.R.string.ok, onClickListener);
		}
		return builder.show();
	}
}
