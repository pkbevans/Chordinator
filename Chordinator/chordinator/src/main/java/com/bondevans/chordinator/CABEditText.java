package com.bondevans.chordinator;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * KITKAT WORKAROUND
 * 
 * This class is a workaround for a problem in kitkat where 
 * if you select text in the editor, and then try to use a custom action mode menu item 
 * from the overfill menu the selection disappears
 * 
 * @author Paul
 *
 */
public class CABEditText extends EditText {
	private boolean shouldWindowFocusWait;

	public CABEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CABEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CABEditText(Context context) {
		super(context);
	}

	public void setWindowFocusWait(boolean shouldWindowFocusWait) {
		this.shouldWindowFocusWait = shouldWindowFocusWait;
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if(!shouldWindowFocusWait) {
			super.onWindowFocusChanged(hasWindowFocus);
		}
	}
}

