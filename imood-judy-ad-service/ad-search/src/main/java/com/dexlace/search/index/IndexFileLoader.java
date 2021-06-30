package com.dexlace.search.index;


import com.alibaba.fastjson.JSON;
import com.dexlace.common.constant.OpType;
import com.dexlace.common.dump.DConstant;
import com.dexlace.common.dump.table.*;
import com.dexlace.search.handler.AdLevelDataHandler;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
@DependsOn("dataTable")  //依赖dataTable bean
public class IndexFileLoader {


    /**
     * 加载索引是有层级关系的，不能搞反，且是增加操作，因为我们是从数据文件中加载全量索引
     */
    @PostConstruct
    public void init() {
        /**
         * 加载二级索引：推广计划索引  增加操作
         */
        List<String> adPlanStrings = loadDumpData(
                String.format("%s\\%s",
                        DConstant.DATA_ROOT_DIR,
                        DConstant.AD_PLAN)
        );
        adPlanStrings.forEach(p -> AdLevelDataHandler.handleLevel2(
                JSON.parseObject(p, AdPlanTable.class),
                OpType.ADD
        ));

        /**
         * 加载二级索引：创意索引 增加操作
         */
        List<String> adCreativeStrings = loadDumpData(
                String.format("%s\\%s",
                        DConstant.DATA_ROOT_DIR,
                        DConstant.AD_CREATIVE)
        );
        adCreativeStrings.forEach(c -> AdLevelDataHandler.handleLevel2(
                JSON.parseObject(c, AdCreativeTable.class),
                OpType.ADD
        ));


        /**
         * 加载三级索引：推广单元索引，默认与推广计划关联
         */
        List<String> adUnitStrings = loadDumpData(
                String.format("%s\\%s",
                        DConstant.DATA_ROOT_DIR,
                        DConstant.AD_UNIT)
        );
        adUnitStrings.forEach(u -> AdLevelDataHandler.handleLevel3(
                JSON.parseObject(u, AdUnitTable.class),
                OpType.ADD
        ));


        /**
         * 加载三级索引：推广单元索引与创意关联
          */
        List<String> adCreativeUnitStrings = loadDumpData(
                String.format("%s\\%s",
                        DConstant.DATA_ROOT_DIR,
                        DConstant.AD_CREATIVE_UNIT)
        );
        adCreativeUnitStrings.forEach(cu -> AdLevelDataHandler.handleLevel3(
                JSON.parseObject(cu, AdCreativeUnitTable.class),
                OpType.ADD
        ));

        /**
         * 加载四级索引：地域限制，与推广单元关联
         */
        List<String> adUnitDistrictStrings = loadDumpData(
                String.format("%s\\%s",
                        DConstant.DATA_ROOT_DIR,
                        DConstant.AD_UNIT_DISTRICT)
        );
        adUnitDistrictStrings.forEach(d -> AdLevelDataHandler.handleLevel4(
                JSON.parseObject(d, AdUnitDistrictTable.class),
                OpType.ADD
        ));

        /**
         * 加载四级索引：兴趣限制，与推广单元关联
         */
        List<String> adUnitItStrings = loadDumpData(
                String.format("%s\\%s",
                        DConstant.DATA_ROOT_DIR,
                        DConstant.AD_UNIT_IT)
        );
        adUnitItStrings.forEach(i -> AdLevelDataHandler.handleLevel4(
                JSON.parseObject(i, AdUnitItTable.class),
                OpType.ADD
        ));

        /**
         * 加载四级索引：关键词限制，与推广单元关联
         */
        List<String> adUnitKeywordStrings = loadDumpData(
                String.format("%s\\%s",
                        DConstant.DATA_ROOT_DIR,
                        DConstant.AD_UNIT_KEYWORD)
        );
        adUnitKeywordStrings.forEach(k -> AdLevelDataHandler.handleLevel4(
                JSON.parseObject(k, AdUnitKeywordTable.class),
                OpType.ADD
        ));
    }

    private List<String> loadDumpData(String fileName) {

        try (BufferedReader br = Files.newBufferedReader(
                Paths.get(fileName)
        )) {
            // 读取成string list
            return br.lines().collect(Collectors.toList());
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
