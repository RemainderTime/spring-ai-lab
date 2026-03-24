package com.xf.aimodelschat.function;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * WeatherFuncation
 *
 * @author 海言
 * @date 2026/3/24
 * @time 11:11
 * @Description 天气查询模型能力
 */

@Configuration
public class WeatherToolFunction {

    //定义大模型提取参数的载体
    public record Weather(String city) { }

    //定义天气查询模型能力 注册 spring bean
    @Bean
    @Description("查询今天天气")
    public Function<Weather, String> weatherFunction() {
        return weather -> {
            // 模拟调用天气查询接口，返回天气信息
            if("成都".equals(weather.city)){
                return "天气查询结果：成都晴转多云，温度 25°C，风向南风";
            }
            return "天气查询结果：未找到该城市天气信息";
        };
    }

}
