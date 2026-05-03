package com.mycompany.sportstrainingacademy;


public class CreditCardPayment extends PaymentFramework {

    public CreditCardPayment(String customerName, double amount, double creditBalance) {
        super(customerName, amount, "CREDIT_CARD", creditBalance);
        this.discountRate = 0.10; // 10% credit card discount
    }

    @Override
    protected boolean validatePayment() {
        boolean valid = creditBalance >= amount;
        System.out.printf("  [VALIDATION] Balance P%.2f vs Amount P%.2f -> %s%n",
                creditBalance, amount, valid ? "APPROVED" : "INSUFFICIENT FUNDS");
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
        creditBalance -= finalAmount;
        System.out.printf("  [PAID]       Credit card charged. Remaining: P%.2f%n", creditBalance);
    }
}
