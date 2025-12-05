package com.acainfo.group.application.port.in;

import com.acainfo.group.application.dto.CreateGroupCommand;
import com.acainfo.group.domain.model.SubjectGroup;

/**
 * Use case for creating groups.
 * Input port defining the contract for group creation.
 */
public interface CreateGroupUseCase {

    /**
     * Create a new group.
     *
     * @param command Group creation data
     * @return The created group
     */
    SubjectGroup create(CreateGroupCommand command);
}
