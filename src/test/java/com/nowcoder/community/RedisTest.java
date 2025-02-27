package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void testString() {
        System.out.println(redisTemplate.getClass().getName());
//        String key = "test3";
//
//        redisTemplate.opsForValue().set(key,1);
//        System.out.println(redisTemplate.opsForValue().get(key));
//        System.out.println(redisTemplate.opsForValue().increment(key));
//        System.out.println(redisTemplate.opsForValue().decrement(key));
    }

    @Test
    public void testHash() {
        String key = "test:hash";

        redisTemplate.opsForHash().put(key,"userName","cx");
        redisTemplate.opsForHash().put(key,"userSex","1");
        System.out.println(redisTemplate.opsForHash().get(key,"userName"));
        System.out.println(redisTemplate.opsForHash().get(key,"userSex"));

    }

    @Test
    public void testList() {
        String key = "test:list";

        redisTemplate.opsForList().leftPush(key,101);
        redisTemplate.opsForList().leftPush(key,102);
        redisTemplate.opsForList().leftPush(key,103);
        System.out.println(redisTemplate.opsForList().size(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().size(key));
    }

    @Test
    public void testSet() {
        String key = "test:set";
        redisTemplate.opsForSet().add(key,101,102,103,104,104);
        System.out.println(redisTemplate.opsForSet().size(key));
        System.out.println(redisTemplate.opsForSet().pop(key));
        System.out.println(redisTemplate.opsForSet().members(key));

    }

    @Test
    public void testZSet() {
        String key = "test:zset";

        redisTemplate.opsForZSet().add(key,"aaa",100);
        redisTemplate.opsForZSet().add(key,"bbb",101);
        redisTemplate.opsForZSet().add(key,"ccc",102);

        System.out.println(redisTemplate.opsForZSet().zCard(key));
        System.out.println(redisTemplate.opsForZSet().reverseRank(key,"ccc"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(key,0,1));
    }

    @Test
    public void testKeys() {
        System.out.println(redisTemplate.delete("test"));
        System.out.println(redisTemplate.hasKey("test"));

    }

    @Test
    public void testTx() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String key = "test:tx2";
                redisOperations.multi();
                redisOperations.opsForSet().add(key,101,102,103,104,104);
                System.out.println(redisOperations.hasKey("test:tx2"));
                redisOperations.hasKey(key);
                return redisOperations.exec();
            }
        });
        System.out.println(obj);
    }
}
