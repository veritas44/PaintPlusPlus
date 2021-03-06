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

package pl.karol202.paintplus.tool.shape.polygon;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import pl.karol202.paintplus.R;
import pl.karol202.paintplus.tool.shape.Join;
import pl.karol202.paintplus.tool.shape.JoinAdapter;
import pl.karol202.paintplus.tool.shape.ShapeProperties;
import pl.karol202.paintplus.util.SeekBarTouchListener;

public class PolygonProperties extends ShapeProperties
{
	private final int MIN_SIDES = 3;
	private final int MAX_SIDES = 20;
	
	private ShapePolygon polygon;
	private String errorToFew;
	private String errorToMany;
	private JoinAdapter adapter;
	
	private View view;
	private ImageButton buttonMinusSides;
	private ImageButton buttonPlusSides;
	private TextInputLayout editLayoutSides;
	private EditText editSides;
	private CheckBox checkFill;
	private SeekBar seekBarWidth;
	private TextView textWidth;
	private Spinner spinnerJoin;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.properties_polygon, container, false);
		
		polygon = (ShapePolygon) shape;
		errorToFew = getActivity().getString(R.string.error_polygon_too_few_sides);
		errorToMany = getActivity().getString(R.string.error_polygon_too_many_sides);
		adapter = new JoinAdapter(getActivity());
		
		buttonMinusSides = view.findViewById(R.id.button_minus_polygon_sides);
		buttonMinusSides.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				if(getSides() > MIN_SIDES) editSides.setText(String.valueOf(getSides() - 1));
			}
		});
		
		buttonPlusSides = view.findViewById(R.id.button_plus_polygon_sides);
		buttonPlusSides.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				if(getSides() < MAX_SIDES) editSides.setText(String.valueOf(getSides() + 1));
			}
		});
		
		editLayoutSides = view.findViewById(R.id.edit_layout_polygon_sides);
		editLayoutSides.setHintEnabled(false);
		
		editSides = editLayoutSides.getEditText();
		if(editSides == null) throw new RuntimeException("TextInputLayout must contain EditText.");
		editSides.setText(String.valueOf(polygon.getSides()));
		editSides.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }
			
			@Override
			public void afterTextChanged(Editable s)
			{
				int sides = getSides();
				if(sides < MIN_SIDES) editLayoutSides.setError(errorToFew);
				else if(sides > MAX_SIDES) editLayoutSides.setError(errorToMany);
				else
				{
					editLayoutSides.setErrorEnabled(false);
					polygon.setSides(getSides());
				}
			}
		});
		
		checkFill = view.findViewById(R.id.check_polygon_fill);
		checkFill.setChecked(polygon.isFill());
		checkFill.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				polygon.setFill(isChecked);
			}
		});
		
		seekBarWidth = view.findViewById(R.id.seek_polygon_width);
		seekBarWidth.setProgress(polygon.getLineWidth() - 1);
		seekBarWidth.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				polygon.setLineWidth(progress + 1);
				textWidth.setText(String.valueOf(progress + 1));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
		});
		seekBarWidth.setOnTouchListener(new SeekBarTouchListener());
		
		textWidth = view.findViewById(R.id.polygon_width);
		textWidth.setText(String.valueOf(polygon.getLineWidth()));
		
		spinnerJoin = view.findViewById(R.id.spinner_polygon_join);
		spinnerJoin.setAdapter(adapter);
		spinnerJoin.setSelection(polygon.getJoin().ordinal());
		spinnerJoin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				Join join = adapter.getItem(position);
				polygon.setJoin(join);
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		return view;
	}
	
	private int getSides()
	{
		if(editSides.getText().length() == 0) return 0;
		return Integer.parseInt(editSides.getText().toString());
	}
}