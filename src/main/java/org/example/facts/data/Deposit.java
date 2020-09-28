package org.example.facts.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by maksim.drobyshev on 01-Sep-20.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Deposit implements Serializable {
    private long id;
    private Long clientId;
    private BigDecimal amount;
    private String currency;
    private String status;
}