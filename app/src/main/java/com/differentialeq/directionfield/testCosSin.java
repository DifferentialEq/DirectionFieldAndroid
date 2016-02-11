package com.differentialeq.directionfield;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by eliselkin on 11/19/15.
 */
public class testCosSin {

    public static void main(String[] args){
        String simpleFormat = "yyyy-MM-dd-HH-mm-ss";
        SimpleDateFormat sdf = new SimpleDateFormat(simpleFormat, Locale.US);
        System.out.println(sdf.format(new Date()).toString());
    }

}
