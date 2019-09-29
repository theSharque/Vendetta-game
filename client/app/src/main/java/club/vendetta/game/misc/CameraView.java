package club.vendetta.game.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Camera redraw class
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    public SurfaceHolder mHolder;
    Camera.Parameters param;
    Camera.CameraInfo info;
    private Camera.Size size = null;
    private ImageButton ibAvatar = null;
    private Camera mCamera;

    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, Camera camera, ImageButton ibAvatar, Camera.CameraInfo info) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.ibAvatar = ibAvatar;
        param = mCamera.getParameters();
        size = param.getPreviewSize();
        this.info = info;

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (mHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (camera != null && info !=null ) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(bytes, ImageFormat.NV21, size.width, size.height, null);
            yuvImage.compressToJpeg(new Rect(0, 0, size.height, size.height), 100, out);
            byte[] imageBytes = out.toByteArray();
            Bitmap bmPhoto = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            Matrix matrix = new Matrix();
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                matrix.preRotate(270);
                matrix.postScale(-1.0f, 1.0f);
            } else {
                matrix.preRotate(90);
            }
            Bitmap bmRotated = Bitmap.createBitmap(bmPhoto, 0, 0, size.height, size.height, matrix, true);
            ibAvatar.setImageBitmap(bmRotated);
        }
    }
}
