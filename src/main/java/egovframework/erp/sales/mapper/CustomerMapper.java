package egovframework.erp.sales.mapper;

import egovframework.erp.sales.domain.CustomerVO;

import java.util.List;

public interface CustomerMapper {

    void insertCustomer(CustomerVO customer);

    CustomerVO selectById(Long id);

    List<CustomerVO> selectAll();
}
