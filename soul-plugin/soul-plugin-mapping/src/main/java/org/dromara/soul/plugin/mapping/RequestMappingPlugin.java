/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.soul.plugin.mapping;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.soul.common.constant.Constants;
import org.dromara.soul.common.dto.RuleData;
import org.dromara.soul.common.dto.SelectorData;
import org.dromara.soul.common.dto.convert.ReqMappingHandle;
import org.dromara.soul.common.enums.PluginEnum;
import org.dromara.soul.common.enums.RpcTypeEnum;
import org.dromara.soul.common.utils.GsonUtils;
import org.dromara.soul.common.utils.HttpParamConverter;
import org.dromara.soul.plugin.api.SoulPluginChain;
import org.dromara.soul.plugin.api.context.SoulContext;
import org.dromara.soul.plugin.base.AbstractSoulPlugin;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Rewrite Plugin.
 *
 * @author xiaoyu(Myth)
 */
@Slf4j
public class RequestMappingPlugin extends AbstractSoulPlugin {

    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final SoulPluginChain chain, final SelectorData selector, final RuleData rule) {
        String handle = rule.getHandle();
        final ReqMappingHandle reqMappingHandle = GsonUtils.getInstance().fromJson(handle, ReqMappingHandle.class);
        if (Objects.isNull(reqMappingHandle) || StringUtils.isBlank(reqMappingHandle.getBackEndMapping())) {
            log.error("request mapping rule can not configuration：{}", handle);
            return chain.execute(exchange);
        }
        log.info("request mapping handle:{}",handle);
        final Map<String,Object> paramContext = new HashMap<>();
        String query = exchange.getRequest().getURI().getQuery();
        if(ReqMappingHandle.QUERY_LOCATION.equalsIgnoreCase(reqMappingHandle.getFrontEndParamLocation()) && StringUtils.isNotBlank(query)){
            paramContext.putAll(GsonUtils.getInstance().convertToMap(HttpParamConverter.ofString(() -> query)));
        }else if(ReqMappingHandle.BODY_LOCATION.equalsIgnoreCase(reqMappingHandle.getFrontEndParamLocation())){
            String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

            exchange.getRequest().getBody().subscribe(buffer -> {
                byte[] bytes = new byte[buffer.readableByteCount()];
                buffer.read(bytes);
                DataBufferUtils.release(buffer);
                try {
                    String bodyString = new String(bytes, "utf-8");
                    log.info("body:{}",bodyString);
                    if(contentType.contains(MediaType.APPLICATION_JSON_VALUE)){
                        paramContext.putAll(GsonUtils.getInstance().convertToMap(bodyString));
                    }else if(contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)){
                        paramContext.putAll(GsonUtils.getInstance().convertToMap(HttpParamConverter.ofString(() -> bodyString)));
                    }
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage(),e);
                }
            });

        }
        String playLoad = "";
        try{
            StringTemplateLoader stringLoader = new StringTemplateLoader();
            Configuration cfg = new Configuration();
            stringLoader.putTemplate("myTemplate", reqMappingHandle.getBackEndMapping());
            cfg.setTemplateLoader(stringLoader);
            Template template = cfg.getTemplate("myTemplate");
            StringWriter writer = new StringWriter();
            template.process(paramContext, writer);
            playLoad = writer.toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        log.info("request mapping playLoad:{}",playLoad);
        if(ReqMappingHandle.QUERY_LOCATION.equalsIgnoreCase(reqMappingHandle.getBackEndParamLocation())){
            exchange.getAttributes().put(Constants.REWRITE_QUERY, playLoad);
        }else if(ReqMappingHandle.BODY_LOCATION.equalsIgnoreCase(reqMappingHandle.getBackEndParamLocation())){
            exchange.getAttributes().put(Constants.REWRITE_BODY, playLoad);
        }
        return chain.execute(exchange);
    }


    @Override
    public Boolean skip(final ServerWebExchange exchange) {
        final SoulContext body = exchange.getAttribute(Constants.CONTEXT);
        return Objects.equals(Objects.requireNonNull(body).getRpcType(), RpcTypeEnum.DUBBO.getName());
    }

    @Override
    public String named() {
        return PluginEnum.REQUEST_MAPPING.getName();
    }

    @Override
    public int getOrder() {
        return PluginEnum.REQUEST_MAPPING.getCode();
    }
}
