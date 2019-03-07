package com.kcchen.nativecanvas.filters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.kcchen.nativecanvas.utils.ImageUtils;
import com.kcchen.nativecanvas.utils.Utility;
import com.kcchen.nativecanvas.view.NCManager;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by kowei on 2018/4/3.
 */

public class NeuralStyle {
    private final static String TAG = NeuralStyle.class.getSimpleName();
    private static final float MIN_SIZE = 720.0f;

    // Copy these lines below
    private TensorFlowInferenceInterface inferenceInterface;

    private static final String MODEL_FILE = "file:///android_asset/www/assets/tensorflow/stylize_quantized.pb";
    private static final String INPUT_NODE = "input";
    private static final String STYLE_NODE = "style_num";
    private static final String OUTPUT_NODE = "transformer/expand/conv3/conv/Sigmoid";

    private static final int NUM_STYLES = 26;
    private static final boolean DEBUG_MODEL = false;
    private final float[] styleVals = new float[NUM_STYLES];
    private final Activity activity;

    private int frameNum = 0;
    private StyleAdapter adapter;
    private int[] intValues;
    private float[] floatValues;
    private byte[][] yuvBytes;
    private Bitmap cropCopyBitmap;
    private Bitmap textureCopyBitmap;
    private boolean isComputing;
    private Handler handler;
    private HandlerThread handlerThread;

    public NeuralStyle(Activity activity) {
        inferenceInterface = new TensorFlowInferenceInterface(activity.getAssets(), MODEL_FILE);
        //adapter = new StyleAdapter();
        this.activity = activity;
        onResume();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    public synchronized void onResume() {
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public synchronized void onPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            handlerThread.quitSafely();
        }
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Bitmap stylize(Bitmap bitmap){
        Bitmap croppedBitmap = getCenterCropBitMap(bitmap);

        int size = croppedBitmap.getWidth() * croppedBitmap.getHeight();
        intValues = new int[size];
        floatValues = new float[size * 3];
        stylizeImage(croppedBitmap);
        return croppedBitmap;
    }

    private Bitmap getCenterCropBitMap(Bitmap bitmap) {
        int desiredSize = (int) Utility.min(bitmap.getWidth(), bitmap.getHeight(), MIN_SIZE);
        Bitmap cropBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);

        final Display display = activity.getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();

        Matrix cropTransform = ImageUtils.getTransformationMatrix(
                bitmap.getWidth(), bitmap.getHeight(),
                desiredSize, desiredSize,
                screenOrientation, true);

        final Canvas canvas = new Canvas(cropBitmap);
        canvas.drawBitmap(bitmap, cropTransform, null);

        return cropBitmap;
    }


    private void stylizeImage(final Bitmap bitmap) {
//
//
//        runInBackground(
//                new Runnable() {
//                    @Override
//                    public void run() {
        isComputing = false;

        ++frameNum;
        intValues = new int[bitmap.getWidth() * bitmap.getHeight()];
        floatValues = new float[bitmap.getWidth() * bitmap.getHeight() * 3];
        styleVals[0] = 1.0f;
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        if (DEBUG_MODEL) {
            // Create a white square that steps through a black background 1 pixel per frame.
            final int centerX = (frameNum + bitmap.getWidth() / 2) % bitmap.getWidth();
            final int centerY = bitmap.getHeight() / 2;
            final int squareSize = 10;
            for (int i = 0; i < intValues.length; ++i) {
                final int x = i % bitmap.getWidth();
                final int y = i / bitmap.getHeight();
                final float val =
                        Math.abs(x - centerX) < squareSize && Math.abs(y - centerY) < squareSize ? 1.0f : 0.0f;
                floatValues[i * 3] = val;
                floatValues[i * 3 + 1] = val;
                floatValues[i * 3 + 2] = val;
            }
        } else {
            for (int i = 0; i < intValues.length; ++i) {
                final int val = intValues[i];
                floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
                floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
                floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
            }
        }

        // TODO: Process the image in TensorFlow here.
        // Copy the input data into TensorFlow.
        inferenceInterface.feed(INPUT_NODE, floatValues,
                1, bitmap.getWidth(), bitmap.getHeight(), 3);
        inferenceInterface.feed(STYLE_NODE, styleVals, NUM_STYLES);
        Log.d("XXXXX", "" + Arrays.toString(styleVals));
        // Execute the output node's dependency sub-graph.
        inferenceInterface.run(new String[]{OUTPUT_NODE}, isDebug());

        // Copy the data from TensorFlow back into our array.
        inferenceInterface.fetch(OUTPUT_NODE, floatValues);

        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] =
                    0xFF000000
                            | (((int) (floatValues[i * 3] * 255)) << 16)
                            | (((int) (floatValues[i * 3 + 1] * 255)) << 8)
                            | ((int) (floatValues[i * 3 + 2] * 255));
        }

        bitmap.setPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

//                    }
//                });
    }

    private void renderDebug(final Canvas canvas) {
        // TODO(andrewharp): move result display to its own View instead of using debug overlay.
        final Bitmap texture = textureCopyBitmap;
        if (texture != null) {
            final Matrix matrix = new Matrix();
            final float scaleFactor =
                    DEBUG_MODEL
                            ? 4.0f
                            : Math.min(
                            (float) canvas.getWidth() / texture.getWidth(),
                            (float) canvas.getHeight() / texture.getHeight());
            matrix.postScale(scaleFactor, scaleFactor);
            canvas.drawBitmap(texture, matrix, new Paint());
        }

        if (!isDebug()) {
            return;
        }

        final Bitmap copy = cropCopyBitmap;
        if (copy == null) {
            return;
        }

        canvas.drawColor(0x55000000);

        final Matrix matrix = new Matrix();
        final float scaleFactor = 2;
        matrix.postScale(scaleFactor, scaleFactor);
        matrix.postTranslate(
                canvas.getWidth() - copy.getWidth() * scaleFactor,
                canvas.getHeight() - copy.getHeight() * scaleFactor);
        canvas.drawBitmap(copy, matrix, new Paint());

        final Vector<String> lines = new Vector<String>();

        // Add these three lines right here:
        final String[] statLines = inferenceInterface.getStatString().split("\n");
        Collections.addAll(lines, statLines);
        lines.add("");

//        lines.add("Frame: " + previewWidth + "x" + previewHeight);
//        lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
//        lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
//        lines.add("Rotation: " + sensorOrientation);
//        lines.add("Inference time: " + lastProcessingTimeMs + "ms");
//        lines.add("Desired size: " + desiredSize);
//        lines.add("Initialized size: " + initializedSize);
//
//        borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
    }

    public boolean isDebug() {
        return NCManager.isDebug;
    }

    private class StyleAdapter extends BaseAdapter {
        final ImageView[] items = new ImageView[NUM_STYLES];

        {
            for (int i = 0; i < NUM_STYLES; ++i) {
                Log.v(TAG, "Creating item "+ i);

                if (items[i] == null) {
                    final ImageView imageView = new ImageView(activity);
                    final Bitmap bm =
                            Utility.getBitmapFromAsset(activity, "www/assets/tensorflow/thumbnails/style" + i + ".jpg");
                    imageView.setImageBitmap(bm);

                    items[i] = imageView;
                }
            }
        }

        @Override
        public int getCount() {
            return NUM_STYLES;
        }

        @Override
        public Object getItem(final int position) {
            return items[position];
        }

        @Override
        public long getItemId(final int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            if (convertView != null) {
                return convertView;
            }
            return (View) getItem(position);
        }
    }
}
