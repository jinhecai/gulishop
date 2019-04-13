package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.CartService;
import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.cart.bean.CartItem;
import com.atguigu.gmall.constant.RedisCacheConstant;
import com.atguigu.gmall.oms.entity.Order;
import com.atguigu.gmall.oms.entity.service.OrderAndPayService;
import com.atguigu.gmall.order.mapper.OrderItemMapper;
import com.atguigu.gmall.order.mapper.OrderMapper;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.ums.entity.MemberReceiveAddress;
import com.atguigu.gmall.ums.service.MemberService;
import com.atguigu.gmall.vo.OrderResponseVo;
import com.atguigu.gmall.vo.OrderSubmitVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Component
public class OrderAndPayServiceImpl implements OrderAndPayService{

    @Reference
    MemberService memberService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Reference
    CartService cartService;

    @Autowired
    JedisPool jedisPool;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Override
    public List<MemberReceiveAddress> getUserRecieveAddress(String token) {
        String s = redisTemplate.opsForValue().get(RedisCacheConstant.USER_INFO_CACHE_KEY + token);
        Member member = JSON.parseObject(s, Member.class);
        if(member!=null){
            return memberService.getUserAddress(member.getId());
        }
        return null;
    }

    @Override
    public String geiwoTradeToken() {
        String gmallusertoken =(String)RpcContext.getContext().getAttachment("gmallusertoken");
        String replace = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set(RedisCacheConstant.TRADE_TOKEN+gmallusertoken,replace,RedisCacheConstant.TRADE_TOKEN_TIME, TimeUnit.MINUTES);
        return replace;
    }

    @Transactional
    @Override
    public OrderResponseVo createOrder(OrderSubmitVo orderSubmitVo) {
        OrderResponseVo responseVo = new OrderResponseVo();
        //1、验证防止重复
        String tradeToken = orderSubmitVo.getTradeToken();
        //分布式锁 RedisCacheConstant.TRADE_TOKEN+userToken(userId)

        //对比防重删令牌
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1] else return 0 end)";
        Jedis jedis = jedisPool.getResource();
        Long eval = (Long) jedis.eval(script, Collections.singletonList(
                RedisCacheConstant.TRADE_TOKEN + orderSubmitVo.getToken()),
                Collections.singletonList(tradeToken));

        if(eval ==1){
            //令牌验证通过并且删除；总单信息；
            Order order = new Order();
            //1.查出用户信息
            String s = redisTemplate.opsForValue().get(RedisCacheConstant.USER_INFO_CACHE_KEY + orderSubmitVo.getToken());
            Member member = JSON.parseObject(s, Member.class);
            order.setMemberId(member.getId());
            order.setMemberUsername(member.getUsername());
            //2、查出用户的收货地址
            Long addressId = orderSubmitVo.getAddressId();
            MemberReceiveAddress address = memberService.getUserAddressByAddressId(addressId);
            order.setReceiverCity(address.getCity());
            order.setReceiverDetailAddress(address.getDetailAddress());
            order.setReceiverName(address.getName());
            order.setReceiverPhone(address.getPhoneNumber());
            order.setReceiverProvince(address.getProvince());
            order.setReceiverRegion(address.getRegion());
            //3、计算订单总额信息；
            List<CartItem> cartItems = cartService.cartItemsForJieSuan(orderSubmitVo.getToken());
            Cart cart = new Cart();
            cart.setItems(cartItems);

            BigDecimal totalPrice = cart.getTotalPrice();
            order.setTotalAmount(totalPrice);
            //4、订单状态是未支付
            //HashMap<Object, Object> map = new HashMap<>();
            //ArrayList<Object> objects = new ArrayList<>();
        }
        return null;
    }
}
