package org.lorislab.p6.process.events;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.LinkedList;
import java.util.List;

public class Test {

    private static int COUNT = 0;

    public static void main(String[] args) {
        Test t = new Test();

        TestData m = new TestData();
        m.name = "start";
        m.count = 0;

        Uni.createFrom().item(m)
                .onItem().apply(t::test).flatMap(x -> x)
                .subscribe()
                .with(c -> {
                    TestData d = c.data;
                    System.out.println("FINISHED: " + d.count);
                    System.out.println("--------");
                    for (TestData td : c.items) {
                        System.out.println("ITEMS: " + td.name);
                    }
                }, Throwable::printStackTrace);


//        return exec(t)
//                .onItem().produceUni(this::execute)
//                .repeat().until(i -> i > 10).collectItems().last();


    }

    public Uni<TestWrapper> test(TestData t) {
        return create(t).onItem().produceUni(this::exec2)
//                .map(this::next)
                .repeat().until(i -> i.data.count >= 10)
                .collectItems().last();
    }

    public Uni<TestWrapper> create(TestData t) {
        System.out.println("## CREATE " + t);
        TestWrapper r = new TestWrapper();
        r.start = t;
        r.data = t;
        return Uni.createFrom().item(r);
    }

    public TestData next(TestDataResult t) {
        TestData r = new TestData();
        r.count = t.input.count;
        r.result = t;
        return r;
    }

    public Uni<TestDataResult> exec(TestData t) {

        t.count = t.count + 1;
        System.out.println("Execute " + t.count);
        TestDataResult r = new TestDataResult();
        r.input = t;
        r.save = t.count >= 10;

        return Uni.createFrom().item(r);
    }

    public Uni<TestWrapper> exec2(TestWrapper t) {
        TestData last = t.data;
        TestData r = new TestData();
        r.count = last.count + 1;
        r.name = "ITEM " + r.count;
        t.items.add(r);
        t.data = r;
        System.out.println("Execute " + t.data.count);
        return Uni.createFrom().item(t);
    }

    public Uni<Integer> execute(int i) {
        System.out.println("EXECUTE " + i + " -- " + COUNT);
        return Uni.createFrom().item(COUNT++);
    }

    public static class TestData {
        public TestData t;
        public String name;
        public int count = 0;
        public TestDataResult result;
    }

    public static class TestDataResult {
        public TestData input;
        public boolean save = false;
    }

    public static class TestMessage {
        public int id = 0;
    }

    public static class TestWrapper {
        public TestData start;
        public TestData data;
        public List<TestData> items = new LinkedList<>();
    }
}
