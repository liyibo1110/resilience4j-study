package com.github.liyibo1110.resilience4j.circularbuffer;

import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * 线程安全的固定容量 + 自动淘汰的队列实现。
 * 写入新数据时，如果队列没满，则写入到尾部，否则删除最老元素，再加入新元素。
 * 底层结构就是个环形数组。
 * @author liyibo
 * @date 2026-02-10 13:40
 */
public class ConcurrentEvictingQueue<E> extends AbstractQueue<E> {
    private static final String ILLEGAL_CAPACITY = "Capacity must be bigger than 0";
    private static final String ILLEGAL_ELEMENT = "Element must not be null";
    private static final String ILLEGAL_DESTINATION_ARRAY = "Destination array must not be null";
    private static final Object[] DEFAULT_DESTINATION = new Object[0];
    private static final int RETRIES = 5;

    /** 最大容量 */
    private final int maxSize;

    /** 控制并发的核心（利用时间戳实现的悲观锁or乐观锁，没使用ReentrantReadWriteLock这类） */
    private final StampedLock stampedLock;

    /** 当前容量，所有的写操作内部保证了这个值的线程安全，加了volatile只是为了提供读字段值的可见性 */
    private volatile int size;

    /** 底层存储结构 */
    private Object[] ringBuffer;
    private int headIndex;
    private int tailIndex;
    private int modificationsCount;

    public ConcurrentEvictingQueue(int capacity) {
        if(capacity <= 0)
            throw new IllegalArgumentException(ILLEGAL_CAPACITY);
        maxSize = capacity;
        ringBuffer = new Object[capacity];
        size = 0;
        headIndex = 0;
        tailIndex = 0;
        modificationsCount = 0;
        stampedLock = new StampedLock();
    }

