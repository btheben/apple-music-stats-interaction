package com.example.modele;

import java.util.Date;

public interface DailyPlayCountProjection {
    Date getDay();
    Long getPlayCount();
}