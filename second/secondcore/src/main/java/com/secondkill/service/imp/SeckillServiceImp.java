package com.secondkill.service.imp;

import com.secondkill.dao.SeckillDao;
import com.secondkill.dao.SuccessKilledDao;
import com.secondkill.dao.cache.RedisDao;
import com.secondkill.dto.Exposer;
import com.secondkill.dto.SeckillExecution;
import com.secondkill.entity.Seckill;
import com.secondkill.entity.SuccessKilled;
import com.secondkill.enums.SeckillStateEnum;
import com.secondkill.exception.RepeatKillException;
import com.secondkill.exception.SeckillCloseException;
import com.secondkill.exception.SeckillException;
import com.secondkill.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImp implements SeckillService {
    private Logger logger= LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKilledDao successKilledDao;
    @Autowired
    private RedisDao redisDao;

    private final String slat="asdjaskdjkl";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill=redisDao.getSeckill(seckillId);
        if (seckill==null){
            seckill=seckillDao.queryById(seckillId);
            if (seckill==null){
                return new Exposer(false,seckillId);
            }else {
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime=seckill.getStartTime();
        Date endTime=seckill.getEndTime();
        Date now=new Date();
        if (now.getTime()<startTime.getTime()||now.getTime()>endTime.getTime()){
            return new Exposer(false,seckillId,now.getTime(),startTime.getTime(),endTime.getTime());
        }
        String md5=getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMD5(long seckillId){
        String base=seckillId+"/"+slat;
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        try{
            if (md5==null||!md5.equals(getMD5(seckillId))){
                throw new SeckillException("seckill data rewrite");
            }

            Date nowTime=new Date();
            int insertCount=successKilledDao.insertSuccessKilled(seckillId,userPhone);
            if (insertCount<=0){
                throw new RepeatKillException("重复秒杀");
            }else {
                    int updateCount=seckillDao.reduceNumber(seckillId,nowTime);
                    if (updateCount<=0){
                        throw new  SeckillCloseException("秒杀关闭");
                    }else {
                        SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                        return new SeckillExecution(seckillId,SeckillStateEnum.SUCCESS,successKilled);
                    }
            }
        }catch (SeckillCloseException e1){
           throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        } catch (Exception e){
           logger.error(e.getMessage(),e);
           throw  new SeckillException("内部异常"+e.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5)  {
       if(md5==null||!md5.equals(getMD5(seckillId))){
           return new SeckillExecution(seckillId,SeckillStateEnum.DATA_REWRITE);
       }
       Date killTime=new Date();
       Map<String,Object>map=new HashMap<String, Object>();
       map.put("seckillId",seckillId);
       map.put("phone",userPhone);
       map.put("killTime",killTime);
       map.put("result",null);

       try{
           seckillDao.killByProcedure(map);
           int result= MapUtils.getInteger(map,"result",-2);
           if (result==1){
               SuccessKilled sk=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
               return new SeckillExecution(seckillId,SeckillStateEnum.SUCCESS,sk);
           }else {
               return new SeckillExecution(seckillId,SeckillStateEnum.stateOf(result));
           }
       }catch (Exception e){
           logger.error(e.getMessage(),e);
           return new SeckillExecution(seckillId,SeckillStateEnum.INNER_ERROR);
       }
    }


}
