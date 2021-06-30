package com.dexlace.sponsor.service.impl;


import com.dexlace.common.exception.AdException;
import com.dexlace.sponsor.constant.Constants;
import com.dexlace.sponsor.dao.AdPlanRepository;
import com.dexlace.sponsor.dao.AdUnitRepository;
import com.dexlace.sponsor.dao.CreativeRepository;
import com.dexlace.sponsor.dao.condition.AdUnitDistrictRepository;
import com.dexlace.sponsor.dao.condition.AdUnitItRepository;
import com.dexlace.sponsor.dao.condition.AdUnitKeywordRepository;
import com.dexlace.sponsor.dao.condition.CreativeUnitRepository;
import com.dexlace.sponsor.entity.AdPlan;
import com.dexlace.sponsor.entity.AdUnit;
import com.dexlace.sponsor.entity.condition.AdUnitDistrict;
import com.dexlace.sponsor.entity.condition.AdUnitInterest;
import com.dexlace.sponsor.entity.condition.AdUnitKeyword;
import com.dexlace.sponsor.entity.condition.CreativeUnit;
import com.dexlace.sponsor.service.IAdUnitService;
import com.dexlace.sponsor.vo.unit.AdUnitRequest;
import com.dexlace.sponsor.vo.unit.AdUnitResponse;
import com.dexlace.sponsor.vo.unit.condition.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdUnitServiceImpl implements IAdUnitService {

    private final AdPlanRepository planRepository;
    private final AdUnitRepository unitRepository;

    private final AdUnitKeywordRepository unitKeywordRepository;
    private final AdUnitItRepository unitItRepository;
    private final AdUnitDistrictRepository unitDistrictRepository;

    private final CreativeRepository creativeRepository;
    private final CreativeUnitRepository creativeUnitRepository;

    @Autowired
    public AdUnitServiceImpl(AdPlanRepository planRepository,
                             AdUnitRepository unitRepository,
                             AdUnitKeywordRepository unitKeywordRepository,
                             AdUnitItRepository unitItRepository,
                             AdUnitDistrictRepository unitDistrictRepository, CreativeRepository creativeRepository, CreativeUnitRepository creativeUnitRepository) {
        this.planRepository = planRepository;
        this.unitRepository = unitRepository;
        this.unitKeywordRepository = unitKeywordRepository;
        this.unitItRepository = unitItRepository;
        this.unitDistrictRepository = unitDistrictRepository;
        this.creativeRepository = creativeRepository;
        this.creativeUnitRepository = creativeUnitRepository;
    }

    @Override
    public AdUnitResponse createUnit(AdUnitRequest request)
            throws AdException {

        if (!request.createValidate()) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }

        Optional<AdPlan> adPlan =
                planRepository.findById(request.getPlanId());
        if (!adPlan.isPresent()) {
            throw new AdException(Constants.ErrorMsg.CAN_NOT_FIND_RECORD);
        }

        AdUnit oldAdUnit = unitRepository.findByPlanIdAndUnitName(
                request.getPlanId(), request.getUnitName()
        );
        if (oldAdUnit != null) {
            throw new AdException(Constants.ErrorMsg.SAME_NAME_UNIT_ERROR);
        }

        AdUnit newAdUnit = unitRepository.save(
                new AdUnit(request.getPlanId(), request.getUnitName(),
                        request.getPositionType(), request.getBudget())
        );

        return new AdUnitResponse(newAdUnit.getId(),
                newAdUnit.getUnitName());
    }

    /**
     * 创建推广单元的关键词  以下逻辑很简单：先看看请求的推广单元存不存在，再去取关键词
     * 看起来代码很复杂的样子，但其实没有
     * @param request 推广单元关键词请求，是一个包含了推广单元id和keyword的list而已（内部定义了UnitKeyword）
     *                其实就是一个vo对象呗
     * @return 关键词计划id
     * @throws AdException
     */
    @Override
    public AdUnitKeywordResponse createUnitKeyword(
            AdUnitKeywordRequest request) throws AdException {

        // stream  java8新特性
        // 一、获取推广单元的id  因为关键词是要依附于推广单元的，所以推广单元的id必须检验存不存在
        List<Long> unitIds = request.getUnitKeywords().stream()
                .map(AdUnitKeywordRequest.UnitKeyword::getUnitId)  // map是映射操作
                .collect(Collectors.toList()); // collect是聚合操作
        if (!isRelatedUnitExist(unitIds)) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }

        // 二 然后才去拿关键词，放入ids
        // 先获取一个空的list 这是需要返回的关键词的id list，推广单元和关键词之间是多对多关系
        List<Long> ids = Collections.emptyList();

        List<AdUnitKeyword> unitKeywords = new ArrayList<>();
        if (!CollectionUtils.isEmpty(request.getUnitKeywords())) {

            // forEach操作取出list中的东西并执行forEach中的逻辑，这里是取出并得到了一个
            // AdUnitKeyword的list,它以推广单元id和关键词keyword初始化
            request.getUnitKeywords().forEach(i -> unitKeywords.add(
                    new AdUnitKeyword(i.getUnitId(), i.getKeyword())
            ));
            // 下述操作一是保存了关键词的实体类，二是进行了映射操作，得到了关键词id的集合
            ids = unitKeywordRepository.saveAll(unitKeywords).stream()
                    .map(AdUnitKeyword::getId)
                    .collect(Collectors.toList());
        }

        return new AdUnitKeywordResponse(ids);
    }


    /**
     * 创建推广单元的兴趣  以下逻辑很简单：先看看请求的推广单元存不存在，再去取兴趣
     * 看起来代码很复杂的样子，但其实没有
     * @param request 推广单元兴趣请求，是一个包含了推广单元id和interest的list而已（内部定义了UnitInterest）
     *                其实就是一个vo对象呗
     * @return 关键词计划id
     * @throws AdException
     */
    @Override
    public AdUnitInterestResponse createUnitInterest(
            AdUnitInterestRequest request) throws AdException {


        // 先拿到推广单元兴趣请求中的推广单元id
        List<Long> unitIds = request.getUnitInterests().stream()
                .map(AdUnitInterestRequest.UnitInterest::getUnitId)
                .collect(Collectors.toList());
        // 查数据库 判断是否存在相应的推广单元id
        if (!isRelatedUnitExist(unitIds)) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }

        // 存在的话就可以放心拿到请求中的兴趣VO类，并实例化一个兴趣实体类
        List<AdUnitInterest> unitIts = new ArrayList<>();
        request.getUnitInterests().forEach(i -> unitIts.add(
                new AdUnitInterest(i.getUnitId(), i.getItTag())
        ));
        // 持久化该实体类，并返回我们所需要的的兴趣实体类的id返回对应的响应vo类
        List<Long> ids = unitItRepository.saveAll(unitIts).stream()
                .map(AdUnitInterest::getId)
                .collect(Collectors.toList());

        return new AdUnitInterestResponse(ids);
    }

    /**
     * 地区限制
     * @param request
     * @return
     * @throws AdException
     */
    @Override
    public AdUnitDistrictResponse createUnitDistrict(
            AdUnitDistrictRequest request) throws AdException {

        // 先拿到推广单元地域限制请求中的推广单元id
        List<Long> unitIds = request.getUnitDistricts().stream()
                .map(AdUnitDistrictRequest.UnitDistrict::getUnitId)
                .collect(Collectors.toList());
        // 查数据库 看该推广单元是否存在
        if (!isRelatedUnitExist(unitIds)) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }


        // 存在就以该请求中的地域限制VO类实例去实例化对应entity类
        List<AdUnitDistrict> unitDistricts = new ArrayList<>();
        request.getUnitDistricts().forEach(d -> unitDistricts.add(
                new AdUnitDistrict(d.getUnitId(), d.getProvince(),
                        d.getCity())
        ));

        // 持久化地域限制实体类  拿到对应的id并返回对应的响应vo类
        List<Long> ids = unitDistrictRepository.saveAll(unitDistricts)
                .stream().map(AdUnitDistrict::getId)
                .collect(Collectors.toList());

        return new AdUnitDistrictResponse(ids);
    }

    /**
     * 创意
     * @param request
     * @return
     * @throws AdException
     */
    @Override
    public CreativeUnitResponse createCreativeUnit(
            CreativeUnitRequest request) throws AdException {

        List<Long> unitIds = request.getUnitItems().stream()
                .map(CreativeUnitRequest.CreativeUnitItem::getUnitId)
                .collect(Collectors.toList());
        List<Long> creativeIds = request.getUnitItems().stream()
                .map(CreativeUnitRequest.CreativeUnitItem::getCreativeId)
                .collect(Collectors.toList());

        if (!(isRelatedUnitExist(unitIds) && isRelatedCreativeExist(creativeIds))) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }

        List<CreativeUnit> creativeUnits = new ArrayList<>();
        request.getUnitItems().forEach(i -> creativeUnits.add(
                new CreativeUnit(i.getCreativeId(), i.getUnitId())
        ));

        List<Long> ids = creativeUnitRepository.saveAll(creativeUnits)
                .stream()
                .map(CreativeUnit::getId)
                .collect(Collectors.toList());

        return new CreativeUnitResponse(ids);
    }

    /**
     * 推广单元是否存在，可以有很多个推广单元，也可能会重复
     * @param unitIds
     * @return
     */
    private boolean isRelatedUnitExist(List<Long> unitIds) {

        if (CollectionUtils.isEmpty(unitIds)) {
            return false;
        }

        // 查到的推广单元是否存在，且和原本的set大小一致
        return unitRepository.findAllById(unitIds).size() ==
                new HashSet<>(unitIds).size();
    }

    /**
     * 这里也是，但是是创意是否存在
     * @param creativeIds
     * @return
     */
    private boolean isRelatedCreativeExist(List<Long> creativeIds) {

        if (CollectionUtils.isEmpty(creativeIds)) {
            return false;
        }

        return creativeRepository.findAllById(creativeIds).size() ==
                new HashSet<>(creativeIds).size();
    }
}
