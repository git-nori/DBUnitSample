package jp.co.shoeisha.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Customer implements Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private Long customerCd;

    private String customerType;

    private String customerName;

    private Timestamp createDateTime;

    private Timestamp lastUpdateDateTime;

    private Timestamp beforeLastUpdateDateTime;

    /**
     * @return customerCd
     */
    public Long getCustomerCd() {
        return customerCd;
    }

    /**
     * @param customerCd セットする customerCd
     */
    public void setCustomerCd(Long customerCd) {
        this.customerCd = customerCd;
    }

    /**
     * @return customerType
     */
    public String getCustomerType() {
        return customerType;
    }

    /**
     * @param customerType セットする customerType
     */
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    /**
     * @return customerName
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @param customerName セットする customerName
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * @return createDateTime
     */
    public Timestamp getCreateDateTime() {
        return createDateTime;
    }

    /**
     * @param createDateTime セットする createDateTime
     */
    public void setCreateDateTime(Timestamp createDateTime) {
        this.createDateTime = createDateTime;
    }

    /**
     * @return lastUpdateDateTime
     */
    public Timestamp getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }

    /**
     * @param lastUpdateDateTime セットする lastUpdateDateTime
     */
    public void setLastUpdateDateTime(Timestamp lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    /**
     * @return beforeLastUpdateDateTime
     */
    public Timestamp getBeforeLastUpdateDateTime() {
        return beforeLastUpdateDateTime;
    }

    /**
     * @param beforeLastUpdateDateTime セットする beforeLastUpdateDateTime
     */
    public void setBeforeLastUpdateDateTime(Timestamp beforeLastUpdateDateTime) {
        this.beforeLastUpdateDateTime = beforeLastUpdateDateTime;
    }

}
