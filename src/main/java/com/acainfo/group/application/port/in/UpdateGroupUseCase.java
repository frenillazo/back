package com.acainfo.group.application.port.in;

import com.acainfo.group.application.dto.UpdateGroupCommand;
import com.acainfo.group.domain.model.SubjectGroup;

/**
 * Use case for updating groups.
 * Input port defining the contract for group updates.
 */
public interface UpdateGroupUseCase {

    /**
     * Update an existing group.
     *
     * @param id Group ID
     * @param command Update data
     * @return The updated group
     */
    SubjectGroup update(Long id, UpdateGroupCommand command);
}
