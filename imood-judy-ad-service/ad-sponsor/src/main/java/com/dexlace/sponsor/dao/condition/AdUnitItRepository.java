package com.dexlace.sponsor.dao.condition;


import com.dexlace.sponsor.entity.condition.AdUnitInterest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdUnitItRepository
        extends JpaRepository<AdUnitInterest, Long> {
}
