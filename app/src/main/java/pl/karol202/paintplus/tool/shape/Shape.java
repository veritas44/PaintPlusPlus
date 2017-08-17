package pl.karol202.paintplus.tool.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import pl.karol202.paintplus.color.ColorsSet;
import pl.karol202.paintplus.helpers.HelpersManager;
import pl.karol202.paintplus.image.Image.OnImageChangeListener;

public abstract class Shape
{
	private boolean smooth;
	private float opacity;
	
	private OnImageChangeListener imageChangeListener;
	private OnShapeEditListener shapeEditListener;
	private boolean editMode;
	private Paint paint;
	private ColorsSet colors;
	
	public Shape(ColorsSet colors, OnImageChangeListener imageChangeListener, OnShapeEditListener shapeEditListener)
	{
		this.smooth = true;
		this.opacity = 1;
		
		this.imageChangeListener = imageChangeListener;
		this.shapeEditListener = shapeEditListener;
		this.paint = new Paint();
		this.colors = colors;
	}
	
	public abstract int getName();
	
	public abstract int getIcon();
	
	public abstract Class<? extends ShapeProperties> getPropertiesClass();
	
	public abstract boolean onTouch(MotionEvent event, HelpersManager helpersManager);
	
	public abstract void onScreenDraw(Canvas canvas);
	
	public abstract void apply(Canvas imageCanvas);
	
	public abstract void cancel();
	
	protected float calcDistance(Point point, int x, int y)
	{
		return (float) Math.hypot(point.x - x, point.y - y);
	}
	
	protected void update()
	{
		updateColor();
		paint.setAntiAlias(smooth);
		imageChangeListener.onImageChanged();
	}
	
	protected void updateColor()
	{
		paint.setColor(colors.getFirstColor());
		paint.setAlpha((int) (opacity * 255));
	}
	
	protected void cleanUp()
	{
		editMode = false;
		imageChangeListener.onImageChanged();
	}
	
	protected boolean isInEditMode()
	{
		return editMode;
	}
	
	protected void enableEditMode()
	{
		editMode = true;
		shapeEditListener.onStartShapeEditing();
	}
	
	protected Paint getPaint()
	{
		return paint;
	}
	
	protected boolean isSmooth()
	{
		return smooth;
	}
	
	protected void setSmooth(boolean smooth)
	{
		this.smooth = smooth;
		update();
	}
	
	protected float getOpacity()
	{
		return opacity;
	}
	
	protected void setOpacity(float opacity)
	{
		this.opacity = opacity;
		update();
	}
}