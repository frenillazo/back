package com.acainfo.intensive.application.port.in;

import com.acainfo.intensive.domain.model.Intensive;

public interface DeleteIntensiveUseCase {

    void delete(Long id);

    Intensive cancel(Long id);
}
