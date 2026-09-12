package egovframework.erp.inventory.mapper;

import egovframework.erp.inventory.domain.ItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemMapper {

    void insertItem(ItemVO item);

    ItemVO selectById(Long id);

    List<ItemVO> selectAll();

    void increaseStock(@Param("itemId") Long itemId, @Param("quantity") int quantity);

    /**
     * 가용 재고가 충분할 때만 원자적으로 차감한다 (docs/adr/ADR-017). 영향받은 행 수를
     * 반환하므로, 0이면 재고 부족(또는 동시성 경합으로 그 사이 재고가 줄어든 상황)을 뜻한다.
     */
    int decreaseStockIfAvailable(@Param("itemId") Long itemId, @Param("quantity") int quantity);
}
