package pl.karol202.paintplus.tool.shape;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import pl.karol202.paintplus.R;

import java.util.ArrayList;

class ShapeAdapter extends ArrayAdapter<Shape>
{
	ShapeAdapter(Context context, ArrayList<Shape> shapes)
	{
		super(context, R.layout.spinner_item_shape, shapes);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view;
		if(convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.spinner_item_shape, parent, false);
		}
		else view = convertView;
		Shape shape = getItem(position);
		
		ImageView imageView = (ImageView) view.findViewById(R.id.image_shape_icon);
		imageView.setImageResource(shape.getIcon());
		
		TextView textView = (TextView) view.findViewById(R.id.text_shape_name);
		textView.setText(shape.getName());
		return view;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		return getView(position, convertView, parent);
	}
}