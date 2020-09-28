package org.example.drools_persistance_spring.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.facts.data.Wallet;

/**
 * Created by maksim.drobyshev on 24-Sep-20.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GetWalletResponse extends StandardResponse {
    private Wallet wallet;
}
