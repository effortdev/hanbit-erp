package egovframework.erp.hr;

import egovframework.erp.hr.domain.ChangeType;
import egovframework.erp.hr.domain.Employee;
import egovframework.erp.hr.domain.EmpHistoryVO;
import egovframework.erp.hr.domain.OrgUnitVO;
import egovframework.erp.hr.domain.Position;
import egovframework.erp.hr.mapper.EmpHistoryMapper;
import egovframework.erp.hr.mapper.OrgMapper;
import egovframework.erp.hr.repository.EmployeeRepository;
import egovframework.erp.hr.service.impl.EmpServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * docs/adr/ADR-003: 발령(transfer)은 JPA(Employee 변경)와 MyBatis(EmpHistory insert)가
 * 하나의 서비스 메서드(@Transactional) 안에서 함께 호출되는지를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class EmpServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmpHistoryMapper empHistoryMapper;
    @Mock
    private OrgMapper orgMapper;

    @Test
    void 발령시_사원소속변경과_이력기록이_함께_일어난다() {
        EmpServiceImpl service = new EmpServiceImpl(employeeRepository, empHistoryMapper, orgMapper);
        Employee employee = new Employee("홍길동", Position.STAFF, 11L, LocalDate.of(2020, 1, 1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(orgMapper.selectOrgUnit(21L)).thenReturn(new OrgUnitVO());

        service.transfer(1L, 21L, "인사팀 김철수");

        assertEquals(21L, employee.getOrgUnitId()); // JPA: dirty checking 대상 변경 확인

        ArgumentCaptor<EmpHistoryVO> captor = ArgumentCaptor.forClass(EmpHistoryVO.class);
        org.mockito.Mockito.verify(empHistoryMapper).insertHistory(captor.capture());
        EmpHistoryVO history = captor.getValue();
        assertEquals(ChangeType.TRANSFER, history.getChangeType());
        assertEquals(11L, history.getBeforeOrgUnitId());
        assertEquals(21L, history.getAfterOrgUnitId());
        assertEquals("인사팀 김철수", history.getChangedBy());
    }
}
