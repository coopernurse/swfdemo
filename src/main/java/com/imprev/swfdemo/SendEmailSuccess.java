package com.imprev.swfdemo;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class SendEmailSuccess {

    private String bmsMailingId;

    public SendEmailSuccess(String bmsMailingId) {
        this.bmsMailingId = bmsMailingId;
    }

    public SendEmailSuccess() { }

    public String getBmsMailingId() {
        return bmsMailingId;
    }

}
