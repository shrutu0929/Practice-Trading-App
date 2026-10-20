package com.tradedesk.quote;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record QuoteRequest(@NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal price) {
}
