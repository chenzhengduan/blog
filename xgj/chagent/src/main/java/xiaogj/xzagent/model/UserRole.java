package xiaogj.xzagent.model;

/**
 * 系统用户角色。
 *
 * <p>第一版只区分超管和普通用户，避免在权限体系尚未稳定时过早引入更多
 * 角色分层。
 */
public enum UserRole {
    /**
     * 超级管理员，可以访问全部管理接口。
     */
    SUPER_ADMIN,

    /**
     * 普通用户，只能使用聊天和维护自己的私有请求头。
     */
    USER
}
