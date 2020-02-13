package com.ak.utils.timer;

import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Performance tuning timer
 */
public class PfTimer
{
    protected static final Logger log = Logger.getLogger(PfTimer.class);
    private String name;

    private long start;

    private static long recent;

    private boolean running; //to

    private static DecimalFormat format = new DecimalFormat("###,##0.000s");

    private static List<String> performanceList = new ArrayList<>();

    public PfTimer(Class clazz)
    {
        this.name = clazz.getName();
        init();
    }

    public PfTimer(String name)
    {
        this.name = name == null ? "UNDEFINED" : name;
        init();
    }

    public void record()
    {
        record("DEFAULT");
    }

    public void record(String action)
    {
        // Ccheck running status
        if (!running) return;

        long now = new Date().getTime();
        String result = "[" + name + "]" +
                (action == null ? "" : "[" + action + "] ") + "Elapsed: " + format.format(new Long(now - recent).floatValue() / 1000);
        log.debug(result);
        recent = now;
        performanceList.add(result);
    }

    public void start()
    {
        init();
        running = true;
        log.debug("[" + name + "]" + "Start timer ...");
    }

    public void reset()
    {
        recent = new Date().getTime();
        log.debug("[" + name + "]" + "Reset Timer ... ");
    }

    public void end()
    {
        if (!running)
        {
            log.warn("Timer has not started.");
            return;
        }

        String result = "Total Time Elapsed: " + format.format(new Long(new Date().getTime() - start).floatValue() / 1000);
        StringBuilder summary = new StringBuilder("[" + name + "]" + "Timer Ends.\nPerformance Summary : ");
        for (String record : performanceList)
        {
            summary.append("\n\t").append(record);
        }
        summary.append("\n").append(result);
        log.debug(summary.toString());
        start = 0;
        recent = 0;
        performanceList.clear();
        running = false;
    }

    private void init()
    {
        Date now = new Date();
        start = now.getTime();
        recent = start;
        performanceList.clear();
    }

    public static void main(String[] args) throws InterruptedException
    {
        PfTimer timer = new PfTimer("TEST");
        timer.start();
        for (int i = 0; i < 10; i++)
        {
            Thread.sleep(toLong(Math.random() * 1000));
            timer.record("Loop " + i);
        }
        timer.end();
    }

    public static Long toLong(Object val)
    {
        if (isEmpty(val))
        {
            return 0L;
        }
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) return Long.parseLong(val.toString());

        throw new IllegalArgumentException("Invalid object type for long conversion: " + val.getClass().getName());
    }

    public static boolean isEmpty(Object val)
    {
        if (val == null)
        {
            return true;
        }
        if (val instanceof String)
        {
            if ("".equals(val))
            {
                return true;
            }
        }
        if (val instanceof Collection)
        {
            if (((Collection) val).size() == 0)
            {
                return true;
            }
        }
        if (val instanceof Map)
        {
            if (((Map) val).size() == 0)
            {
                return true;
            }
        }
        return false;
    }
}
