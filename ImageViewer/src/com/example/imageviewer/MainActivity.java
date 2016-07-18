package com.example.imageviewer;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("Image Viewer Sample");
		
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager winMgr = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		winMgr.getDefaultDisplay().getMetrics(metrics);
				
		
		final GridView gv = (GridView) findViewById((R.id.gridView1));
		MyGridAdapter mga = new MyGridAdapter(this, metrics.widthPixels);
		gv.setAdapter(mga);
				
		
	}
		
}
