package com.kcchen.nativecanvas.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.kcchen.nativecanvas.sticker.TextSticker;
import com.kcchen.penpal.R;

/**
 * Transparent Dialog Fragment, with no title and no background
 * <p>
 * The fragment imitates capturing input from keyboard, but does not display anything
 * the result from input from the keyboard is passed through {@link OnTextLayerListener}
 * <p>
 * Activity that uses {@link TextEditorDialogFragment} must implement {@link OnTextLayerListener}
 * <p>
 * If Activity does not implement {@link OnTextLayerListener}, exception will be thrown at Runtime
 */
public class TextEditorDialogFragment extends DialogFragment {
    private static final String TAG = TextEditorDialogFragment.class.getSimpleName();

    public static final String ARG_TEXT = "editor_text_arg";
    private static final String ARG_ID = "editor_text_id";

    protected EditText editText;

    private OnTextLayerListener textLayerListener;
    private String id = "";

    @Deprecated
    public TextEditorDialogFragment() {
        // empty, use getInstance
    }


    public static TextEditorDialogFragment getInstance(TextSticker textSticker) {
        @SuppressWarnings("deprecation")
        TextEditorDialogFragment fragment = new TextEditorDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, textSticker.getLayer().getText());
        args.putString(ARG_ID, textSticker.getID());
        fragment.setArguments(args);
        return fragment;
    }

//    public static TextEditorDialogFragment getInstance(String textValue) {
//        @SuppressWarnings("deprecation")
//        TextEditorDialogFragment fragment = new TextEditorDialogFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_TEXT, textValue);
//        args.putString(ARG_ID, "");
//        fragment.setArguments(args);
//        return fragment;
//    }

    public void setOnTextLayerListener(OnTextLayerListener listener){
        this.textLayerListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nc__text_editor_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        RelativeLayout container = view.findViewById(R.id.text_editor_root);
        editText = view.findViewById(R.id.edit_text_view);

        container.setBackgroundColor(0x33FF0000);
        editText.setBackgroundColor(0x3300FF00);

        Bundle args = getArguments();
        String text = "";
        if (args != null) {
            text = args.getString(ARG_TEXT);
            id = args.getString(ARG_ID);
        }


        initWithTextEntity(text);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (textLayerListener != null) {
                    textLayerListener.textChanged(id, s.toString());
                }
            }
        });

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // exit when clicking on background
                //Log.i(TAG,"> NCN text sticker dismiss");
                textLayerListener.onDismiss();
                dismiss();
            }
        });
    }

    private void initWithTextEntity(String text) {
        editText.setText(text);
        if (textLayerListener != null) {
            textLayerListener.textChanged(id, text);
        }
        editText.post(new Runnable() {
            @Override
            public void run() {
                if (editText != null) {
                    Selection.setSelection(editText.getText(), editText.length());
                }
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();

        // clearing memory on exit, cos manipulating with text uses bitmaps extensively
        // this does not frees memory immediately, but still can help
        System.gc();
        Runtime.getRuntime().gc();
    }

    @Override
    public void onDetach() {
        // release links
        this.textLayerListener = null;
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.requestWindowFeature(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // remove background
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

                // remove dim
                WindowManager.LayoutParams windowParams = window.getAttributes();
                window.setDimAmount(0.0F);
                window.setAttributes(windowParams);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        editText.post(new Runnable() {
            @Override
            public void run() {
                // force show the keyboard
                setEditText(true);
                editText.requestFocus();
                InputMethodManager ims = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                ims.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void setEditText(boolean gainFocus) {
        if (!gainFocus) {
            editText.clearFocus();
            editText.clearComposingText();
        }
        editText.setFocusableInTouchMode(gainFocus);
        editText.setFocusable(gainFocus);
    }

    /**
     * Callback that passes all user input through the method
     * {@link OnTextLayerListener#textChanged(String, String)}
     */
    public interface OnTextLayerListener {
        void textChanged(@NonNull String id, @NonNull String text);
        void onDismiss();
    }
}
