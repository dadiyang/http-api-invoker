package com.github.dadiyang.httpinvoker;

import com.github.dadiyang.httpinvoker.interfaces.CityService;
import com.github.dadiyang.httpinvoker.requestor.Requestor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * @author huangxuyang
 * date 2018/11/27
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestApplication.class)
public class HttpApiInvokerSpringTest {
    @Autowired
    private CityService cityService;
    @Autowired
    private Requestor requestor;

    @Test
    public void test() throws Exception {
        // 此处只测试是否能拿到CityService的实现对象
        assertNotNull(cityService);
        HttpApiInvokerTest test = new HttpApiInvokerTest();
        String host = "http://localhost:8080";
        test.setHost(host);
        test.setService(cityService);
        test.setRequestor(requestor);

        test.getAllCitiesTest();
        test.getCityTest();
        test.getCityByNameTest();
        test.getCityRest();

        test.saveCitiesTest();
        test.saveCityTest();


    }


}
