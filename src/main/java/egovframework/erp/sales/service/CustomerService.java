package egovframework.erp.sales.service;

import egovframework.erp.sales.domain.CustomerVO;

import java.util.List;

/** FR-6-1: 거래처 등록/조회. */
public interface CustomerService {

    Long registerCustomer(String name, String contactPerson, String phone, String email, String address);

    List<CustomerVO> getAllCustomers();
}
