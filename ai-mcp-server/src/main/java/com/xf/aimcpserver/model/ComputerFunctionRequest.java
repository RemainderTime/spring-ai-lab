package com.xf.aimcpserver.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

/**
 * @Description:
 * @ClassName: ComputerFunctionRequest
 * @Author: xiongfeng
 * @Date: 2025/12/3 22:54
 * @Version: 1.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComputerFunctionRequest {

	@JsonProperty(required = true, value = "computer")
	@JsonPropertyDescription("电脑名称")
	private String computer;

}