    @Override
    public Iterator<E> iterator() {
        return this.readConcurrently(() -> new Iter(headIndex, modificationsCount));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(E e) {
        requireNonNull(e, ILLEGAL_ELEMENT);
        Supplier<Boolean> offerElement = () -> {
            if(size == 0) { // 空队列，直接写入
                ringBuffer[tailIndex] = e;
                modificationsCount++;
                size++;
            }else if(size == maxSize) { // 满队列，淘汰最老，注意size不变
                headIndex = nextIndex(headIndex);
                tailIndex = nextIndex(tailIndex);
                ringBuffer[tailIndex] = e;
                modificationsCount++;
            }else { // 不空也没满
                tailIndex = nextIndex(tailIndex);
                ringBuffer[tailIndex] = e;
                size++;
                modificationsCount++;
            }
            return true;
        };
        return writeConcurrently(offerElement);
    }

    /**
     * 注意是取head下标对应的元素（因为是先进先出）
     */
    @Override
    public E poll() {
        Supplier<E> pollElement = () -> {
            if(size == 0)
                return null;
            E result = (E)ringBuffer[headIndex];
            ringBuffer[headIndex] = null;
            if(size != 1)   // 如果还有其它元素，就移动headIndex一格
                headIndex = nextIndex(headIndex);
            size--;
            modificationsCount++;
            return result;
        };
        return writeConcurrently(pollElement);  // 相当于写入null，然后返回旧数据
    }

    @Override
    public E peek() {
        return readConcurrently(() -> {
            if(size == 0)
                return null;
            return (E)ringBuffer[headIndex];
        });
    }

    @Override
    public void clear() {
        Supplier<Object> clearStrategy = () -> {
            if(size == 0)
                return null;
            Arrays.fill(ringBuffer, null);
            size = 0;
            headIndex = 0;
            tailIndex = 0;
            modificationsCount++;
            return null;
        };
        writeConcurrently(clearStrategy);
    }

    @Override
    public Object[] toArray() {
        if(size == 0)
            return new Object[0];
        Object[] dest = toArray(DEFAULT_DESTINATION);
        return dest;
    }

    @Override
    public <T> T[] toArray(final T[] dest) {
        requireNonNull(dest, ILLEGAL_DESTINATION_ARRAY);
        Supplier<T[]> copyRingBuffer = () -> {
            if(size == 0)
                return dest;
            T[] result = dest;
            if(dest.length < size)
                result = (T[]) Array.newInstance(result.getClass().getComponentType(), size);
            if(headIndex <= tailIndex)  // 没有绕环，即初始队列还没有满过，直接顺序复制
                System.arraycopy(ringBuffer, headIndex, result, 0, size);
            else {  // 分批复制
                int toTheEnd = ringBuffer.length - headIndex;
                System.arraycopy(ringBuffer, headIndex, result, 0, toTheEnd);
                System.arraycopy(ringBuffer, 0, result, toTheEnd, tailIndex + 1);
            }
            return result;
        };
        return readConcurrentlyWithoutSpin(copyRingBuffer);
    }

    /**
     * 将索引下标，指向下一个元素
     */
    private int nextIndex(final int ringIndex) {
        int nextIndex = ringIndex + 1;
        if(nextIndex == maxSize)
            return 0;
        return nextIndex;
    }

    /**
     * 执行给定的supplier，同时保证并发读取队列中的特定数据（乐观锁 + 自旋 or 悲观锁）
     * 注意这个方法通用性语义很强，不单单可以读取队列里面的元素，相当于是个并发安全的对象生成器。
     */
    private <T> T readConcurrently(final Supplier<T> readSupplier) {
        T result;
        long stamp;
        // 先尝试乐观锁 + 自选
        for(int i = 0; i < RETRIES; i++) {
            stamp = stampedLock.tryOptimisticRead();    // 乐观锁
            if(stamp == 0)
                continue;
            // 到这里说明获取到锁了
            result = readSupplier.get();
            if(stampedLock.validate(stamp)) // 验证是否并发安全，即没有其它线程也读过
                return result;
        }
        // 到这里说明自旋过后也没有读到数据，说明并发读比较激烈，直接切换成悲观锁
        stamp = stampedLock.readLock();
        try {
            result = readSupplier.get();
        } finally {
            stampedLock.unlockRead(stamp);
        }
        return result;
    }

    /**
     * 执行给定的supplier，同时保证并发读取队列中的特定数据（直接悲观锁）
     */
    private <T> T readConcurrentlyWithoutSpin(final Supplier<T> readSupplier) {
        T result;
        long stamp = stampedLock.readLock();
        try {
            result = readSupplier.get();
        } finally {
            stampedLock.unlockRead(stamp);
        }
        return result;
    }

    /**
     * 执行给定的supplier，同时保证并发写入特定数据到队列（直接悲观锁）
     */
    private <T> T writeConcurrently(final Supplier<T> writeSupplier) {
        T result;
        long stamp = stampedLock.writeLock();
        try {
            result = writeSupplier.get();
        } finally {
            stampedLock.unlockWrite(stamp);
        }
        return result;
    }

    private class Iter implements Iterator<E> {
        private int visitedCount = 0;
        private int cursor;
        private int expectedModificationsCount; // 检测遍历时的并发安全

        Iter(final int headIndex, final int modificationsCount) {
            this.cursor = headIndex;
            this.expectedModificationsCount = modificationsCount;
        }

        @Override
        public boolean hasNext() {
            return visitedCount < size;
        }

        @Override
        public E next() {
            Supplier<E> nextElement = () -> {
                checkForModification(); // 迭代器的标准行为，原数据列表被修改，则直接抛出异常
                if(visitedCount >= size)
                    throw new NoSuchElementException();
                E item = (E) ringBuffer[cursor];
                cursor = nextIndex(cursor);
                visitedCount++;
                return item;
            };
            return readConcurrently(nextElement);
        }

        /**
         * 检测遍历过程中，原队列元素数量是否有了变化
         */
        private void checkForModification() {
            if(modificationsCount != expectedModificationsCount)
                throw new ConcurrentModificationException();
        }
    }
}
