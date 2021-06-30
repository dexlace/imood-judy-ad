package com.dexlace.search.index.creativeunit;


import com.dexlace.search.index.IndexAware;
import com.dexlace.search.index.adunit.AdUnitObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
@Component
public class CreativeUnitIndex implements
        IndexAware<String, CreativeUnitObject> {


    /**
     * 首先是一个整体的连接索引，
     *  <adId-unitId, CreativeUnitObject>
     */
    private static Map<String, CreativeUnitObject> objectMap;
    /**
     * 下面两个关系总体上体现为多对多的关系
     */
    /**
     *   <adId, unitId Set>
     *   一个创意可以有很多推广单元
     */

    private static Map<Long, Set<Long>> creativeUnitMap;
    /**
     * 一个推广单元可以有很多创意
     *   <unitId, adId set>
     */
    private static Map<Long, Set<Long>> unitCreativeMap;

    static {
        objectMap = new ConcurrentHashMap<>();
        creativeUnitMap = new ConcurrentHashMap<>();
        unitCreativeMap = new ConcurrentHashMap<>();
    }

    @Override
    public CreativeUnitObject get(String key) {
        return objectMap.get(key);
    }


    /**
     * 总之，拿到CreativeUnitObject对象去构造两个一对多的关系
     * @param key
     * @param value
     */
    @Override
    public void add(String key, CreativeUnitObject value) {

        log.info("before add: {}", objectMap);

        objectMap.put(key, value);


        /**
         * 从CreativeUnitObject对象中拿取创意id，创意：推广单元的map中找到所有的推广单元
         */
        Set<Long> unitSet = creativeUnitMap.get(value.getAdId());
        /**
         * 创意对应的推广单元为空则创建之
         */
        if (CollectionUtils.isEmpty(unitSet)) {
            unitSet = new ConcurrentSkipListSet<>();
            // 为空就创建创意：推广单元的map，以创意为key，这里的推广单元为空
            // 需要去下文拿到
            creativeUnitMap.put(value.getAdId(), unitSet);
        }
        // unitSet从创意推广单元中取得推广单元id以构造上面的创意：推广单元的creativeUnitMap
        // 如果不为空的话，不需要去put
        unitSet.add(value.getUnitId());

        Set<Long> creativeSet = unitCreativeMap.get(value.getUnitId());
        if (CollectionUtils.isEmpty(creativeSet)) {
            creativeSet = new ConcurrentSkipListSet<>();
            unitCreativeMap.put(value.getUnitId(), creativeSet);
        }
        creativeSet.add(value.getAdId());

        log.info("after add: {}", objectMap);
    }

    /**
     * 不支持更新
     * @param key
     * @param value
     */
    @Override
    public void update(String key, CreativeUnitObject value) {

        log.error("CreativeUnitIndex not support update");
    }

    @Override
    public void delete(String key, CreativeUnitObject value) {

        log.info("before delete: {}", objectMap);

        objectMap.remove(key);

        /**
         * 删除一个一对多
         */
        Set<Long> unitSet = creativeUnitMap.get(value.getAdId());
        if (CollectionUtils.isNotEmpty(unitSet)) {
            unitSet.remove(value.getUnitId());
        }

        /**
         * 删除另一个一对多
         */
        Set<Long> creativeSet = unitCreativeMap.get(value.getUnitId());
        if (CollectionUtils.isNotEmpty(creativeSet)) {
            creativeSet.remove(value.getAdId());
        }

        log.info("after delete: {}", objectMap);
    }


    /**
     * 通过推广单元索引对象查找对应的创意id set
     * @param unitObjects 推广单元索引对象
     * @return 创意id set
     */
    public List<Long> selectAds(List<AdUnitObject> unitObjects) {

        if (CollectionUtils.isEmpty(unitObjects)) {
            return Collections.emptyList();
        }

        List<Long> result = new ArrayList<>();

        for (AdUnitObject unitObject : unitObjects) {

            Set<Long> adIds = unitCreativeMap.get(unitObject.getUnitId());
            if (CollectionUtils.isNotEmpty(adIds)) {
                result.addAll(adIds);
            }
        }

        return result;
    }
}
