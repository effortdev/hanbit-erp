package egovframework.erp.approval.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 문서 봉투. 유형 전용 필드(amount/startDate/endDate)는 필요한 유형에서만 채운다.
 * 구매요청서의 품목/수량 같은 세부 항목은 Module 5(재고/구매관리)가 별도 테이블로 확장할 예정.
 */
public class ApprovalDocumentVO {

    private Long id;
    private DocumentType documentType;
    private String title;
    private String content;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long drafterId;
    private LocalDateTime createdAt;

    // 화면 표시용 부가 정보 (조인 결과)
    private String drafterName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getDrafterId() {
        return drafterId;
    }

    public void setDrafterId(Long drafterId) {
        this.drafterId = drafterId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDrafterName() {
        return drafterName;
    }

    public void setDrafterName(String drafterName) {
        this.drafterName = drafterName;
    }
}
