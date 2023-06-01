package io.github.anyzm.graph.ocean.dao.impl;

import io.github.anyzm.graph.ocean.dao.GraphValueFormatter;

import java.util.Date;

public class TimeStampGraphValueFormatter implements GraphValueFormatter {

    public static final TimeStampGraphValueFormatter INSTANCE = new TimeStampGraphValueFormatter();

    @Override
    public Object format(Object oldValue) {
        if(null == oldValue) {
            return null;
        }
        Date date = (Date) oldValue;
        return "timestamp(" + date.getTime()/1000 + ")";
    }

    @Override
    public Object reformat(Object nebulaValue) {
        if(null == nebulaValue) {
            return null;
        }
        return new Date((Long) nebulaValue * 1000);
    }
}
