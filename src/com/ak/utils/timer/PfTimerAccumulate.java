package com.ak.utils.timer;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Performance Tuning timer to accumulate times based on a name
 * Created by Andrew.Kozak on 1/29/2019.
 */
public class PfTimerAccumulate
{
    protected static final Logger log = Logger.getLogger(PfTimerAccumulate.class);

    private Map<String, Long> accumulativeTimerMap = new HashMap<>();

    // Contains Timer name with Start time for Timer
    private Map<String, Long> currentTimerMap = new HashMap<>();
    private Map<String, Integer> accumulativeTimerCounterMap = new HashMap<>();

    private static DecimalFormat format = new DecimalFormat("###,##0.000s");

    // Qty break for logging time info. I.e. 100 => output info for every 100 times recorded
    // Note: If not using the Qty Break should just use PfTimer class
    private int qtyBreak = 0;

    // Defaults
    private Priority priority = Priority.DEBUG;
    private org.jboss.logging.Logger.Level level = org.jboss.logging.Logger.Level.DEBUG;

    public void setQtyBreak(int qtyBreak)
    {
        this.qtyBreak = qtyBreak;
    }

    /**
     * Start timer for given name
     * @param name Timer Name
     */
    public void start(String name)
    {
        if (currentTimerMap.containsKey(name))
        {
            log.warn("Timer already started for " + name);
        }
        else
        {
            currentTimerMap.put(name, new Date().getTime());
        }
    }

    /**
     * Add time in milliseconds to a given Timer
     * @param name Timer Name
     * @param time Elapsed Time in millis from start for Timer Name
     */
    protected void addTime(String name, Long time)
    {
        if (qtyBreak > 0)
        {
            incrementCounter(name);
        }
        // Not really accumulating. Just a time from the Timer start
            accumulativeTimerMap.put(name, time);
        }

    /**
     * Increment counter for Accumulative Timer
     * @param name Accumulative Timer Name
     */
    protected void incrementCounter(String name)
    {
            if (accumulativeTimerCounterMap.containsKey(name))
            {
                accumulativeTimerCounterMap.put(name, accumulativeTimerCounterMap.get(name) + 1);
            }
            else
            {
                accumulativeTimerCounterMap.put(name, 1);
            }
        }

    /**
     * Record time for a given Timer name and accumulate total time for this Timer Name
     * Also output timings and reset timer if a qty break is reached
     * @param name Timer Name
     */
    public void record(String name)
    {
        if (currentTimerMap.containsKey(name))
        {
            long now = new Date().getTime();
            Long elapsedTime = now - currentTimerMap.get(name);
//            log.log(priority, "elapsedTime is: " + format.format(elapsedTime.floatValue() / 1000));
            addTime(name, elapsedTime);

            if (qtyBreak > 0)
            {
            // Check for Qty Break
                if (qtyBreakCheck(name))
                {
                    // Clear Base Timer so we can determine elapsedTime for the next recording
                    currentTimerMap.put(name, now);
                }
            }
        }
        else
        {
            log.warn("Timer has not started for " + name);
        }
    }

    /**
     * Check for a Qty Break and output timings if a Qty Break is hit
     * @param name Timer Name
     * @return true if a Qty Break is hit
     */
    protected boolean qtyBreakCheck(String name)
    {
        boolean bReturn = false;

        if (qtyBreak > 0)
        {
            int iCounter = accumulativeTimerCounterMap.containsKey(name) ? accumulativeTimerCounterMap.get(name) : 0;
            if (qtyBreak == iCounter)
            {
                long lAccTime = accumulativeTimerMap.containsKey(name) ? accumulativeTimerMap.get(name) : 0L;
                log.log(priority, "Time Accumulated for [" + name + "] for "  + qtyBreak + " recordings is: " + format.format(new Long(lAccTime).floatValue()/1000));

                // Reset
                accumulativeTimerCounterMap.remove(name);
                accumulativeTimerMap.remove(name);
                bReturn = true;
            }
        }

        return bReturn;
    }

    /**
     * End the Timer and log all of the recorded names with their times
     */
    public void end()
    {
            StringBuilder sb = new StringBuilder("Accumulate Timer Ends.\nAccumulated Times : ");

        // Loop through all Timers
        for (String sTimerName : currentTimerMap.keySet())
        {
            // Qty Break mode
            if (qtyBreak > 0)
            {
                if (accumulativeTimerMap.containsKey(sTimerName))
            {
                sb.append("\n\t");
                    sb.append("Total Time Accumulated for [").append(sTimerName).append("]");

                    int iCounter = accumulativeTimerCounterMap.containsKey(sTimerName) ? accumulativeTimerCounterMap.get(sTimerName) : 0;
                    if (iCounter > 0)
                    {
                        sb.append(" for last ").append(iCounter).append(" recordings");
            }

                    sb.append(" is: ").append(format.format(accumulativeTimerMap.get(sTimerName).floatValue() / 1000));
                }
                else
                {
                    sb.append("No time recorded for [").append(sTimerName).append("]");
                }
            }
            // No Qty Break (Just a time from start to last recording)
            else
            {
                sb.append("\n\t");
                sb.append("Total Time Accumulated for [").append(sTimerName).append("] is: ").append(format.format(accumulativeTimerMap.get(sTimerName).floatValue() / 1000));
            }
        }

            log.log(priority, sb.toString());

            accumulativeTimerMap.clear();
            accumulativeTimerCounterMap.clear();
            currentTimerMap.clear();
        }

    public Priority getPriority()
    {
        return priority;
    }

    /**
     * Set the Priority for the Timer log output (Default is DEBUG)
     * @param priority Priority
     */
    public void setPriority(Priority priority)
    {
        if (priority != null)
        {
            this.priority = priority;
        }
    }

    public org.jboss.logging.Logger.Level getLevel()
    {
        return level;
    }

    public void setLevel(org.jboss.logging.Logger.Level level)
    {
        this.level = level;
    }

}
