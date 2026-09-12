-- ㈜한빛전자 미니 ERP 스키마
-- 모듈이 추가될 때마다 이 파일에 이어서 누적한다.

-- =========================================================
-- Module 1: 조직/사원관리 (egovframework.erp.hr)
-- =========================================================

CREATE TABLE IF NOT EXISTS org_unit (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(10)  NOT NULL COMMENT 'HQ(본부) | TEAM(팀)',
    parent_id   BIGINT NULL COMMENT '팀인 경우 소속 본부 id, 본부는 NULL',
    CONSTRAINT fk_org_unit_parent FOREIGN KEY (parent_id) REFERENCES org_unit (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS employee (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(50)  NOT NULL,
    position     VARCHAR(20)  NOT NULL COMMENT '사원|대리|과장|차장|팀장|본부장|대표이사',
    org_unit_id  BIGINT NOT NULL,
    hire_date    DATE NOT NULL,
    status       VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|LEFT',
    CONSTRAINT fk_employee_org_unit FOREIGN KEY (org_unit_id) REFERENCES org_unit (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 발령 이력: append-only (update/delete 매퍼를 두지 않음 — docs/adr/ADR-003 참고)
CREATE TABLE IF NOT EXISTS emp_history (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id         BIGINT NOT NULL,
    change_type         VARCHAR(20) NOT NULL COMMENT 'TRANSFER(부서이동)|PROMOTION(승진)',
    before_org_unit_id  BIGINT NULL,
    after_org_unit_id   BIGINT NULL,
    before_position     VARCHAR(20) NULL,
    after_position      VARCHAR(20) NULL,
    changed_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by          VARCHAR(50) NOT NULL,
    memo                VARCHAR(200) NULL,
    CONSTRAINT fk_emp_history_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 조직도(요구사항 문서 기준) 시드 데이터
INSERT INTO org_unit (id, name, type, parent_id) VALUES
  (1, '경영지원본부', 'HQ', NULL),
  (2, '영업본부',     'HQ', NULL),
  (3, '물류본부',     'HQ', NULL),
  (11, '인사팀',   'TEAM', 1),
  (12, '회계팀',   'TEAM', 1),
  (13, '총무팀',   'TEAM', 1),
  (21, '국내영업팀', 'TEAM', 2),
  (22, '해외영업팀', 'TEAM', 2),
  (31, '구매팀',     'TEAM', 3),
  (32, '재고관리팀', 'TEAM', 3),
  (33, '생산관리팀', 'TEAM', 3);
