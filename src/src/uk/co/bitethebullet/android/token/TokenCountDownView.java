package uk.co.bitethebullet.android.token;

///////////////////////////////////////
//this is not used!!
//////////////////////////////////////


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

public class TokenCountDownView extends View {
	
	public TokenCountDownView(Context context){
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x88FF0000);			
		
		RectF oval = new RectF( 0, 0, 50, 50);
		drawArc(canvas, oval, true, p);		
	}
	
	
	private void drawArc(Canvas canvas, RectF oval, boolean useCenter, Paint paint) {
		
		float mStart = 0;
		float mSweep = 360;
		
		canvas.drawArc(oval, mStart, mSweep, useCenter, paint);
	}
	
	
}
