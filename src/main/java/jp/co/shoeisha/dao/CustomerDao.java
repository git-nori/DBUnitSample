package jp.co.shoeisha.dao;

import java.util.List;

import jp.co.shoeisha.exception.DuplicateException;
import jp.co.shoeisha.exception.NotFoundException;
import jp.co.shoeisha.model.Customer;

public interface CustomerDao {

    Customer findByKey(Long customerCd, String customerType) throws NotFoundException;

    List < Customer > findByCustomerCd(Long customerCd);

    List < Customer > findByAny(Customer keys);

    void insert(Customer target) throws DuplicateException;

    void update(Customer target) throws NotFoundException;

    void deleteByKey(Long customerCd, String customerType) throws NotFoundException;

}
