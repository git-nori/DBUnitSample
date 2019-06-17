package jp.co.shoeisha.dao.impl;

import java.util.List;

import jp.co.shoeisha.dao.CustomerDao;
import jp.co.shoeisha.exception.DuplicateException;
import jp.co.shoeisha.exception.NotFoundException;
import jp.co.shoeisha.model.Customer;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;


/**
 * T_CUSTOMERにアクセスするDAO実装.
 * @author Katagiri
 */
public class CustomerDaoImpl extends SqlMapClientDaoSupport implements CustomerDao {

    @SuppressWarnings("unchecked")
    public Customer findByKey(Long customerCd, String customerType)
            throws NotFoundException {

        if(customerCd == null || StringUtils.isEmpty(customerType)) {
            throw new IllegalArgumentException("引数が不正");
        }

        Customer param = new Customer();
        param.setCustomerCd(customerCd);
        param.setCustomerType(customerType);

        List <Customer> list =
            (List <Customer>) getSqlMapClientTemplate().queryForList(
                    "Customer.findByKey", param);


        if(list.size() != 1) {
            throw new NotFoundException();
        }
        return list.get(0);
    }


    @SuppressWarnings("unchecked")
    public List<Customer> findByCustomerCd(Long customerCd) {
        if(customerCd == null) {
            throw new IllegalArgumentException("引数が不正");
        }
        return (List <Customer>) getSqlMapClientTemplate().queryForList(
                "Customer.findByCustomerCd", customerCd);
    }


    @SuppressWarnings("unchecked")
    public List<Customer> findByAny(Customer keys) {
        if(keys == null) {
            throw new IllegalArgumentException("引数が不正");
        }

        String customerName = keys.getCustomerName();
        if(StringUtils.isNotEmpty(customerName)) {
            //※これでは、引数のcustomerNameに"%"が設定されたときに全件検索をするので
            //　注意が必要
            keys.setCustomerName(customerName + "%");
        }

        return (List <Customer>) getSqlMapClientTemplate().queryForList(
                "Customer.findByAny", keys);
    }

    public void insert(Customer target) throws DuplicateException {

        if(target == null) {
            throw new IllegalArgumentException("引数が不正");
        }

        try {
            //Insert
            getSqlMapClientTemplate().insert("Customer.insert", target);
        } catch(DataIntegrityViolationException e) {
            //一意制約違反が発生した場合
            throw new DuplicateException();
        }
    }

    public void update(Customer target) throws NotFoundException {

        if(target == null || target.getBeforeLastUpdateDateTime() == null) {
            throw new IllegalArgumentException("引数が不正");
        }

        int exeCnt = getSqlMapClientTemplate().update("Customer.updateByPrimaryKey", target);
        if(exeCnt != 1) {
            //更新件数が1件で無い場合
            throw new NotFoundException();
        }
    }

    public void deleteByKey(Long customerCd, String customerType)
            throws NotFoundException {

        if(customerCd == null || StringUtils.isEmpty(customerType)) {
            throw new IllegalArgumentException("引数が不正");
        }

        Customer param = new Customer();
        param.setCustomerCd(customerCd);
        param.setCustomerType(customerType);

        int exeCnt = getSqlMapClientTemplate().delete("Customer.deleteByPrimaryKey", param);
        if(exeCnt != 1) {
            //更新件数が1件で無い場合
            throw new NotFoundException();
        }

    }
}
