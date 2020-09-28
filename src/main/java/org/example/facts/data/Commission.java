package org.example.facts.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by maksim.drobyshev on 01-Sep-20.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Commission implements Serializable {
    private Deposit deposit;
    private BigDecimal amount;
    private String currency;
}
