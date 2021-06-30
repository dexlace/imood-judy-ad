package com.dexlace.search.handler;

import com.alibaba.fastjson.JSON;
import com.dexlace.common.constant.OpType;
import com.dexlace.common.dump.table.*;
import com.dexlace.search.index.DataTable;
import com.dexlace.search.index.IndexAware;
import com.dexlace.search.index.adplan.AdPlanIndex;
import com.dexlace.search.index.adplan.AdPlanObject;
import com.dexlace.search.index.adunit.AdUnitIndex;
import com.dexlace.search.index.adunit.AdUnitObject;
import com.dexlace.search.index.creative.CreativeIndex;
import com.dexlace.search.index.creative.CreativeObject;
import com.dexlace.search.index.creativeunit.CreativeUnitIndex;
import com.dexlace.search.index.creativeunit.CreativeUnitObject;
import com.dexlace.search.index.district.UnitDistrictIndex;
import com.dexlace.search.index.interest.UnitItIndex;
import com.dexlace.search.index.keyword.UnitKeywordIndex;

import com.dexlace.search.utils.CommonUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: xiaogongbing
 * @Description: 索引之间存在层级划分，也就是有依赖关系的划分
 *                加载全量索引其实是增量索引“添加”的一种特殊实现
 * @Date: 2021/5/20
 */
@Slf4j
public class AdLevelDataHandler {


    /**
     * 推广计划的索引是第二层级的
     * @param planTable 推广计划表数据 其实与导出的表json数据对应
     * @param type 操作类型
     */
    public static void handleLevel2(AdPlanTable planTable, OpType type) {

//        AdPlanObject planObject = new AdPlanObject(
//                planTable.getId(),
//                planTable.getUserId(),
//                planTable.getPlanStatus(),
//                planTable.getStartDate(),
//                planTable.getEndDate()
//        );
        /**
         * 构造索引类对象
         */
        AdPlanObject planObject=new AdPlanObject();
        planObject.setPlanId(planTable.getId());
        BeanUtils.copyProperties(planTable,planObject);

        handleBinlogEvent(
                DataTable.of(AdPlanIndex.class),  // 使用索引目录datatable加载AdPlanIndex索引
                planObject.getPlanId(), //推广计划的key
                planObject, // 推广计划索引对象
                type //操作
        );
    }


    /**
     * 创意也是第二层级的  不与推广计划相关联
     * @param creativeTable 创意表数据
     * @param type 操作类型
     */
    public static void handleLevel2(AdCreativeTable creativeTable,
                                    OpType type) {
//        CreativeObject creativeObject = new CreativeObject(
//                creativeTable.getAdId(),
//                creativeTable.getName(),
//                creativeTable.getType(),
//                creativeTable.getMaterialType(),
//                creativeTable.getHeight(),
//                creativeTable.getWidth(),
//                creativeTable.getAuditStatus(),
//                creativeTable.getAdUrl()
//        )
        CreativeObject creativeObject=new CreativeObject();
        BeanUtils.copyProperties(creativeTable,creativeObject);

        handleBinlogEvent(
                DataTable.of(CreativeIndex.class),
                creativeObject.getAdId(),
                creativeObject,
                type
        );
    }


    /**
     * 推广单元是第三层级的  与推广计划有依赖关系
     * @param unitTable 推广单元表数据
     * @param type 操作类型
     */
    public static void handleLevel3(AdUnitTable unitTable, OpType type) {
        /**
          * 首先必须获取对应的推广计划是否存在
         *  推广单元表数据中含有推广计划的planId
          */
        AdPlanObject adPlanObject = DataTable.of(
                AdPlanIndex.class
        ).get(unitTable.getPlanId());
        /**
         * 如果不存在，则此推广单元索引无法加载
         */
        if (null == adPlanObject) {
            log.error("handleLevel3 found AdPlanObject error: {}",
                    unitTable.getPlanId());
            return;
        }

        /**
         * 存在才创建推广单元的索引对象
         */
        AdUnitObject unitObject=new AdUnitObject();
        unitObject.setAdPlanObject(adPlanObject);
        BeanUtils.copyProperties(unitTable,unitObject);
//        AdUnitObject unitObject = new AdUnitObject(
//                unitTable.getUnitId(),
//                unitTable.getUnitStatus(),
//                unitTable.getPositionType(),
//                unitTable.getPlanId(),
//                adPlanObject
//        );

        /**
         * 加载推广单元索引
         */
        handleBinlogEvent(
                DataTable.of(AdUnitIndex.class),
                unitTable.getUnitId(),
                unitObject,
                type
        );
    }


