package com.dexlace.search.index;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 所以只需注入dataTable组件即可，而不要去注入索引类
 * 这是一个索引目录
 *
 */
@Component
public class DataTable implements ApplicationContextAware, PriorityOrdered {

    /**
     * 得到一个applicationContext对象
     */
    private static ApplicationContext applicationContext;

    /**
     * 保存所有index服务
     * key为索引类型
     * value为索引类的bean
     * 所以这是一个目录  缓存了所有索引类
     */
    public static final Map<Class, Object> dataTableMap =
            new ConcurrentHashMap<>();

    /**
     * 重写 ApplicationContextAware接口的setApplicationContext方法
     * 可以获得applicationContext容器
     * @param applicationContext 无需解释
     * @throws BeansException bean异常
     */
    @Override
    public void setApplicationContext(
            ApplicationContext applicationContext) throws BeansException {
        DataTable.applicationContext = applicationContext;
    }

    /**
     * 给本javabean设置初始化优先级为最高级
     * @return
     */
    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }

    /**
     * 获取索引的方法
     * @param clazz 索引类
     * @param <T> 索引类
     * @return
     */
    @SuppressWarnings("all")
    public static <T> T of(Class<T> clazz) {

        T instance = (T) dataTableMap.get(clazz);
        if (null != instance) {
            // 获取到了索引bean
            return instance;
        }

        // 索引类与bean的对应关系
        dataTableMap.put(clazz, bean(clazz));
        return (T) dataTableMap.get(clazz);
    }

    /**
     * 获取bean的方法一
     * @param beanName
     * @param <T>
     * @return
     */
    @SuppressWarnings("all")
    private static <T> T bean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 获取bean的方法二
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("all")
    private static <T> T bean(Class clazz) {
        return (T) applicationContext.getBean(clazz);
    }
}
