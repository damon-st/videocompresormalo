package com.damon.videocompress.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgo {
    public  String getTimeAgo(long duration){
        Date now = new Date();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime()-duration);
        long minutes =  TimeUnit.MILLISECONDS.toMinutes(now.getTime()-duration);
        long hours =  TimeUnit.MILLISECONDS.toHours(now.getTime()-duration);
        long days =  TimeUnit.MILLISECONDS.toDays(now.getTime()-duration);

        if (seconds < 60){
            return " justo ahora";
        }else if (minutes == 1){
            return " hace un minuto atras";
        }else if (minutes > 1 && minutes < 60){
            return minutes + " minutos atras";
        }else if (hours ==1){
            return " hace una hora";
        }else if (hours > 1 && hours < 24){
            return  hours + " horas atras";
        }else if (days ==1){
            return  " hace un dia";
        }else {
            return days + " dias atras";
        }
    }
}