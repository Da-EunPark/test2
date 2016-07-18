package com.example.imageviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class MyGridAdapter extends BaseAdapter {
	Context context;
	int wSize;
	int hSize;

    private final ImageDownloader downloader = new ImageDownloader();
	private static final String[] imgURL = {"https://attachment.namu.wiki/pika-heroine1.jpg", "http://www.bitcultures.com/wp-content/uploads/2015/08/pokemonday-pikachu-image-1.png", "https://attachment.namu.wiki/pika-heroine2.jpg"};
	
	public MyGridAdapter(Context c, int s) {
		context = c;
		wSize = (int)(s / 3.3);
		hSize = (int)(s / 3.3);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return imgURL.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return imgURL[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final int pos = position;

		ImageView imgView = new ImageView(context);
		
		imgView.setLayoutParams(new GridView.LayoutParams(wSize, hSize));
		imgView.setScaleType(ImageView.ScaleType.CENTER_CROP); 
		imgView.setPadding(5, 5, 5, 5);
		
		downloader.download(imgURL[position], imgView);
		
		imgView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				View dlgView = (View)View.inflate(context, R.layout.dialog, null);
				AlertDialog.Builder dlg = new AlertDialog.Builder(context);
				ImageView ivDlg = (ImageView) dlgView.findViewById(R.id.ivDlg);
				downloader.download(imgURL[pos], ivDlg);
				dlg.setView(dlgView);
				dlg.setNegativeButton("Close", null);
				dlg.show();
			
				
			}
		});
		
		
		return imgView;
	}
	
	
}
