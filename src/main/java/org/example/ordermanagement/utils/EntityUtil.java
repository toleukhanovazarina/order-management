package org.example.ordermanagement.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

@Component
@Slf4j
public class EntityUtil {

    public static <T> T findByIdOrThrow(Optional<T> optionalEntity, String errorMessage, Object... logParams) {
        return optionalEntity.orElseThrow(() -> {
            log.error(errorMessage, logParams);
            return new IllegalArgumentException(String.format(errorMessage, logParams));
        });
    }

    public static <T> void updateField(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
