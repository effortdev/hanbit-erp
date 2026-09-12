package egovframework.erp.hr;

import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.hr.domain.OrgType;
import egovframework.erp.hr.domain.OrgUnitVO;
import egovframework.erp.hr.mapper.OrgMapper;
import egovframework.erp.hr.repository.EmployeeRepository;
import egovframework.erp.hr.service.impl.OrgServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ADR-004(조직 계층 표현 방식)이 강제하는 "본부-팀 2단계 고정" 규칙 검증.
 */
@ExtendWith(MockitoExtension.class)
class OrgServiceImplTest {

    @Mock
    private OrgMapper orgMapper;
    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    void hq는_상위조직을_가질_수_없다() {
        OrgServiceImpl service = new OrgServiceImpl(orgMapper, employeeRepository);
        OrgUnitVO hq = unit("신설본부", OrgType.HQ, 1L);

        assertThrows(BusinessException.class, () -> service.registerOrgUnit(hq));
    }

    @Test
    void team은_상위조직이_필수다() {
        OrgServiceImpl service = new OrgServiceImpl(orgMapper, employeeRepository);
        OrgUnitVO team = unit("신설팀", OrgType.TEAM, null);

        assertThrows(BusinessException.class, () -> service.registerOrgUnit(team));
    }

    @Test
    void team의_상위조직은_반드시_본부여야_한다() {
        OrgServiceImpl service = new OrgServiceImpl(orgMapper, employeeRepository);
        OrgUnitVO anotherTeam = unit("다른팀", OrgType.TEAM, 11L);
        when(orgMapper.selectOrgUnit(11L)).thenReturn(unit("인사팀", OrgType.TEAM, 1L));

        assertThrows(BusinessException.class, () -> service.registerOrgUnit(anotherTeam));
    }

    @Test
    void 유효한_팀은_정상_등록된다() {
        OrgServiceImpl service = new OrgServiceImpl(orgMapper, employeeRepository);
        OrgUnitVO team = unit("신설팀", OrgType.TEAM, 1L);
        when(orgMapper.selectOrgUnit(1L)).thenReturn(unit("경영지원본부", OrgType.HQ, null));

        service.registerOrgUnit(team);

        verify(orgMapper).insertOrgUnit(team);
    }

    @Test
    void 유효한_본부는_정상_등록된다() {
        OrgServiceImpl service = new OrgServiceImpl(orgMapper, employeeRepository);
        OrgUnitVO hq = unit("신설본부", OrgType.HQ, null);

        service.registerOrgUnit(hq);

        verify(orgMapper).insertOrgUnit(hq);
        assertNull(hq.getParentId());
    }

    private static OrgUnitVO unit(String name, OrgType type, Long parentId) {
        OrgUnitVO vo = new OrgUnitVO();
        vo.setName(name);
        vo.setType(type);
        vo.setParentId(parentId);
        return vo;
    }
}
