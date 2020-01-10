# shiro反序列化漏洞复现

这个漏洞在我司还挺常见的，最开始是源于林鹏给了一个网站，说这个洞有shiro反序列化漏洞，但是一直没有研究，后来写扫描器插件的时候，用扫描器扫了一下公司的网站，扫出来3个shiro反序列化的漏洞。我去，可怕。

我这里是用github的shiro代码，拉下来自己,自己打包的，打好的war包我会上传至github，在本目录


seebug上这里面的环境是自己在pom里添加的collention4，因为数组的bug，正常来说如果没有这个调用链是没法起到复现效果的。

![](http://tiaotiaolong2.cn-bj.ufileos.com/blog28-01.jpg)

大家可以用我的war包试着复现一下，另外平时用扫描器扫的时候都是使用DNSLOG实现的，用的都是基础类。


seebug原文： https://paper.seebug.org/shiro-rememberme-1-2-4/


