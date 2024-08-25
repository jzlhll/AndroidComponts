package com.allan.androidlearning.alg;

public class AdcFilter {
    public AdcFilter(int maxSize, int diffValue) {
        MAX_DIFF_VALUE = diffValue;
        MAX_SIZE = maxSize;
        data = new int[MAX_SIZE];
    }

    public AdcFilter(int diffValue) {
        this(6, diffValue);
    }

    public AdcFilter() {
        this(6, 4);
    }

    private final int MAX_DIFF_VALUE;
    private final int MAX_SIZE;
    private final int[] data;

    private int in_data;
    private int changeIndex;
    private int dataSize = 0;

    private int clippingOldData;

    private int clipping_filter_run(int clippingInData) {
        if(Math.abs(clippingInData - clippingOldData) < MAX_DIFF_VALUE) {
            return clippingOldData;//让旧值生效
        } else {
            clippingOldData = clippingInData;//新值变旧值
            return clippingInData;//让新值生效
        }
    }

    private int recursive_average_run() {
        data[changeIndex] = in_data;//将新值覆盖掉数组中的一个数据
        if (dataSize < MAX_SIZE) {
            dataSize++;
        }

        if(changeIndex < MAX_SIZE - 1)
            changeIndex++;
        else
            changeIndex = 0;

        //计算平均值
        int sum = 0;//清空总和
        for(int loop = 0; loop < dataSize; loop++)
        {
            sum += data[loop];//计算总和
        }

        return (sum / dataSize);
    }

    //限幅平均滤波运算
    public int limiting_average_run(int in_data)
    {
        this.in_data = clipping_filter_run(in_data);//启动限幅运算,将运算结果给到递推平均滤波函数
        return recursive_average_run();//读出结果
    }
}