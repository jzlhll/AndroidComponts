package com.au.module_android.utils;

import java.util.LinkedList;
import java.util.List;

public class FixedSizeLinkedList<E> extends LinkedList<E> {
    private final int capacity;

    public FixedSizeLinkedList(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean add(E e) {
        super.add(e);
        while (size() > capacity) {
            removeFirst(); // O(1)时间复杂度
        }
        return true;
    }

    /**
     * 取出前 num 个元素（FIFO 顺序），并移除它们
     * @param num 要取出的元素数量
     * @return 取出的元素列表（如果 num > size()，返回所有可用元素）
     * @throws IllegalArgumentException 如果 num <= 0
     */
    public List<E> pollFirstN(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("num must be positive");
        }

        List<E> result = new LinkedList<>();
        int actualNum = Math.min(num, size()); // 避免越界

        for (int i = 0; i < actualNum; i++) {
            result.add(removeFirst()); // 逐个取出并移除
        }

        return result;
    }
}
