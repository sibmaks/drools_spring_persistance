package org.example.facts.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by maksim.drobyshev on 01-Sep-20.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Wallet implements Serializable {
    private long id;
    @NotNull
    private String clientId;
    @NotNull
    private BigDecimal balance;
    @NotNull
    private String currency;
    @NotNull
    private LocalDateTime modified;
}