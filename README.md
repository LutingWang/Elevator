BUG

第一次作业

bug1：

java.lang.Error: Maximum lock count exceeded.

原因：轮询时没有在提前退出的分支释放锁，因此锁被不停申请最终超过数量上限。

bug2：

Input:
[0.0]1-FROM-1-TO-2

Output:
OPEN-1
IN-1-1
CLOSE-1
OPEN-2
CLOSE-2
OPEN-2
CLOSE-2
...

分析：没有进入PeopleIn线程

原因：Elevator线程获得写锁后没有释放，因此PeopleIn线程无法获得读锁。

bug3:

层间转移时间过短。

分析：问题出在NULL状态转RUNNING状态时，和优化算法有关

原因：优化时为了预测未来，允许NULL状态直接跳转到上下层，但是没有限制时间。应限制在NULL状态至少停留0.4秒才能转移。

bug4：

死锁

分析：使用condition过于僵硬，为了获取锁而申请，并非真的需要数据才获取锁。因此在分析共享数据的关系时没有考虑到死锁，而为了实现等待唤醒机制，需要申请锁在唤醒。这时出现了死锁。