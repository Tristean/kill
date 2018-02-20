package com.secondkill.dao;

import com.secondkill.entity.SuccessKilled;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
    @Resource
    private SuccessKilledDao successKilledDao;
    @Test
    public void insertSuccessKilled() {
        long id=1001L;
        long phone=133565497L;
        int count=successKilledDao.insertSuccessKilled(id,phone);
        System.out.println("counnt="+count);
    }

    @Test
    public void queryByIdWithSeckill() {
        long id=1001L;
        long phone=133565497L;
        SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(id,phone);
        System.out.println(successKilled);
        System.out.println("number="+successKilled.getSeckill().getNumber());
    }
}