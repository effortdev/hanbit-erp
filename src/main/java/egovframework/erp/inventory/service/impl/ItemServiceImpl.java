package egovframework.erp.inventory.service.impl;

import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.inventory.domain.ItemVO;
import egovframework.erp.inventory.mapper.ItemMapper;
import egovframework.erp.inventory.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemMapper itemMapper;

    @Autowired
    public ItemServiceImpl(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional
    public Long registerItem(String name, String unit, int initialStock, int safetyStock) {
        if (initialStock < 0 || safetyStock < 0) {
            throw new BusinessException("재고 수량은 음수가 될 수 없습니다."); // NFR-5-1
        }
        ItemVO item = new ItemVO();
        item.setName(name);
        item.setUnit(unit);
        item.setCurrentStock(initialStock);
        item.setSafetyStock(safetyStock);
        itemMapper.insertItem(item);
        return item.getId();
    }

    @Override
    public List<ItemVO> getAllItems() {
        return itemMapper.selectAll();
    }
}
