package com.imprev.swfdemo;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class BuyDomainRequest {

    private String creditCard;
    private String domainName;
    private String customerEmail;

    public BuyDomainRequest(String creditCard, String domainName, String customerEmail) {
        this.creditCard = creditCard;
        this.domainName = domainName;
        this.customerEmail = customerEmail;
    }

    public BuyDomainRequest() {
    }

    public String getCreditCard() {
        return creditCard;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }
}
