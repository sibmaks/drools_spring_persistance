package org.example.facts.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.facts.data.Deposit;

/**
 * Created by maksim.drobyshev on 01-Sep-20.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DepositChargeErrorNotification extends Notification {
    private Deposit deposit;
    private String reason;
}
