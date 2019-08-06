##Fastjson反序列化漏洞分析(1)之poc里那几个不起眼的字段

### 本文章属于[tiaoVulenv](https://github.com/tiaotiaolong/tiaoVulenv)项目的系列文章。

这一篇主要记录一下自己调试Java反序列化的一些过程，同时也对很出名的Apache Common组件中利用反序列化进行命令执行的过程和原理进行记录。代码环境在上级目录，你可以直接下载，使用IDEA进行调试。由于代码片段比较简单，也可以直接自己用IDEA进行构建，方便熟悉。



###关于漏洞
今天准备分享一下关于fastjson的安全问题，与其说是分析，不如说是调试跟踪，看看这些poc是怎么执行起来的，同时又是满足什么样的条件才可以使调用链执行起来。我这里对文章的标题加了一个索引，怎么说呢？由于Fastjson漏洞在17年爆发出来之后，陆陆续续的出现了几次补丁绕过的问题，本质上就是想办法去绕过Fastjson的黑名单，这块我也大致跟踪了一下。所以我觉得自己有可能会把剩下的一些我能看得懂的，并且在调试的时候看得见的东西继续写下来，所以斗胆算是写了个小连载！

还是老样子，说Fastjson漏洞之前我先扯一下JVM的加载。我们编写的Java和C++还不太一样，C++是直接经过编译器的编译，链接器的链接，优化生成了可执行文件，这个文件是恢复难度较高的，当然了，一些熟悉汇编或者使用IDA等方式还是能看出大致逻辑，我这里说只能是看出逻辑，你没办法恢复成原样了！

但是Java呢，还是有点不一样的。他首先呢是将Java文件编译成Class字节码文件。这个文件理论上可以交给不同平台上的JVM的，JVM都会认识它的。JVM拿到class文件之后呢，就使用类加载器来加载，当然我们说加载，实际上也要经过一个比较复杂的过程，比如包含验证(不验证怎么知道你是不是class文件)，初始化，使用等等。

所以对于JVM来说，他执行的是class字节码文件，只要是符合规则的字节码文件，不管你是什么语言编译出来的，都可以。也就是说JVM可不一定只能运行Java。

再说一下Fastjson在反序列化的过程中，默认是不能对私有属性进行反序列化的，但是如果parseObject使用了SupportNonPublicField属性，就可以对私有属性进行反序列化了。

我们这里是使用TemplatesImpl做调用链的，其中@type我们可以在指定反序列化的类型，bytecode是我们目标文件的class字节码。

这篇连载里呢，除了跟踪一下fastjson的漏洞原因，我也打算说说poc里面那几个不起眼的字段，不起眼有点过分了，就是给自己的好奇心解解渴。

###分析过程
首先我们这个项目呢还是使用maven构建，fastjson的版本为1.2.41
大致看一下项目目录。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-01.jpg)

和我们有关的就是TemplatesImplPoc和EvilObject这个恶意类。
TemplatesImplPoc里主要就是编写POC并调用parseObject()去加载EvilObject这个恶意类。
再看一下这个EvilObject恶意类！
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-02.jpg)

然后在parseObject下断点，但是这个漏洞要是跟踪到有问题的点非常的深，中间那个过程我跟踪了很久才看见真正和漏洞相关的代码。调用栈非常深。这里我把调用栈贴出来，方便大家按照这个调用关系慢慢调试到处理POC的地方。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-03.jpg)

终于来到了SetValue这里。
这块之外的逻辑有一个大循环，我们这里会每次处理反序列化的字段。Fastjson不是用ReadObject这种方法来实现反序列化的，而是在内部通过调用set,get方法来创造目标对象的。
我们这里是通过反序列化_outputProperties这个属性，来调用getoutputProperties函数实现的。
所以我们在这里下断点，点击调试器最右边的向右下角的箭头快速运行至下一断点。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-04.jpg)

当反序列化_outputProperties字段时，然后单步调试几下，你会发现直接就进入了我们的Evil类。这里我们前面说了，反序列化是通过get,set实现的。

所以这里干脆，我们直接在com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl类中的getoutputProperties下断点了。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-05.jpg)

运行到这里的时候，步入会发现
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-06.jpg)
tfactory我们设置的为{}，这样是为了解决兼容性的问题，如果这里不传的话，会造成一个异常点，后面再说。继续步入进入第一个参数去获取实例。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-07.jpg)

首先是判断_name,这里解释了为什么我们要在POC中设置一个_name字段。
然后进入了defineTransletClasses()中。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-08.jpg)
我们的bytecodes实际上就是EvilObject的字节码。如果我们不设置_tfactory,那默认为null，第一个红框处是会异常的，无法走到命令执行的地方了。
第二个红框处会按照字节码加载成我们的目标类，随后会put进_auxClasses中。然后基本defineTransletClasses上执行完毕。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-09.jpg)

执行完就直接调用newInstance()实例化了。再看看Evil类，命令执行是写在构造函数中的。所以这里就直接调用计算器了。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-10.jpg)

刚才调试的时候解释了_tfactory，name,为什么需要这几个字段。
其中name不为空就行，随意指定。还剩_transletIndex，你只要把这个字段删掉，然后重新一步一步调试，就会有结果的。你会发现，这个值只能是0,其余都不可以。
当删除的情况，fastjson会默认赋值成-1，这样就直接进入抛异常了。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-11.jpg)

那整数是不是可以，这里我改成了1试试。
![](http://tiaotiaolong.cn-bj.ufileos.com/blog21-12.jpg)
原来是我们的字节码只有一个文件，如果是1的话，实际上就是数组第2个。
那经过思考，也并不是除了0别的都不可以，只要是满足我们所要调用的字节码文件和我们的这个index索引相匹配就可以。

还有很多点没有解释，比如为什么字节码文件是用base64编码的，肯定是在我那个调用栈的过程中自动解码了呗！再比如除了这个调用链还有其他的利用方法吗？后续的新的漏洞又是怎么绕过黑名单的？等等，吃饱饭，继续调试。













