package org.example.drools_persistance_spring.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by maksim.drobyshev on 24-Sep-20.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDumpRequest implements Serializable {
    @NotNull
    private String clientId;
}
