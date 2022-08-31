package com.example.ssmdemo.support;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PagerInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取拦截的对象
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        // 待执行的sql对象
        BoundSql boundSql = statementHandler.getBoundSql();
        // 判断是否查询语句
        if (!"".equals(boundSql.getSql()) && boundSql.getSql().toUpperCase().trim().startsWith("SELECT")) {
            // 获取参数
            Object params = boundSql.getParameterObject();
            // 如果查询方法中传入的参数是Pager的一个实例，则为分页查询
            if (params instanceof Pager) {
                Pager pager = (Pager) params;
                Connection connection = (Connection) invocation.getArgs()[0];

                MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
                // 查询总条数
                int count = count(connection, (ParameterHandler) metaObject.getValue("delegate.parameterHandler"), boundSql);
                pager.setTotalCount(count);
                // 拼接分页语句
                String pageSql = boundSql.getSql() + " limit " +
                        ((pager.getCurrentPage() - 1) * pager.getPageSize()) +
                        ", " + pager.getPageSize();

                metaObject.setValue("delegate.boundSql.sql", pageSql);

                return invocation.proceed();
            }
        }

        return invocation.proceed();
    }

    public int count(Connection connection, ParameterHandler parameterHandler, BoundSql boundSql) throws SQLException {
        String countSql = "select count(0) from (" + boundSql.getSql() + ") as total";
        PreparedStatement countStmt = null;
        ResultSet rs = null;

        try {
            countStmt = connection.prepareStatement(countSql);
            parameterHandler.setParameters(countStmt);
            rs = countStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            if (null != countStmt) {
                countStmt.close();
            }
            if (null != rs) {
                rs.close();
            }
        }
        return 0;
    }


    @Override
    public Object plugin(Object o) {
        // 注意：此处判断使用的是RoutingStatementHandler，而不是StatementHandler
        if (o instanceof RoutingStatementHandler) {
            return Plugin.wrap(o, this);
        } else {
            return o;
        }
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}
