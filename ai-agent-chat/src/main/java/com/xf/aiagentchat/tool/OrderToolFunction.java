package com.xf.aiagentchat.tool;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * OrderToolFunction
 *
 * @author 海言
 * @date 2026/3/24
 * @time 11:48
 * @Description
 */
@Configuration
public class OrderToolFunction {

    public record Order(String orderId) { }

    @Bean
    @Description("查询订单信息")
    public Function<Order, String> orderFunction() {
        return order -> {
            // 模拟调用订单查询接口，返回订单信息
            if("D123456".equals(order.orderId)){
                return "订单查询结果：订单编号 D123456，订单金额 100.00，订单状态:已完成,物流状态：已放置代收点";
            }
            return "订单查询结果：未找到该订单信息";
        };
    }

}
