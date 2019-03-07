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


import com.kcchen.nativecanvas.svg.css.util.SMap;

public class SiblingElementMatcher extends ElementMatcher {

	private ElementMatcher prev;

	private ElementMatcher curr;

	boolean prevMatched;

	private SparseStack state = new SparseStack();

	public SiblingElementMatcher(Selector selector, ElementMatcher prev, ElementMatcher curr) {
		super(selector);
		this.prev = prev;
		this.curr = curr;
	}

	public void popElement() {
		prev.popElement();
		curr.popElement();
		prevMatched = state.pop() != null;
	}

	public MatchResult pushElement(String ns, String name, SMap attrs) {
		MatchResult p = prev.pushElement(ns, name, attrs);
		if( p != null && p.getPseudoElement() != null )
			return null; // something illegal like foo:first-line + bar
		state.push(p);
		MatchResult c = curr.pushElement(ns, name, attrs);
		if( !prevMatched )
			c = null;
		prevMatched = false;
		return c;
	}

}