    /**
     * 推广单元与创意之间也有依赖关系
     * @param creativeUnitTable 创意与推广单元关系表
     * @param type 操作类型
     */
    public static void handleLevel3(AdCreativeUnitTable creativeUnitTable,
                                    OpType type) {


        /**
         * 不可以更新
         */
        if (type == OpType.UPDATE) {
            log.error("CreativeUnitIndex not support update");
            return;
        }

        /**
         * 由推广单元索引AdUnitIndex查询是否存在该推广单元索引对象AdUnitObject
         */
        AdUnitObject unitObject = DataTable.of(
                AdUnitIndex.class
        ).get(creativeUnitTable.getUnitId());
        /**
         * 由创意索引CreativeIndex查询是否存在该创意索引对象CreativeObject
         */
        CreativeObject creativeObject = DataTable.of(
                CreativeIndex.class
        ).get(creativeUnitTable.getAdId());

        /**
         * 只要一个不存在就无用
         */
        if (null == unitObject || null == creativeObject) {
            log.error("AdCreativeUnitTable index error: {}",
                    JSON.toJSONString(creativeUnitTable));
            return;
        }

        /**
         * 创建对应的索引类对象
         */
        CreativeUnitObject creativeUnitObject = new CreativeUnitObject(
                creativeUnitTable.getAdId(),
                creativeUnitTable.getUnitId()
        );
        /**
         * 加载索引，注意这个关系索引的key需要连接创意和推广单元
         */
        handleBinlogEvent(
                DataTable.of(CreativeUnitIndex.class),
                CommonUtils.stringConcat(
                        creativeUnitObject.getAdId().toString(),
                        creativeUnitObject.getUnitId().toString()
                ),
                creativeUnitObject,
                type
        );
    }


    /**
     * 地域限制与推广单元的依赖
     * @param unitDistrictTable 推广单元与地域限制关系表
     * @param type 操作类型
     */
    public static void handleLevel4(AdUnitDistrictTable unitDistrictTable,
                                    OpType type) {

        if (type == OpType.UPDATE) {
            log.error("district index can not support update");
            return;
        }


        /**
         * 先要去看推广单元索引是否存在，才能获取推广单元对象
         */
        AdUnitObject unitObject = DataTable.of(
                AdUnitIndex.class
        ).get(unitDistrictTable.getUnitId());
        if (unitObject == null) {
            log.error("AdUnitDistrictTable index error: {}",
                    unitDistrictTable.getUnitId());
            return;
        }

        /**
         * 地域限制索引类对象的key由省份和city连接而成
         */
        String key = CommonUtils.stringConcat(
                unitDistrictTable.getProvince(),
                unitDistrictTable.getCity()
        );
        Set<Long> value = new HashSet<>(
                Collections.singleton(unitDistrictTable.getUnitId())
        );
        handleBinlogEvent(
                DataTable.of(UnitDistrictIndex.class),
                key, value,
                type
        );
    }


    /**
     * 兴趣限制
     * @param unitItTable 推广单元与兴趣限制表
     * @param type 操作
     */
    public static void handleLevel4(AdUnitItTable unitItTable, OpType type) {

        if (type == OpType.UPDATE) {
            log.error("it index can not support update");
            return;
        }

        AdUnitObject unitObject = DataTable.of(
                AdUnitIndex.class
        ).get(unitItTable.getUnitId());
        if (unitObject == null) {
            log.error("AdUnitItTable index error: {}",
                    unitItTable.getUnitId());
            return;
        }

        Set<Long> value = new HashSet<>(
                Collections.singleton(unitItTable.getUnitId())
        );
        handleBinlogEvent(
                DataTable.of(UnitItIndex.class),
                unitItTable.getItTag(),
                value,
                type
        );
    }


    public static void handleLevel4(AdUnitKeywordTable keywordTable,
                                    OpType type) {

        if (type == OpType.UPDATE) {
            log.error("keyword index can not support update");
            return;
        }

        AdUnitObject unitObject = DataTable.of(
                AdUnitIndex.class
        ).get(keywordTable.getUnitId());
        if (unitObject == null) {
            log.error("AdUnitKeywordTable index error: {}",
                    keywordTable.getUnitId());
            return;
        }

        Set<Long> value = new HashSet<>(
                Collections.singleton(keywordTable.getUnitId())
        );
        handleBinlogEvent(
                DataTable.of(UnitKeywordIndex.class),
                keywordTable.getKeyword(),
                value,
                type
        );
    }


    /**
     * 真正对索引加载
     * @param index 各个索引实现
     * @param key 索引key
     * @param value 对应的索引对象
     * @param type 操作
     * @param <K> 索引key的类型
     * @param <V> 对应索引对象的类型
     */
    private static <K, V> void handleBinlogEvent(
            IndexAware<K, V> index, // 索引的实现
            K key,   // 索引key
            V value,  // 索引的value，value是索引类对象
            OpType type) {  // 操作类型

        switch (type) {
            case ADD:
                index.add(key, value);
                break;
            case UPDATE:
                index.update(key, value);
                break;
            case DELETE:
                index.delete(key, value);
                break;
            default:
                break;
        }
    }
}
