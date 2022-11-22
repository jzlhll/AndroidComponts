### RecyclerView

#### 基础使用3要素

​	1. 实现`RecyclerView.Adapter<MyViewHolder>() ` 并且内部有`list<Bean>`并实现3个接口

	2. 自定义`MyViewHolder`，简单的将`findViewById`搞定为全局变量
	2. 别忘记设置`layoutManager`

* onBindViewHolder的2个区别

```java
public abstract void onBindViewHolder(@NonNull VH holder, int position);
public void onBindViewHolder(@NonNull VH holder, int position,
                @NonNull List<Object> payloads) 
```

一般情况，我们都是处理onBindViewHolder在初始化数据设置，或者notifyDataSetChanged() notifyItemChanged(int position)变化的时候处理；

当我们调用了notifyItemChanged(int position, @Nullable Object payload) 进行一个itemView的部分控件更新。

就需要实现下面payloads这个函数。



#### itemDecoration

[RecyclerView系列之一ItemDecoration_Luckie stone的博客-CSDN博客_recyclerview.state](https://blog.csdn.net/suyimin2010/article/details/86550236)

#### itemAnimator



#### DiffUtil

用来计算2个数列的差异。并且

* [Android的RV列表刷新？Payload 与 Diff 的方式有什么异同？ - 掘金 (juejin.cn)](https://juejin.cn/post/7156512023973462053)
* [[Android\] DiffUtil在RecyclerView中的使用详解_卖火柴的小男孩2020的博客-CSDN博客](https://blog.csdn.net/u014644594/article/details/87881157)
* [RecyclerView的好伴侣：详解DiffUtil_Luckie stone的博客-CSDN博客](https://blog.csdn.net/suyimin2010/article/details/106870884)
* [5.1.0| DiffUtil.ItemCallback实现细颗粒度的差分更新_茶不思基的博客-CSDN博客](https://blog.csdn.net/wsx1048/article/details/109326391?spm=1001.2101.3001.6650.3&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~Rate-3-109326391-blog-87881157.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~Rate-3-109326391-blog-87881157.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=4)



DiffUtil.Callback：这是最核心的类,你可以将它理解成比较新老数据集时的规则。

DiffUtil：通过静态方法DiffUtil.calculateDiff(DiffUtil.Callback)来计算数据集的更新。

DiffResult：是DiffUtil的计算结果对象，通过DiffResult.dispatchUpdatesTo(RecyclerView.Adapter)来进行更新。



使用方式：

```kotlin
DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallBack(mDatas, newDatas), true);
//前面的计算可以做成异步
mAdapter.setDatas(newDatas);
diffResult.dispatchUpdatesTo(mAdapter);
```

最后androidx库会帮你执行

```java
adapter.notifyItemRangeInserted(position, count);
adapter.notifyItemRangeRemoved(position, count);
adapter.notifyItemMoved(fromPosition, toPosition);
adapter.notifyItemRangeChanged(position, count, payload);
```
接下来解释一下callback作为一个比较对象工具的处理。

```java
 public abstract static class Callback {

        public abstract int getOldListSize();
        public abstract int getNewListSize();

        public abstract boolean areItemsTheSame(int oldItemPosition, int newItemPosition);

        public abstract boolean areContentsTheSame(int oldItemPosition, int newItemPosition);

        @Nullable
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return null;
        }
    }
```

areItemsTheSame()：判断是否是同一个Item。 
areContentsTheSame()：如果是同一个Item，此方法用于判断是否同一个 Item 的内容也相同。

前两个是获取数据集长度的方法，这没什么好说的。但是后两个方法，主要是为了对应多布局的情况产生的，也就是存在多个 viewType 和多个 ViewHodler 的情况。首先需要使用 areItemsTheSame() 方法比对是否来自同一个 viewType（也就是同一个 ViewHolder ） ，然后再通过 areContentsTheSame() 方法比对其内容是否也相等。

其实 Callback 还有一个 getChangePayload() 的方法，它可以在 ViewType 相同，但是内容不相同的时候，用 payLoad 记录需要在这个 ViewHolder 中，具体需要更新的View。

areItemsTheSame()、areContentsTheSame()、getChangePayload() 分别代表了不同量级的刷新。

**首先会通过 areItemsTheSame() 判断当前 position 下，ViewType 是否一致，如果不一致就表明当前 position 下，从数据到 UI 结构上全部变化了，那么就不关心内容，直接更新就好了。如果一致的话，那么其实 View 是可以复用的，就还需要再通过 areContentsTheSame() 方法判断其内容是否一致，如果一致，则表示是同一条数据，不需要做额外的操作。但是一旦不一致，则还会调用 getChangePayload() 来标记到底是哪个地方的不一样，最终标记需要更新的地方，最终返回给 DiffResult 。**

当然，对性能要是要求没那么高的情况下，是可以不使用 getChangedPayload() 方法的。



#### 列头拉动效果(虚假的下拉刷新)

即，虚假的下拉刷新。

todo 今天必须完成。



#### 下拉刷新或上拉刷新



#### Adapter的常用其他函数

`onViewDetachedFromWindow`： view贴上去了
`onViewAttachedToWindow` ：view下掉了。