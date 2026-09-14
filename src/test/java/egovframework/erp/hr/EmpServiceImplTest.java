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
import egovframework.erp.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * docs/adr/ADR-003: 발령(transfer)은 JPA(Employee 변경)와 MyBatis(EmpHistory insert)가
 * 하나의 서비스 메서드(@Transactional) 안에서 함께 호출되는지를 검증한다.
 * docs/adr/ADR-019: 발령 처리는 팀장급 이상만 할 수 있어야 한다 — 사원 계정으로도 발령
 * 처리가 가능했던 권한 누락(일반 사원의 자기 승진/자기 부서이동 악용 가능성)을 재현/회귀 방지한다.
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

        service.transfer(1L, 21L, "인사팀 김철수", Position.TEAM_LEADER);

        assertEquals(21L, employee.getOrgUnitId()); // JPA: dirty checking 대상 변경 확인

        ArgumentCaptor<EmpHistoryVO> captor = ArgumentCaptor.forClass(EmpHistoryVO.class);
        verify(empHistoryMapper).insertHistory(captor.capture());
        EmpHistoryVO history = captor.getValue();
        assertEquals(ChangeType.TRANSFER, history.getChangeType());
        assertEquals(11L, history.getBeforeOrgUnitId());
        assertEquals(21L, history.getAfterOrgUnitId());
        assertEquals("인사팀 김철수", history.getChangedBy());
    }

    @Test
    void 사원은_부서이동_발령을_처리할_수_없다() {
        EmpServiceImpl service = new EmpServiceImpl(employeeRepository, empHistoryMapper, orgMapper);

        assertThrows(BusinessException.class,
                () -> service.transfer(1L, 21L, "최사원", Position.STAFF));

        verify(employeeRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
        verify(empHistoryMapper, never()).insertHistory(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 사원은_승진_발령을_처리할_수_없다() {
        EmpServiceImpl service = new EmpServiceImpl(employeeRepository, empHistoryMapper, orgMapper);

        // 자기 자신을 대표이사로 승진시키는 시도 — ApprovalLineResolverImpl.requireSingleCeo()의
        // "대표이사는 정확히 1명" 불변조건을 깨는 악용 시나리오(docs/adr/ADR-019)와 동일한 입력이지만,
        // 권한 검증에서 먼저 막혀 Employee/이력 갱신 자체가 일어나지 않아야 한다.
        assertThrows(BusinessException.class,
                () -> service.promote(1L, Position.CEO, "최사원", Position.STAFF));

        verify(employeeRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
        verify(empHistoryMapper, never()).insertHistory(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 팀장급_이상은_승진_발령을_처리할_수_있다() {
        EmpServiceImpl service = new EmpServiceImpl(employeeRepository, empHistoryMapper, orgMapper);
        Employee employee = new Employee("홍길동", Position.STAFF, 11L, LocalDate.of(2020, 1, 1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        service.promote(1L, Position.ASSISTANT_MANAGER, "이인사", Position.TEAM_LEADER);

        assertEquals(Position.ASSISTANT_MANAGER, employee.getPosition());
        verify(empHistoryMapper).insertHistory(org.mockito.ArgumentMatchers.any());
    }
}
