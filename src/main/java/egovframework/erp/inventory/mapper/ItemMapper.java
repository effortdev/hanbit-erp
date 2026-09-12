package egovframework.erp.inventory.mapper;

import egovframework.erp.inventory.domain.ItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemMapper {

    void insertItem(ItemVO item);

    ItemVO selectById(Long id);

    List<ItemVO> selectAll();

    void increaseStock(@Param("itemId") Long itemId, @Param("quantity") int quantity);
}
