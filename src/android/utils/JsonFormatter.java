/**
 * <pre><font size=2>
 * Project Name:superUaAndroid
 * File Name:JsonFormatter.java
 * Package Name:com.datamite.superua.util
 * Date:2013/10/3下午3:54:03
 * Copyright (c) 2013, kc.chen@datamitetek.com All Rights Reserved.
 * </pre>
*/

package com.kcchen.nativecanvas.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * ClassName:JsonFormatter <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2013/10/3 下午3:54:03 <br/>
 * @author   "KC Chen(kc.chen@datamitetek.com)"
 * @version
 * @since    JDK 1.6
 * @see
 */
public class JsonFormatter{

    public static String format(final JSONObject object) throws JSONException{
        if (object == null) return "";
        final JsonVisitor visitor = new JsonVisitor(4, ' ');
        visitor.visit(object, 0);
        return visitor.toString();
    }

    public static String format(final JSONArray object) throws JSONException{
        if (object == null) return "";
        final JsonVisitor visitor = new JsonVisitor(4, ' ');
        visitor.visit(object, 0);
        return visitor.toString();
    }

    public static String formatObject(final String jsonObjectString) throws JSONException{
        JSONObject object = new JSONObject(jsonObjectString);
        final JsonVisitor visitor = new JsonVisitor(4, ' ');
        visitor.visit(object, 0);
        return visitor.toString();
    }

    public static String formatArray(final String jsonArrayString) throws JSONException{
        JSONArray array = new JSONArray(jsonArrayString);
        final JsonVisitor visitor = new JsonVisitor(4, ' ');
        visitor.visit(array, 0);
        return visitor.toString();
//        String outString = "[\n";
//        for (int i = 0; i < array.length(); i++) {
//            JSONObject object = array.getJSONObject(i);
//            final JsonVisitor visitor = new JsonVisitor(4, ' ');
//            visitor.visit(object, 0);
//            if(i > 0) outString += ",\n";
//            outString += visitor.toString();
//        }
//        return outString + "]\n";
    }

    private static class JsonVisitor{

        private final StringBuilder builder = new StringBuilder();
        private final char indentationChar;
        private final int indentationSize;

        public JsonVisitor(final int indentationSize, final char indentationChar){
            this.indentationSize = indentationSize;
            this.indentationChar = indentationChar;
        }

        private void visit(final JSONArray array, final int indent) throws JSONException{
            final int length = array.length();
            if(length == 0){
                write("[]", indent);
            } else{
                write("[", indent);
                for(int i = 0; i < length; i++){
                    visit(array.get(i), indent + 1, indent + 1);
                    if(i + 1 < length){
                        write(",", indent + 1);
                    }
                }
                write("]", indent);
            }

        }

        private void visit(final JSONObject obj, final int indent) throws JSONException{
            final int length = obj.length();
            if(length == 0){
                write("{}", indent);
            } else{
                write("{", indent);
                final Iterator<String> keys = obj.keys();
                while(keys.hasNext()){
                    final String key = keys.next();
                    write("\"" + key + "\" :", indent + 1);
                    visit(obj.get(key), indent + 1, 0);
                    if(keys.hasNext()){
                        write(",", 0);
                    }
                }
                write("}", indent);
            }

        }

        private void visit(final Object object, final int indent, final int stringLeadingSpace) throws JSONException{
            if(object instanceof JSONArray){
                visit((JSONArray) object, indent);
            } else if(object instanceof JSONObject){
                visit((JSONObject) object, indent);
            } else{
                if(object instanceof String){
                    write("\"" + (String) object + "\"", stringLeadingSpace);
                } else{
                    write(String.valueOf(object), stringLeadingSpace);
                }
            }

        }

        private void write(final String data, final int indent){
//            for(int i = 0; i < (indent * indentationSize); i++){
//                builder.append(indentationChar);
//            }
            if(data.equals(",")){
                builder.append(data);
                builder.append(System.getProperty("line.separator"));
            }else if(data.equals("{") || data.equals("[")){
                builder.append(System.getProperty("line.separator"));
                builder.append(getIndent(indent));
                builder.append(data);
                builder.append(System.getProperty("line.separator"));
            }else if(data.equals("}") || data.equals("]")){
                builder.append(System.getProperty("line.separator"));
                builder.append(getIndent(indent));
                builder.append(data);
            }else {
                builder.append(getIndent(indent));
                builder.append(data);
            }
        }

        private String getIndent(int indent) {
            if(indent > 0)
                return String.format("%0" + (indent * indentationSize) + "d", 0).replace("0", "" + indentationChar);
            else
                return "";
        }

        @Override
        public String toString(){
            return builder.toString();
        }

    }

}
