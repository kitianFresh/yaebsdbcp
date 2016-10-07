# J2EE environment setup on ubuntu16.04

## Tomcat8.0.37
### Install via apt
```
// Update
sudo apt-get update && apt-get upgrade
 
// Install tomcat packages
sudo apt-get install tomcat8( default is tomcat 8.0)

// Optional
sudo apt-get install tomcat8-docs tomcat8-admin tomcat8-examples tomcat8-user
 
// Make sure you have defined java environment varibale JAVA_HOME

// To start/stop/reatart
sudo service tomcat8 start/stop/restart

// If success, you can access tomcat via http://localhost:8080
 
```
&emsp;&emsp;以上命令对我的ubuntu16.04尽然失败了。。。

### Install Manually
#### Step 1: Download
&emsp;&emsp;可以去[官网](http://tomcat.apache.org)下载tomcat8.0.37，选择Binary distribution => Core => Download 来下载"tar.gz"格式的包;也可以使用命令(wget http://apache.fayea.com/tomcat/tomcat-8/v8.0.37/bin/apache-tomcat-8.0.37.tar.gz)下载8.5.5版本;

#### Step 2: Install
&emsp;&emsp;首先解压并创建软链接
```
cd ~/Download # enter tomcat package dir
sudo tar -xzf apache-tomcat-8.0.37.tar.gz -C /opt  # extract archieve to directory /opt

// 这里创建一个软链接到apache-tomcat-8.0.37的目的就是方便以后更换tomcat版本，只用更换apache-tomcat-8.0.37目录下的内容即可
// 另外，注意/opt/tomcat8后面不要带'/'
sudo ln -s /opt/apache-tomcat-8.0.37/ /opt/tomcat8 # create link for conveniece
```


&emsp;&emsp;下面添加tomcat8用户是为了安全考虑，tomcat8由tomcat8用户和组运行，其他人没有权限；并更改软链接和目录的owner；
```
sudo useradd -s /sbin/nologin tomcat8                 # add user to run tomcat service
sudo chown -R tomcat8:tomcat8 /opt/tomcat8            # and give it ownership
sudo chown -R tomcat8:tomcat8 /opt/tomcat8/           # to tomcat's directory
```


&emsp;&emsp;然后创建系统服务，这一部分可以选择不要。在/etc/init.d/下面新建一个tomcat8文件，将以下代码拷贝过去；
```
#!/bin/sh
### BEGIN INIT INFO
# Provides:          tomcat8
# Required-Start:    $local_fs $remote_fs $network $syslog
# Required-Stop:     $local_fs $remote_fs $network $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start/Stop Apache Tomcat 8
### END INIT INFO

TOMCAT_USER=tomcat8
TOMCAT_DIR=/opt/tomcat8

case "$1" in
start)
  echo "Starting Apache Tomcat 8..."
  su - $TOMCAT_USER -s /bin/bash -c "$TOMCAT_DIR/bin/startup.sh"
  exit $?
  ;;
stop)
  echo "Stopping Apache Tomcat 8..."
  su - $TOMCAT_USER -s /bin/bash -c "$TOMCAT_DIR/bin/shutdown.sh"
  exit $?
  ;;
restart)
  $0 stop
  $0 start
  exit $?
  ;;
*)
  echo "Usage: /etc/init.d/tomcat8 {start|stop|restart}"
  exit 1
  ;;
esac
exit 0
```

&emsp;&emsp;最后启动服务

```
sudo chmod 755 /etc/init.d/tomcat8                    # make it executable
sudo systemctl daemon-reload
sudo service tomcat8 start                            # run tomcat as service
sudo update-rc.d tomcat8 defaults                     # make tomcat run with system
```

&emsp;&emsp;最后可以使用一下命令启动/关闭/重启tomcat；
```
sudo service tomcat8 stop 
sudo service tomcat8 start
sudo service tomcat8 restart
```
&emsp;&emsp;以上的设置是成功的，但是你无法再用自己的账户访问/opt/tomcat8了，因为他属于tomcat8用户（chown），这个很蛋疼；除非你切换到root用户。其实我们不一定要这样设置一个新用户来运行tomcat8，之所以这样设置是出于安全考虑的；下面来看看用户管理；
你会好奇这个命令是干什么？

```
sudo useradd -s /sbin/nologin tomcat8                 # add user to run tomcat service
```

&emsp;&emsp;这里关于用户管理只讲解本教程相关问题，更多请参考鸟哥私房菜；再一次提醒，**linux下的终端terminal不是shell**，千万不要被表面迷惑；我们都知道linux是**多用户多任务的系统**；但是由于现在我们都是用微机也就是个人电脑，基本上就你一个人在用，所以没有感觉什么叫多用户多任务；如果回到过去，没有盖茨和乔布斯的时代，那个时候个人是很难用到电脑的，你要用计算机，你得申请某一个时段，到机房去，在一个显示器面前使用命令行，登陆，然后进项相应任务，但你看到的并不是主机，那只是从大型主机连接过来的显示器或者说终端（现在你应该知道为啥叫terminal了吧），所以很多个科学家登陆到同一台主机同一时间段做任务，这就需要主机操作系统可以进行多用户多任务管理了。

&emsp;&emsp;同样，现在，我们人手一台计算机，其他人不用了，就你一个人用，当然可以同时执行多个任务，这个在操作系统分时调度任务大家应该都知道，并且如果你用的是Windows，你就更不可能很好的理解什么叫多用户了。如果是在linux下面，初学者也很少碰到多用户管理，因为一直是自己用。但是实际上，linux上有很多用户(**用户不一定是人，也可以是程序**)，并且有的还和你一起在同时使用linux呢。

&emsp;&emsp;现在在linux上，你使用Ctrl+Alt+T打开终端Terminal（记住那不是shell），严格来说这是一个终端模拟器，并不是真正的终端；真正的终端会占用整个显示器的，就是字符终端，不信你可以试试Ctrl+Alt+F1～F12，这是不同的终端。终端控制显示器，而不同的用户和任务会控制终端，当操作系统调度到该任务的时候，如果他需要用到终端显示器，此时终端控制权就给了该任务（比如用C语言写一个hello world）；因此，你登陆后其实默认只是你自己的账户，你也可以切换到root账户，但是你会发现目录什么的都变了。这就是多用户显示出来的区别了，现在root掌管了你的虚拟终端，并且你以root身份登陆到了shell中，即root用户使用shell（shell也有很多种，bash，zsh等等）控制了你的终端模拟器（就是你看到的那个黑框框），你会看到root@your-host-name:,对，这个就说明现在是root在掌管了。你可以exit，然后就回到你现在的用户了，变成了your-name@your-host-name对吧。但是你还是在同一个黑框框下面（记住他是虚拟终端而不是shell，shell是一个程序在运行，而terminal负责输入输出显示器），你先在应该有了terminal和shell的区别的概念了吧，同时也知道了多用户的概念了吧。

&emsp;&emsp;不过你可能有疑惑，并没有实现多个用户同时登陆啊，是的，刚才每次只有一个用户在执行。其实你可以打开多个terminal，然后在每一个terminal中使用sudo su account-name来切换用户，这几个终端登陆不同的用户，结果，你发现几个不同的用户同时登陆了你的主机了。如果你的主机开放了端口并且可以当做服务器，别人如果有你的账户和密码，也是可以通过ssh username@host-ip（如果是内网直接ssh username@hostname也可以）远程登陆到你的主机的。不信，如果你开通了腾讯云主机，你可以使用这种方法登陆你的云主机。我们如何添加一个用户呢？很简单使用adduser或者useradd就可以了。

&emsp;&emsp;再回到我们的主题，上面那个sudo useradd -s /sbin/nologin tomcat8中，-s /sbin/nolongin其实是设置这个用户的登陆方式。这里其实压根没有给tomcat8登陆方式，因为ubuntu16.04没有这个目录和shell。正确的应该是/usr/sbin/nologin或者/bin/false,你可以到相应的目录找到这两个程序，他们都是禁止登陆的。也就是你使用sudo su tomcat8没有用的，你不能登陆。

&emsp;&emsp;为什么设置一个不能登陆的用户？就是为了安全，前面说过，别人可以用账户名和密码远程登陆你的服务器的。这样别人就没办法入侵了。另外，如果你是在自己电脑上而不是服务器上，其实可以设置一个可以登陆的shell的；使用 less /etc/passwd可以看到你的tomcat用户的登陆方式，你可以使用sudo usermod -s /bin/bash tomcat8 来更改tomcat8的登陆shell。这样，你就可以登陆到tomcat8用户了，就可以访问/opt/tomcat8了。你现在可以直接进入/opt/tomcat8/bin目录，执行./startup.sh或./catalina.sh start或者./catalina.sh run(调试模式)就可以启动tomcat8了。其实开始讲的创建service模式也是调用这个目录下的命令程序。

&emsp;&emsp;以上单独创建tomcat8用启动tomcat8是为了在production server中部署，在本地开发环境中开发没必要；所以再次我把tomcat8用户删除了，并且tomcat目录用户权限全部改成自己常用的了，这样更方便开发，要不然每次到都要切换用户或者到root权限才能修改文件；
```
// 删除tomtcat8
sudo userdel tomcat8
// 修改tomcat8所属用户
sudo chown -R kinny:kinny /opt/tomcat8
sudo chown -R kinny:kinny /opt/tomcat8/
// tomcat8的执行权限可以不用修改，还是755

//最后将/etc/profile.d/tomcat8文件中的TOMCAT\_USER变量修改为your_account(例如我的是TOMCAT\_USER=kinny)
```

#### Step 3:setup user account
&emsp;&emsp;为了通过web方式管理webapp和tomcat，可以设置tomcat用户
在/opt/tomcat8/conf目录下找到tomcat-users.xml,设置用户如下：
```
<role rolename="manager-gui"/>
<user username="manager" password="manager" roles="manager-gui"/>
<role rolename="admin-gui"/>
<user username="admin" password="admin" roles="manager-gui,admin-gui"/>

```
&emsp;&emsp;此时，你就可以通过主页上Manager APP和Host Manager输入用户名和密码就可以通过web管理tomcat了；


## Mysql JDBC Driver
```
sudo apt-get install libmysql-java

// 在该目录下会看到mysql-connector-java.jar
ll /usr/share/java/mysql-connector-java*
-rw-r--r-- 1 root root 987191 2月   7  2016 /usr/share/java/mysql-connector-java-5.1.38.jar
lrwxrwxrwx 1 root root     31 2月   7  2016 /usr/share/java/mysql-connector-java.jar -> mysql-connector-java-5.1.38.jar

// 拷贝到/usr/lib/jvm/java-8-oracle/jre/lib/ext/目录下（因为如果你的servlet程序需要访问mysql数据库，需要mysq-connector-java.jar包，就需要tomcat知道他在哪儿，为了简单，你可以直接拷贝到jre/lib/ext/库里，其实还可以设置环境变量，这是linux高级主题），记住不是tomcat8的lib，而是tomcat8使用的jre的lib
sudo cp /usr/share/java/mysql-connector-java-5.1.38.jar /usr/lib/jvm/java-8-oracle/jre/lib/ext/

```

## SQL injection
&emsp;&emsp;针对querybook.html 和QueryServlet,由于QueryServlet采用获取http querystring中的参数，比如author，然后构造sql语句执行，如果你知道了后台构造sql的方式，你就可以进行sql injection attack，比如你可以通过在querybooktextfield.html页面直接输入一下author name；就会得到所有的作者信息；或者直接使用url（http://localhost:8000/ebookshop/query?author=Ku%25%27+or+author+like+%27%25）攻击
```
// 正常查询
author name 输入
Kumar

You query is : select * from books where author like '%Kumar%' and qty > 0 order by author asc, title asc

Kumar, A Cup of Java, $55.55

==== 1recods found ====

// 攻击查询，整个数据库都被查出来了
author name 输入
Ku%' or author like '%

You query is : select * from books where author like '%Ku%' or author like '%%' and qty > 0 order by author asc, title asc

Kevin Jones, A Teaspoon of Java, $66.66

Kumar, A Cup of Java, $55.55

Mohammad Ali, More Java for more dummies, $33.33

Tan Ah Teck, Java for dummies, $11.11

Tan Ah Teck, More Java for dummies, $22.22

==== 5recods found ====

```

## 前端后端双层验证
&emsp;&emsp;一般情况下，我们给用户的输入是通过html，用户输入通过前端js等操作，此时js可以对用户的输入合法性等进行简单验证而不用麻烦服务端进行，此时也可以降低服务端请求压力。但是仅仅在客户端验证并不安全，因为有恶意用户可能不通过页面操作，而是自己通过js脚本或者其他方式（如直接在浏览器输入url及相应参数访问后台）发送get或者post请求，此时服务器端必须进行再次验证才能保证安全性；

## HttpSession
&emsp;&emsp;Http协议中说他自己时stateless即无状态的，什么意思？就是你的前一个请求是用户登陆，后一个请求是加入购物车（但是后一个请求并不知道你登陆了），请求之间并不能相互获取状态数据。如何解决这个问题？我们在设计一个类的时候，类的行为或者说函数会多次执行或者调用，类如何保证每次调用都知道当前自己状态（即上一次执行后的状态）呢？类的状态由成员变量来维持！同样，为了保持HTTP请求之间知道状态，你必须提供状态数据供每一次请求参考，这个数据状态的维持就抽象成了数据共享存储模型的设计，无非就是内存/文件/数据库（本质上也是文件）/消息 来维持了，就是现在比较流行的三种方式，客户端cookie（文件），服务端session（内存），数据库table。这样每一次Http请求都带上或者查询这些状态数据，就实现了状态的维持了！为什么不能用消息（比如上一次Http请求完成后，将数据以消息形式异步发送给下一个请求），我猜测可能的原因是消息太复杂了，没必要。消息发送接受还不是要开辟一块内存，不如直接存储在内存中供大家使用，使得程序更复杂，因此该场景不适合用消息。

&emsp;&emsp;不过，目前有结合三种方式一起使用，从而优化用户体验！

&emsp;&emsp;发现如果不登陆京东，直接加入购物车，最多只能加50个item，此时只是在本地使用了cookie存储item；另外，如果你在chrome中禁用cookie，那么不登陆加入购物车的功能基本上是不能用的。而且而且很重要的一点，如果你禁用了cookie，京东根本无法登陆啊，有图有真相。这说明京东默认大家都使用cookie，并且登陆后设置使用cookie和user关联起来，当登陆以后，这些item会被添加到你的登陆后的购物车，并且云端同步到手机客户端，这说明登陆后在服务端存储了item，猜测可能时memcache或者database存储的；

&emsp;&emsp;tomcat8提供的servlet中的session management也是如此，HttpSession实际上是需要客户端打开cookie的，如果禁止，即使你在服务端使用request.getSession(),那也是一个新的session了。并不能维持会话状态，亲自测验过了。

&emsp;&emsp;如果你只是使用同一个浏览器发送请求，因为服务端将data存储到session的缘故，你的shopping cart状态得以维持，因为对于和shoppingcart相关的每一个请求，都会使用request.getSession获取这个全局的session。取出数据并返回给客户端；但是如果是换了另一个浏览器呢？

&emsp;&emsp;我同时使用chrome和firefox进行购物车操作，发现尽然互不影响，这说明tomcat8 webcontainer使用了方法来辨别request来自是哪一个浏览器；进过测验之后发现，其实在/start对应的EntryServlet和/search对应的QueryServlet还有/checkout对应的CheckoutServlet都使用request.getSession(false);//已经存在就返回，不存在什么都不干！ 而在CartServlet中使用request.getSession(true);//已经存在就返回，不存则创建一个！

&emsp;&emsp;实验也证明合理我们在（没有请求过/cart前提下）请求/start和/search时，request和response header中都没有Cookie
，因为他们都不创建session；但是当我第一次请求/cart时，response header中多了个Set-Cookie，因为第一次请求CartServlet是要创建新session的；此后一系列的request header中都自动添加了Cookie了；重要的事情来了，Tomcat8 中的Servlet都是单例模式，他是如何知道request来自哪里，又是如何轻而易举的通过request.getSession()获取到客户端想要的那个session呢？查看Set-Cookie选项你就知道了，原来服务端生成了一个JSESSIONID=4525C1896DCB743808B956F3EF9DC623唯一标示，这样servlet就可以轻而易举的维持不同session了；因为每一个请求都自带了这个ID。也就是说，**Servlet Session的实现依赖于客户端的cookie设置**,如果你要在禁止cookie的情况下也能实现会话，那么你需要做额外处理了。比如根据IP唯一表示（但ip也可能变化），或者让用户登陆 使用userid做唯一标示，最后就是将unique id隐藏在html中，每一次都通过url附带这个id参数发送出去，然后服务端取出来。

>>A servlet should be able to handle cases in which the client does not choose to join a session, such as when 
>>cookies are intentionally turned off. Until the client joins the session, isNew returns true. If the client 
>>chooses not to join the session, getSession will return a different session on each request, and isNew will 
>>always return true.

## 参考
[How to Install Apache Tomcat 8.0.x on Linux](http://linoxide.com/linux-how-to/install-tomcat-8-0-x-linux/)
[Setting up environment for JEE development under Ubuntu/Debian](http://sukharevd.net/environment-for-j2ee-development-under-ubuntu.html#tomcat)
[defference between /sbin/nologin and /bin/false](http://serverfault.com/questions/519215/what-is-the-difference-between-sbin-nologin-and-bin-false)
[Does /usr/sbin/nologin as a login shell serve a security purpose?](http://unix.stackexchange.com/questions/155139/does-usr-sbin-nologin-as-a-login-shell-serve-a-security-purpose)
[How To Install Apache Tomcat 8 on Ubuntu 16.04](https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-16-04)
