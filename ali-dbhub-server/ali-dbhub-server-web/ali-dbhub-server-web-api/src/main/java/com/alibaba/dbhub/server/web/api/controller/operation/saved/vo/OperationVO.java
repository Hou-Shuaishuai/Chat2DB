package com.alibaba.dbhub.server.web.api.controller.operation.saved.vo;

import com.alibaba.dbhub.server.domain.support.enums.DbTypeEnum;

import lombok.Data;

/**
 * @author moji
 * @version DdlVO.java, v 0.1 2022年09月18日 11:06 moji Exp $
 * @date 2022/09/18
 */
@Data
public class OperationVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 文件别名
     */
    private String name;

    /**
     * 数据源id
     */
    private Long dataSourceId;

    /**
     * 数据源名称
     */
    private String dataSourceName;

    /**
     * 是否可连接
     */
    private Boolean connectable;

    /**
     * DB名称
     */
    private String databaseName;

    /**
     * ddl语言类型
     * @see DbTypeEnum
     */
    private String type;

    /**
     * ddl内容
     */
    private String ddl;

    /**
     * ddl语句状态:DRAFT/RELEASE
     */
    private String status;

    /**
     * 是否在tab中被打开,y表示打开,n表示未打开
     */
    private String tabOpened;
}
