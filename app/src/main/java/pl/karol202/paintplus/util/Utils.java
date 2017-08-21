package pl.karol202.paintplus.util;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Utils
{
	public static float map(float src, float srcMin, float srcMax, float dstMin, float dstMax)
	{
		float srcPoint = (src - srcMin) / (srcMax - srcMin);
		return lerp(srcPoint, dstMin, dstMax);
	}

	public static float lerp(float value, float v1, float v2)
	{
		return v1 + value * (v2 - v1);
	}
	
	public static int dpToPixels(DisplayMetrics metrics, int dp)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
	}
	
	public static double getAngle(Point center, Point point)
	{
		double deltaX = point.x - center.x;
		double deltaY = center.y - point.y;
		double ratio = deltaX / deltaY;
		double angleRad = Math.atan(ratio);
		double angleDeg = Math.toDegrees(angleRad);
		if(deltaY < 0) angleDeg += 180;
		if(angleDeg < 0) angleDeg += 360;
		return angleDeg;
	}
}