package org.oobd.kadaver;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class ProgressView extends FrameLayout {
    private ImageView mProgressImage;
    private TranslateAnimation mProgressAnimation;

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mProgressImage = new ImageView(getContext());
        
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 20);
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        mProgressImage.setLayoutParams(layoutParams);
        addView(mProgressImage);
    }

    public void setBackgroundAsTile(int tileImageResId, boolean toRight) {
        Bitmap tileBitmap = BitmapFactory.decodeResource(getResources(), tileImageResId);
        BitmapDrawable tileRepeatedBitmap = new BitmapDrawable(getResources(), tileBitmap);
        tileRepeatedBitmap.setTileModeX(TileMode.REPEAT);

        initAnimation(tileBitmap.getWidth(), toRight);

        mProgressImage.setBackgroundDrawable(tileRepeatedBitmap);
    }

    private void initAnimation(int tileImageWidth, boolean toRight) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mProgressImage.getLayoutParams();
        layoutParams.setMargins(-tileImageWidth, 0, 0, 0);

        // *HACK* tileImageWidth-3 is used because of *lags*(slow pause) in the moment
        // of animation END-RESTART.
        if(toRight){
	        mProgressAnimation = new TranslateAnimation(0, tileImageWidth - 3, 0, 0);
	        mProgressAnimation.setInterpolator(new LinearInterpolator());
	        mProgressAnimation.setDuration(500);
	        mProgressAnimation.setRepeatCount(Animation.INFINITE);
        } else {
        	mProgressAnimation = new TranslateAnimation(tileImageWidth - 3, 0 , 0, 0);
            mProgressAnimation.setInterpolator(new LinearInterpolator());
            mProgressAnimation.setDuration(500);
            mProgressAnimation.setRepeatCount(Animation.INFINITE);	
        }
    }

    public void startAnimation() {
        mProgressImage.startAnimation(mProgressAnimation);
    }
}