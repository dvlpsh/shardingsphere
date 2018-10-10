/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.repository.mybatis.service;

import io.shardingsphere.example.repository.api.entity.Order;
import io.shardingsphere.example.repository.api.entity.OrderItem;
import io.shardingsphere.example.repository.mybatis.repository.MybatisOrderItemRepository;
import io.shardingsphere.example.repository.mybatis.repository.MybatisOrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemoService {
    
    @Resource
    private MybatisOrderRepository mybatisOrderRepository;
    
    @Resource
    private MybatisOrderItemRepository mybatisOrderItemRepository;
    
    public void demo() {
        mybatisOrderRepository.createTableIfNotExists();
        mybatisOrderItemRepository.createTableIfNotExists();
        mybatisOrderRepository.truncateTable();
        mybatisOrderItemRepository.truncateTable();
        List<Long> orderIds = new ArrayList<>(10);
        System.out.println("1.Insert--------------");
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(51);
            order.setStatus("INSERT_TEST");
            mybatisOrderRepository.insert(order);
            long orderId = order.getOrderId();
            orderIds.add(orderId);
            
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setUserId(51);
            item.setStatus("INSERT_TEST");
            mybatisOrderItemRepository.insert(item);
        }
        System.out.println("Order Data--------------");
        System.out.println(mybatisOrderRepository.selectAll());
        System.out.println("OrderItem Data--------------");
        System.out.println(mybatisOrderItemRepository.selectAll());
        System.out.println("2.Delete--------------");
        for (Long each : orderIds) {
            mybatisOrderRepository.delete(each);
            mybatisOrderItemRepository.delete(each);
        }
        System.out.println("Order Data--------------");
        System.out.println(mybatisOrderRepository.selectAll());
        System.out.println("OrderItem Data--------------");
        System.out.println(mybatisOrderItemRepository.selectAll());
        mybatisOrderItemRepository.dropTable();
        mybatisOrderRepository.dropTable();
    }
}
