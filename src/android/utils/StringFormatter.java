package com.kcchen.nativecanvas.utils;

import android.util.SparseIntArray;



public class StringFormatter {

    public static String formatTable(boolean isLineNumber, int column, String... string){
        if(Utility.isValid(string) && Utility.isValid(string.length)){
            int size = string.length;
            String out  = "";
            SparseIntArray columnSize = new SparseIntArray();
            for (int i = 0; i < string.length; i++) {
                int index = i % column;
                if(Utility.isValid(columnSize.get(index))){
                    if(columnSize.get(index) < string[i].length()){
                        columnSize.put(index, string[i].length());
                    }
                }else {
                    columnSize.put(index, string[i].length());
                }
            }
            if(isLineNumber){
                int sizeLength =  String.valueOf(size).length();
                for (int i = 0; i < string.length; i++) {
                    int countLength =  String.valueOf(i / column + 1).length();
                    int index = i % column;
                    if(index == 0){
                        out += (i / column + 1) + "." + Utility.getSpace(sizeLength - countLength + 2);
                    }
                    out += string[i] + Utility.getSpace(columnSize.get(index) - string[i].length() + 2);
                    if(index == column -1 && i != string.length - 1){
                        out += System.getProperty("line.separator");
                    }
                }
            }else {
                for (int i = 0; i < string.length; i++) {
                    int index = i % column;
                    out += string[i] + Utility.getSpace(columnSize.get(index) - string[i].length() + 2);
                    if(index == column -1 && i != string.length - 1){
                        out += System.getProperty("line.separator");
                    }
                }
            }
            return out;
        }else {
            return "";
        }
    }

}
