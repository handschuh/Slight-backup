/**
 * Slight backup - a simple backup tool
 *
 * Copyright (c) 2011, 2012 Stefan Handschuh
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

import java.io.IOException;
import java.io.Writer;

import android.database.Cursor;
import android.provider.MediaStore.Audio;
import android.text.TextUtils;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

/**
 * This exporter stores the information that is contained in a playlist.
 *
 * Only the file names are saved instead of the files itself.
 */
public class PlaylistExporter extends SimpleExporter {
	public static final int ID = 5;
	
	public static final int NAMEID = R.string.playlists;
	
	public static final String NAME = Strings.PLAYLISTS;
	
	private static final String QUERY_ID = Audio.Media._ID+"=?";
	
	private static final String ENDQUOTETAG = "\"/>";
	
	private static final String[] PROJECTION_AUDIOID = new String[] {Audio.Playlists.Members.AUDIO_ID};
	
	private static final String[] PROJECTION_DATA = new String[] {Audio.Media.DATA};
	
	private int idPosition;
	
	public PlaylistExporter(ExportTask exportTask) {
		super(Strings.TAG_PLAYLIST, Audio.Playlists.EXTERNAL_CONTENT_URI, exportTask);
		idPosition = -1;
	}

	@Override
	public void addText(Cursor cursor, Writer writer) throws IOException {
		if (idPosition < 0) {
			idPosition = cursor.getColumnIndex(Audio.Playlists._ID);
		}
		writer.write('\n');
		
		Cursor audioIdCursor = context.getContentResolver().query(Audio.Playlists.Members.getContentUri(Strings.EXTERNAL, cursor.getLong(idPosition)), PROJECTION_AUDIOID, null, null, Audio.Playlists.Members.PLAY_ORDER);
		
		while(!canceled && audioIdCursor.moveToNext()) {
			Cursor audioFileCursor = context.getContentResolver().query(Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION_DATA, QUERY_ID, new String[] {audioIdCursor.getString(0)}, null);
			
			if (audioFileCursor.moveToNext()) {
				writer.write('<');
				writer.write(Strings.TAG_FILE);
				writer.write(' ');
				writer.write(Audio.Media.DATA);
				writer.write(EQUALS);
				writer.write(TextUtils.htmlEncode(audioFileCursor.getString(0)));
				writer.write(ENDQUOTETAG);
			}
			audioFileCursor.close();
		}
		audioIdCursor.close();
	}

}
