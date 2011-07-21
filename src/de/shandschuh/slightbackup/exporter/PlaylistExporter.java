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

package de.shandschuh.slightbackup.exporter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio;
import de.shandschuh.slightbackup.Strings;

public class PlaylistExporter extends SimpleExporter {
	private static final String EXTERNAL = "external";
	
	private static final String QUERY_ID = Audio.Media._ID+"=?";
	
	private static final String[] PROJECTION_AUDIOID = new String[] {Audio.Playlists.Members.AUDIO_ID};
	
	private static final String[] PROJECTION_DATA = new String[] {Audio.Media.DATA};
	
	private int idPosition;
	
	public PlaylistExporter(Context context, ExportTask exportTask) {
		super(context, Strings.TAG_PLAYLIST, Audio.Playlists.EXTERNAL_CONTENT_URI, exportTask);
		idPosition = -1;
	}

	@Override
	public String getContentName() {
		return Strings.PLAYLISTS;
	}

	@Override
	public void addText(Cursor cursor, StringBuilder builder) {
		if (idPosition < 0) {
			idPosition = cursor.getColumnIndex(Audio.Playlists._ID);
		}
		
		Cursor audioIdCursor = context.getContentResolver().query(Audio.Playlists.Members.getContentUri(EXTERNAL, cursor.getLong(idPosition)), PROJECTION_AUDIOID, null, null, null);
		
		while(!canceled && audioIdCursor.moveToNext()) {
			Cursor audioFileCursor = context.getContentResolver().query(Audio.Media.getContentUri(EXTERNAL), PROJECTION_DATA, QUERY_ID, new String[] {audioIdCursor.getString(0)}, null);
			
			if (audioFileCursor.moveToNext()) {
				builder.append('<');
				builder.append(Strings.TAG_FILE);
				builder.append('>');
				builder.append(audioFileCursor.getString(0));
				builder.append(Strings.STARTENDTAG);
				builder.append(Strings.TAG_FILE);
				builder.append('>');
			}
			audioFileCursor.close();
		}
		audioIdCursor.close();
	}

	

}
