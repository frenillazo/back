package acainfo.back.user.application.ports.in;

import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.UserDomain;
import acainfo.back.user.domain.model.UserStatus;

import java.util.List;

/**
 * Use case port for user management operations.
 * Defines the contract for user administration business logic.
 */
public interface ManageUserUseCase {

    /**
     * Command object for creating a teacher.
     */
    record CreateTeacherCommand(
            String email,
            String password,
            String firstName,
            String lastName,
            String phone
    ) {}

    /**
     * Command object for updating a teacher.
     */
    record UpdateTeacherCommand(
            Long teacherId,
            String firstName,
            String lastName,
            String phone
    ) {}

    /**
     * Command object for updating user profile.
     */
    record UpdateProfileCommand(
            Long userId,
            String firstName,
            String lastName,
            String phone
    ) {}

    /**
     * Creates a new teacher user.
     *
     * @param command teacher creation data
     * @return the created user
     */
    UserDomain createTeacher(CreateTeacherCommand command);

    /**
     * Updates an existing teacher.
     *
     * @param command teacher update data
     * @return the updated user
     */
    UserDomain updateTeacher(UpdateTeacherCommand command);

    /**
     * Deletes a teacher by ID.
     *
     * @param teacherId the teacher ID
     */
    void deleteTeacher(Long teacherId);

    /**
     * Gets a user by ID.
     *
     * @param userId the user ID
     * @return the user
     */
    UserDomain getUserById(Long userId);

    /**
     * Gets a user by email.
     *
     * @param email the user email
     * @return the user
     */
    UserDomain getUserByEmail(String email);

    /**
     * Gets all users with a specific role.
     *
     * @param roleType the role type
     * @return list of users
     */
    List<UserDomain> getUsersByRole(RoleType roleType);

    /**
     * Gets all active users with a specific role.
     *
     * @param roleType the role type
     * @return list of active users
     */
    List<UserDomain> getActiveUsersByRole(RoleType roleType);

    /**
     * Gets all teachers.
     *
     * @return list of all teachers
     */
    List<UserDomain> getAllTeachers();

    /**
     * Gets all active teachers.
     *
     * @return list of active teachers
     */
    List<UserDomain> getActiveTeachers();

    /**
     * Updates user profile.
     *
     * @param command profile update data
     * @return the updated user
     */
    UserDomain updateProfile(UpdateProfileCommand command);

    /**
     * Changes user status.
     *
     * @param userId the user ID
     * @param status the new status
     * @return the updated user
     */
    UserDomain changeUserStatus(Long userId, UserStatus status);

    /**
     * Assigns a role to a user.
     *
     * @param userId the user ID
     * @param roleType the role type to assign
     * @return the updated user
     */
    UserDomain assignRole(Long userId, RoleType roleType);

    /**
     * Removes a role from a user.
     *
     * @param userId the user ID
     * @param roleType the role type to remove
     * @return the updated user
     */
    UserDomain removeRole(Long userId, RoleType roleType);

    /**
     * Searches users by name.
     *
     * @param searchTerm the search term
     * @return list of matching users
     */
    List<UserDomain> searchUsersByName(String searchTerm);
}
