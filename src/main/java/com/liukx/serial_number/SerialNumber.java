package com.liukx.serial_number;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by liukx on 2019-03-20.
 */
public class SerialNumber {

    private DateFormat dateFormat = new SimpleDateFormat("yyMMdd");

    private static StringRedisTemplate stringRedisTemplate;

    static {
        stringRedisTemplate = new StringRedisTemplate(new JedisConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1",6379)));
    }

    public static void main(String[] args) {
        String aa =  new SerialNumber().getSystemSerialNumber();
        System.out.println(aa);
    }

    /**
     * 流水号生成,高并发,基于redis的increment,单redis不会再有重复了
     */
    public String getSystemSerialNumber(){
        String date = dateFormat.format(new Date());
        ValueOperations valueOperations = stringRedisTemplate.opsForValue();
        Object o = valueOperations.get(date);
        if(o==null){
            int m=100;
            int n=99999;
            int temp=(m+(int)(Math.random()*(n+1-m)))/4; //生成从m到n的随机整数
            long value = Long.parseLong(date+String.format("%05d",temp));
            valueOperations.increment(date,value);
            stringRedisTemplate.expire(date,1, TimeUnit.DAYS);
        }
        //if(o==null)如果并发进来,在return的时候也不会有影响,redis是单线程的,这里只会有一个值
        return valueOperations.increment(date,1)+"";
    }

}
