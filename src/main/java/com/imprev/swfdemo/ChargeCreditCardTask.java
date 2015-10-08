package com.imprev.swfdemo;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class ChargeCreditCardTask {

    private String cardNumber;

    public ChargeCreditCardTask(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public ChargeCreditCardTask() {
    }

    public String getCardNumber() {
        return cardNumber;
    }
}
