package com.dexlace.sponsor.vo.unit.condition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdUnitInterestRequest {

    private List<UnitInterest> unitInterests;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitInterest {

        private Long unitId;
        private String itTag;
    }
}
