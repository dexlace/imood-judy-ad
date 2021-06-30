package com.dexlace.kafka.mysql.listener;


import com.dexlace.common.dto.definition.BinlogRowData;

/**
 * 注册和监听的功能
 */
public interface Ilistener {

    void register();

    void onEvent(BinlogRowData eventData);
}
