package io.github.anyzm.graph.ocean.dao.impl;

import com.vesoft.nebula.client.graph.data.DateTimeWrapper;
import io.github.anyzm.graph.ocean.dao.GraphValueFormatter;

import java.util.Calendar;
import java.util.Date;

public class DateTimeGraphValueFormatter implements GraphValueFormatter {

    public static final DateTimeGraphValueFormatter INSTANCE = new DateTimeGraphValueFormatter();

    @Override
    public Object format(Object oldValue) {
        if(null == oldValue) {
            return null;
        }
        Date date = (Date) oldValue;
        return "datetime(" + date.getTime()/1000 + ")";
    }

    @Override
    public Object reformat(Object nebulaValue) {
        if(null == nebulaValue) {
            return null;
        }
        DateTimeWrapper dateTimeWrapper = (DateTimeWrapper) nebulaValue;
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateTimeWrapper.getYear(), dateTimeWrapper.getMonth(), dateTimeWrapper.getDay(),
                dateTimeWrapper.getHour(), dateTimeWrapper.getMinute(), dateTimeWrapper.getSecond());
        return calendar.getTime();
    }
}
