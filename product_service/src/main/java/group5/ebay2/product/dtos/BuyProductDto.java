package group5.ebay2.product.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BuyProductDto {

    @NotBlank
    private String paymentMethodType;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    public String getPaymentMethodType() { return paymentMethodType; }
    public void setPaymentMethodType(String paymentMethodType) { this.paymentMethodType = paymentMethodType; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
