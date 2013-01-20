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

package de.shandschuh.slightbackup.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import de.shandschuh.slightbackup.BackupTask;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

/**
 * This parser is currently only usable for the playlist but the fields are
 * prepared to be generalized in case this is needed later.
 *
 * @author Stefan Handschuh
 *
 */
public class PlaylistParser extends Parser {
	public static final String NAME = Strings.PLAYLISTS;
	
	public static final int NAMEID = R.string.playlists;
	
	private String levelOneTag;
	
	private String levelTwoTag;
	
	/**
	 * The level one flag is used determine the current xml tree depth to
	 * distinguish between the playlists and the playlist entries.
	 */
	private boolean levelOneTagEntered;
	
	/**
	 * Flag to determine whether we are at the playlist level or at the file
	 * level of a playlist.
	 */
	private boolean levelTwoTagEntered;
	
	private long levelOneId;
	
	private String[] levelOneProjection;
	
	private String levelOneSelection;
	
	private String[] levelTwoProjection;
	
	private String levelTwoSelection;
	
	private String levelTwoExistenceSelection;
	
	private int position;
	
	private int playOrder;
	
	public PlaylistParser(Context context, ImportTask importTask) {
		super(context, importTask);
		levelOneTag = Strings.TAG_PLAYLIST;
		levelTwoTag = Strings.TAG_FILE;
		levelOneProjection = new String[] { Audio.Playlists._ID };
		levelOneSelection = Audio.Playlists.NAME + Strings.DB_ARG;
		levelTwoProjection = new String[] { Audio.Media._ID };
		levelTwoSelection = Audio.Media.DATA + Strings.DB_ARG;
		levelTwoExistenceSelection = new StringBuilder(
				Audio.Playlists.Members.AUDIO_ID).append(Strings.DB_ARG)
				.append(Strings.AND).append(Audio.Playlists.Members.PLAY_ORDER)
				.append(Strings.DB_ARG).toString();
		position = 0;
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (canceled) {
			return;
		}
		if (levelOneTagEntered && localName.equals(levelOneTag)) {
			levelOneTagEntered = false;
			levelOneId = 0;
			importTask.progress(BackupTask.MESSAGE_PROGRESS, ++position);
		} else if (levelTwoTagEntered && localName.equals(levelTwoTag)) {
			levelTwoTagEntered = false;
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (canceled) {
			return;
		}
		if (!levelOneTagEntered && localName.equals(levelOneTag)) {
			levelOneTagEntered = true;
			
			String name = attributes.getValue(Strings.EMPTY,
					Audio.Playlists.NAME);
			
			if (name != null) {
				Cursor playlistCursor = context.getContentResolver().query(
						Audio.Playlists.EXTERNAL_CONTENT_URI,
						levelOneProjection, levelOneSelection,
						new String[] { name }, null);
				
				if (playlistCursor.moveToNext()) {
					
					addHint(context.getString(R.string.hint_existed, name));
					// we do not want to import into an existing list
					levelOneTagEntered = false;
				} else {
					ContentValues values = new ContentValues();
					
					values.put(Audio.Playlists.NAME, name);
					levelOneId = Long.parseLong(context.getContentResolver()
							.insert(Audio.Playlists.EXTERNAL_CONTENT_URI,
									values).getLastPathSegment());
				}
				playlistCursor.close();
			}
			playOrder = 0;
		} else if (!levelTwoTagEntered && localName.equals(levelTwoTag)
				&& levelOneTagEntered) {
			levelTwoTagEntered = true;
			
			String data = attributes.getValue(Strings.EMPTY, Audio.Media.DATA);
			
			if (data != null) {
				Cursor audioCursor = context.getContentResolver().query(
						Audio.Media.EXTERNAL_CONTENT_URI, levelTwoProjection,
						levelTwoSelection, new String[] { data }, null);
				
				if (audioCursor.moveToNext()) {
					long audioId = audioCursor.getLong(0);
					
					Uri contentUri = Audio.Playlists.Members.getContentUri(
							Strings.EXTERNAL, levelOneId);
					
					Cursor playlistMemberCursor = context
							.getContentResolver()
							.query(contentUri, null, levelTwoExistenceSelection,
									new String[] { Long.toString(audioId),
											Integer.toString(playOrder) }, null);
					
					if (!playlistMemberCursor.moveToNext()) {
						ContentValues values = new ContentValues();
						
						values.put(Audio.Playlists.Members.AUDIO_ID, audioId);
						values.put(Audio.Playlists.Members.PLAY_ORDER,
								playOrder);
						context.getContentResolver().insert(contentUri, values);
					}
					playOrder++;
					playlistMemberCursor.close();
				}
				audioCursor.close();
			}
			
		} else {
			String count = attributes.getValue(Strings.EMPTY, COUNT);
			
			if (count != null) {
				try {
					importTask.progress(BackupTask.MESSAGE_COUNT, Integer
							.parseInt(count));
				} catch (Exception e) {
					
				}
			}
		}
	}

}
