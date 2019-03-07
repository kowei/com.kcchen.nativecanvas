package com.kcchen.nativecanvas.colorpicker.renderer;


import com.kcchen.nativecanvas.colorpicker.ColorCircle;

import java.util.List;

public interface ColorWheelRenderer {
	float GAP_PERCENTAGE = 0.025f;

	void draw();

	ColorWheelRenderOption getRenderOption();

	void initWith(ColorWheelRenderOption colorWheelRenderOption);

	List<ColorCircle> getColorCircleList();
}
