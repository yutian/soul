package org.dromara.soul.common.dto.convert;

import lombok.Data;

/**
 * this is rateLimiter plugin handle.
 *
 * @author xiaoyu(Myth)
 */
@Data
public class ReqMappingHandle {

    public static String BODY_LOCATION = "BODY";

    public static String QUERY_LOCATION = "QUERY";

    public static String HTTP_RPC_TYPE = "HTTP";

    public static String TAF_RPC_TYPE = "TAF";

    /**
     * 请求方法.
     */
    private String method;

    /**
     * 前端请求参数位置，QUERY/BODY.
     */
    private String frontEndParamLocation;

    /**
     * 前端请求参数.
     */
    private String frontEndParam;


    /**
     * 后端Rpc类型，HTTP/TAF
     */
    private String backEndRpcType;

    /**
     * 后端映射参数位置，QUERY/BODY.
     */
    private String backEndParamLocation;

    /**
     * 后端映射配置.
     */
    private String backEndMapping;

}
