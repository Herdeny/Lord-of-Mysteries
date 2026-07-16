package top.aurora.lordofmysteries.commission;

public final class CommissionCurrency {

    public static final long PENCE_PER_SHILLING = 12L;
    public static final long PENCE_PER_POUND = 240L;

    private CommissionCurrency() {}

    public static String format(long pence) {
        long safe = Math.max(0L, pence);
        long pounds = safe / PENCE_PER_POUND;
        long remainder = safe % PENCE_PER_POUND;
        long shillings = remainder / PENCE_PER_SHILLING;
        long pennies = remainder % PENCE_PER_SHILLING;
        return pounds + "£ " + shillings + "s " + pennies + "d";
    }
}
