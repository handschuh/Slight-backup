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

package de.shandschuh.slightbackup.parser;

import android.content.Context;
import android.provider.CallLog;
import android.widget.ProgressBar;
import de.shandschuh.slightbackup.Strings;

public class CallLogParser extends SimpleParser {
	public CallLogParser(Context context, ProgressBar progressBar) {
		super(context, Strings.TAG_CALL, new String[] {CallLog.Calls.CACHED_NAME,
				CallLog.Calls.CACHED_NUMBER_LABEL,
				CallLog.Calls.CACHED_NUMBER_TYPE,
				CallLog.Calls.DATE,
				CallLog.Calls.DURATION,
				CallLog.Calls.NEW,
				CallLog.Calls.NUMBER,
				CallLog.Calls.TYPE
		}, CallLog.Calls.CONTENT_URI, progressBar);
	}

}
