package org.fg.concurrent;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;

/**
 * RIBREAU Francois-Guillaume Date: 27/11/12 - 14:07
 */
public class DelaySetQueue<E extends Delayed> extends DelayQueue<E> {

  private ConcurrentSkipListSet<E> set;

  public DelaySetQueue(Comparator<? super E> setComparator){
    super();
    set = new ConcurrentSkipListSet<E>(setComparator);
  }



  @Override
  public boolean add(E e){
    // If e is inside the set do nothing
    if(!set.add(e)){
      return false;
    }

    super.add(e);

    return true;
  }

  /**
   * A "synchronized" here block the execution of all other methods
   *
   * @return
   *
   * @throws InterruptedException
   */
//  @Override
  public E take() throws InterruptedException{
    E item = super.take();
    set.remove(item);
    return item;
  }

  public int setSize(){
    return set.size();
  }
}
