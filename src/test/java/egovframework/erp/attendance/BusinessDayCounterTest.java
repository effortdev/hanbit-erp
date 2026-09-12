package egovframework.erp.attendance;

import egovframework.erp.attendance.domain.BusinessDayCounter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessDayCounterTest {

    @Test
    void 하루짜리_평일은_1일이다() {
        assertEquals(1, BusinessDayCounter.count(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 14))); // 월요일
    }

    @Test
    void 주말은_제외된다() {
        // 2026-09-12(토) ~ 2026-09-13(일)
        assertEquals(0, BusinessDayCounter.count(LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 13)));
    }

    @Test
    void 한주_전체는_5일이다() {
        // 2026-09-14(월) ~ 2026-09-20(일)
        assertEquals(5, BusinessDayCounter.count(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 20)));
    }
}
