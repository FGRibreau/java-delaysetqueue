package org.fg.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelaySetQueueTest {

  /**
   * Stub
   */
  class TestItem implements Comparator, Delayed {

    public int getVal(){
      return a;
    }

    private int  a;
    private long delay;

    public TestItem(int val, int delay){
      this.a = val;
      this.delay = System.currentTimeMillis() + delay;
    }

    @Override
    public int compare(Object t1, Object t2){
      return ((TestItem) t1).getVal() - ((TestItem) t2).getVal();
    }

    @Override
    public long getDelay(TimeUnit timeUnit){
      return timeUnit.convert(delay - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed delayed){
      return compare(this, delayed);
    }
  }

  /**
   * Test
   */

  @Test
  public void testConstructor(){
    DelaySetQueue<TestItem> queue = new DelaySetQueue<TestItem>(null);
    Assert.assertNotNull(queue);
  }

  @Test
  public void testadd(){
    DelaySetQueue<TestItem> queue = new DelaySetQueue<TestItem>(null);

    queue.add(new TestItem(1, 10000));
    queue.add(new TestItem(2, 10000));

    // Queue size should be 2
    Assert.assertEquals(2, queue.size());
    Assert.assertEquals(2, queue.setSize());
  }

  @Test
  public void testaddSave(){
    DelaySetQueue<TestItem> queue = new DelaySetQueue<TestItem>(null);

    queue.add(new TestItem(1, 10000));
    queue.add(new TestItem(2, 10000));
    queue.add(new TestItem(1, 10000));

    // Queue size should be 1
    Assert.assertEquals(2, queue.size());
    Assert.assertEquals(2, queue.setSize());
  }

  @Test
  public void testTake() throws InterruptedException{
    DelaySetQueue<TestItem> queue = new DelaySetQueue<TestItem>(null);

    queue.add(new TestItem(1, 100));
    queue.add(new TestItem(1, 10000));
    queue.add(new TestItem(1, 10000));
    queue.add(new TestItem(2, 100));
    queue.add(new TestItem(2, 100));
    queue.add(new TestItem(2, 100));
    queue.add(new TestItem(1, 100));
    queue.add(new TestItem(4, 200));// 2sec
    queue.add(new TestItem(3, 1));

    Assert.assertEquals(4, queue.size());

    Assert.assertEquals(queue.take().getVal(), 1);
    Assert.assertEquals(((TestItem) queue.take()).getVal(), 2);

    Assert.assertEquals(2, queue.size());
    Assert.assertEquals(2, queue.setSize());

    Assert.assertEquals(queue.take().getVal(), 3);
    Assert.assertEquals(((TestItem) queue.take()).getVal(), 4);


    Assert.assertEquals(0, queue.size());
    Assert.assertEquals(0, queue.setSize());
  }

  @Test
  public void testConcurrent() throws InterruptedException{
    final DelaySetQueue<TestItem> queue = new DelaySetQueue<TestItem>(null);
    final TreeSet<TestItem> set = new TreeSet();

    // Put
    Runnable add1 = new Runnable() {
      @Override
      public void run(){
        int i = 9000;
        while (i-- > 0) {
          TestItem item = new TestItem((int) (Math.round(Math.random() * 1000)), 200);
          queue.add(item);// wait 2 sec each time
        }
      }
    };

    Runnable add2 = new Runnable() {
      @Override
      public void run(){
        int i = 100000;
        while (i-- > 0) {
          TestItem item = new TestItem((int) (Math.round(Math.random() * 500)), 100);
          queue.add(item);// wait 1 sec each time
        }
      }
    };

    // Take
    Runnable take1 = new Runnable() {
      @Override
      public void run(){

        while (queue.size() > 0) {
          try {
            TestItem item = queue.take();

            synchronized (set) {
              if (!set.add(item)) {
                Assert.fail("item already present in list");
              }
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    };

    Runnable take2 = new Runnable() {
      @Override
      public void run(){

        while (queue.size() > 0) {
          try {
            TestItem item = queue.take();
            synchronized (set) {
              if (!set.add(item)) {
                Assert.fail("item already present in list");
              }
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    };

    new Thread(add1).run();
    new Thread(add2).run();
    new Thread(take1).run();
    new Thread(take2).run();
    Assert.assertNotNull("ok", "ok");
  }
}
