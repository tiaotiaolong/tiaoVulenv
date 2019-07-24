# Apache Common组件反序列化原理

这一篇主要记录一下自己调试Java反序列化的一些过程，同时也对很出名的Apache Common组件中利用反序列化进行命令执行的过程和原理进行记录。代码环境在上级目录，你可以直接下载，使用IDEA进行调试。由于代码片段比较简单，也可以直接自己用IDEA进行构建，方便熟悉。

## 项目介绍
我们这个项目是使用pom进行构建，这样比较方便下载我们的依赖项，我们这里主要是依赖apache的组件。我们这里使用的是3.1版本。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-01.jpg)
只有有一个Java文件，Main。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-02.jpg)
第一个红色框主要是建立一个Transformer数组，这个数组里面是由4个Transformer组成。
第二个红色是利用这个数组转换成一个TransformerChain，形成Transformer链。
就像原子弹的链式反应，第一个爆炸随后引发第二个第三个，以此类推。
第三个是生成一个map,并且经过decorate的修饰，变成了一个新的map。这个新的map有一个特性，就是一旦自己的key,value发生改变，便会引起和自己绑定的Transformer链形成链式反应，就像原子弹一样！！！
第四个就是我们主动让map进行改变，触发爆炸，形成上面的Transformer链进行连环引爆。

## 正文
序列化的场景很多，实际上就是保存内存的数据格式，方面下次我用到这个数据的时候直接加载到内存，这样效率高，稳定性好。比如我们服务器存储session，网络之间传递数据，等等，这些都是序列化的应用场景。但是，当你传递序列化数据的时候，假如这个时候有个黑客，守在你家的路由器旁边，对你的序列化数据进行修改，那样，当你反序列化的时候，你生成的对象就不是咱们预期的对象了，比如修改了对象的某个属性，让他银行账户的钱从5块到5万，然后执行相关的转账操作，四不四挺可怕！

在Java中，当你准备发序列化的时候，是直接调用的ReadObject函数。假如你的ReadObject有一些危险操作，并且对序列化的数据也没校验，这就很容易出问题。

好。上段白话了一点，到这结束，说正文，我调试的内容！
首先，上面的代码执行起来，是会执行那个Transformer链，那个链很明显是调用了计算器程序。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-03.jpg)

然后我就在代码最后一行开始下断点，从代码里看看为什么执行那个链！
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-04.jpg)
这里Step Into
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-05.jpg)
你会发现，当你对Map进行修改的时候，这里调用了checkSetValue()。
连续2个Step into
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-06.jpg)
原来当执行了checkSetValue，实际上会调用自己的transform，多个transformer连接起来，在这里循环的被调用起来。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-07.jpg)

现在到这，可能会有疑问，这段代码和反序列化有毛关系，挺多就算是一个链式反应的触发方式啊！
没错，这里确实和反序列化漏洞没啥关系。但是说到这，我想先画个图，从宏观上描述了一下我刚才到底在干啥！
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-08.jpg)

上图分左右看吧 左边是这个TransformerChain形成的过程，右边的是一个map当触发相应的修改的时候，就会执行与之绑定的Chain，这个Chain是由每个Transformer链接起来的，就是Transformer数组！
那每一个Transformer里的transform()为啥可以执行命令呢？
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-09.jpg)
因为每一个transformer都调用了invoke进行反射！
因为每一个transformer都调用了invoke进行反射！
因为每一个transformer都调用了invoke进行反射！

说到这里，你会发现，这根发序列化还是没关系啊！
恩恩，对！

没关系。

现在我们说说反序列化，根据上面所画的图，我们找到了触发原子弹链式反应的导火索，就是找到一个这样的map，并且在这个类中的readobject函数进行了对这个map修改！
你还别说，java的JDK中有一个这样的类。
sun.reflect.annotation.AnnotationInvocationHandler
![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-10.jpg)

![](http://tiaotiaolong.cn-bj.ufileos.com/blog20-11.jpg)

也就是说我们生成一个AnnotationInvocationHandler类型的序列化数据，当执行readobject的时候，就会修改我们设计好的map，进而引发链式反应。

好像是明白了，但是我在实际的过程中还是遇到了几个问题，这里大家主要是参考ysoserial的来生成我们相应的payload吧！

![](http://tiaotiaolong.cn-bj.ufileos.com/wechatzanshangma.jpg)

