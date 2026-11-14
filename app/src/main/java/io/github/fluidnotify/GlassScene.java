package io.github.fluidnotify;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/* loaded from: classes.dex */
final class GlassScene {
    GlassScene() {
    }

    static Bitmap create(int i, int i2) {
        Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.scale(i / 1000.0f, i2 / 1000.0f);
        Paint paint = new Paint(1);
        paint.setShader(new LinearGradient(0.0f, 0.0f, 1000.0f, 1000.0f, new int[]{-2034961, -4924199, -3613967}, (float[]) null, Shader.TileMode.CLAMP));
        canvas.drawRect(0.0f, 0.0f, 1000.0f, 1000.0f, paint);
        paint.setShader(new RadialGradient(900.0f, 60.0f, 660.0f, -863542, 15913674, Shader.TileMode.CLAMP));
        canvas.drawRect(0.0f, 0.0f, 1000.0f, 1000.0f, paint);
        Path path = new Path();
        path.moveTo(-100.0f, 760.0f);
        path.cubicTo(160.0f, 70.0f, 490.0f, 840.0f, 1100.0f, 190.0f);
        path.lineTo(1100.0f, 420.0f);
        path.cubicTo(470.0f, 1040.0f, 160.0f, 330.0f, -100.0f, 990.0f);
        path.close();
        paint.setShader(new LinearGradient(180.0f, 330.0f, 650.0f, 790.0f, new int[]{-9848656, -7220024, -2755607}, (float[]) null, Shader.TileMode.CLAMP));
        canvas.drawPath(path, paint);
        Path path2 = new Path();
        path2.moveTo(-100.0f, 760.0f);
        path2.cubicTo(160.0f, 70.0f, 490.0f, 840.0f, 1100.0f, 190.0f);
        paint.setShader(null);
        paint.setColor(-1426063361);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3.0f);
        canvas.drawPath(path2, paint);
        paint.setStyle(Paint.Style.FILL);
        path.rewind();
        path.moveTo(-100.0f, 930.0f);
        path.cubicTo(260.0f, 560.0f, 640.0f, 1120.0f, 1100.0f, 570.0f);
        path.lineTo(1100.0f, 1050.0f);
        path.lineTo(-100.0f, 1050.0f);
        path.close();
        paint.setShader(new LinearGradient(160.0f, 650.0f, 900.0f, 1050.0f, -1720014116, -2037002, Shader.TileMode.CLAMP));
        canvas.drawPath(path, paint);
        return createBitmap;
    }
}
