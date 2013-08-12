package com.lyt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class myImageView extends ImageView {

	private int co;
	private int borderwidth;
	public myImageView(Context context) {
		super(context);
	}
	public myImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public myImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
    //ÉèÖÃÑÕÉ«
	public void setColour(int color){
		co = color;
	}
	//ÉèÖÃ±ß¿ò¿í¶È
	public void setBorderWidth(int width){
		
		borderwidth = width;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// »­±ß¿ò
		Rect rec = canvas.getClipBounds();
		rec.bottom--;
		rec.right--;
		Paint paint = new Paint();
		//ÉèÖÃ±ß¿òÑÕÉ«
		paint.setColor(co);
		paint.setStyle(Paint.Style.STROKE);
		//ÉèÖÃ±ß¿ò¿í¶È
		paint.setStrokeWidth(borderwidth);
		canvas.drawRect(rec, paint);
	}
}
