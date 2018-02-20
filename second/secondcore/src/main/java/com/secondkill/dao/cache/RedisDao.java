package com.secondkill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.secondkill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {
    private Logger logger= LoggerFactory.getLogger(this.getClass());
    private JedisPool jedisPool;

    private RuntimeSchema<Seckill>schema=RuntimeSchema.createFrom(Seckill.class);

    private RedisDao(String ip,int port){
        jedisPool=new JedisPool(ip,port);
    }

    public Seckill getSeckill(long seckillId){
        try {
            Jedis jedis=jedisPool.getResource();
            jedis.auth("guliangjing52");
            try {
                String key="seckill:"+seckillId;
                byte[]bytes=jedis.get(key.getBytes());
                if (bytes!=null){
                    Seckill seckill=schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    System.out.println("ok!!!!");
                    return seckill;
                }
            }finally {
                    jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        try {
            Jedis jedis=jedisPool.getResource();
            jedis.auth("guliangjing52");
            try {
               String key="seckill:"+seckill.getSeckillId();
               byte[]bytes=ProtostuffIOUtil.toByteArray(seckill,schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
               //超时缓存
                int timeout=60*60;
                String result=jedis.setex(key.getBytes(),timeout,bytes);
                return result;
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }
}
