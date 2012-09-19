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

import android.provider.UserDictionary;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public class UserDictionaryExporter extends SimpleExporter {
	public static final int ID = 4;
	
	public static final int NAMEID = R.string.userdictionary;
	
	public static final String NAME = Strings.USERDICTIONARY;

	public UserDictionaryExporter(ExportTask exportTask) {
		super(Strings.TAG_WORD, UserDictionary.Words.CONTENT_URI, exportTask);
	}

}
