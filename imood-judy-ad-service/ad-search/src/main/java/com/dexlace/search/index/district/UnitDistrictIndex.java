package com.dexlace.search.index.district;


import com.dexlace.search.index.IndexAware;
import com.dexlace.search.search.vo.feature.DistrictFeature;
import com.dexlace.search.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UnitDistrictIndex implements IndexAware<String, Set<Long>> {


    /**
     * 整体表现为多对多，分开表述为两个一对多
     */

    /**
     * 推广单元地域限制：推广单元id的map
     */
    private static Map<String, Set<Long>> districtUnitMap;
    /**
     * 推广单元id：推广单元地域限制
     */
    private static Map<Long, Set<String>> unitDistrictMap;

    static {
        districtUnitMap = new ConcurrentHashMap<>();
        unitDistrictMap = new ConcurrentHashMap<>();
    }

    /**
     * 根据关键词得到推广单元的id
     * @param key 关键词
     * @return 推广单元的id Set
     */
    @Override
    public Set<Long> get(String key) {
        return districtUnitMap.get(key);
    }

    @Override
    public void add(String key, Set<Long> value) {

        log.info("UnitDistrictIndex, before add: {}", unitDistrictMap);

        /**
         * 不存在这个key，则得到一个ConcurrentSkipListSet为该key的键值
         * 并赋值给unitIds
         */
        Set<Long> unitIds = CommonUtils.getorCreate(
                key, districtUnitMap,
                ConcurrentSkipListSet::new
        );
        // 这里把这个set进行初始化
        unitIds.addAll(value);

        /**
         * 遍历新增的推广单元的id，如果没有就创建并返回
         */
        for (Long unitId : value) {

            Set<String> districts = CommonUtils.getorCreate(
                    unitId, unitDistrictMap,
                    ConcurrentSkipListSet::new
            );

            // 同时初始化
            districts.add(key);
        }

        log.info("UnitDistrictIndex, after add: {}", unitDistrictMap);
    }

    @Override
    public void update(String key, Set<Long> value) {

        log.error("district index can not support update");
    }

    @Override
    public void delete(String key, Set<Long> value) {

        log.info("UnitDistrictIndex, before delete: {}", unitDistrictMap);

        Set<Long> unitIds = CommonUtils.getorCreate(
                key, districtUnitMap,
                ConcurrentSkipListSet::new
        );
        unitIds.removeAll(value);

        for (Long unitId : value) {

            Set<String> districts = CommonUtils.getorCreate(
                    unitId, unitDistrictMap,
                    ConcurrentSkipListSet::new
            );
            districts.remove(key);
        }

        log.info("UnitDistrictIndex, after delete: {}", unitDistrictMap);
    }

    public boolean match(Long adUnitId,
                         List<DistrictFeature.ProvinceAndCity> districts) {

        /**
         * 推广单元id:地域限制的map
         * 判断是否存在推广单元
         * 判断是否存在该推广单元的地域限制
         */
        if (unitDistrictMap.containsKey(adUnitId) &&
                CollectionUtils.isNotEmpty(unitDistrictMap.get(adUnitId))) {

            /**
             * 得到推广单元的地域限制
             */
            Set<String> unitDistricts = unitDistrictMap.get(adUnitId);

            /**
             * 得到地域限制的list
             */
            List<String> targetDistricts = districts.stream()
                    .map(
                            d -> CommonUtils.stringConcat(
                                    d.getProvince(), d.getCity()
                            )
                    ).collect(Collectors.toList());

            // 判断目标list是否是地域限制list的子集  并返回结果
            return CollectionUtils.isSubCollection(targetDistricts, unitDistricts);
        }

        return false;
    }
}
