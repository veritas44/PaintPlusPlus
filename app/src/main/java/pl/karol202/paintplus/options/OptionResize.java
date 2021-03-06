/*
 * Copyright 2017 karol-202
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package pl.karol202.paintplus.options;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.*;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import pl.karol202.paintplus.R;
import pl.karol202.paintplus.activity.AppContext;
import pl.karol202.paintplus.image.Image;
import pl.karol202.paintplus.util.GraphicsHelper;
import pl.karol202.paintplus.util.Utils;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public abstract class OptionResize extends Option implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener
{
	private class BoundsChangeListener implements TextWatcher
	{
		private TextInputLayout inputLayout;
		
		BoundsChangeListener(TextInputLayout inputLayout)
		{
			this.inputLayout = inputLayout;
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }
		
		@Override
		public void afterTextChanged(Editable s)
		{
			checkSize(s);
			manageBounds();
		}
		
		private void checkSize(Editable s)
		{
			int value = parseInt(s.toString());
			if(value <= 0)
				inputLayout.setError(getString(R.string.message_image_invalid_size));
			else if(value > GraphicsHelper.getMaxTextureSize())
				inputLayout.setError(getString(R.string.message_image_size_too_big));
			else
			{
				inputLayout.setError(null);
				inputLayout.setErrorEnabled(false);
			}
		}
		
		private void manageBounds()
		{
			if(!dontFireEvent)
			{
				dontFireEvent = true;
				
				int newWidth = parseInt(editWidth.getText().toString());
				int newHeight = parseInt(editHeight.getText().toString());
				
				changeBounds(newWidth, newHeight);
				
				dontFireEvent = false;
			}
			updatePreview();
		}
		
		private String getString(int resource)
		{
			return getContext().getString(resource);
		}
	}
	
	private class OffsetChangeListener implements TextWatcher
	{
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }
		
		@Override
		public void afterTextChanged(Editable s)
		{
			updatePreview();
		}
	}
	
	private AlertDialog dialog;
	private TextInputLayout inputLayoutWidth;
	private TextInputLayout inputLayoutHeight;
	private EditText editWidth;
	private EditText editHeight;
	private EditText editX;
	private EditText editY;
	private ImageView imagePreview;
	private CheckBox checkKeepRatio;
	
	private Bitmap preview;
	private Canvas prevEdit;
	private int newWidth;
	private int newHeight;
	private int oldWidth;
	private int oldHeight;
	private float ratio;
	private boolean dontFireEvent;
	
	OptionResize(AppContext context, Image image)
	{
		super(context, image);
	}
	
	@Override
	@SuppressLint("InflateParams")
	public void execute()
	{
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.dialog_resize, null);
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
		dialogBuilder.setTitle(getTitle());
		dialogBuilder.setView(view);
		dialogBuilder.setPositiveButton(R.string.ok, this);
		dialogBuilder.setNegativeButton(R.string.cancel, this);
		
		newWidth = getObjectWidth();
		newHeight = getObjectHeight();
		oldWidth = getOldObjectWidth();
		oldHeight = getOldObjectHeight();
		ratio = -1;
		
		inputLayoutWidth = view.findViewById(R.id.inputLayout_object_width);
		
		inputLayoutHeight = view.findViewById(R.id.inputLayout_object_height);
		
		editWidth = view.findViewById(R.id.edit_object_width);
		editWidth.setText(String.valueOf(newWidth));
		editWidth.addTextChangedListener(new BoundsChangeListener(inputLayoutWidth));
		
		editHeight = view.findViewById(R.id.edit_object_height);
		editHeight.setText(String.valueOf(newHeight));
		editHeight.addTextChangedListener(new BoundsChangeListener(inputLayoutHeight));
		
		editX = view.findViewById(R.id.edit_object_x);
		editX.setText(String.valueOf(getObjectX()));
		editX.addTextChangedListener(new OffsetChangeListener());
		
		editY = view.findViewById(R.id.edit_object_y);
		editY.setText(String.valueOf(getObjectY()));
		editY.addTextChangedListener(new OffsetChangeListener());
		
		imagePreview = view.findViewById(R.id.image_resize_preview);
		
		checkKeepRatio = view.findViewById(R.id.check_keep_ratio);
		checkKeepRatio.setOnCheckedChangeListener(this);
		
		dialog = dialogBuilder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override
			public void onShow(DialogInterface dialog)
			{
				createPreview();
			}
		});
		dialog.show();
	}
	
	protected abstract int getTitle();
	
	protected abstract int getObjectWidth();
	
	protected abstract int getObjectHeight();
	
	protected abstract int getOldObjectWidth();
	
	protected abstract int getOldObjectHeight();
	
	protected abstract int getObjectX();
	
	protected abstract int getObjectY();

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if(which == BUTTON_POSITIVE)
		{
			if(newWidth == 0 || newHeight == 0)
			{
				getAppContext().createSnackbar(R.string.message_invalid_bounds, Snackbar.LENGTH_LONG).show();
				return;
			}
			if(newWidth > GraphicsHelper.getMaxTextureSize() || newHeight > GraphicsHelper.getMaxTextureSize())
			{
				getAppContext().createSnackbar(R.string.message_too_big, Snackbar.LENGTH_LONG).show();
				return;
			}
			int x = parseInt(editX.getText().toString());
			int y = parseInt(editY.getText().toString());
			applySize(x, y, newWidth, newHeight);
			getImage().updateImage();
		}
	}
	
	protected abstract void applySize(int x, int y, int width, int height);
	
	private void changeBounds(int newWidth, int newHeight)
	{
		if(ratio != -1)
		{
			if(newWidth != this.newWidth)
			{
				newHeight = Math.round(newWidth / ratio);
				editHeight.setText(String.valueOf(newHeight));
			}
			else if(newHeight != this.newHeight)
			{
				newWidth = Math.round(newHeight * ratio);
				editWidth.setText(String.valueOf(newWidth));
			}
		}
		this.newWidth = newWidth;
		this.newHeight = newHeight;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if(isChecked && (newWidth == 0 || newHeight == 0))
		{
			checkKeepRatio.setChecked(false);
			return;
		}
		if(isChecked) ratio = (float) newWidth / newHeight;
		else ratio = -1;
	}
	
	private void createPreview()
	{
		int x = imagePreview.getWidth();
		int y = imagePreview.getHeight();
		preview = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
		imagePreview.setImageBitmap(preview);
		prevEdit = new Canvas(preview);
		updatePreview();
	}
	
	private void updatePreview()
	{
		preview.eraseColor(Color.argb(0, 0, 0, 0));
		
		int offsetX = parseInt(editX.getText().toString());
		int offsetY = parseInt(editY.getText().toString());
		
		int left = Math.min(0, offsetX);
		int top = Math.min(0, offsetY);
		int right = Math.round(Math.max(oldWidth, newWidth + offsetX));
		int bottom = Math.round(Math.max(oldHeight, newHeight + offsetY));
		
		int min = Math.min(left, top);
		int max = Math.max(right, bottom);
		int previewSize = Math.max(preview.getWidth(), preview.getHeight());
		
		float oldLeft = Utils.map(0, min, max, 0, previewSize);
		float oldTop = Utils.map(0, min, max, 0, previewSize);
		float oldRight = Utils.map(oldWidth, min, max, 0, previewSize);
		float oldBottom = Utils.map(oldHeight, min, max, 0, previewSize);
		RectF oldR = new RectF(oldLeft, oldTop, oldRight, oldBottom);
		
		float newLeft = Utils.map(offsetX, min, max, 0, previewSize);
		float newTop = Utils.map(offsetY, min, max, 0, previewSize);
		float newRight = Utils.map(offsetX + newWidth, min, max, 0, previewSize);
		float newBottom = Utils.map(offsetY + newHeight, min, max, 0, previewSize);
		RectF newR = new RectF(newLeft, newTop, newRight, newBottom);
		
		Paint paint = new Paint();
		
		paint.setColor(Color.argb(255, 255, 255, 141));
		prevEdit.drawRect(oldR, paint);
		
		paint.setColor(Color.argb(204, 27, 124, 209));
		prevEdit.drawRect(newR, paint);
	}
	
	private int parseInt(String text)
	{
		return text.equals("") || text.equals("-") ? 0 : Integer.parseInt(text);
	}
}