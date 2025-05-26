package com.psiw.proj.backend.service.interfaces;

import org.springframework.core.io.Resource;

public interface MovieImageService {
    public Resource loadImageResource(Long movieId);
}
