# Thinkphp3.2 SQL注入分析

thinkphp框架的漏洞还是相对好复现一点的，我个人觉得相关的资料也很多，并且漏洞年代也有一段时间了，比较适合像我这种小菜来理解，分析。
相关的代码我已经推到这个项目里面去了。关于环境部署和复现过程我简单记录一下。

## 环境部署
我这里利用的是MAMP集成环境，如果在windows上，大家也可以使用WAMP。
直接把代码放到根目录下即可。
关于数据库要建立一个thinkphp的数据库。里面建立一张user表，3个字段，分别是id,username,password。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-01.jpg)

关于建库建表的命令我就不说了，直接百度就有。

## 复现
poc:http://localhost:8888/tp3/index.php?m=Home&c=Index&a=test&id[where]=1%20and%20updatexml(1,concat(0x7e,user(),0x7e),1)--

![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-02.jpg)

首先在业务代码这里下断点。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-03.jpg)
然后逐步跟进find函数的实现来看看。
![](mhttp://tiaotiaolong.cn-bj.ufileos.com/blog19-04.jpg)

首先(721行)是判断options是不是数字或者字符串，我们这里显然不是。
然后是获取符合主键，用来查库。pk为id，可见是单主键。
判断(728行是不是数组)，这里肯定也不是。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-05.jpg)
limit设置为1，至少查1条记录。
然后调用__parseOption(),但是我们看我们的where现在还是我们输入的原型。
执行完这个__parseOption，实际上只增加了options2个主键 一个table,一个model.
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-06.jpg)

然后是去判断缓存 执行select。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-07.jpg)

select里面又调用了parseSql。这个主要利用str_replace,是做替换用的,最终形成真正的sql语句。
然后你会发现，这个sql语句还是有危害的！
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-08.jpg)
举个例子，你看这个parseTable,实际上最后就是把%TABLE%经过一系列的判断和处理替换成了user,如下图。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-09.jpg)

这里我很好奇的是，我原以为应该会在处理where的时候进行安全转义。但是实际上，只执行了一个赋值就结束了。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-10.jpg)
只是把where赋值给wherestr了。直接用WHERE和我们可控的字符串做拼接了。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-11.jpg)

于是我还是很好奇这里为什么做的这么草草了事，难道是不应该在这里做安全检查吗。然后我仔细看了一下poc，发现这里的利用思路是使用php数组对非数组的情况进行绕过。
比如我把这里的id中的[where]去掉，你会发现，根本就不是说没有做安全过滤。
于是poc成了这样：
http://localhost:8888/tp3/index.php?m=Home&c=Index&a=test&id=1%20and%20updatexml(1,concat(0x7e,user(),0x7e),1)--

然后前面代码执行的过程基本没什么变化，等到了__parseOption()函数。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-12.jpg)

在__parseOption()的时候，发现还是脏数据。但是他是在下面__parseType()函数里做了类型的校验。我们跟进去看一下。在这里用了intval做了转换。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-13.jpg)

现在清楚了poc是使用数组对校验过程进行了巧妙的绕过，一开始的思路偏了，误以为是在WHERE里面没有做安全检查。把错误的思路也记录下来，提醒以后的自己！

最后贴一张他的修复方案，大家可以想一下为什么这么修复？
![](http://tiaotiaolong.cn-bj.ufileos.com/blog19-14.jpg)











