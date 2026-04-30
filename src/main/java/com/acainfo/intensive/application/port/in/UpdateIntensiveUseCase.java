package com.acainfo.intensive.application.port.in;

import com.acainfo.intensive.application.dto.UpdateIntensiveCommand;
import com.acainfo.intensive.domain.model.Intensive;

public interface UpdateIntensiveUseCase {
    Intensive update(Long id, UpdateIntensiveCommand command);
}
