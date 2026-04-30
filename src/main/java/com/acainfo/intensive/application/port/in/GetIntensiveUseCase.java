package com.acainfo.intensive.application.port.in;

import com.acainfo.intensive.application.dto.IntensiveFilters;
import com.acainfo.intensive.domain.model.Intensive;
import org.springframework.data.domain.Page;

import java.util.List;

public interface GetIntensiveUseCase {

    Intensive getById(Long id);

    Page<Intensive> findWithFilters(IntensiveFilters filters);

    List<Intensive> findAll();
}
