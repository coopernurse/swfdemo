package com.imprev.swfdemo;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class ChargeCreditCardError {

    public enum Reason {
        Declined,
        Expired,
        GatewayDown
    }

    private Reason reason;
    private String message;

    public ChargeCreditCardError(Reason reason, String message) {
        this.reason = reason;
        this.message = message;
    }

    public ChargeCreditCardError() {
    }

    public Reason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }
}
