package com.kcchen.nativecanvas.colorpicker.builder;


import com.kcchen.nativecanvas.colorpicker.ColorPickerView;
import com.kcchen.nativecanvas.colorpicker.renderer.ColorWheelRenderer;
import com.kcchen.nativecanvas.colorpicker.renderer.FlowerColorWheelRenderer;
import com.kcchen.nativecanvas.colorpicker.renderer.SimpleColorWheelRenderer;

public class ColorWheelRendererBuilder {
	public static ColorWheelRenderer getRenderer(ColorPickerView.WHEEL_TYPE wheelType) {
		switch (wheelType) {
			case CIRCLE:
				return new SimpleColorWheelRenderer();
			case FLOWER:
				return new FlowerColorWheelRenderer();
		}
		throw new IllegalArgumentException("wrong WHEEL_TYPE");
	}
}
