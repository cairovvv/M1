package com.mycompany.sportstrainingacademy;

/**
 * EWalletPayment — Concrete PaymentFramework subclass.
 * Validates e-wallet balance and applies a 15% promotional discount.
 */
public class EWalletPayment extends PaymentFramework {

    private final String walletProvider;

    public EWalletPayment(String customerName, double amount,
                           double walletBalance, String walletProvider) {
        super(customerName, amount, "E-WALLET", walletBalance);
        this.walletProvider = walletProvider;
        this.discountRate   = 0.15; // 15% e-wallet promo
    }

    @Override
    protected boolean validatePayment() {
        boolean valid = creditBalance >= amount;
        System.out.printf("  [VALIDATION] %s balance P%.2f vs Amount P%.2f -> %s%n",
                walletProvider, creditBalance, amount, valid ? "APPROVED" : "INSUFFICIENT BALANCE");
        return valid;
    }

    @Override
    protected double applyDiscount() {
        double discounted = amount * (1 - discountRate);
        System.out.printf("  [DISCOUNT]   %d%% e-wallet promo -> P%.2f%n",
                (int)(discountRate * 100), discounted);
        return discounted;
    }

    @Override
    protected void finalizeTransaction() {
        creditBalance -= finalAmount;
        System.out.printf("  [PAID]       %s debited. Remaining: P%.2f%n",
                walletProvider, creditBalance);
    }
}
