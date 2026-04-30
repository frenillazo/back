package com.acainfo.intensive.application.port.in;

import com.acainfo.intensive.application.dto.CreateIntensiveCommand;
import com.acainfo.intensive.domain.model.Intensive;

public interface CreateIntensiveUseCase {
    Intensive create(CreateIntensiveCommand command);
}
