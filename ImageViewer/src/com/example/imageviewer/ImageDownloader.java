package com.example.imageviewer;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.widget.ImageView;

public class ImageDownloader {
	
	
	public void download(String url, ImageView imageView) {
		resetPurgeTimer();
		Bitmap bitmap = getBitmapFromCache(url);
		
		if (bitmap == null) {
			BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
			} else {
				task.execute(url);
			}
			
		} else {
			imageView.setImageBitmap(bitmap);	
		}
		
	}
	
		
	private Bitmap downBmp(String imgUrl) {
		URL url = null;
		HttpURLConnection conn = null;
		Bitmap bmpImg = null;
		try {
			url = new URL(imgUrl);
			conn = (HttpURLConnection)url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bmpImg = BitmapFactory.decodeStream(is);
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if(conn != null)
				conn.disconnect();
		}
		return bmpImg;
	}
	
	
	private class BitmapDownloaderTask extends AsyncTask<String, String, Bitmap> {
		private String url;
		private final WeakReference<ImageView> imageViewReference;

		
		public BitmapDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		protected Bitmap doInBackground(String... params) {
			url = params[0];
			return downBmp(url);
		}
		
		protected void onPostExecute(Bitmap bitmap) {
			
			addBitmapToCache(url, bitmap);
			 
			if (imageViewReference != null)	{
				ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
					
				}
			}
		}
		
	}

    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */
    
    private static final int HARD_CACHE_CAPACITY = 10;
    private static final int DELAY_BEFORE_PURGE = 10 * 100000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> sHardBitmapCache =
        new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
        new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Adds this bitmap to the cache.
     * @param bitmap The newly downloaded bitmap.
     */
    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, bitmap);
            }
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url) {
        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }

        return null;
    }
 
    /**
     * Clears the image cache used internally to improve performance. Note that for memory
     * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }

}
