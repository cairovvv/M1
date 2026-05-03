package com.mycompany.sportstrainingacademy;


public abstract class PaymentFramework {

  
    protected String transactionID;
    protected String customerName;
    protected double amount;
    protected String paymentMethod;
    protected double creditBalance;
    protected double discountRate;
    protected double vatRate    = 0.12;  
    protected double finalAmount;



    public PaymentFramework(String customerName, double amount,
                             String paymentMethod, double creditBalance) {
        this.customerName  = customerName;
        this.amount        = amount;
        this.paymentMethod = paymentMethod;
        this.creditBalance = creditBalance;
    }

   
    protected abstract boolean validatePayment();

    protected abstract double applyDiscount();

    protected abstract void finalizeTransaction();


    protected double applyVAT(double discountedAmount) {
        return discountedAmount * (1 + vatRate);
    }

    public void processInvoice() {
        this.transactionID = generateTransactionID();

        System.out.println("\n" + "-".repeat(44));
        System.out.println("  Transaction ID : " + transactionID);
        System.out.println("  Customer       : " + customerName);
        System.out.println("  Payment Method : " + paymentMethod);
        System.out.printf ("  Base Amount    : P%.2f%n", amount);
        System.out.println("  " + "-".repeat(42));

        if (!validatePayment()) {
            System.out.println("  [ABORTED] Validation failed. No charges applied.");
            System.out.println("-".repeat(44));
            return;
        }

        double discounted = applyDiscount();
        this.finalAmount  = applyVAT(discounted);

        System.out.printf ("  After Discount : P%.2f%n", discounted);
        System.out.printf ("  VAT (%d%%)       : P%.2f%n",
                (int)(vatRate * 100), finalAmount - discounted);
        System.out.printf ("  Final Amount   : P%.2f%n", finalAmount);
        System.out.println("  " + "-".repeat(42));

        finalizeTransaction();
        System.out.println("-".repeat(44));
    }

   
    protected String generateTransactionID() {
        return "TXN-" + paymentMethod.toUpperCase().replace(" ", "_")
                + "-" + System.currentTimeMillis();
    }

   
    public void setVatRate(double rate)      { this.vatRate      = rate; }

    public void setDiscountRate(double rate) { this.discountRate = rate; }

    public double getFinalAmount()   { return finalAmount;   }

    public String getTransactionID() { return transactionID; }
}
