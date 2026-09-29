package com.example.modele;

import java.util.Date;

public interface PeriodPlayCountProjection {
    Date getPeriod();
    Long getPlayCount();
}