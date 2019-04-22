[TOC]

# 总 

本博文是2019年北航面向对象（OO）课程第二单元作业（电梯）的总结。三次作业的要求大致如下：

- 第一次作业：单部多线程傻瓜调度（FAFS）电梯的模拟
- 第二次作业：单部多线程可捎带调度（ALS）电梯的模拟
- 第三次作业：多部多线程智能（SS）调度电梯的模拟

源代码及项目要求均已发布到 [github](https://github.com/LutingWang/Elevator "Luting") ，读者可以下载检查。以下将对这一单元作业进行简单总结。

## 架构

项目的总体构架参考 $MVC$ 模式，将电梯、调度器、输入输出分为三个包进行编码。

### controller 

主要由 $Controller$ 类组成，负责连接输入输出和电梯，以及启动线程。在 $Controller$ 类中包含了所有的公共常数，比如 $DEBUG$ 等。

### model

$model$ 包中主要有调度器、人群模拟器、电梯模拟器三个部分。

- 调度器并不是一个线程，而是一个工具类。他包含了一些调度常用的调度函数，供电梯的内部类和控制器类调用。调度器的主要任务是将实时输入的请求做出合理规划并分配给特定电梯。
- 人群模拟器是一个线程安全的 $arrayList$ 包装类，他模拟了电梯内外人群的特征。这个类的存在主要是为了减轻电梯类的模拟负担。
- 电梯中包含了前面两个部分的子类，作为内部类，同时也是两个线程。这比较符合现实中电梯的组织形式，即电梯中的控制器和人群对电梯的内部信息具有访问权限。在一般的电梯群控系统中，所有电梯共享一个总控器，并对其负责。而每个电梯各自有一个子控器，有这个子控器生成自身运行的指令。由于子控器的依赖函数处在总控器中，为了更好的封装，我将两者赋予了继承关系。而电梯内外的人群具有自行进出的功能。

这样的设计最根本的是为了解决电梯线程运行时逻辑复杂从而导致计时不准的情况。尽管最终被证明这样的优化并没有用。

### view

$view$ 包中包含 $io$ 的全部操作。由于输入输出类由助教团队提供，本包只是对这些方法的进一步包装，使得他们更容易被其他类调用。

## 优化算法

本单元作业推荐了 $FAFS$ 和 $ALS$ 两种算法。但是根据实际经验，单纯使用这两种算法的性能分并不会很高。事实上，性能分的计算是一种统计平均。也就是说，助教团队希望**同学们课下交流算法，并且所有同学使用同一种算法完成作业**。这是因为，在假设大多数人使用同一种算法编程时（实际如此），测试点的运行时间方差会很小。这时无论更快或是更慢，很小的时间差都会导致性能分的大幅播动。一般来说，大多数人选择的算法在性能上即使不是最好，也是一种平均意义上的较优。如果不能保证自己的算法一定比大多数人采用的算法运行效率高，那么最好随波逐流。否则，即使你的算法在平均意义上优于这种算法，也可能因为测试数据的**随机性**丢掉几乎全部性能分。

同时，实际经验显示在一定范围内越简单的调度算法效率越高。也就是说我们并不需要对复杂情况进行考虑，只要算法选的好，一切好说。到目前为止我和我周围的人都不能对这种：算法复杂性降低而电梯运行效率提高的异常现象做出解释。

因此此处只是记录最常见的 $Look$ 算法，以及一些可能有用的小伎俩。

### Look 算法

$Look$ 算法就是平时比较常见的电梯调度算法。电梯首先向上运行至不再有更高层请求为止，而后向下运行，直到没有更底层的请求为止，以此类推。

### 多种算法取优

单独的某一种算法可能无法对所有情况得到最优解，但是算法种类越多，可以覆盖的情况也就越多。在评测机取消了 $PrintStream$ 功能后，使用已有电梯类进行运算不太可行。但是仍然可以将其稍作修改，在调度器中仿真运行。最终得到多种算法计算出的最优解。即使这种方法也不能保证取到全局最优，但是已经几乎时已知信息可以得到的局部最优解了。（由于太过复杂，没有亲测。以上内容，纯属推测，无效勿怪）

### 预测未来

由于电梯每个时刻的运行状态不需要实时输出，因此我可以在新的请求输入后，临时改变过去电梯的运行轨迹。宏观来看，仿佛是过去的电梯可以预测未来。具体的做法是，在电梯等待或运行时，先不改变电梯的所属层。直到电梯的等待时间到，说明在运行时没有新的请求出现，电梯可以按照原计划运行。但是如果电梯被强行打断，说明新的请求使得原来的调度方案无效，需要按照新的调度方案运行。这时电梯就可以随意切换其所属楼层。

在第六次作业之前，这种做法可以跨越多层。但是在第七次作业中，电梯到达每层都需要输出信息，这一算法的优化率也就低了许多。

## 多线程

~~数学建模能力~~多线程设计是本单元作业的考察重点。以下按照三次作业分别分析多线程的协同和同步控制方面的设计。

### 第五次作业

#### 线程

第一次作业比较简单，涉及到的线程主要有

- Controller.main(String[])
- Input.run()
- Controller.PeopleOut.run()
- Elevator.run()
- Elevator.PeopleIn.run()
- Elevator.Manager.run()

这些线程的关系是：控制器线程初始化其他线程，最先结束；输入线程在接收到新的请求后会调用函数，将请求传入电梯控制器；电梯类自身线程会根据控制器的调度指令运行，是最简单的线程之一；$PeopleOut$ 和$PeopleIn$ 线程会依靠轮询确定自身是否做出反应，用于分担电梯线程的运行压力；电梯控制器线程会用轮询的方式对请求遍历，为电梯线程提供最新的调度策略。

#### 共享数据

上述涉及的共享数据主要有以下几个

- Controller.out: 电梯外人群
- Elevator.in: 电梯内人群
- Elevator.status: 电梯运行状态，有 NULL, OPEN, UP, DOWN
- Elevator.Manager.stop: 控制器给出的在当前楼层停车请求
- Elevator.Manager.dir: 控制器给出的运行方向

其他数据要么是线程安全的，要么只会被以特定的顺序访问，不会造成安全问题。其中 Controller.out 可以被电梯类及其内部类读写，也可以被输入线程写入；Elevator.in 可以被 Controller.out 写入，也可以被 Elevator.Manager 读取；Elevator.status 可以被电梯及其所有内部类读写；而 Elevator.Manager 的共享属性则需要被其自身写入，被电梯类读取。

#### 设计策略

对于这五个共享数据，我为每个都申请了一把锁。尤其是 $Elevator.status$ 的锁，不仅可以保证线程互斥，避免了并发修改；还保证了线程同步，即电梯关门的行为必须发生在上下人结束后（电梯关门和上下人均需要对 $status$ 进行修改）。

### 第六次作业

#### 线程

第二次作业的要求并没有很大改动，但是由于限制了 $CPU$ 的运行时间，我还是将架构做了微小改动。新架构中的线程主要有

- Controller.main(String[])
- Input.run()
- Elevator.run()
- Elevator.PeopleIn.run()
- Elevator.Manager.run()

可以看到，我取消了 Controller.PeopleOut 线程。这是因为其功能和 Elevator.PeopleIn 的功能相似度极高，没有必要作为两个线程存在。这样修改后，全部与请求有关的操作均由 Elevator.PeopleIn 线程完成。但 Controller.PeopleOut 对象还存在，只是不作为单独线程，只提供对外接口。

#### 共享数据

修改后，程序中的共享数据也对应减少了（主要是线程少了一个），包括

- Controller.out: 电梯外人群
- Elevator.status: 电梯运行状态，有 NULL, OPEN, UP, DOWN
- Elevator.Manager.stop: 控制器给出的在当前楼层停车请求
- Elevator.Manager.dir: 控制器给出的运行方向

由于 Elevator.in 只由 Elevator.PeopleIn 线程读写，不属于共享数据，故将其删除。但 Controller.out 仍然需要同时被电梯类和输入类读写，仍属于共享数据。

#### 设计策略

第六次作业的修改策略主要是合并不必要的线程，进而减少共享数据的个数。由于多线程本身比单线程复杂的多，编程时首先选择的应该是单线程编程。只有要求同步运行的代码才由必要使用多线程编程。这样的原则可以在一定程度上减少线程个数，减轻 debug 负担，同时减少错误发生的可能性。

### 第七次作业

#### 线程

第七次作业中涉及到三部电梯，同时我对于系统的架构又进行了微调。最终的线程如下

- Controller.main(String[])
- Input.run()
- ElevatorA.run()
- ElevatorA.PeopleIn.run()
- ElevatorA.Manager.run()
- ElevatorB.run()
- ElevatorB.PeopleIn.run()
- ElevatorB.Manager.run()
- ElevatorC.run()
- ElevatorC.PeopleIn.run()
- ElevatorC.Manager.run()

本质上，除了增加了三个电梯以外，线程设计上没有明显区别。但是由于新增的几个电梯，共享数据的数量由明显增加。

#### 共享数据

- ElevatorX.out: 电梯外人群
- ElevatorX.status: 电梯运行状态，有 NULL, OPEN, UP, DOWN
- ElevatorX.Manager.stop: 控制器给出的在当前楼层停车请求
- ElevatorX.Manager.dir: 控制器给出的运行方向

可以看到，原本的 Controller.out 变成了 ElevatorX.out 。这是因为三部电梯每个都应有自己的目标人群，而不应该共享同一个人群。现在所有的共享数据几乎都是用于在电梯类和控制器类之间传递信息所用，因此解决其安全问题并不复杂。

#### 设计策略

本次作业的设计策略主要是将共享数据集中在两个类之间，减少可能造成线程安全问题的线程个数。这样虽然不能让潜在的线程安全问题减少，但是可以将错误的出现区域锁定在较小的范围内，降低 debug 难度。

# 代码静态分析

以下使用 $Metrics$ 和 $Statistics$ 等插件对最终项目代码进行静态分析。

## UML 类图

![uml](https://img2018.cnblogs.com/blog/1615581/201904/1615581-20190423010949824-1811073132.png)

## 类复杂度

| Class                     | OCavg |  WMC |
| ------------------------- | ----: | ---: |
| controller.Controller     |  2.25 |    9 |
| controller.ControllerTest |     1 |    2 |
| controller.Tools          |     2 |    2 |
| model.Elevator            |  2.09 |   23 |
| model.Elevator.Dir        |   n/a |    0 |
| model.Elevator.PeopleIn   |  1.67 |   10 |
| model.Elevator.Status     |   n/a |    0 |
| model.Elevator.SubManager |  2.78 |   25 |
| model.Manager             |  5.25 |   21 |
| model.People              |  1.25 |   10 |
| model.Person              |  1.29 |    9 |
| model.RedefinableAttr     |   1.4 |    7 |
| view.Input                |     2 |    8 |
| view.Output               |     1 |    6 |

### 类总代码规模

| Source File          | Total Lines | Source Code Lines | Source Code Lines[%] | Comment Lines | Comment Lines[%] | Blank Lines | Blank Lines[%] |
| -------------------- | ----------: | ----------------: | -------------------: | ------------: | ---------------: | ----------: | -------------: |
| AutoStart.java       |          29 |                24 |                 0.83 |             0 |              0.0 |           5 |           0.17 |
| Controller.java      |          94 |                76 |                 0.81 |             4 |             0.04 |          14 |           0.15 |
| ControllerTest.java  |          30 |                23 |                 0.77 |             1 |             0.03 |           6 |           0.20 |
| Elevator.java        |         353 |               317 |                 0.90 |             0 |              0.0 |          36 |           0.10 |
| Input.java           |          57 |                47 |                 0.82 |             3 |             0.05 |           7 |           0.12 |
| Manager.java         |          99 |                90 |                 0.91 |             1 |             0.01 |           8 |           0.08 |
| Output.java          |          40 |                32 |                 0.80 |             0 |              0.0 |           8 |           0.20 |
| People.java          |          75 |                65 |                 0.87 |             0 |              0.0 |          10 |           0.13 |
| Person.java          |          54 |                43 |                0.880 |             0 |              0.0 |          11 |           0.20 |
| RedefinableAttr.java |          39 |                32 |                 0.82 |             0 |              0.0 |           7 |           0.18 |
| Tools.java           |          12 |                10 |                 0.83 |             0 |              0.0 |           2 |           0.17 |

可以看出所有类中，除了 Elevator 类，规模都比较小。Elevator 类之所以行数较多主要是因为它包含两个内部类 SubManager 和 PeopleIn 。因此平均来说每个类的规模都在 $100$ 行以内，类总代码规模合适。

### 属性个数

从 $UML$ 类图中可以看出，每个类的属性个数大多在 $5$ 个以下。属性个数较多的类有电梯类和控制器类，但其中大半是常数或锁。真正有效的属性个数只有分别 $5$ 个和 $1$ 个。因此每个类的属性个数都适中。

## 方法复杂度

| Method                                                     | ev(G) | iv(G) | v(G) |
| ---------------------------------------------------------- | :---: | :---: | :--: |
| controller.AutoStart.isDeamon()                            |   1   |   1   |  1   |
| controller.AutoStart.start()                               |   1   |   1   |  1   |
| controller.Controller.isInputAlive()                       |   1   |   1   |  1   |
| controller.Controller.main(String[])                       |   1   |   2   |  2   |
| controller.Controller.newPerson(Person)                    |   1   |   1   |  1   |
| controller.Controller.setInputAlive(boolean)               |   1   |   2   |  2   |
| controller.ControllerTest.main()                           |   1   |   1   |  1   |
| controller.ControllerTest.temp()                           |   1   |   1   |  1   |
| controller.Tools.threadMonitor()                           |   1   |   2   |  2   |
| model.Elevator.Elevator(String,int,ArrayList<Integer>,int) |   1   |   1   |  1   |
| model.Elevator.PeopleIn.PeopleIn(int)                      |   1   |   1   |  1   |
| model.Elevator.PeopleIn.addPerson(Person)                  |   2   |   1   |  2   |
| model.Elevator.PeopleIn.full()                             |   1   |   1   |  1   |
| model.Elevator.PeopleIn.getThreadName()                    |   1   |   1   |  1   |
| model.Elevator.PeopleIn.isDeamon()                         |   1   |   1   |  1   |
| model.Elevator.PeopleIn.run()                              |   1   |   6   |  6   |
| model.Elevator.SubManager.direction()                      |   4   |   2   |  4   |
| model.Elevator.SubManager.getDir()                         |   1   |   1   |  1   |
| model.Elevator.SubManager.getStop()                        |   1   |   1   |  1   |
| model.Elevator.SubManager.getThreadName()                  |   1   |   1   |  1   |
| model.Elevator.SubManager.isDeamon()                       |   1   |   1   |  1   |
| model.Elevator.SubManager.refresh()                        |   1   |   1   |  1   |
| model.Elevator.SubManager.run()                            |   1   |  11   |  13  |
| model.Elevator.SubManager.stop()                           |   1   |   1   |  1   |
| model.Elevator.SubManager.stop(int)                        |   4   |   2   |  4   |
| model.Elevator.canStop(int)                                |   1   |   1   |  1   |
| model.Elevator.getFloor()                                  |   1   |   1   |  1   |
| model.Elevator.getName()                                   |   1   |   1   |  1   |
| model.Elevator.getPeopleIn()                               |   1   |   1   |  1   |
| model.Elevator.getPeopleOut()                              |   1   |   1   |  1   |
| model.Elevator.getSpeed()                                  |   1   |   1   |  1   |
| model.Elevator.getThreadName()                             |   1   |   1   |  1   |
| model.Elevator.isAlive()                                   |   1   |   5   |  5   |
| model.Elevator.run()                                       |   1   |  10   |  10  |
| model.Elevator.signalAll(String)                           |   2   |   3   |  5   |
| model.Manager.arrangePerson(Person)                        |   1   |   2   |  2   |
| model.Manager.getLock()                                    |   1   |   1   |  1   |
| model.Manager.init()                                       |   1   |   6   |  16  |
| model.Manager.stopFloors(Elevator,boolean)                 |   1   |   2   |  2   |
| model.People.addPerson(Person)                             |   1   |   1   |  1   |
| model.People.getLock()                                     |   1   |   1   |  1   |
| model.People.getPeople()                                   |   1   |   1   |  1   |
| model.People.getPeople(int)                                |   1   |   3   |  3   |
| model.People.isEmpty()                                     |   1   |   1   |  1   |
| model.People.size()                                        |   1   |   1   |  1   |
| model.People.stream()                                      |   1   |   1   |  1   |
| model.People.toString()                                    |   1   |   1   |  1   |
| model.Person.Person(PersonRequest)                         |   1   |   1   |  1   |
| model.Person.cacheFloor(int)                               |   1   |   1   |  1   |
| model.Person.call(int)                                     |   1   |   1   |  1   |
| model.Person.getFloor()                                    |   1   |   1   |  1   |
| model.Person.getIn(Elevator)                               |   1   |   2   |  2   |
| model.Person.getOut()                                      |   1   |   2   |  2   |
| model.Person.toString()                                    |   1   |   1   |  1   |
| model.RedefinableAttr.RedefinableAttr(Supplier<T>)         |   1   |   1   |  1   |
| model.RedefinableAttr.cache()                              |   1   |   1   |  1   |
| model.RedefinableAttr.get()                                |   2   |   2   |  2   |
| model.RedefinableAttr.isPresent()                          |   1   |   1   |  1   |
| model.RedefinableAttr.peek()                               |   1   |   2   |  2   |
| view.Input.Input()                                         |   1   |   1   |  1   |
| view.Input.getInstance()                                   |   1   |   1   |  1   |
| view.Input.getThreadName()                                 |   1   |   1   |  1   |
| view.Input.run()                                           |   3   |   7   |  7   |
| view.Output.arrive(Elevator)                               |   1   |   1   |  1   |
| view.Output.close(Elevator)                                |   1   |   1   |  1   |
| view.Output.in(Person,Elevator)                            |   1   |   1   |  1   |
| view.Output.init()                                         |   1   |   1   |  1   |
| view.Output.open(Elevator)                                 |   1   |   1   |  1   |
| view.Output.out(Person,Elevator)                           |   1   |   1   |  1   |

### 方法个数


从上表可以看出，每个类的方法个数大约是 $5\sim 10$ 个，不算太多。

### 方法规模

由类总代码规模和平均方法个数来估计，每个方法的平均行数大概是 $10\sim 20$ 行。当然这个数字是由于大量 $getter$，$setter$ 方法存在的结果，但是据统计最长的方法也没有超过 $50$ 行。因此我认为方法规模适中或较小。

### 控制分支数目

从上表可以看出，大多数方法的控制分支数目较少。超过阈值的方法有 model.Elevator.run(), model.Elevator.SubManager.direction(), model.Elevator.SubManager.run(), model.Elevator.SubManager.stop(int)。可以看出，所有控制分支数目较多的方法都在电梯类和子控器类中。这些类都与调度算法有关，说明在测试过程中应该对调度算法进行大量测试。

## 优缺点

### 优点

类的内剧度高，类间逻辑关系清晰。根据类的功能，将其分为模型、输入输出、控制器三个包，包间耦合度较低。同时大部分类和方法的度量值都比较低，说明其内聚度高，复杂度低。

![package-uml](https://img2018.cnblogs.com/blog/1615581/201904/1615581-20190423011030043-1049554196.png)

从图中可以看到，三个包之间的耦合度是比较低的。

### 缺点

算法相关的类和方法有较高的耦合度，这与其内在的面向对象属性有关。为了尽量将类的耦合度降低，我将部分调度器的功能转移到电梯类的内部。但调度器和电梯的耦合度，及其自身复杂度仍然居高不下。从类图中可以看出，model 包中的类间耦合度较高，尤其是电梯类和其他类之间。

## SOLID 原则

### 单一职责原则（SRP）

程序中每个类都有且只有一个指责，这一点是可以肯定的，但是类与类之间的职责可能存在交叉。比如为了模拟电梯内外的人群，PeopleIn 和 PeopleOut 类就具有类似的职责。在设计时我尽可能将相同功能抽取到父类 People 中，但是例如上下电梯这样的特异性方法则无法合并，即使他们的功能相似。

### 开放封闭原则（OCP）

除了调度器类 Manager ，每个类中的方法都是十分基本的。也就是说，除非要更改架构，没有必要修改这些方法。由于调度器类使用了类似模板设计模式的思想，在修改调度算法时只需修改调度器中的特定函数即可。虽然违反了 $OCP$ 原则，但是从整体来看是利大于弊的，而且不会造成系统其他部分的问题。

### 里氏替换原则（LSP）

本程序中只有两组继承关系，第一个是 PeopleIn 类继承了 People 类，第二个是 SubManager 类继承了 Manager 类。前者中的 People 类并不能实例化对象，因而不会违反里氏替换原则。后者实际上没有继承的必要，因为 Manager 类中完全是静态方法。但是为了强调两者的逻辑关系，我还是将其赋予了继承关系。因此实际上这并不违反里氏替换原则。

### 接口隔离原则（ISP）

唯一的接口 AutoStart 中只有两个抽象方法，start 和 run，均为必要方法。

### 依赖倒置原则（DIP）

程序中没有严格区分高层模块和底层模块，造成模块的依赖关系比较复杂。可以认为这是违反了依赖倒置原则的后果。但本程序的模块层次最多分为两层，即使避免依赖倒置，还是不可避免的有顶层模块调用底层模块的问题产生。重构时我认为能做的最多是将各个包中的顶层模块剥离出来，允许其互相调用，不允许调用其他包中的底层类。

# 多线程协作

## UML 图

![thread-uml](https://img2018.cnblogs.com/blog/1615581/201904/1615581-20190423011118669-766240935.png)

## 分析

上图中为了简化只绘制了一个电梯的线程。main 线程的唯一作用就是开启其他线程。当用户输入一个请求后，改请求会依次唤醒 Input 线程、SubManager 线程、Elevator 线程以及 PeopleIn 线程。每个线程各自完成自身的任务。

### 线程间通信

唯一的线程间通信发生在 SubManager 线程和 Elevator 线程之间。由于电梯线程需要从调度器线程中获取运行指令，这样的通信是无法避免的。而其他位置几乎不会出现线程通信，这从一定层次上降低了线程安全问题发生的概率。

### 并行任务的独立性

由图可以看出，各个线程的独立性其实并不高。大体上可以把所有线程分为三类：主线程、输入线程和电梯线程。其实原本电梯线程可以算作一个线程，但是这样会造成计时误差。为了消除这样的误差，我从第五次作业开始就把电梯的运行拆分成三个相对独立的线程进行模拟了。尽管增加了线程的复杂度，但是由于这三个线程都在电梯类中，因此程序的内聚度比较高，没有太过负面的影响。

# 坑

此处记录了一些在开发过程中遇到的 $Bug$ ，希望通过反思总结警醒自己。由于本次作业的输入输出接口都由助教团队提供，因此本次作业的 $Bug$ 全部集中在运算过程中。根据 $Bug$ 的性质，我将其分为**多线程错误**和一般的**运行时错误**。因为多线程错误多数无法复现，此处不给出具体的输入样例，只是说明 $Bug$ 产生的原因及样例构造方法。

## 多线程错误

**多线程**是本单元作业的训练重点，自然也是 $Bug$ 的集中地。最初在完成第五次作业（第一次电梯作业）时，我对于多线程编程并没有很深的认识。由于当时作业难度较低，我并不需要对多线程的安全性等进行严密的推敲即可完成。但是在后面的两次作业中，运行线程的数量逐渐提高。尤其是在第七次作业时，我的程序中同时存在 $11$ 个线程。这时的线程安全问题就已经十分严重，需要我逐行推理，对于每个共享数据加锁。尽管在编程过程中遇到了很多问题，但这些问题也推动我更加深入的思考多线程编程的方法。以下记录的都是我在开发过程中遇到的典型问题。

### Maximum lock count exceeded 错误

在第五次作业的开发过程中，我的程序总是会报出

> java.lang.Error: Maximum lock count exceeded.

然后程序就终止了。当时我并不能理解这个错误的意思，甚至在网上搜索之后也没有搞懂。无奈之下，我只好查看了 java.util.concurrent.locks 包中的源代码。最终发现这个错误是由于申请锁和释放锁行为大量不匹配造成的。

#### 位置

model.Elevator.PeopleIn.run()

#### 错误分析

引发错误的是类似

```java
// Elevator.java
statusLock.writeLock.lock()
```

这样的语句，因此我将问题定位到 lock 函数中。但是查阅了源码后我发现 lock 函数只是其抽象静态内部类 Sync 的一个对外接口

```java
// ReentrantReadWriteLock.java
public void lock() {
    sync.acquire(1);
}
```

acquire 这个函数定义在 Sync 类的父类 AbstractQueuedSynchronizer 中

```java
// AbstractQueuedSynchronizer.java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

原本我以为问题出在 selfInterrupt 函数内部，但他实际上只是调用了线程的 interrupt 方法，并不会产生错误。在回到 ReentrantReadWriteLock 类中对报错信息进行搜索时，我发现错误其实是 Sync 类报出的，而所在函数正是上面 if 语句块中的 tryAcquire 方法。

```java
protected final boolean tryAcquire(int acquires) {
    /*
     * Walkthrough:
     * 1. If read count nonzero or write count nonzero
     *    and owner is a different thread, fail.
     * 2. If count would saturate, fail. (This can only
     *    happen if count is already nonzero.)
     * 3. Otherwise, this thread is eligible for lock if
     *    it is either a reentrant acquire or
     *    queue policy allows it. If so, update state
     *    and set owner.
     */
    Thread current = Thread.currentThread();
    int c = getState();
    int w = exclusiveCount(c);
    if (c != 0) {
        // (Note: if c != 0 and w == 0 then shared count != 0)
        if (w == 0 || current != getExclusiveOwnerThread())
            return false;
        if (w + exclusiveCount(acquires) > MAX_COUNT)
            /******************
             * Expected error *
             ******************/
            throw new Error("Maximum lock count exceeded");
        // Reentrant acquire
        setState(c + acquires);
        return true;
    }
    if (writerShouldBlock() ||
        !compareAndSetState(c, c + acquires))
        return false;
    setExclusiveOwnerThread(current);
    return true;
}
```

从函数中我们可以看出，只有在已分配锁的数量超过常数 MAX_COUNT 后，函数才会报出错误。而 MAX_COUNT 的定义如下

```java
static final int SHARED_SHIFT   = 16;
static final int MAX_COUNT      = (1 << SHARED_SHIFT) - 1;
```

可以看出他是一个非常大的常数，我的程序中本不应申请这么多锁。于是我回到程序中检查 lock 函数与 unlock 函数是否配对。最后发现我的程序结构如下

```java
while (cond1) {
    lock.writeLock.lock();
    if (cond2) {
        continue;
    }
    // ...
    lock.writeLock.unlock();
}
```

#### 问题原因

这是一个用轮询解决实时控制的极差的例子，但他说明了上述错误的原因。如果满足 cond2，我的程序会重新执行 lock 函数，而没有继续执行至 unlock 函数。（如果使用 synchronized 语句块则不会出现这样的问题，这正是我当时并未留意此处的原因。）

#### 解决方法

在 continue 语句前释放锁。这样既不会改变锁的作用域，同时可以避免申请锁过多而造成锁数量越界的问题。

在进展到后面两次作业时，CPU 的运行时间受到限制，因此这种轮询方式被放弃，相应的这样的 $Bug$ 也就没有再次出现。避免暴力轮询或许是网上没有很多人遇到这一问题的根本原因。

#### 反思

这个 $Bug$ 是我在开始使用显式锁后遇到的第一个多线程相关问题，而且没有成熟的解决方案供参考。能够解决这个问题使我感觉很是幸运，但同时对多线程 debug 的难度有了最直接的感受。另外这个 $Bug$ 在禁止暴力轮询之后是会自动消失的，这也说明架构本身的合理设计可以在一定程度上预防 $Bug$ 的产生。

### 线程睡眠时仍持有锁

在输入

> [0.0]1-FROM-1-TO-2

后，程序输出如下

> OPEN-1
> IN-1-1
> CLOSE-1
> OPEN-2
> CLOSE-2
> OPEN-2
> CLOSE-2
> ...

#### 位置

model.Elevator.run()

#### 错误分析

电梯在 $2$ 层不停开关门，但是没有人下电梯。这样可以确定 PeopleIn 线程没有正常工作。在对 PeopleIn 线程的关键节点打印辅助信息后，我发现这一线程没有成功获得 statusLock 的读锁。但是一旦电梯线程被唤醒，PeopleIn 线程就又开始运行，说明他已经获取了 statusLock 的读锁。

#### 问题原因

检查了电梯线程后，我发现其程序结构如下

```java
statusLock.writeLock.lock();
try {
    Thread.sleep(500);
} catch (InterruptedException e) {
    // ...
}
statusLock.writeLock.unlock();
```

也就是说电梯线程在睡眠状态下并没有释放锁，导致其他线程无法获取读锁，直到电梯线程被唤醒。但这时电梯线程检测到还有人要上下电梯，就又恢复了睡眠状态，始终无法释放写锁。

#### 解决方法

将 sleep 方法改为 Condition 类的 awaitUntil 方法。这样就可以在进入睡眠状态的同时释放写锁，在恢复运行时重新获取写锁。

#### 反思

这是一个十分简单的 $Bug$ 。但是他与后来出现的许多 $Bug$ 却惊人的相似。要识别共享数据的使用是很容易的，比较困难的是将锁仅仅加在必须的地方并且不造成同步问题。由于锁的作用范围太大，使得其他线程无法正常访问共享数据的问题在这之后也发生过几次，但是这样的问题一般影响严重，可以复现，属于比较容易解决的问题。

### 死锁

在接触多线程编程前就对死锁有所耳闻。印象中这仿佛是一个十分致命的 $Bug$ ，但是直到我解决了自己的第一个死锁问题，我才对死锁的本质有所了解。

#### 位置

model.Elevator.run(), model.Elevator.PeopleIn.run()

#### 错误分析

说到底，死锁是因为程序中存在逆序的多重锁导致的。多重锁这个东西，他本身就会引发许多 $Bug$ 。比如在多重锁中实现等待唤醒机制的时候，wait 方法永远只能释放一个锁，而另一个锁会一直保持锁住的状态直到线程被唤醒。

因此我在程序中原本是尽量避免锁嵌套的结构的。但是在一些特殊情况下，多重锁还是隐蔽的出现了。

#### 问题原因

在两个 run 方法间，我希望实现一个等待唤醒机制，这样就涉及到两个锁 statusLock 和 attrLock。问题在于，两个线程并不是绝对的等待唤醒关系。电梯线程在自身等待时间结束后会自己苏醒，或者在新的请求到来时会被唤醒。这时电梯持有的是 statusLock。

而对于 PeopleIn 线程来说，attr 是需要操作的共享数据，因此需要持有 attrLock。这时如果要唤醒电梯，PeopleIn 线程需要获取 statusLock。然而很不幸电梯线程占用了 statusLock，因此 PeopleIn 线程进入阻塞状态。

而电梯处理完请求后，准备进入睡眠状态。但在此之前，电梯需要唤醒 PeopleIn 线程，允许乘客上下电梯，这样做就需要获取 attrLock。这时，死锁便产生了。

#### 解决方法

将 statusLock 和 attrLock 的功能部分合并，即在会产生死锁的部分将 attrLock 换成 statusLock。这样虽然会降低效率（statusLock 的作用域扩大了），但是可以很好的解决死锁问题。

#### 反思

由于要使用 condition，我申请锁的动作变得十分僵硬。很多情况下，申请锁并不是为了获取对共享数据的控制，而是为了唤醒某个线程。由于这样的操作很复杂，我将所有需要唤醒的 condition 封装到一个函数中。这样在需要的时候只需要调用函数就可以隐式的对锁进行管理，并唤醒制定线程。但这样使得原本的二重锁结构不那么清晰，进而导致死锁的产生。

因此我认为我对 Lock 和 condition 的理解还远远不够。但目前为止我想不到任何替换方案可以完美的解决二重锁的问题。

## 运行时错误

以下所述的运行时错误包含本单元作业中，全部的不由多线程技术引发的错误。

### 第一次运行时等待时间不足

由于引入了优化，我的电梯可以在等待一定时间以后出现在当前层及上下一层的任意一层中，减少了等待时间。但是如果在程序运行之初输入，电梯可能会打破最低时间限制，直接跳到对应层。这个错误主要是因为引入了优化诞生的，是典型的不作就不会死。但是既然选择了优化就不愿轻易放弃，因此我没有回滚代码，而是对 $Bug$ 进行了简单的处理。这又导致后面的另外一些问题。

#### 位置

model.Elevator.run()

#### 问题原因及解决办法

最初在本地测试时我并没有发现这个问题，因为 $Bug$ 的出现条件比较苛刻，必须用自动测试脚本才能复现。提交评测机后，我发现许多测试点出现了：电梯运行的第一层间隔时间过短的问题。分析之后发现，这是由于优化算法产生的。我的最初算法如下

```flow
st=>start: start
c1=>condition: input alive
c2=>condition: people not null
sub=>subroutine: consult manager
c3=>condition: dir == NULL
wait1=>operation: wait interruptedly
wait2=>operation: wait for T ms
floorinc=>operation: floor++ or floor--
c4=>condition: dir == UP or DOWN
e=>end: end
st->c1
c1(yes, down)->c2
c1(no, right)->e
c2(yes, down)->sub
c2(no, right)->e
sub->c3
c3(yes, down)->wait1
c3(no, right)->c4
c4(yes, right)->floorinc->wait2
c4(no, down)->wait2
wait1(left)->c1
wait2(right)->c1
```

这样设计的目的是当电梯没人时，等待的时间可以当作电梯在运行。但是从算法图中可以看出，如果电梯初始化状态为 NULL，来了一个乘客之后，如果电梯此刻需要上下行，则会让 floor 变量自增。这时就会产生错误。

改进后的算法如下

```flow
st=>start: start
c1=>condition: input alive
c2=>condition: people not null
sub=>subroutine: consult manager
c3=>condition: dir == NULL
wait1=>operation: wait interruptedly
wait2=>operation: wait for T ms
wait3=>operation: wait for T ms
floorinc=>operation: floor++ or floor--
c4=>condition: dir == UP or DOWN
e=>end: end
st->c1
c1(yes, down)->c2
c1(no, right)->e
c2(yes, down)->sub
c2(no, right)->e
sub->c3
c3(yes, down)->wait3->wait1(right)->c1
c3(no, right)->c4
c4(yes, right)->floorinc->wait2
c4(no, down)->wait2
wait2(left)->c1
```

修改后，我可以根据电梯当前所处的状态来选择性的唤醒。这样虽然在一定程度上解决了问题，但是如果输入速度足够快，还是会出现问题。当前算法主要的假设是电梯在第一个输入到来前就已经进入了等待状态。然而在第七次作业中，评测机的第一次输入可以发生在电梯进入等待状态前（主要是因为我的调度算法初始化时间较长）。为了确保电梯处于等待状态，我将输入线程延迟启动。

#### 反思

电梯线程的运行并不复杂，但是由于引入了这样的优化，其编码变得十分冗长。尤其是这一优化在调度优化面前显得毫无意义。**显然助教团队不希望我们通过编程手段对代码进行优化，而是利用数学工具对调度模型进行改进**。这种舍本逐末的命题方式很好的锻炼了我们的数学建模能力。

### 满载的策略更改

第七次作业中，电梯引入了荷载的概念。然而我在编码时没有考虑到所有由荷载引发的问题，导致电梯在满载后可能产生震荡的情况。

#### 位置

model.Manager.stopFloors(Elevator, boolean)

#### 问题原因

没有修改前面几次作业中使用的调度器。

#### 解决办法

在运行调度时传入表示是否满载的参数。如果满载则不再考虑电梯外的请求。

#### 反思

这个错误非常简单，但是破坏力极大。这里将其记录下来就是为了提醒自己在确认提交前做好测试。

# 互测策略

一般来说，我测试其他人代码分为三个步骤：

1. 利用针对自己代码的测试集进行测试
2. 阅读代码，针对性测试
3. 利用脚本大量测试

本次由于正确性判定比较复杂，因此我没有写自己的评测机，也就无法用脚本自动测试。因此本次测试中我使用了随机测试。

## 测试集测试

一般情况下，这种测试方法只能检查程序的基本表现。由于测试集是针对我的代码编写的，尽管从我的编码逻辑上做到了覆盖，但并不一定能覆盖他人的代码。因此这一轮测试只是检查他人代码能否完成最基本的求导操作。

## 针对性测试

这个阶段我会阅读对方代码。阅读重点放在输入和输出的处理，因为运算部分比较简单。如果有比较明显的逻辑错误，在这个阶段就可以暴露出来。如果阅读一遍没有发现问题，我会查看运算部分的逻辑，同时编写测试集进行测试。但是这里的测试集一般不能做到覆盖，只是针对顶层的逻辑进行检查，否则没有时间测试更多代码。

由于本单元作业与上一单元有本质（难度）上的区别，阅读代码的难度陡增。因此这种方法实际使用并不多，效果自然也不是十分明显。

## 随机测试

只要你在 c 屋，随机生成测试样例就可以 hack。这是到目前为止最有效的测试方法。

## 反思

从自测和互测中，我认识到测试是十分重要的。但是对于多线程测试，我并没有找到很好的测试方法。甚至有时由于时间限制，我无法构造出一个完整覆盖的测试集（主要是检查正确性太过复杂）。

事实上，线程安全问题并不是我测试的重点。这是因为我的共享数据几乎都是集合类型，而且多采用函数式编程。如果有线程不安全的问题，在任何一组测试样例中都会出现。因此只要程序正常运行，我一般认为线程安全问题是不存在的。与之相反，我真正担心的是算法考虑情况不全。例如电梯运行的方向判断时，究竟考虑不考虑电梯外的人群。这样的问题并不属于多线程问题，原本是可以被全面覆盖的。但是我将重点放在了多线程协调调度上，忽视了第一单元作业中强调的重点。

回头来想，我认为本单元测试教会我的最重要的一件事就是：不要用自己的先验知识去推测 $Bug$ 出现的位置。自己注意到的可能有问题的位置，往往是防范最为充分的地方。而正是自己认为没有问题的函数最有可能隐含 $Bug$ 。

# 心得体会

## 线程安全

本单元作业是我第一次接触多线程编程。从完全不了解其编程方法，到逐渐适应，我认为我最大的收获就是学会了多线程的分析方法。在编程时，我对于每个线程进行梳理，每当遇到一行操作共享数据的代码，我就遍历一遍其他线程可能的操作，检查多线程会不会对共享数据的使用造成影响。如此下来，我不仅严密的证明了程序的线程安全性，同时极大的锻炼了自己分析多线程程序的能力。

但是在做完三次作业之后，我逐渐发现，多线程最终的目的其实是将线程和共享数据的数量降低到最少，而非相反。多线程的确是一个非常有用的工具，但他只应被用在必须并行的情况下。如果条件允许，将多个线程合并为同一个线程是很好的优化。因为这样可以降低出现 $Bug$ 的风险，同时降低 debug 难度。

第五次作业中我还不知道这个道理，因此我把所有可以并行处理的程序段都封装成线程，让其独立运行。在允许轮询的情况下，这样的设计还说得过去。毕竟所有线程都在轮询，表面看上去就好像这是一个线程在运行一样。但是从第六次作业开始，CPU 的运行时间被限制，轮询这一方法被严格禁止。这时线程过多的危害就显现出来了。当时我为了把所有线程改为等待唤醒，几乎用了整整一个周末。也正是在那时，我想到许多线程并没有单独存在的价值，例如 PeopleIn 和 PeopleOut 。从此，我开始简化我的代码结构。直到第七次作业结束，我认为我的代码已经简化到一个效率与复杂度的平衡点。（这里的效率与性能分无关，单纯指开发和 debug 的效率）。

只要有可能，以后我还是会尽可能避免多线程编程。即使是必须的情况，我也会选择对每个线程进行单独的单元测试，最终组装到一个项目中，以此来尽量避免多线程同时运行的窘境。

## 设计原则

本单元作业中，我主要遵循的设计原则是封装。最初，我把人群进出电梯和电梯调度都作为方法封装在电梯类的内部。但是这样会使电梯的性能受到影响。为了解决这一问题，我的第一想法是把和电梯运行无关的逻辑单独提出，作为线程封装；同时电梯线程只保留最基本的相应调度的功能即可。但是这样的设计就不可避免的要在其他类中访问电梯类的私有属性，打破了电梯类原本良好的封装性。

这时就出现了一个矛盾：优化电梯势必要破坏封装。为了解决这个问题，我将原本电梯类中的方法封装成内部类，这样就可以让他们调用电梯的私有方法，同时不会破坏电梯本身的封装。实际上这种想法是来源于实际生活中电梯的运行情况的：电梯的调度器应该有权限访问电梯的所有属性，仿佛他就是电梯一样；而电梯里的人应该知道电梯运行的楼层和状态。

但是这种优化导致了另一个问题，类的复杂度过高。每当电梯类中出现一个 $Bug$ ，我都需要翻阅三个类来定位（多数情况下是真的不知道自己看到的函数处在哪个内部类中）。因此我进一步将不涉及电梯私有属性的、和其他类有共性的方法抽离出来再同一包中形成父类。至此，封装和优化的矛盾被基本解决，只是类之间的耦合度又增高了。这是一个按下葫芦浮起瓢的过程。

通过这次优化，我意识到有些情况下，代码是没有办法兼顾简洁性和高效性的。甚至大多数情况下，我们需要再开发效率和运行效率之间寻求一个平衡。一味的追求代码的静态要求很有可能导致程序优化不够，而一味追求优化又可能造成代码可维护性差等问题。可以说这是编程语言的局限性，同时这也是对我们开发能力的考查。

所谓的设计原则，也就是他人经过大量开发后总结出来的较优的平衡点。面临具体的情况时，还是需要我们自己权衡，而不是依赖于原则。