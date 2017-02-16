package pl.karol202.paintplus.color;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import pl.karol202.paintplus.color.ColorChannel.ColorChannelType;
import pl.karol202.paintplus.color.manipulators.CurveManipulatorParams;
import pl.karol202.paintplus.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class ColorCurvesView extends View
{
	private final int HORIZONTAL_GRID_LINES = 7;
	private final int VERTICAL_GRID_LINES = 7;
	
	private final int LEFT_GRID_MARGIN = 20;
	private final int TOP_GRID_MARGIN = 9;
	private final int RIGHT_GRID_MARGIN = 9;
	private final int BOTTOM_GRID_MARGIN = 20;
	
	private final int HORIZONTAL_SCALE_HEIGHT = 10;
	private final int VERTICAL_SCALE_WIDTH = 10;
	
	private final int POINT_RADIUS = 8;
	private final int POINT_INNER_RADIUS = 3;
	
	private final int MAX_TOUCH_DISTANCE = 65;
	
	private ColorChannelType channelType;
	private ColorChannel channelIn;
	private ColorChannel channelOut;
	private HashMap<ChannelInOutSet, ColorCurve> curves;
	
	private Point viewSize;
	private float[] grid;
	private Paint gridPaint;
	private Shader horizontalScaleShader;
	private Shader verticalScaleShader;
	private Paint horizontalScalePaint;
	private Paint verticalScalePaint;
	private ArrayList<RectF> points;
	private ArrayList<RectF> innerPoints;
	private Paint pointPaint;
	private Paint pointInnerPaint;
	private Paint curvePaint;
	
	private Point draggedScreenPoint;
	private Point draggedCurvePoint;
	private int draggedPointIndex;
	private Point touchStartPoint;
	
	public ColorCurvesView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		channelType = null;
		channelIn = null;
		channelOut = null;
		
		gridPaint = new Paint();
		gridPaint.setColor(Color.LTGRAY);
		gridPaint.setStrokeWidth(2f);
		
		pointPaint = new Paint();
		pointPaint.setColor(Color.BLACK);
		pointPaint.setAntiAlias(true);
		
		pointInnerPaint = new Paint();
		pointInnerPaint.setColor(Color.WHITE);
		pointInnerPaint.setAntiAlias(true);
		
		curvePaint = new Paint();
		curvePaint.setColor(Color.BLACK);
		curvePaint.setStrokeWidth(3f);
		curvePaint.setAntiAlias(true);
	}
	
	private void initChannels(ColorChannelType type)
	{
		if(channelType != null) throw new RuntimeException("Channel settings are already set!");
		channelType = type;
		
		curves = new HashMap<>();
		for(ColorChannel channelIn : ColorChannel.filterByType(channelType))
		{
			for(ColorChannel channelOut : ColorChannel.filterByType(channelType))
			{
				ChannelInOutSet set = new ChannelInOutSet(channelIn, channelOut);
				if(channelIn == channelOut) curves.put(set, ColorCurve.defaultCurve(set));
				else curves.put(set, ColorCurve.zeroCurve(set));
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getMeasuredWidth();
		setMeasuredDimension(width, width);
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		viewSize = new Point(canvas.getWidth(), canvas.getHeight());
		if(channelType == null) return;
		
		drawGrid(canvas);
		drawScale(canvas);
		
		Point[] points = getCurrentCurve().getPoints();
		drawCurve(canvas, points);
		drawPoints(canvas, points);
	}
	
	private void drawGrid(Canvas canvas)
	{
		if(grid == null) createGrid(canvas.getWidth(), canvas.getHeight());
		canvas.drawLines(grid, gridPaint);
	}
	
	private void drawScale(Canvas canvas)
	{
		if(horizontalScaleShader == null || verticalScaleShader == null) createScalePaints();
		canvas.drawRect(LEFT_GRID_MARGIN, canvas.getHeight() - HORIZONTAL_SCALE_HEIGHT - 1,
						canvas.getWidth() - RIGHT_GRID_MARGIN, canvas.getHeight() - 1, horizontalScalePaint);
		canvas.drawRect(0, TOP_GRID_MARGIN, VERTICAL_SCALE_WIDTH,
						canvas.getHeight() - BOTTOM_GRID_MARGIN, verticalScalePaint);
	}
	
	private void drawPoints(Canvas canvas, Point[] curvePoints)
	{
		if(points == null) createPoints(curvePoints);
		for(RectF point : points) canvas.drawOval(point, pointPaint);
		for(RectF point : innerPoints) canvas.drawOval(point, pointInnerPaint);
	}
	
	private void drawCurve(Canvas canvas, Point[] curvePoints)
	{
		if(points == null) createPoints(curvePoints);
		float lastX = LEFT_GRID_MARGIN;
		float lastY = points.get(0).centerY();
		for(RectF rect : points)
		{
			float x = rect.centerX();
			float y = rect.centerY();
			canvas.drawLine(lastX, lastY, x, y, curvePaint);
			lastX = x;
			lastY = y;
		}
		canvas.drawLine(lastX, lastY, canvas.getWidth() - RIGHT_GRID_MARGIN, lastY, curvePaint);
	}
	
	private void createGrid(int width, int height)
	{
		float[] linesHorizontal = new float[HORIZONTAL_GRID_LINES * 4];
		for(int i = 0; i < HORIZONTAL_GRID_LINES; i++)
		{
			int verticalMargins = TOP_GRID_MARGIN + BOTTOM_GRID_MARGIN;
			float y = (height - verticalMargins) * (i / (HORIZONTAL_GRID_LINES - 1f)) + TOP_GRID_MARGIN;
			linesHorizontal[i * 4] = LEFT_GRID_MARGIN;
			linesHorizontal[i * 4 + 1] = y;
			linesHorizontal[i * 4 + 2] = width - RIGHT_GRID_MARGIN;
			linesHorizontal[i * 4 + 3] = y;
		}
		
		float[] linesVertical = new float[VERTICAL_GRID_LINES * 4];
		for(int i = 0; i < VERTICAL_GRID_LINES; i++)
		{
			int horizontalMargins = LEFT_GRID_MARGIN + RIGHT_GRID_MARGIN;
			float x = (width - horizontalMargins) * (i / (VERTICAL_GRID_LINES - 1f)) + LEFT_GRID_MARGIN;
			linesVertical[i * 4] = x;
			linesVertical[i * 4 + 1] = TOP_GRID_MARGIN;
			linesVertical[i * 4 + 2] = x;
			linesVertical[i * 4 + 3] = height - BOTTOM_GRID_MARGIN;
		}
		
		grid = new float[linesHorizontal.length + linesVertical.length];
		System.arraycopy(linesHorizontal, 0, grid, 0, linesHorizontal.length);
		System.arraycopy(linesVertical, 0, grid, linesHorizontal.length, linesVertical.length);
	}
	
	private void createScalePaints()
	{
		createHorizontalScalePaints();
		createVerticalScalePaints();
	}
	
	private void createHorizontalScalePaints()
	{
		if(channelIn != ColorChannel.HUE)
		{
			int firstColor = Color.BLACK;
			int secondColor = Color.WHITE;
			if(channelIn == ColorChannel.RED) secondColor = Color.RED;
			else if(channelIn == ColorChannel.GREEN) secondColor = Color.GREEN;
			else if(channelIn == ColorChannel.BLUE) secondColor = Color.BLUE;
			else if(channelIn == ColorChannel.SATURATION)
			{
				firstColor = Color.WHITE;
				secondColor = Color.RED;
			}
			horizontalScaleShader = new LinearGradient(LEFT_GRID_MARGIN, 0, viewSize.x - RIGHT_GRID_MARGIN,
					0, firstColor, secondColor, Shader.TileMode.CLAMP);
		}
		else
		{
			int[] colors = new int[] { Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED };
			horizontalScaleShader = new LinearGradient(LEFT_GRID_MARGIN, 0, viewSize.x - RIGHT_GRID_MARGIN,
					0, colors, null, Shader.TileMode.CLAMP);
		}
		horizontalScalePaint = new Paint();
		horizontalScalePaint.setShader(horizontalScaleShader);
		horizontalScalePaint.setStyle(Paint.Style.FILL);
	}
	
	private void createVerticalScalePaints()
	{
		if(channelOut != ColorChannel.HUE)
		{
			int firstColor = Color.BLACK;
			int secondColor = Color.WHITE;
			if(channelOut == ColorChannel.RED) secondColor = Color.RED;
			else if(channelOut == ColorChannel.GREEN) secondColor = Color.GREEN;
			else if(channelOut == ColorChannel.BLUE) secondColor = Color.BLUE;
			else if(channelOut == ColorChannel.SATURATION)
			{
				firstColor = Color.WHITE;
				secondColor = Color.RED;
			}
			verticalScaleShader = new LinearGradient(0, viewSize.y - BOTTOM_GRID_MARGIN, 0,
					TOP_GRID_MARGIN, firstColor, secondColor, Shader.TileMode.CLAMP);
		}
		else
		{
			int[] colors = new int[] { Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED };
			verticalScaleShader = new LinearGradient(0, viewSize.y - BOTTOM_GRID_MARGIN, 0,
					TOP_GRID_MARGIN, colors, null, Shader.TileMode.CLAMP);
		}
		verticalScalePaint = new Paint();
		verticalScalePaint.setShader(verticalScaleShader);
		verticalScalePaint.setStyle(Paint.Style.FILL);
	}
	
	private void createPoints(Point[] curvePoints)
	{
		points = new ArrayList<>();
		innerPoints = new ArrayList<>();
		for(Point point : curvePoints)
		{
			Point newPoint = curveToScreen(point);
			RectF oval = new RectF(newPoint.x - POINT_RADIUS, newPoint.y - POINT_RADIUS,
								   newPoint.x + POINT_RADIUS, newPoint.y + POINT_RADIUS);
			RectF innerOval = new RectF(newPoint.x - POINT_INNER_RADIUS, newPoint.y - POINT_INNER_RADIUS,
										newPoint.x + POINT_INNER_RADIUS, newPoint.y + POINT_INNER_RADIUS);
			points.add(oval);
			innerPoints.add(innerOval);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(event.getAction() == MotionEvent.ACTION_DOWN) onTouchDown((int) event.getX(), (int) event.getY());
		else if(event.getAction() == MotionEvent.ACTION_MOVE) onTouchMove((int) event.getX(), (int) event.getY());
		else if(event.getAction() == MotionEvent.ACTION_UP) onTouchUp((int) event.getX(), (int) event.getY());
		
		invalidate();
		return true;
	}
	
	private void onTouchDown(int x, int y)
	{
		Point touch = new Point(x, y);
		
		Point nearest = null;
		int nearestIndex = -1;
		float shortestDistance = -1;
		for(int i = 0; i < points.size(); i++)
		{
			RectF rect = points.get(i);
			Point point = new Point((int) rect.centerX(), (int) rect.centerY());
			float distance = distance(point, touch);
			if((nearest == null || distance < shortestDistance) && distance < MAX_TOUCH_DISTANCE)
			{
				nearest = point;
				nearestIndex = i;
				shortestDistance = distance;
			}
		}
		
		ColorCurve curve = getCurrentCurve();
		if(nearest != null)
		{
			draggedScreenPoint = nearest;
			draggedPointIndex = nearestIndex;
			
			draggedCurvePoint = curve.getPoints()[draggedPointIndex];
			
			touchStartPoint = touch;
		}
		else
		{
			draggedScreenPoint = new Point(touch);
			draggedCurvePoint = screenToCurve(draggedScreenPoint);
			touchStartPoint = touch;
			curve.addPoint(draggedCurvePoint);
			
			Point[] curvePoints = curve.getPoints();
			createPoints(curvePoints);
			for(int i = 0; i < curvePoints.length; i++)
			{
				if(curvePoints[i].equals(draggedCurvePoint))
				{
					draggedPointIndex = i;
					break;
				}
			}
		}
	}
	
	private void onTouchMove(int x, int y)
	{
		if(draggedScreenPoint != null)
		{
			Point newScreenPoint = new Point(draggedScreenPoint);
			newScreenPoint.offset(x - touchStartPoint.x, y - touchStartPoint.y);
			checkBounds(newScreenPoint);
			
			RectF oval = new RectF(newScreenPoint.x - POINT_RADIUS, newScreenPoint.y - POINT_RADIUS,
					newScreenPoint.x + POINT_RADIUS, newScreenPoint.y + POINT_RADIUS);
			RectF innerOval = new RectF(newScreenPoint.x - POINT_INNER_RADIUS, newScreenPoint.y - POINT_INNER_RADIUS,
					newScreenPoint.x + POINT_INNER_RADIUS, newScreenPoint.y + POINT_INNER_RADIUS);
			points.set(draggedPointIndex, oval);
			innerPoints.set(draggedPointIndex, innerOval);
		}
	}
	
	private void onTouchUp(int x, int y)
	{
		if(draggedScreenPoint != null)
		{
			Point newScreenPoint = new Point(draggedScreenPoint);
			newScreenPoint.offset(x - touchStartPoint.x, y - touchStartPoint.y);
			checkBounds(newScreenPoint);
			
			Point newCurvePoint = screenToCurve(newScreenPoint);
			getCurrentCurve().movePoint(draggedCurvePoint, newCurvePoint);
			
			draggedScreenPoint = null;
			draggedPointIndex = -1;
			draggedCurvePoint = null;
			touchStartPoint = null;
			points = null;
		}
	}
	
	private void checkBounds(Point point)
	{
		if(point.x < LEFT_GRID_MARGIN) point.x = LEFT_GRID_MARGIN;
		else if(point.x >= viewSize.x - RIGHT_GRID_MARGIN) point.x = viewSize.x - RIGHT_GRID_MARGIN - 1;
		if(point.y < TOP_GRID_MARGIN) point.y = TOP_GRID_MARGIN;
		else if(point.y >= viewSize.y - BOTTOM_GRID_MARGIN) point.y = viewSize.y - BOTTOM_GRID_MARGIN - 1;
	}
	
	private float distance(Point first, Point second)
	{
		return (float) Math.sqrt(Math.pow(first.x - second.x, 2) + Math.pow(first.y - second.y, 2));
	}
	
	private Point curveToScreen(Point point)
	{
		Point newPoint = new Point(point);
		newPoint.x = Math.round(Utils.map(point.x, 0, channelIn.getMaxValue(),
										  LEFT_GRID_MARGIN, viewSize.x - RIGHT_GRID_MARGIN));
		newPoint.y = Math.round(Utils.map(point.y, channelOut.getMaxValue(), 0,
										  TOP_GRID_MARGIN, viewSize.y - BOTTOM_GRID_MARGIN));
		return newPoint;
	}
	
	private Point screenToCurve(Point point)
	{
		Point newPoint = new Point(point);
		newPoint.x = Math.round(Utils.map(point.x, LEFT_GRID_MARGIN, viewSize.x - RIGHT_GRID_MARGIN,
										  0, channelIn.getMaxValue()));
		newPoint.y = Math.round(Utils.map(point.y, TOP_GRID_MARGIN, viewSize.y - BOTTOM_GRID_MARGIN,
									  	  channelOut.getMaxValue(), 0));
		return newPoint;
	}
	
	private void updatePoints()
	{
		this.points = null;
	}
	
	public void attachCurvesToParamsObject(CurveManipulatorParams params)
	{
		for(ChannelInOutSet set : curves.keySet())
		{
			ColorCurve curve = curves.get(set);
			params.addCurve(set, curve);
		}
	}
	
	public void restoreCurrentCurve()
	{
		ChannelInOutSet set = new ChannelInOutSet(channelIn, channelOut);
		if(channelIn == channelOut) curves.put(set, ColorCurve.defaultCurve(set));
		else curves.put(set, ColorCurve.zeroCurve(set));
		updatePoints();
		invalidate();
	}
	
	private ColorCurve getCurrentCurve()
	{
		ChannelInOutSet set = new ChannelInOutSet(channelIn, channelOut);
		return curves.get(set);
	}
	
	public void setChannelType(ColorChannelType channelType)
	{
		initChannels(channelType);
	}
	
	public ColorChannel getChannelIn()
	{
		return channelIn;
	}
	
	public void setChannelIn(ColorChannel channelIn)
	{
		this.channelIn = channelIn;
		this.horizontalScaleShader = null;
		this.verticalScaleShader = null;
		updatePoints();
		invalidate();
	}
	
	public ColorChannel getChannelOut()
	{
		return channelOut;
	}
	
	public void setChannelOut(ColorChannel channelOut)
	{
		this.channelOut = channelOut;
		this.horizontalScaleShader = null;
		this.verticalScaleShader = null;
		updatePoints();
		invalidate();
	}
}