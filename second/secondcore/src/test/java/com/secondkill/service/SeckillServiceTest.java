package com.secondkill.service;

import com.secondkill.dto.Exposer;
import com.secondkill.dto.SeckillExecution;
import com.secondkill.entity.Seckill;
import com.secondkill.exception.RepeatKillException;
import com.secondkill.exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"" +
        "classpath:spring/spring-dao.xml",
         "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
    private Logger logger= LoggerFactory.getLogger(this.getClass());
    @Autowired
    private  SeckillService seckillService;
    @Test
    public void getSeckillList() {
        List<Seckill>list=seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void getById() {
        long id=1000;
        Seckill seckill=seckillService.getById(id);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void exportSeckillUrl() {
        long id=1001;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()){
            logger.info("exposer={}",exposer);
            // 91a7929be4b3773dadbe559b86e0737
            long phone=134567894;
            String md5=exposer.getMd5();
            try{
                SeckillExecution execution=seckillService.executeSeckill(id,phone,md5);
                logger.info("result={}",execution);
            }catch (RepeatKillException e){
                logger.error(e.getMessage());
            }catch (SeckillCloseException e){
                logger.error(e.getMessage());
            }
        }
    }


    @Test
    public void  test(){
        long seckillId=1001;
        long phone=12345678;
        Exposer exposer=seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()){
            String md5=exposer.getMd5();
            SeckillExecution execution=seckillService.executeSeckillProcedure(seckillId,phone,md5);
            logger.info(execution.getStateInfo());
        }

    }
    @Test
    public void executeSeckill() {
        long id=1000;
        long phone=125465465;
        String md5="91a7929be4b3773dadbe559b86e07379";
        SeckillExecution execution=seckillService.executeSeckill(id,phone,md5);
        logger.info("result={}",execution);
    }
}