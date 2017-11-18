package pl.karol202.paintplus.options;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.widget.Toast;
import pl.karol202.paintplus.AsyncBlocker;
import pl.karol202.paintplus.AsyncManager;
import pl.karol202.paintplus.R;
import pl.karol202.paintplus.activity.ActivityPaint;
import pl.karol202.paintplus.activity.ActivityResultListener;
import pl.karol202.paintplus.file.BitmapSaveAsyncTask;
import pl.karol202.paintplus.file.BitmapSaveAsyncTask.OnBitmapSaveListener;
import pl.karol202.paintplus.file.BitmapSaveParams;
import pl.karol202.paintplus.file.UriMetadata;
import pl.karol202.paintplus.file.UriUtils;
import pl.karol202.paintplus.file.explorer.FileExplorer;
import pl.karol202.paintplus.file.explorer.FileExplorerFactory;
import pl.karol202.paintplus.image.Image;
import pl.karol202.paintplus.recent.OnFileEditListener;
import pl.karol202.paintplus.settings.ActivitySettings;

import static android.app.Activity.RESULT_OK;

public class OptionFileSave extends Option implements ActivityResultListener, AsyncBlocker, OnBitmapSaveListener
{
	private static final int REQUEST_SAVE_FILE = 2;
	
	private ActivityPaint activity;
	private OnFileEditListener listener;
	private AsyncManager asyncManager;
	private int quality;
	
	private BitmapSaveAsyncTask asyncTask;
	private Uri uri;
	private ParcelFileDescriptor parcelFileDescriptor;
	
	public OptionFileSave(ActivityPaint activity, Image image, AsyncManager asyncManager, OnFileEditListener listener)
	{
		super(activity, image);
		this.activity = activity;
		this.asyncManager = asyncManager;
		this.listener = listener;
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		this.quality = preferences.getInt(ActivitySettings.KEY_JPG_QUALITY, 100);
	}
	
	@Override
	public void execute()
	{
		activity.registerActivityResultListener(REQUEST_SAVE_FILE, this);
		
		FileExplorer explorer = FileExplorerFactory.createFileExplorer(activity);
		explorer.saveFile(REQUEST_SAVE_FILE);
	}
	
	public void execute(Uri uri)
	{
		this.uri = uri;
		saveBitmapAsynchronously();
	}
	
	@Override
	public void onActivityResult(int resultCode, Intent intent)
	{
		activity.unregisterActivityResultListener(REQUEST_SAVE_FILE);
		if(resultCode != RESULT_OK) return;
		uri = intent.getData();
		saveBitmapAsynchronously();
	}
	
	private void saveBitmapAsynchronously()
	{
		image.setLastUri(uri);
		asyncManager.block(this);
		
		parcelFileDescriptor = UriUtils.createFileSaveDescriptor(context, uri);
		UriMetadata metadata = new UriMetadata(context, uri);
		BitmapSaveParams params = new BitmapSaveParams(this, image.getFullImage(), metadata.getDisplayName(),
													   parcelFileDescriptor.getFileDescriptor(), quality);
		asyncTask = new BitmapSaveAsyncTask();
		asyncTask.execute(params);
	}
	
	@Override
	public void cancel()
	{
		asyncTask.cancel(true);
		asyncManager.unblock(this);
	}
	
	@Override
	public int getMessage()
	{
		return R.string.dialog_save_message;
	}
	
	@Override
	public void onBitmapSaved(boolean saved, Bitmap bitmap)
	{
		asyncManager.unblock(this);
		if(!saved) Toast.makeText(activity, R.string.message_cannot_save_file, Toast.LENGTH_SHORT).show();
		else if(listener != null) listener.onFileEdited(uri, bitmap);
		UriUtils.closeFileDescriptor(parcelFileDescriptor);
	}
}