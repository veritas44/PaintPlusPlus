package pl.karol202.paintplus.tool.drag;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;
import pl.karol202.paintplus.R;
import pl.karol202.paintplus.helpers.HelpersManager;
import pl.karol202.paintplus.image.Image;
import pl.karol202.paintplus.image.layer.Layer;
import pl.karol202.paintplus.tool.CoordinateSpace;
import pl.karol202.paintplus.tool.Tool;
import pl.karol202.paintplus.tool.ToolProperties;

public class ToolDrag extends Tool
{
	private HelpersManager helpersManager;
	private Layer layer;
	private int oldLayerX;
	private int oldLayerY;
	private float oldTouchX;
	private float oldTouchY;
	
	public ToolDrag(Image image)
	{
		super(image);
	}
	
	@Override
	public int getName()
	{
		return R.string.tool_drag;
	}
	
	@Override
	public int getIcon()
	{
		return R.drawable.ic_tool_drag_black_24dp;
	}
	
	@Override
	public Class<? extends ToolProperties> getPropertiesFragmentClass()
	{
		return DragProperties.class;
	}
	
	@Override
	public CoordinateSpace getCoordinateSpace()
	{
		return CoordinateSpace.IMAGE_SPACE;
	}
	
	@Override
	public boolean isUsingSnapping()
	{
		return false;
	}
	
	@Override
	public boolean onTouch(MotionEvent event, HelpersManager manager, Context context)
	{
		helpersManager = manager;
		
		float x = event.getX() - image.getViewX();
		float y = event.getY() - image.getViewY();
		if(event.getAction() == MotionEvent.ACTION_DOWN) onTouchStart(x, y);
		else if(event.getAction() == MotionEvent.ACTION_MOVE) onTouchMove(x, y);
		return true;
	}
	
	private void onTouchStart(float x, float y)
	{
		layer = image.getSelectedLayer();
		oldLayerX = layer.getX();
		oldLayerY = layer.getY();
		oldTouchX = x;
		oldTouchY = y;
	}
	
	private void onTouchMove(float x, float y)
	{
		int deltaTouchX = Math.round(x - oldTouchX);
		int deltaTouchY = Math.round(y - oldTouchY);
		
		PointF snapped = new PointF(oldLayerX + deltaTouchX, oldLayerY + deltaTouchY);
		helpersManager.snapPoint(snapped);
		
		layer.setX((int) snapped.x);
		layer.setY((int) snapped.y);
	}
	
	@Override
	public boolean isImageLimited()
	{
		return false;
	}
	
	@Override
	public boolean doesScreenDraw(boolean layerVisible)
	{
		return false;
	}
	
	@Override
	public boolean isDrawingOnTop()
	{
		return false;
	}
	
	@Override
	public void onScreenDraw(Canvas canvas) { }
}