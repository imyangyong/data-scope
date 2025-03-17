package com.domain.service;

import com.domain.model.metadata.SchemaInfo;

import javax.sql.DataSource;

/**
 * 数据源服务接口
 * 负责管理数据源连接和元数据
 */
public interface DataSourceService {

    /**
     * 获取数据源连接
     *
     * @param dataSourceId 数据源ID
     * @return 数据源连接
     */
    DataSource getDataSource(String dataSourceId);

    /**
     * 获取数据库模式信息
     *
     * @param dataSourceId 数据源ID
     * @return 数据库模式信息
     */
    SchemaInfo getSchemaInfo(String dataSourceId);

    /**
     * 同步数据源元数据
     *
     * @param dataSourceId 数据源ID
     * @return 是否同步成功
     */
    boolean syncMetadata(String dataSourceId);

    /**
     * 测试数据源连接
     *
     * @param dataSourceId 数据源ID
     * @return 是否连接成功
     */
    boolean testConnection(String dataSourceId);
}