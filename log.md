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