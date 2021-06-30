package com.dexlace.search.index.keyword;


import com.dexlace.search.index.IndexAware;

import com.dexlace.search.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * key是keyword
 * value是推广单元的id，是一个set类型
 * 这里没使用UnitKeywordObject，很奇怪
 */
@Slf4j
@Component
public class UnitKeywordIndex implements IndexAware<String, Set<Long>> {

    /**
     * 关键词keyword到推广单元id的映射，一个关键词可以有很多推广单元
     */
    private  static Map<String,Set<Long>> keywordUnitMap;

    /**
     * 推广单元id到关键词keyword的映射，一个推广单元可以有很多关键词
     */
    private static  Map<Long,Set<String>> unitKeywordMap;

    static {
        keywordUnitMap = new ConcurrentHashMap<>();
        unitKeywordMap = new ConcurrentHashMap<>();
    }

    /**
     * 通过关键词获得推广单元
     * @param key 关键词
     * @return 推广单元的id
     */
    @Override
    public Set<Long> get(String key) {
        // 关键词不存在 ，返回空的set
        if (StringUtils.isEmpty(key)) {
            return Collections.emptySet();
        }

        // 否则去关键词到推广单元的map中得到推广单元的id set集合
        Set<Long> result = keywordUnitMap.get(key);
        // 返回一个空的set
        if (result == null) {
            return Collections.emptySet();
        }

        return result;
    }

    @Override
    public void add(String key, Set<Long> value) {

        log.info("UnitKeywordIndex, before add: {}", unitKeywordMap);

        /**
         * 关键词到推广单元的索引
         * key：关键词
         * value: 推广单元的set
         * 当keywordUnitMap不存在key时，会去new出来一个 ConcurrentSkipListSet，属于推广单元的set
         */
        Set<Long> unitIdSet = CommonUtils.getorCreate(
                key, keywordUnitMap,
                ConcurrentSkipListSet::new
        );
        /**
         * 用value实例化unitIdSet，则能完成keywordUnitMap的更新
         */
        unitIdSet.addAll(value);


        /**
         * 下面的原理类似
         * 推广单元到关键词的索引
         * 如果unitKeywordMap（推广单元到关键词的索引）没有该推广单元作为key
         * 则new出一个oncurrentSkipListSet赋值给keywordSet，一个关键词索引的set
         * add方法会更新这个unitKeywordMa
         */
        for (Long unitId : value) {
            Set<String> keywordSet = CommonUtils.getorCreate(
                    unitId, unitKeywordMap,
                    ConcurrentSkipListSet::new
            );
            keywordSet.add(key);
        }

        log.info("UnitKeywordIndex, after add: {}", unitKeywordMap);

    }

    @Override
    public void update(String key, Set<Long> value) {
        log.error("keyword index can not support update");

    }

    @Override
    public void delete(String key, Set<Long> value) {
        log.info("UnitKeywordIndex, before delete: {}", unitKeywordMap);

        Set<Long> unitIds = CommonUtils.getorCreate(
                key, keywordUnitMap,
                ConcurrentSkipListSet::new
        );
        unitIds.removeAll(value);

        for (Long unitId : value) {

            Set<String> keywordSet = CommonUtils.getorCreate(
                    unitId, unitKeywordMap,
                    ConcurrentSkipListSet::new
            );
            keywordSet.remove(key);
        }

        log.info("UnitKeywordIndex, after delete: {}", unitKeywordMap);

    }

    public boolean match(Long unitId, List<String> keywords) {

        if (unitKeywordMap.containsKey(unitId)
                && CollectionUtils.isNotEmpty(unitKeywordMap.get(unitId))) {

            Set<String> unitKeywords = unitKeywordMap.get(unitId);

            return CollectionUtils.isSubCollection(keywords, unitKeywords);
        }

        return false;
    }


}
