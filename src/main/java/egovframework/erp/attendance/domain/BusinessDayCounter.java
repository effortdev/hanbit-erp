package egovframework.erp.attendance.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;

/** 토/일을 제외한 평일 일수를 센다. 공휴일 캘린더는 이번 범위에서 다루지 않는다. */
public final class BusinessDayCounter {

    private BusinessDayCounter() {
    }

    public static int count(LocalDate start, LocalDate end) {
        int days = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days++;
            }
        }
        return days;
    }
}
