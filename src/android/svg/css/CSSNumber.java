/*
	SVG Kit for Android library
    Copyright (C) 2015 SCAND Ltd, svg@scand.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.kcchen.nativecanvas.svg.css;

import java.io.PrintWriter;

public class CSSNumber extends CSSValue {

	private Number number;

	public CSSNumber(Number number) {
		this.number = number;
	}

	public CSSNumber(int number) {
		this.number = new Integer(number);
	}

	public CSSNumber(double number) {
		this.number = new Double(number);
	}

	public Number getNumber() {
		return number;
	}

	public void serialize(PrintWriter out) {
		out.print(number);
	}

	public String toString() {
		return number.toString();
	}

	public boolean equals(Object other) {
		if (other.getClass() != getClass())
			return false;
		return ((CSSNumber) other).number.equals(number);
	}

	public int hashCode() {
		return number.hashCode();
	}
}
