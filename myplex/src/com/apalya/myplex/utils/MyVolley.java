
package com.apalya.myplex.utils;

import android.app.ActivityManager;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


/**
 * Helper class that is used to provide references to initialized RequestQueue(s) and ImageLoader(s)
 * 
 * @author 
 * 
 */
public class MyVolley {
    private static int MAX_IMAGE_CACHE_ENTIRES  = 100;
    
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;


    private MyVolley() {
        // no instances
    }


    public static void init(Context context) {
    	
    	int memClass = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    	 // Use 1/8th of the available memory for this memory cache.
    	MAX_IMAGE_CACHE_ENTIRES = 1024 * 1024 * memClass / 8;
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(MAX_IMAGE_CACHE_ENTIRES));
    }


    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }


    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means
     * that no memory caching is used. This is useful for images that you know that will be show
     * only once.
     * 
     * @return
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }
}
