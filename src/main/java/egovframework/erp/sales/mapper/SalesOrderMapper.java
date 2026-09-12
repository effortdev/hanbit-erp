package egovframework.erp.sales.mapper;

import egovframework.erp.sales.domain.SalesOrderVO;
import egovframework.erp.sales.domain.SalesSummaryVO;

import java.util.List;

public interface SalesOrderMapper {

    void insertOrder(SalesOrderVO order);

    SalesOrderVO selectById(Long id);

    List<SalesOrderVO> selectByEmployeeId(Long employeeId);

    /** REGISTERED -> CONFIRMED로 전환하며 confirmed_at을 채운다 (docs/adr/ADR-018). */
    void confirmOrder(Long id);

    /** FR-6-4: CONFIRMED 수주를 조직×연월로 집계 (docs/adr/ADR-018 — 별도 매출 테이블 없이 파생). */
    List<SalesSummaryVO> selectMonthlySummary();
}
