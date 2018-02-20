package com.secondkill.service;

import com.secondkill.dto.Exposer;
import com.secondkill.dto.SeckillExecution;
import com.secondkill.entity.Seckill;
import com.secondkill.exception.RepeatKillException;
import com.secondkill.exception.SeckillCloseException;
import com.secondkill.exception.SeckillException;

import java.util.List;

public interface SeckillService {
    List<Seckill> getSeckillList();

    Seckill getById(long seckillId);

    //输出秒杀接口地址
    Exposer exportSeckillUrl(long seckillId);

    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException,RepeatKillException,SeckillCloseException;

    SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);
}
