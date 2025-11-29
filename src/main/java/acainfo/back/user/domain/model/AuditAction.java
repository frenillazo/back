package acainfo.back.user.domain.model;

public enum AuditAction {
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    PASSWORD_CHANGED,
    ROLE_ASSIGNED,
    ROLE_REVOKED,
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,
    ACCESS_DENIED
}
