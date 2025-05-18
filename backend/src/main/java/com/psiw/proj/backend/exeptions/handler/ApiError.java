package com.psiw.proj.backend.exeptions.handler;

import java.time.LocalDateTime;

public record ApiError(
        String path,
        String message,
        int statusCode,
        LocalDateTime timestamp
) {
}
