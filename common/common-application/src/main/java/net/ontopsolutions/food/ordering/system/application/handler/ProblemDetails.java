package net.ontopsolutions.food.ordering.system.application.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProblemDetails {

    private final String code;
    private final String message;

}
