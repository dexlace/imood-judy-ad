package com.dexlace.search.index.creativeunit;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreativeUnitObject {

    /**
     * 创意id
     */
    private Long adId;
    /**
     * 推广单元的id
     */
    private Long unitId;

    // adId-unitId
}
