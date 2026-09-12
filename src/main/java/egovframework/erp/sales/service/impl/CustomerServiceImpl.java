package egovframework.erp.sales.service.impl;

import egovframework.erp.common.exception.BusinessException;
import egovframework.erp.sales.domain.CustomerVO;
import egovframework.erp.sales.mapper.CustomerMapper;
import egovframework.erp.sales.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerServiceImpl(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional
    public Long registerCustomer(String name, String contactPerson, String phone, String email, String address) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("거래처명은 필수입니다.");
        }
        CustomerVO customer = new CustomerVO();
        customer.setName(name);
        customer.setContactPerson(contactPerson);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        customerMapper.insertCustomer(customer);
        return customer.getId();
    }

    @Override
    public List<CustomerVO> getAllCustomers() {
        return customerMapper.selectAll();
    }
}
