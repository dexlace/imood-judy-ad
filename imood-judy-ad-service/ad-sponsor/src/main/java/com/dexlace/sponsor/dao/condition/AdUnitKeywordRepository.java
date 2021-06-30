package com.dexlace.sponsor.dao.condition;


import com.dexlace.sponsor.entity.condition.AdUnitKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdUnitKeywordRepository extends
        JpaRepository<AdUnitKeyword, Long> {
}
