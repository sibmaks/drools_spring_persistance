package org.example.facts.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by maksim.drobyshev on 01-Sep-20.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommissionFree implements Serializable {
    private long clientId;
    private long walletId;
}
