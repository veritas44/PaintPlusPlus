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

import pl.karol202.paintplus.activity.AppContext;
import pl.karol202.paintplus.history.action.ActionLayerResize;
import pl.karol202.paintplus.image.Image;
import pl.karol202.paintplus.image.layer.Layer;

public class OptionLayerToImageSize extends Option
{
	public OptionLayerToImageSize(AppContext context, Image image)
	{
		super(context, image);
	}
	
	@Override
	public void execute()
	{
		Layer layer = getImage().getSelectedLayer();
		
		ActionLayerResize action = new ActionLayerResize(getImage());
		action.setLayerBeforeResize(layer);
		
		layer.resize(-layer.getX(), -layer.getY(), getImage().getWidth(), getImage().getHeight());
		getImage().updateImage();
		
		action.applyAction();
	}
}