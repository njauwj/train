package com.wj.train.business;

import com.wj.train.business.domain.Train;
import com.wj.train.business.domain.TrainExample;
import com.wj.train.business.mapper.TrainMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author wj
 * @create_time 2023/7/17
 * @description
 */
@SpringBootTest
public class Test1 {

    @Resource
    private TrainMapper trainMapper;

    @Test
    void test1() {
        TrainExample trainExample = new TrainExample();
        trainExample.createCriteria().andCodeEqualTo("111");
        //没有数据时，trains为空集合而不是 null
        List<Train> trains = trainMapper.selectByExample(trainExample);
        System.out.println(trains);
    }

}
