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

package pl.karol202.paintplus.history.action;

import android.graphics.Bitmap;
import android.graphics.Color;
import pl.karol202.paintplus.R;
import pl.karol202.paintplus.image.Image;
import pl.karol202.paintplus.image.layer.Layer;

public class ActionLayerScale extends Action
{
	private int layerId;
	private Bitmap bitmap;
	
	public ActionLayerScale(Image image)
	{
		super(image);
		this.layerId = -1;
	}
	
	private void updateBitmap(Image image)
	{
		Bitmap layerBitmap = image.getLayerAtIndex(layerId).getBitmap();
		getPreviewBitmap().eraseColor(Color.TRANSPARENT);
		getPreviewCanvas().drawBitmap(layerBitmap, null, transformLayerRect(layerBitmap), null);
	}
	
	@Override
	public boolean undo(Image image)
	{
		if(!super.undo(image)) return false;
		updateBitmap(image);
		scale(image);
		return true;
	}
	
	@Override
	public boolean redo(Image image)
	{
		if(!super.redo(image)) return false;
		updateBitmap(image);
		scale(image);
		return true;
	}
	
	private void scale(Image image)
	{
		Layer layer = image.getLayerAtIndex(layerId);
		Bitmap oldBitmap = layer.getBitmap();
		layer.setBitmap(bitmap);
		bitmap = oldBitmap;
	}
	
	@Override
	boolean canApplyAction()
	{
		Layer layer = getImage().getLayerAtIndex(layerId);
		return layerId != -1 && bitmap != null && (bitmap.getWidth() != layer.getWidth() ||
												   bitmap.getHeight() != layer.getHeight());
	}
	
	@Override
	public int getActionName()
	{
		return R.string.history_action_layer_scale;
	}
	
	public void setLayerBeforeScaling(Layer layer)
	{
		if(isApplied()) throw new IllegalStateException("Cannot alter history!");
		this.layerId = getImage().getLayerIndex(layer);
		this.bitmap = layer.getBitmap();
		updateBitmap(getImage());
	}
}