package org.example.drools_persistance_spring.service;

import lombok.extern.slf4j.Slf4j;
import org.example.drools_persistance_spring.Application;
import org.example.drools_persistance_spring.conf.DataSourceStub;
import org.example.facts.data.LongFact;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by maksim.drobyshev on 28-Sep-20.
 */
@Slf4j
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration(classes = Application.class)
@Import(DataSourceStub.class)
public class DroolsServiceTest {

    private static final int COUNT = 2500;

    @Autowired
    private DroolsService droolsService;

    @Test
    public void singleSessionWritesTest() throws InterruptedException {
        Executor executor = Executors.newFixedThreadPool(16);
        String id = UUID.randomUUID().toString();
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger finished = new AtomicInteger();
        long time = System.currentTimeMillis();
        for(int i = 0; i < COUNT; i++) {
            executor.execute(() -> {
                droolsService.processFacts(id, new LongFact(counter.incrementAndGet()));
                finished.incrementAndGet();
            });
        }
        while (finished.get() != COUNT) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        long end = System.currentTimeMillis();
        log.info("{} execution time. {} op per second", (end - time), (0.0 + end - time) / COUNT);

        Assert.assertEquals(COUNT, droolsService.sessionDump(id).stream().filter(it -> it instanceof LongFact).count());
        Assert.assertEquals(COUNT, droolsService.processQuery(id, "getLongs").size());
    }

    @Test
    public void multipleWritesTest() throws InterruptedException {
        Random random = new Random();
        Executor executor = Executors.newFixedThreadPool(16);
        List<String> ids = new ArrayList<>();
        for(int i = 0; i < 16; i++) {
            ids.add(UUID.randomUUID().toString());
        }
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger finished = new AtomicInteger();
        long time = System.currentTimeMillis();
        for(int i = 0; i < COUNT; i++) {
            executor.execute(() -> {
                droolsService.processFacts(ids.get(random.nextInt(ids.size())), new LongFact(counter.incrementAndGet()));
                finished.incrementAndGet();
            });
        }
        while (finished.get() != COUNT) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        long end = System.currentTimeMillis();
        log.info("{} execution time. {} op per second", (end - time), (0.0 + end - time) / COUNT);

        int result = 0;
        int resultQ = 0;
        for(String id : ids) {
            result += droolsService.sessionDump(id).stream().filter(it -> it instanceof LongFact).count();
            resultQ += droolsService.processQuery(id, "getLongs").size();
        }
        Assert.assertEquals(COUNT, result);
        Assert.assertEquals(COUNT, resultQ);
    }

    @Test
    public void checkStored() {
        String id = "dfd64fbe-4fb8-4097-a32a-7d2d33334025";
        Assert.assertEquals(COUNT, droolsService.sessionDump(id).stream().filter(it -> it instanceof LongFact).count());
        Assert.assertEquals(COUNT, droolsService.processQuery(id, "getLongs").size());
    }
}