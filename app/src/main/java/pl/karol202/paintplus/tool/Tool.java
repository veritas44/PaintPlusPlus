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

package pl.karol202.paintplus.tool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

public interface Tool
{
	int getName();
	
	int getIcon();
	
	Class<? extends ToolProperties> getPropertiesFragmentClass();
	
	Class<? extends ToolBottomBar> getBottomBarFragmentClass();
	
	ToolCoordinateSpace getCoordinateSpace();
	
	boolean isUsingSnapping();
	
	boolean onTouch(MotionEvent event, Context context);
	
	boolean providesDirtyRegion();
	
	Rect getDirtyRegion();
	
	void resetDirtyRegion();
	
	boolean doesOnLayerDraw(boolean layerVisible);
	
	boolean doesOnTopDraw();
	
	ToolCoordinateSpace getOnLayerDrawingCoordinateSpace();
	
	ToolCoordinateSpace getOnTopDrawingCoordinateSpace();

	void onLayerDraw(Canvas canvas);
	
	void onTopDraw(Canvas canvas);
}