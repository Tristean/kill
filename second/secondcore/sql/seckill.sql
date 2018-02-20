--秒杀执行存储过程
DELIMITER $$ --console;转化为$$
--定义存储过程
CREATE PROCEDURE `seckill`.`execute_seckill`
  (in v_seckill_id BIGINT, in v_phone BIGINT,
      in v_kill_time TIMESTAMP, out r_result int)

  BEGIN
      DECLARE insert_count int DEFAULT 0;
      START TRANSACTION;
        INSERT IGNORE INTO success_killed
          (seckill_id, user_phone, create_time, state)
        VALUES
          (v_seckill_id,v_phone,v_kill_time, 0);

        SELECT row_count() INTO insert_count;
        IF (insert_count = 0) THEN
            -- 重复秒杀
            ROLLBACK ;
            SET r_result = -1;
        ELSEIF (insert_count < 0) THEN
            ROLLBACK ;
            -- 系统异常
            SET r_result = -2;
        ELSE
          UPDATE
              seckill
          SET
              number = number -1
          WHERE seckill_id = v_seckill_id
          AND end_time > v_kill_time
          AND start_time < v_kill_time
          AND number > 0;

          SELECT row_count() INTO insert_count;
          IF (insert_count = 0) THEN
            -- 秒杀结束
            ROLLBACK ;
            SET r_result = 0;
          ELSEIF (insert_count < 0) THEN
            -- 系统异常
            ROLLBACK ;
            SET r_result = -2;
          ELSE
            -- 秒杀成功
            COMMIT ;
            SET r_result = 1;
          END IF;
        END IF;
  END;
$$

DELIMITER;
SET @r_result=-3;

call execute_seckill(1003,12345678945,now(),@r_result);
--优化事务行级锁持有的时间
--不宜过度依赖