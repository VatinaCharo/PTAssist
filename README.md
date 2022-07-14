# PTAssist

PTAssist，意为PT比赛小帮手，致力于减轻比赛管理负担和规范化比赛流程。

## 特性

1. 分客户端和服务端两部分，直接通过网络交互数据，
   避免了传统的U盘拷数据的繁琐工作和可能的错误风险
2. 比赛核心进程业务同比赛规则业务分离，能很好的适应各种比赛的规则变化
3. 支持导出比赛进程回顾，减轻比赛运维压力

## 安装

## 使用

### 服务端
服务端是比赛承办方所使用的软件，其功能主要为**赛前比赛对阵表的生成**，**赛时比赛信息的收集与规则判断**，和**赛后比赛结果的导出**。
下面，我们将按照比赛流程顺序依次介绍服务端软件的使用。
#### 1. 配置服务端
在`Server/ServerData`文件夹中找到`server_config.xlsx`并打开。此excel表格是用于配置服务端的文件，其中共有**四个**子表，
分别为 **“软件配置”**，**“赛题信息”**，**“队伍信息”**和**“裁判信息”**。接下来我们将依次介绍它们。
##### 软件配置
端口号：服务端的端口号，将用于网络文件传输。(Int)

每场比赛裁判个数：一个会场內所需的裁判个数，将主要用于带裁判的比赛对阵表的生成。(Int)

会场总个数：此次比赛所有会场的个数，将主要用于对阵表的生成。(Int)

比赛轮数:所需要生成对阵表的比赛轮数。(Int)

正方/反方/评方分数权重：此次比赛默认的正方/反方/评方初始分数权重。(Double)

##### 赛题信息
用于此次比赛的题号(Int)与题名。

##### 队伍信息
参赛队伍的学校名，队伍名，抽签号(Int)，以及队员名字和性别(填"男"或"女")。
当队伍中有多位队员时，可按照名字，性别这样的顺序在同一行依次填入。
>注：在没有抽签之前，可不用填写抽签号，在生成没有裁判的对阵表后，请主办方举办队伍抽签，再将抽签号填入，即可生成有裁判的完整对阵表

##### 裁判信息
参赛的学校名称与其学校的裁判。当裁判有多位时，可在同一行依次填入。


#### 2. 生成对阵表
进入服务端软件，进入启动页界面。在配置好`server_data.xlsx`后，点击左下角的 **”生成对阵表(无裁判)“** 按钮即可在
`ServerData`文件夹下生成`counterpartTable.xlsx`的文件，在其表”对阵表(无裁判)“中即可看到对阵表信息。
此时，在`ServerCache`文件夹下也将生成名为`counterpartTable.json`的缓存文件，用于存储对阵表信息。
同时，由于`ServerCache`文件夹中没有配置的缓存文件，excel配置文件中的“软件配置”表单内容
将会加载到`ServerCache/config_data.json`中。

在`server_data.xlsx`的队伍信息填写好抽签号以后，点击软件左下角的 **”生成对阵表(有裁判)“** 按钮，
软件将读取`counterpartTable.json`缓存文件并在`ServerData`文件夹的`counterpartTable.xlsx`文件
中生成表 **”对阵表“** 与 **”对阵表(含学校名)“**。

#### 3. 进入比赛
点击左下角的 **”进入比赛“** 按钮，此时软件将检测`ServerData`文件夹下是否有比赛数据文件`data.json`，
若有则直接读取该文件，若无则将按照`server_data.xlsx`配置文件初始化一个比赛文件。

>注：当比赛开始后即data.json已经生成，此时无法再通过修改配置文件excel表格来修改任何信息。
> 只能通过修改data.json文件修改配置信息。
> 这一定程度上保证了信息安全，但需要配置服务端时务必再三检查，确认信息无误后再进入比赛。

进入比赛后，软件界面由启动页变为比赛记录界面。该界面主要由**五部分**组成。左侧从上到下依次是操作栏，学校名称栏，队伍名称栏，队员信息栏。
其中在点击学校名称时，队伍名称栏中会显示出该学校的队伍名称。学校可以配合使用`Ctrl/Command`键或`Shift`键进行多选。
接下来，再点击某个队伍名称时，队员信息栏和右侧的比赛记录栏将对该队伍的信息进行展示。

操作栏中共有四个按钮。第一个是修改按钮，它可以开启/关闭对选手信息和队伍记录信息表格的更改。当开启更改后，
界面边框将变成橙色，双击表格中的某个单元格后可以进入编辑模式，输入完后按回车即完成修改。第二个是保存按钮，
当修改完信息后，按此按钮将内存中的数据保存至文件中。第三个是增删按钮，点击后将下拉出四个选项，分别可以对
选手和记录进行增删。第四个是导出按钮，点击后将弹出导出界面，用于比赛数据统计结果的导出。其最上栏可选择导出的轮数，
可多选。下侧可选择导出表格的类型，分别为“各轮回顾表”，“各队伍总得分”和“个人得分情况”表，可多选。最下面是
确定导出的按钮，点击后，将根据所选项在`ServerData`中生成`第x轮成绩.xlsx`文件，不同的表在不同的表单(sheet)中展示。

>注：修改模式下不可修改学校名和队伍名。增删以后仍需要点击保存按钮以保存更改。

#### 4. 修改服务端配置
在生成对阵表或已经成功进入比赛后，`ServerCache`文件夹中应该已经生成从excel表格读取生成的`config_data.json`文件。
此时按下启动页的设置按钮，则可读取该缓存文件，并进入修改界面。若`ServerCache`中并没有缓存文件，则会读取excel中的“软件配置”表单
生成缓存文件。


### 客户端



## LICENSE

Copyright (C) 2022  [Eur3ka](https://github.com/VatinaCharo),[EnjoyXu](https://github.com/EnjoyXu)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.