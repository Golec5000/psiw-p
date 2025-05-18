package com.psiw.proj.backend.utils.responseDto.helpers;

import lombok.Builder;

@Builder
public record MovieSimpleDto(
        Long id,
        String title
) {}
