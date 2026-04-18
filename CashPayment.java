package com.mycompany.sportstrainingacademy;

/**
 * CashPayment — Concrete PaymentFramework subclass.
 * Validates cash on hand and applies a 5% cash discount.
 */
public class CashPayment extends PaymentFramework {

    public CashPayment(String customerName, double amount, double cashOnHand) {
        super(customerName, amount, "CASH", cashOnHand);
        this.discountRate = 0.05; // 5% cash discount
    }

    @Override
    protected boolean validatePayment() {
        boolean valid = creditBalance >= amount;
        System.out.printf("  [VALIDATION] Cash on hand P%.2f vs Amount P%.2f -> %s%n",
                creditBalance, amount, valid ? "APPROVED" : "INSUFFICIENT CASH");
        return valid;
    }

    @Override
    protected double applyDiscount() {
        double discounted = amount * (1 - discountRate);
        System.out.printf("  [DISCOUNT]   %d%% off -> P%.2f%n",
                (int)(discountRate * 100), discounted);
        return discounted;
    }

    @Override
    protected void finalizeTransaction() {
        double change = creditBalance - finalAmount;
        System.out.printf("  [PAID]       Cash accepted. Change: P%.2f%n", change);
    }
}