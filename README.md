# DisappearingGobangServer

DisappearingGobang Server Source Code.

## 效果图

![](http://image-whatbegblog.oss-cn-shanghai.aliyuncs.com/images/GobangServer.jpg)

## 5月7日晚更新

### 1. 修正每五步消失棋子逻辑

原来逻辑是 STEP%6==0 消失子，即第6,12,18...步消失子，而PPT中的本来设计思路为每五步消失子，
即第6,11,16...步消失子。统一采用后者。
判断逻辑改为 STEP != 1 && (STEP-1)%5==0 消失子。
更新的文件有：

* Gobang.cpp
* GobangServer1.0.2.jar

### 2. 服务器更新

修正了已知的BUG. 当一方调用noStep过多时，会出现该消失子时没有子可以消失的情况，这种情况表示该方过于
消极下子，直接判负。

更新的文件有：
* GobangServer1.0.2.jar

## 5月13日晚及14日更新

本次更新主要是针对5月12号模拟赛中竞赛模式在实测环境下的一些问题所做的修复和增强。
其中多个连接导致服务器卡死的情况以试图修复，经测试有所改善，有待实测环境检测。

### 1. 支持断连后重新连接

之前一旦连接成功后的断连（比赛开始前），则无法再次连接。本版本支持断连后重连，多次以同一用户登录则取最新登录的参加比赛。
需注意的是，在比赛模式下，只要用户成功登录过一次，不管是否掉线，服务器都会当做已经登录，人数足够则开始比赛，所以，用户如果成功连上以后掉线，则必须在比赛开始之前重连上服务器，否则比赛开始后无法重连。

更新的文件有：
* GobangServer1.0.3_rc3.jar

### 2. 修正多局比赛同时进行时的输赢判定错误问题

调查表明该bug出自不恰当的静态变量的使用，已修复。

更新的文件有：
* GobangServer1.0.3_rc3.jar

### 3. 增加了蒟蒻的两个电脑AI： RobotOmega 和 RobotGamma

就不告诉你们编号~

更新的文件有：
* GobangServer1.0.3_rc3.jar

### 4. 修复了下子时间统计为负数的情况

由于数值溢出导致时间统计为负数，已修复。

更新的文件有
* GobangServer1.0.3_rc3.jar

### 5. 添加服务器属性see.error

增加了一个服务器配置属性: see.error，默认为true，即所有异常都会打印在stdout中，用于服务器错误调试。不用修改。

更新的文件有：
* server.properties

## 5月15日模拟赛后更新

### 1. 将玩家列表players list从.list格式改成了.txt格式

新版本的服务器读取.txt格式，老版本只能读取.list格式。

更新的文件有：
* GobangServer1.0.4.jar

### 2. 比赛时log面板加入中文名信息

为了更加方便的可视化用户信息，在log面板中加入了player的中文名信息。
只需在players-x.txt中以比如“161220001,123456,张三”的一行表示用户即可。
也可以不加这个信息，即以比如“161220001,123456”的一行表示用户即可。

更新的文件有：
* GobangServer1.0.4.jar

### 说明：

### 1. 双击运行jar时中文名乱码问题

尝试在cmd中用如下语句
"java -Dfile.encoding=UTF-8 -jar GobangServer1.0.4.jar"
来运行Jar文件。

### 2. 回放模拟赛，正赛，小组赛棋局的方法

看每一轮的竞赛需要将每一轮的player-x.txt放到players文件夹中
（不要有别的txt文件）
在players里面保留players-0.txt，则在主面板Load Contest Results时会看到第1轮比赛的棋局列表。
players-1.txt放到players中就是看第2轮的比赛

