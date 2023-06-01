package io.github.anyzm.graph.ocean.dao.impl;

import com.vesoft.nebula.client.graph.data.DateWrapper;
import io.github.anyzm.graph.ocean.dao.GraphValueFormatter;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Calendar;
import java.util.Date;

public class DateGraphValueFormatter implements GraphValueFormatter {

    public static final DateGraphValueFormatter INSTANCE = new DateGraphValueFormatter();

    @Override
    public Object format(Object oldValue) {
        if(null == oldValue) {
            return null;
        }
        Date date = (Date) oldValue;
        return "date(" + DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(date) + ")";
    }

    @Override
    public Object reformat(Object nebulaValue) {
        if(null == nebulaValue) {
            return null;
        }
        DateWrapper dateWrapper = (DateWrapper) nebulaValue;
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateWrapper.getYear(), dateWrapper.getMonth(), dateWrapper.getDay(), 0, 0, 0);
        return calendar.getTime();
    }
}
