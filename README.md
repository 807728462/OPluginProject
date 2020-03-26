# OPluginProject
一个用于插件化的模拟学习工程

已完成：
1.宿主与插件之间的activity跳转\n
2.在插件apk中启动多个服务，注册多个广播\n
3.静态注册的广播，在manager初始化的时候，使用反射去解析插件中manifest文件，并注册\n
4.插件与插件之间activity的跳转（在加载apk的时候，区分不同的classLoader跟resource，然后跳转的时候带上apk的标识，
加载不同的classLoder跟resource）\n
5.动态的去修改activity的mResource属性，存储的资源文件，解决了各个插件之间的资源id重复问题\n


未解决问题：
1.宿主与插件，及插件之间的通信问题
2.service的bindService
3.如何设置activity的启动模式
