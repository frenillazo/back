package com.acainfo.user.application.port.in;

import com.acainfo.user.domain.model.User;

import java.util.List;

public interface GetUserProfileUseCase {
    User getUserById(Long userId);
    User getUserByEmail(String email);

    /**
     * Find user IDs whose email contains the given search term (case insensitive).
     * Used for filtering payments/enrollments by student email.
     *
     * @param emailSearch partial email to search for
     * @return list of user IDs matching the search
     */
    List<Long> findIdsByEmailContaining(String emailSearch);
}
