-- ㈜한빛전자 미니 ERP 스키마
-- 모듈이 추가될 때마다 이 파일에 이어서 누적한다.

-- =========================================================
-- Module 1: 조직/사원관리 (egovframework.erp.hr)
-- =========================================================

CREATE TABLE IF NOT EXISTS org_unit (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    type                VARCHAR(10)  NOT NULL COMMENT 'HQ(본부) | TEAM(팀)',
    parent_id           BIGINT NULL COMMENT '팀인 경우 소속 본부 id, 본부는 NULL',
    leader_employee_id  BIGINT NULL COMMENT '이 조직의 결재권자(팀장/본부장). position만으로는 유일하게 특정할 수 없어 도입 — docs/adr/ADR-006',
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

-- org_unit -> employee는 employee 테이블이 만들어진 뒤에야 FK를 걸 수 있다 (순환 참조 회피)
ALTER TABLE org_unit ADD CONSTRAINT fk_org_unit_leader FOREIGN KEY (leader_employee_id) REFERENCES employee (id);

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

-- 결재라인 데모를 위한 최소 인원 시드 (대표이사/본부장/팀장/사원)
-- 대표이사는 조직도상 상위 조직이 없어 인사관리 편의상 경영지원본부 소속으로 등록하지만,
-- 결재라인 판별 시에는 org_unit_id가 아니라 position=CEO 단일 조회로 처리한다 (docs/adr/ADR-006).
INSERT INTO employee (id, name, position, org_unit_id, hire_date) VALUES
  (1, '김대표', 'CEO',            1,  '2015-01-02'),
  (2, '박본부', 'DIVISION_HEAD',  1,  '2016-03-02'),
  (3, '이인사', 'TEAM_LEADER',    11, '2017-03-02'),
  (4, '최사원', 'STAFF',          11, '2023-07-01'),
  (5, '정영업', 'DIVISION_HEAD',  2,  '2016-03-02'),
  (6, '강국내', 'TEAM_LEADER',    21, '2018-05-01'),
  (7, '윤물류', 'DIVISION_HEAD',  3,  '2016-03-02'),
  (8, '한구매', 'TEAM_LEADER',    31, '2018-05-01');

UPDATE org_unit SET leader_employee_id = 2 WHERE id = 1;  -- 경영지원본부장 박본부
UPDATE org_unit SET leader_employee_id = 3 WHERE id = 11; -- 인사팀장 이인사
UPDATE org_unit SET leader_employee_id = 5 WHERE id = 2;  -- 영업본부장 정영업
UPDATE org_unit SET leader_employee_id = 6 WHERE id = 21; -- 국내영업팀장 강국내
UPDATE org_unit SET leader_employee_id = 7 WHERE id = 3;  -- 물류본부장 윤물류
UPDATE org_unit SET leader_employee_id = 8 WHERE id = 31; -- 구매팀장 한구매

-- =========================================================
-- Module 2: 전자결재 (egovframework.erp.approval / security)
-- =========================================================

-- 로그인 자격증명은 사원 마스터와 분리한다 (docs/adr/ADR-005)
CREATE TABLE IF NOT EXISTS account (
    employee_id    BIGINT PRIMARY KEY,
    username       VARCHAR(50) NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    enabled        TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_account_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 데모 계정: 전원 비밀번호 erp1234! (BCrypt) — 실제 서비스라면 초기 비밀번호 발급/재설정 절차 필요
INSERT INTO account (employee_id, username, password_hash) VALUES
  (1, 'ceo',    '$2a$10$oT5yKBCJ31mKkzTXgJ6bvuAQj0jsKjXWvfkOww61KiVo2TtHuF0JG'),
  (2, 'hqhead1','$2a$10$oT5yKBCJ31mKkzTXgJ6bvuAQj0jsKjXWvfkOww61KiVo2TtHuF0JG'),
  (3, 'leader1','$2a$10$oT5yKBCJ31mKkzTXgJ6bvuAQj0jsKjXWvfkOww61KiVo2TtHuF0JG'),
  (4, 'staff1', '$2a$10$oT5yKBCJ31mKkzTXgJ6bvuAQj0jsKjXWvfkOww61KiVo2TtHuF0JG'),
  (5, 'hqhead2','$2a$10$oT5yKBCJ31mKkzTXgJ6bvuAQj0jsKjXWvfkOww61KiVo2TtHuF0JG'),
  (6, 'leader2','$2a$10$oT5yKBCJ31mKkzTXgJ6bvuAQj0jsKjXWvfkOww61KiVo2TtHuF0JG'),
  (7, 'hqhead3','$2a$10$oT5yKBCJ31mKkzTXgJ6bvuAQj0jsKjXWvfkOww61KiVo2TtHuF0JG'),
  (8, 'leader3','$2a$10$oT5yKBCJ31mKkzTXgJ6bvuAQj0jsKjXWvfkOww61KiVo2TtHuF0JG');

-- "몇 단계/어떤 조건"을 정의하는 결재라인 규칙 (docs/adr/ADR-006). "누가"는 실시간 조직도 조회로 채운다.
CREATE TABLE IF NOT EXISTS approval_line_rule (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type    VARCHAR(20) NOT NULL COMMENT 'VACATION|EXPENSE|PURCHASE|GENERAL',
    step_order       INT NOT NULL,
    approver_level   VARCHAR(20) NOT NULL COMMENT 'TEAM_LEADER|DIVISION_HEAD|CEO',
    condition_expr   VARCHAR(50) NULL COMMENT 'NULL=항상 포함, AMOUNT_GTE_1M|AMOUNT_GTE_5M=금액 조건부(ADR-011)',
    UNIQUE KEY uq_rule_type_step (document_type, step_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO approval_line_rule (document_type, step_order, approver_level, condition_expr) VALUES
  ('VACATION', 1, 'TEAM_LEADER',   NULL),
  -- 금액 구간별 결재라인 (docs/adr/ADR-011) — 이전엔 예산 소진율(BUDGET_80_EXCEEDED) 조건이었으나 폐기(docs/adr/ADR-012)
  ('EXPENSE',  1, 'TEAM_LEADER',   NULL),
  ('EXPENSE',  2, 'DIVISION_HEAD', 'AMOUNT_GTE_1M'),
  ('EXPENSE',  3, 'CEO',           'AMOUNT_GTE_5M'),
  ('PURCHASE', 1, 'TEAM_LEADER',   NULL),
  ('PURCHASE', 2, 'DIVISION_HEAD', NULL),
  ('PURCHASE', 3, 'CEO',           NULL),
  ('GENERAL',  1, 'TEAM_LEADER',   NULL),
  ('GENERAL',  2, 'DIVISION_HEAD', NULL);

-- 문서 봉투: 유형별 전용 필드(amount/start_date/end_date)는 필요한 유형에서만 채운다.
-- 품목/수량 같은 구매요청서의 세부 항목은 Module 5(재고/구매관리)가 자체 테이블로 확장할 예정.
CREATE TABLE IF NOT EXISTS approval_document (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type  VARCHAR(20) NOT NULL COMMENT 'VACATION|EXPENSE|PURCHASE|GENERAL',
    title          VARCHAR(200) NOT NULL,
    content        VARCHAR(1000) NULL,
    amount         DECIMAL(15,2) NULL COMMENT 'EXPENSE/PURCHASE 전용',
    start_date     DATE NULL COMMENT 'VACATION 전용',
    end_date       DATE NULL COMMENT 'VACATION 전용',
    drafter_id     BIGINT NOT NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_document_drafter FOREIGN KEY (drafter_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 결재라인 정의: 문서 상신 시점에 1회 계산되어 고정된다(이후 발령이 나도 바뀌지 않음 — docs/adr/ADR-006)
CREATE TABLE IF NOT EXISTS approval_step (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id          BIGINT NOT NULL,
    step_order           INT NOT NULL,
    approver_level       VARCHAR(20) NOT NULL,
    approver_employee_id BIGINT NOT NULL,
    CONSTRAINT fk_approval_step_document FOREIGN KEY (document_id) REFERENCES approval_document (id),
    CONSTRAINT fk_approval_step_approver FOREIGN KEY (approver_employee_id) REFERENCES employee (id),
    UNIQUE KEY uq_step_document_order (document_id, step_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 승인/반려 행위 로그: append-only (update/delete 매퍼를 두지 않음 — Module 1 emp_history와 동일한 패턴, NFR-2-3)
CREATE TABLE IF NOT EXISTS approval_action (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    step_id     BIGINT NOT NULL,
    action      VARCHAR(10) NOT NULL COMMENT 'APPROVE|REJECT',
    comment     VARCHAR(500) NULL,
    acted_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_action_step FOREIGN KEY (step_id) REFERENCES approval_step (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- Module 3: 근태관리 (egovframework.erp.attendance)
-- =========================================================

-- 근속연수별 연차 부여일수 규칙 (docs/adr/ADR-009). "1년 미만 11일, 이후 매년 +1일, 상한 25일"
CREATE TABLE IF NOT EXISTS vacation_policy (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    min_years    INT NOT NULL,
    max_years    INT NULL COMMENT 'NULL = 상한 없음(그 이상 전부 해당)',
    annual_days  INT NOT NULL,
    UNIQUE KEY uq_vacation_policy_min_years (min_years)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO vacation_policy (min_years, max_years, annual_days) VALUES
  (0, 0, 11),
  (1, 1, 12),
  (2, 2, 13),
  (3, 3, 14),
  (4, 4, 15),
  (5, 5, 16),
  (6, 6, 17),
  (7, 7, 18),
  (8, 8, 19),
  (9, 9, 20),
  (10, 10, 21),
  (11, 11, 22),
  (12, 12, 23),
  (13, 13, 24),
  (14, NULL, 25);

-- 휴가신청: 전자결재 문서와 1:1 연결 (docs/adr/ADR-008). days는 신청 시점 스냅샷(docs/adr/ADR-009).
CREATE TABLE IF NOT EXISTS attendance_vacation_request (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id            BIGINT NOT NULL,
    start_date             DATE NOT NULL,
    end_date               DATE NOT NULL,
    days                   INT NOT NULL,
    reason                 VARCHAR(500) NULL,
    status                 VARCHAR(10) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|APPROVED|REJECTED',
    approval_document_id   BIGINT NOT NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vacation_request_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_vacation_request_document FOREIGN KEY (approval_document_id) REFERENCES approval_document (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 출퇴근 기록: 사원 자가입력, 서버 시각 기준 (docs/adr/ADR-010)
CREATE TABLE IF NOT EXISTS attendance_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     BIGINT NOT NULL,
    work_date       DATE NOT NULL,
    check_in_time   DATETIME NULL,
    check_out_time  DATETIME NULL,
    UNIQUE KEY uq_attendance_employee_date (employee_id, work_date),
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- Module 4: 예산/지출관리 (egovframework.erp.budget)
-- =========================================================

-- 배정 단위: 조직 x 계정과목 x 회계연도 (docs/adr/ADR-013)
CREATE TABLE IF NOT EXISTS budget_allocation (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_unit_id       BIGINT NOT NULL,
    account_category  VARCHAR(20) NOT NULL COMMENT 'LABOR|TRAVEL|SUPPLIES|ENTERTAINMENT|OTHER',
    fiscal_year       INT NOT NULL,
    amount            DECIMAL(15,2) NOT NULL,
    UNIQUE KEY uq_budget_allocation (org_unit_id, account_category, fiscal_year),
    CONSTRAINT fk_budget_allocation_org FOREIGN KEY (org_unit_id) REFERENCES org_unit (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 지출결의서: 전자결재 문서와 1:1 연결 (Module 3 attendance_vacation_request와 동일한 패턴)
-- org_unit_id는 기안 시점 소속의 스냅샷이다 — 이후 발령이 나도 이미 집행된 예산 집계가 흔들리지 않도록.
CREATE TABLE IF NOT EXISTS budget_expense_request (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id         BIGINT NOT NULL,
    org_unit_id         BIGINT NOT NULL,
    account_category    VARCHAR(20) NOT NULL,
    amount              DECIMAL(15,2) NOT NULL,
    reason              VARCHAR(500) NULL,
    exception_reason    VARCHAR(500) NULL COMMENT 'ADR-012: 예산 100% 초과 + 500만원 이상 건의 예외 승인 사유',
    status              VARCHAR(10) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|APPROVED|REJECTED',
    approval_document_id BIGINT NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_expense_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_budget_expense_org FOREIGN KEY (org_unit_id) REFERENCES org_unit (id),
    CONSTRAINT fk_budget_expense_document FOREIGN KEY (approval_document_id) REFERENCES approval_document (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 데모 배정 (회계연도 2026 — 이 프로젝트의 기준 "현재"와 동일)
INSERT INTO budget_allocation (org_unit_id, account_category, fiscal_year, amount) VALUES
  (11, 'LABOR',         2026, 50000000),
  (11, 'TRAVEL',        2026, 5000000),
  (11, 'SUPPLIES',      2026, 3000000),
  (11, 'ENTERTAINMENT', 2026, 2000000),
  (11, 'OTHER',         2026, 1000000),
  (21, 'TRAVEL',        2026, 10000000),
  (21, 'ENTERTAINMENT', 2026, 8000000),
  (21, 'OTHER',         2026, 2000000),
  (31, 'SUPPLIES',      2026, 20000000),
  (31, 'OTHER',         2026, 3000000);
