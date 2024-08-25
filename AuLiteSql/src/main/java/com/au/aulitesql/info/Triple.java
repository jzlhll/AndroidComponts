package com.au.aulitesql.info;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Triple<P1, P2, P3> {
    public final P1 p1;
    public final P2 p2;
    public final P3 p3;

    Triple(P1 p1, P2 p2, P3 p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    /**
     * 比较2个列表。得到第一部分，相同的；第二部分，news单独的；第三部分，olds单独的。
     */
    public static <T> Triple<List<T>,List<T>,List<T>> compare(@NonNull List<T> news,
                                                                     @NonNull List<T> olds,
                                                              @NonNull IEquals<T> eqFun) {
        List<T> sames = new ArrayList<>();
        List<T> indNews = new ArrayList<>();
        List<T> indOlds = new ArrayList<>();

        for (T newOne : news) {
            boolean isInOld = false;
            for (T oldOne : olds) {
                if (eqFun.equals(newOne, oldOne)) {
                    sames.add(newOne);
                    isInOld = true;
                }
            }
            if (!isInOld) {
                indNews.add(newOne);
            }
        }

        for (T oldOne : olds) {
            if (!news.contains(oldOne)) {
                indOlds.add(oldOne);
            }
        }

        return (Triple<List<T>, List<T>, List<T>>) new Triple(
                sames,
                indNews,
                indOlds
        );
    }

    public interface IEquals<T> {
        boolean equals(T t1, T t2);
    }
}
