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

package pl.karol202.paintplus.file;

import android.graphics.Bitmap;

public class BitmapSaveResult
{
	public enum Result
	{
		SUCCESSFUL, UNSUPPORTED_FORMAT, ERROR
	}
	
	private Bitmap bitmap;
	private Result result;
	
	BitmapSaveResult(Bitmap bitmap, Result result)
	{
		this.bitmap = bitmap;
		this.result = result;
	}
	
	public Bitmap getBitmap()
	{
		return bitmap;
	}
	
	public Result getResult()
	{
		return result;
	}
}