package org.example.facts.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by maksim.drobyshev on 28-Sep-20.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LongFact implements Serializable {
    private long value;
}
