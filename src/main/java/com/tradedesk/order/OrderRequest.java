package com.tradedesk.order;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OrderRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z][A-Za-z0-9.-]{0,11}") String symbol,
        @NotNull OrderSide side,
        @NotNull OrderType type,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantity,
        @DecimalMin(value = "0", inclusive = false) BigDecimal limitPrice,
        @DecimalMin(value = "0", inclusive = false) BigDecimal stopPrice) {
}
