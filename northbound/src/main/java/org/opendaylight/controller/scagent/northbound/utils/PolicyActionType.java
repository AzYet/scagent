package org.opendaylight.controller.scagent.northbound.utils;

public enum PolicyActionType {
	REDIRECT_FLOW,
    RESTORE_REDIRECT_FLOW,
    CREATE_FLOW,
    BYOD_INIT,
    BYOD_ALLOW,
    RESTORE_BYOD_ALLOW,
    DELETE_FLOW,
    DROP_FLOW,
    ALLOW_FLOW,
    RESTORE_ALLOW_FLOW,
    BIND_MAC_IP,
    BLOCK_VM,
    // adjust reputation
    ADJUST_REPUTATION,
    UNKNOWN, RESTORE_DROP_FLOW, RESTORE_BYOD_INIT,
}
