package com.imprev.swfdemo;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class RegisterDomainTask {

    private String domainName;

    public RegisterDomainTask(String domainName) {
        this.domainName = domainName;
    }

    public RegisterDomainTask() {
    }

    public String getDomainName() {
        return domainName;
    }
}
