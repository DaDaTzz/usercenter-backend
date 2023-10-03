
/**
  用户表
 */
CREATE TABLE `user`
(
    `id`             bigint                                  NOT NULL AUTO_INCREMENT COMMENT '用户id',
    `nickname`       varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
    `login_account`  varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
    `profile_photo`  varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '用户头像',
    `sex`            tinyint                                          DEFAULT NULL COMMENT '性别',
    `login_password` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
    `phone`          varchar(128) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '电话',
    `email`          varchar(128) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '邮箱',
    `states`         int                                     NOT NULL DEFAULT '0' COMMENT '0-正常 1-封号',
    `type`           int                                     NOT NULL DEFAULT '0'  COMMENT '0-默认权限 1-管理员',
    `tags`           varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '标签列表',
    `create_time`    datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`      tinyint                                 NOT NULL DEFAULT '0' COMMENT '是否删除',
    `profile`        varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '个人简介',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 15162572
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户';








