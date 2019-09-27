---
title: 详解Linux-I2C驱动
date: 2015-12-02 12:00:43 Z
tags:
- linux
layout: post
subtitle: Linux I2C driver
author: hello2mao
---

#目录

-   [一、LinuxI2C 驱动--概述](#1)
-   [1.1 写在前面](#1.1)
-   [1.2 I2C](#1.2)
-   [1.3 硬件](#1.3)
-   [1.4 软件](#1.4)
-   [1.5 参考](#1.5)
-   [二、LinuxI2C 驱动--I2C 总线](#2)
-   [2.1 I2C 总线物理结构](#2.1)
-   [2.2 I2C 总线特性](#2.2)
-   [2.3 开始和停止条件](#2.3)
-   [2.4 数据传输格式](#2.4)
-   [2.5 响应](#2.5)
-   [2.6 总线仲裁](#2.6)
-   [三、LinuxI2C 驱动--解析 EEPROM 的读写](#3)
-   [3.1 概述](#3.1)
-   [3.2 设备地址](#3.2)
-   [3.3 读 eeprom](#3.3)
-   [3.4 写 eeprom](#3.4)
-   [四、LinuxI2C 驱动--从两个访问 eeprom 的例子开始](#4)
-   [4.1 通过 sysfs 文件系统访问 I2C 设备](#4.1)
-   [4.2 通过 devfs 访问 I2C 设备](#4.2)
-   [4.3 总结](#4.3)
-   [五、LinuxI2C 驱动--浅谈 LinuxI2C 驱动架构](#5)
-   [5.1 I2C 体系结构](#5.1)
-   [5.2 I2C 重要数据结构](#5.2)
-   [六、LinuxI2C 驱动--I2C 设备驱动](#6)
-   [6.1 eeprom 板级设备资源](#6.1)
-   [6.2 AT24C01A EEPROM 的 I2C 设备驱动](#6.2)
-   [6.2.1 at24_driver](#6.2.1)
-   [6.2.2 at24probe() / at24remove()](#6.2.2)
-   [6.2.3 at24binread()](#6.2.3)
-   [6.2.4 at24binwrite()](#6.2.4)
-   [6.3 总结](#6.3)
-   [七、LinuxI2C 驱动--I2C 总线驱动](#7)
-   [7.1 三星 S5PV210 i2c 适配器的硬件描述](#7.1)
-   [7.2 i2c 总线驱动的加载/卸载](#7.2)
-   [7.3 i2c 总线驱动的 probe](#7.3)
-   [7.4 启动 i2c 传输](#7.4)
-   [7.5 通过中断来推进 i2c 的传输](#7.5)
-   [7.6 总结](#7.6)

<h2 id="1">一、LinuxI2C驱动--概述</h2>
<h3 id="1.1">1.1 写在前面</h3>
本人学生一枚，之前没有详细的接触过linux驱动，只是读过宋宝华的《Linux设备驱动开发详解》，这段时间想静下心来学习下linux i2c驱动，在网上找了很多资料，前辈们写的文章让我受益匪浅，但是一开始上手真的很痛苦，基本上大家都是从linux i2c体系结构的三大组成谈起：i2c核心，i2c总线驱动，i2c设备驱动，好抽象。所以我才想写这个文章，从一个新人的角度分享下我学习linux i2c驱动的心得，写的不对的地方欢迎大家批评指正。

因为对 Linux 设备模型还不是很熟悉，所以我按照如何去实现一个 i2c 传输来讲述，对于平台总线、设备与总线如何去匹配等暂时忽略。

当然很多东西都是我从网上搜刮而来的，也请大家原谅。我会把一些有用的博文链接放在后面，希望对大家有用。

<h3 id="1.2">1.2 I2C</h3>
I2C总线是由Philips公司开发的两线式串行总线，这两根线为时钟线(SCL)和双向数据线(SDA)。由于I2C总线仅需要两根线，因此在电路板上占用的空间更少，带来的问题是带宽较窄。I2C在标准模式下传输速率最高100Kb/s，在快速模式下最高可达400kb/s。属于半双工。

在嵌入式系统中，I2C 应用非常广泛，大多数微控制器中集成了 I2C 总线，一般用于和 RTC，EEPROM，智能电池电路，传感器，LCD 以及其他类似设备之间的通信。

<h3 id="1.3">1.3 硬件</h3>
开发板：飞凌OK210

CPU 型号：Samsung S5PV210

EEPROM 型号：AT24C01A

![linux-i2c-1.3.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-1.3.png)

<h3 id="1.4">1.4 软件</h3>
linux版本：Linux 2.6.35.7

I2C 总线驱动：drivers/i2c/busses/i2c-s3c2410.c

eeprom 驱动：drivers/misc/eeprom/at24.c

<h3 id="1.5">1.5 参考</h3>
* 《Linux设备驱动开发详解》 宋宝华
* <http://blog.csdn.net/liaozc/article/details/6655015> Zechin的专栏
* <http://www.linxh.blog.chinaunix.net/uid/25445243/sid-179653-list-1.html> 辉辉308
* <http://www.linuxidc.com/Linux/2011-11/47651.htm> Linux社区 作者：cjok376240497
* <http://www.embedu.org/Column/Column190.htm> 作者：刘老师,华清远见嵌入式学院讲师。
* <http://www.linuxidc.com/Linux/2013-10/91993p14.htm> Linux社区 作者：赵春江

<h2 id="2">二、LinuxI2C驱动--I2C总线</h2>
本节分析下I2C总线协议，因为我的开发板是三星s5pv210芯片，所以就以此为例。

<h3 id="2.1">2.1 I2C总线物理结构</h3>

![linux-i2c-2.1.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-2.1.png)

I2C 总线在物理连接上非常简单，分别由 SDA(串行数据线)和 SCL(串行时钟线)及上拉电阻组成。通信原理是通过对 SCL 和 SDA 线高低电平时序的控制，来产生 I2C 总线协议所需要的信号进行数据的传递。在总线空闲状态时，这两根线一般被上面所接的上拉电阻拉高，保持着高电平。

<h3 id="2.2">2.2 I2C总线特性</h3>
* 每个连接到总线的器件都可以通过唯一的地址和一直存在的简单的主机/从机关系来软件设定地址
* 多主机总线，如果两个或者更多的主机同时初始化数据传输，可以通过仲裁防止数据被破坏。
* 串行8位双向数据传输
* 标准模式传输速率为100kbits/s
* 快速模式传输速率为400kbits/s
* 7位地址模
* 支持主机发、主机收，从机发、从机收

<h3 id="2.3">2.3 开始和停止条件</h3>
当SCL是高电平时，SDA线由高电平向低电平切换，表示开始；当SCL是高电平时，SDA线由低电平向高电平切换，表示停止。如下图所示。

![linux-i2c-2.3.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-2.3.png)

<h3 id="2.4">2.4 数据传输格式</h3>
发送到SDA线上的每个字节必须为8位，每次传输可以发送的字节数不受限制，但是每个字节后面必须跟一个响应位。

![linux-i2c-2.4.1.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-2.4.1.png)

![linux-i2c-2.4.2.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-2.4.2.png)

<h3 id="2.5">2.5 响应</h3>
数据传输必须带响应，响应时钟脉冲由主机产生，在SCL的第9个时钟脉冲上，前8个时钟脉冲用来传输8位即1byte的数据。

当发送端收到响应时钟脉冲的时候就会拉高 SDA 从而释放 SDA 线，而接收端通过拉低 SDA 先来表示收到数据，即 SDA 在响应期间保持低电平。

![linux-i2c-2.5.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-2.5.png)

<h3 id="2.6">2.6 总线仲裁</h3>
当两个主机在总线上产生竞争时就需要仲裁。

SDA 线低电平的优先级高于高电平。当一个主机首先产生低电平，而紧接着另一个主机产生高电平，但是由于低电平的优先级高于高电平，所以总线成低电平，也就是发低电平的主机占有总线而发高电平的主机不占有总线。如果两个主机都是发送低电平，那么继续比较下一个时钟周期的电平来决定谁占有总线，以此类推。

![linux-i2c-2.6.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-2.6.png)

<h2 id="3">三、LinuxI2C驱动--解析EEPROM的读写</h2>
本节介绍eeprom的读写时序，参考的是AT24C01A的datasheet。

<h3 id="3.1">3.1 概述</h3>
AT24C01A的存储大小是1K，页大小是8个字节。

![linux-i2c-3.1.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-3.1.png)

<h3 id="3.2">3.2 设备地址</h3>
![linux-i2c-3.2.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-3.2.png)

7 位地址，前四位是 1010，后三位由芯片引脚决定，由原理图可知后三位是 000，也就是设备地址为 0x50，因为数据传输是 8 位的，最后一位决定是读还是写。

<h3 id="3.3">3.3 读eeprom</h3>
![linux-i2c-3.3.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-3.3.png)

读任意地址 eeprom 的数据，首先第一个字节得先在 SDA 上发出 eeprom 的设备地址，也就是 0x50，并且 8 位数据的最后一位是低电平表示写设备，然后第二个字节是要读的数据在 eeprom 内的地址，这样以后再产生开始条件，第三个字节在 SDA 上发出设备地址，此时的最后一位是高电平，表示读设备，第四个字节的数据就是读 eeprom 的对应地址的数据。

可以看到，读 eeprom 需要两个开始条件，也就是 2 条消息，第一条消息写 eeprom 确定读的位置，大小为 2 个字节，第二条消息才是真正的读 eeprom。

<h3 id="3.4">3.4 写eeprom</h3>
![linux-i2c-3.4.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-3.4.png)

写 eeprom 就相对简单，只需一个开始条件，第一个字节发出设备地址和置最低位为低电平表示写 eeprom，第二个字节发出要读数据在 eerpom 的地址，第三个字节读到的数据就对应地址在 eeprom 上的数据

<h2 id="4">四、LinuxI2C驱动--从两个访问eeprom的例子开始</h2>
本小节介绍两个在linux应用层访问eeprom的方法，并给出示例代码方便大家理解。第一个方法是通过sysfs文件系统对eeprom进行访问，第二个方法是通过eeprom的设备文件进行访问。这两个方法分别对应了i2c设备驱动的两个不同的实现，在后面的小结会详细的分析。

<h3 id="4.1">4.1 通过sysfs文件系统访问I2C设备</h3>
eeprom的设备驱动在/sys/bus/i2c/devices/0-0050/目录下把eeprom设备映射为一个二进制节点，文件名为eeprom。对这个eeprom文件的读写就是对eeprom进行读写。

我们可以先用 cat 命令来看下 eeprom 的内容。

    [root@FORLINX210]# cat eeprom
    �����������X�����������������������������������������������

发现里面都是乱码，然后用 echo 命令把字符串“test”输入给 eeprom 文件，然后再 cat 出来。

就会发现字符串 test 已经存在 eeprom 里面了，我们知道 sysfs 文件系统断电后就没了，也无法对数据进行保存，为了验证确实把“test”字符串存储在了 eeprom，可以把系统断电重启，然后 cat eeprom，会发现 test 还是存在的，证明确实对 eeprom 进行了写入操作。

当然，因为 eeprom 已经映射为一个文件了，我们还可以通过文件 I/O 写应用程序对其进行简单的访问测试。比如以下程序对特定地址（0x40）写入特定数据（Hi,this is an eepromtest!），然后再把写入的数据在此地址上读出来。

    #include<stdio.h>
    #include<stdlib.h>
    #include<sys/types.h>
    #include<sys/stat.h>
    #include<fcntl.h>
    #include<string.h>

    int main(void){
      int fd, size, len, i;
      char buf[50]= {0};
      char *bufw="Hi,this is an eepromtest!";//要写入的数据

      len=strlen(bufw);//数据长度
      fd= open("/sys/bus/i2c/devices/0-0050/eeprom",O_RDWR);//打开文件
      if(fd< 0)
      {
          printf("####i2c test device open failed####/n");
          return(-1);
      }
      //写操作
      lseek(fd,0x40,SEEK_SET); //定位地址，地址是0x40
      if((size=write(fd,bufw, len))<0)//写入数据
      {
          printf("write error\n");
          return 1;
      }
      printf("writeok\n");
      //读操作
      lseek(fd,0x40, SEEK_SET);//准备读，首先定位地址，因为前面写入的时候更新了当前文件偏移量，所以这边需要重新定位到0x40.
      if((size=read(fd,buf,len))<0)//读数据
      {
          printf("readerror\n");
          return 1;
      }
      printf("readok\n");
      for(i=0; i< len; i++)
          printf("buff[%d]=%x\n",i, buf[i]);//打印数据
      close(fd);

      return 0;
    }

<h3 id="4.2">4.2 通过devfs访问I2C设备</h3>
linux的i2c驱动会针对每个i2c适配器在/dev/目录下生成一个主设备号为89的设备文件，简单的来说，对于本例的eeprom驱动，/dev/i2c/0就是它的设备文件，因此接下来的eeprom的访问就变为了对此设备文件的访问。

我们需要用到两个结构体 i2c_msg 和 i2c_rdwr_ioctl_data。

    struct i2c_msg { //i2c消息结构体，每个i2c消息对应一个结构体
     __u16 addr; /* 从设备地址，此处就是eeprom地址，即0x50 */
     __u16 flags;    /* 一些标志，比如i2c读等*/
     __u16 len;      /* i2c消息的长度 */
     __u8 *buf;      /* 指向i2c消息中的数据 */
     };

    struct i2c_rdwr_ioctl_data {
     struct i2c_msg __user *msgs;    /* 指向一个i2c消息 */
     __u32 nmsgs;            /* i2c消息的数量 */
    };

对一个 eeprom 上的特定地址（0x10）写入特定数据（0x58）并在从此地址读出写入数据的示例程序如下所示。

    #include <stdio.h>
    #include <linux/types.h>
    #include <stdlib.h>
    #include <fcntl.h>
    #include <unistd.h>
    #include <sys/types.h>
    #include <sys/ioctl.h>
    #include <errno.h>
    #include <linux/i2c.h>
    #include <linux/i2c-dev.h>

    int main()
    {
        int fd,ret;
        struct i2c_rdwr_ioctl_data e2prom_data;
        fd=open("/dev/i2c/0",O_RDWR);//打开eeprom设备文件结点
        if(fd<0)
        {
            perror("open error");
        }

        e2prom_data.nmsgs=2;
        e2prom_data.msgs=(struct i2c_msg*)malloc(e2prom_data.nmsgs*sizeof(struct i2c_msg));//分配空间
        if(!e2prom_data.msgs)
        {
            perror("malloc error");
            exit(1);
        }
        ioctl(fd,I2C_TIMEOUT,1);/*超时时间*/
        ioctl(fd,I2C_RETRIES,2);/*重复次数*/

        /*写eeprom*/
        e2prom_data.nmsgs=1;//由前面eeprom读写分析可知，写eeprom需要一条消息
        (e2prom_data.msgs[0]).len=2; //此消息的长度为2个字节，第一个字节是要写入数据的地址，第二个字节是要写入的数据
        (e2prom_data.msgs[0]).addr=0x50;//e2prom 设备地址
        (e2prom_data.msgs[0]).flags=0; //写
        (e2prom_data.msgs[0]).buf=(unsigned char*)malloc(2);
        (e2prom_data.msgs[0]).buf[0]=0x10;// e2prom 写入目标的地址
        (e2prom_data.msgs[0]).buf[1]=0x58;//写入的数据
        ret=ioctl(fd,I2C_RDWR,(unsigned long)&e2prom_data);//通过ioctl进行实际写入操作，后面会详细分析
        if(ret<0)
        {
            perror("ioctl error1");
        }
        sleep(1);

        /*读eeprom*/
        e2prom_data.nmsgs=2;//读eeprom需要两条消息
        (e2prom_data.msgs[0]).len=1; //第一条消息实际是写eeprom，需要告诉eeprom需要读数据的地址，因此长度为1个字节
        (e2prom_data.msgs[0]).addr=0x50; // e2prom 设备地址
        (e2prom_data.msgs[0]).flags=0;//先是写
        (e2prom_data.msgs[0]).buf[0]=0x10;//e2prom上需要读的数据的地址
        (e2prom_data.msgs[1]).len=1;//第二条消息才是读eeprom，
        (e2prom_data.msgs[1]).addr=0x50;// e2prom 设备地址
        (e2prom_data.msgs[1]).flags=I2C_M_RD;//然后是读
        (e2prom_data.msgs[1]).buf=(unsigned char*)malloc(1);//存放返回值的地址。
        (e2prom_data.msgs[1]).buf[0]=0;//初始化读缓冲，读到的数据放到此缓冲区
        ret=ioctl(fd,I2C_RDWR,(unsigned long)&e2prom_data);//通过ioctl进行实际的读操作
        if(ret<0)
        {
            perror("ioctl error2");
        }

        printf("buff[0]=%x\n",(e2prom_data.msgs[1]).buf[0]);
        /***打印读出的值，没错的话，就应该是前面写的0x58了***/
        close(fd);

        return 0;
    }

<h3 id="4.3">4.3 总结</h3>
本小节介绍了两种在linux应用层访问eeprom的方法，并且给出了示例程序，通过sysfs文件系统访问eeprom操作简单，无需了解eeprom的硬件特性以及访问时序，而通过devfs访问eeprom的方法则需要了解eeprom的读写时序。

后面分析后会发现，第一种通过 sysfs 文件系统的二进制结点访问 eeprom 的方法是由 eeprom 的设备驱动实现的，是一种专有的方法；而第二种通过 devfs 访问 eeprom 的方法是 linux i2c 提供的一种通用的方法，访问设备的能力有限。

<h2 id="5">五、LinuxI2C驱动--浅谈LinuxI2C驱动架构</h2>
前面几个小结介绍了i2c总线的协议，又介绍了我们关注的eeprom的读写访问时序，还给出了两个访问eeprom的例子，我的目的是为了能更好的理解后面解析Linux下i2c驱动。

网上介绍 Linux I2C 驱动架构的文章非常的多，我把这些内容做了个归纳与简化，但是在搬出这些非常抽象的内容之前，我想先谈下我的理解。

如下图

![linux-i2c-5.0.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-5.0.png)

图中画了一个三星的 s5pv210 处理器，在处理器的里面集成了一个 I2C 适配器，外面有一个 eeprom，通过 SDA、SCL 连接到 cpu 内集成的 i2c 适配器上。这样 cpu 就可以控制 i2c 适配器与外部的 eeprom 进行交互，也就是 i2c 适配器产生符合 i2c 协议的信号与 eeprom 进行通信。

所以对应到 linux 驱动下，控制 i2c 适配器有一套驱动代码，叫做 i2c 总线驱动，是用来产生 i2c 时序信号的，可以发送和接受数据；控制 eeprom 有一套驱动代码，叫做 i2c 设备驱动，这套驱动代码才是真正的对硬件 eeprom 控制。这也符合 linux 设备驱动分层的思想。而两套驱动代码之间有一个 i2c 核心，用来起到承上启下的作用。

以一个写 eeprom 为例，应用层发出写 eeprom 消息，i2c 设备驱动接到消息，把消息封装成一个前文提到的 i2c 消息结构体，然后经 i2c 核心的调度把消息传给 i2c 适配器，i2c 适配器就根据当前 cpu 的 i2c 总线协议把消息通过 SDA 和 SCL 发给了 eeprom。

接下来开始搬运，，

<h3 id="5.1">5.1 I2C体系结构</h3>
linux的i2c体系结构分为三个组成部分。放张图加深理解。

![linux-i2c-5.0.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-5.0.png)

（1）i2c 核心

提供了 I2C 总线驱动的注册、注销方法
提供了 I2C 设备驱动的注册、注销方法
提供了 I2C 通信方法(algorithm)
对应代码：drivers/i2c/i2c-core.c

（2）i2c 总线驱动

I2C 总线驱动是对 I2C 硬件体系结构中适配器端的实现，适配器可由 CPU 控制，甚至集成在 CPU 内部(大多数微控制器都这么做)。适配器就是我们经常所说的控制器。

经由 I2C 总线驱动的代码，我们可以控制 I2C 适配器以主控方式产生开始位，停止位，读写周期，以及以从设备方式被读写，产生 ACK 等。

I2C 总线驱动由 i2c_adapter 和 i2c_algorithm 来描述
对应代码：drivers/i2c/busses/i2c-s3c2410.c

（3）i2c 设备驱动

I2C 设备驱动是对 I2C 硬件体系结构中设备端的实现，设备一般挂接在收 CPU 控制的 I2C 适配器上，通过 I2C 适配器与 CPU 交换数据。

I2C 设备驱动程序由 i2c_driver 来描述

对应代码：drivers/misc/eeprom/at24.c

<h3 id="5.2">5.2 I2C重要数据结构</h3>
在include/linux/i2c.h中定义四个I2C驱动中重要的数据结构：i2c_adapter,i2c_algorithm,i2c_driver,i2c_client.

i2c_adapter 对应物理上的一个 i2c 适配器

    struct i2c_adapter {
      struct module *owner;//所属模块
      unsigned int id;
      unsigned int class;       /* classes to allow probing for */
      const struct i2c_algorithm *algo; /* 总线通讯方法指针，需要其产生特定的访问周期信号 */
      void *algo_data;

      /* data fields that are valid for all devices   */
      struct rt_mutex bus_lock;

      int timeout;            /* in jiffies */
      int retries;/* 重复次数 */
      struct device dev;      /* the adapter device */

      int nr;
      char name[48];
      struct completion dev_released;

      struct list_head userspace_clients;
    };

i2c_algorithm 对应一套通讯方法

    struct i2c_algorithm {
      int (*master_xfer)(struct i2c_adapter *adap, struct i2c_msg *msgs,
                int num);//产生i2c访问周期说需要的信号
      int (*smbus_xfer) (struct i2c_adapter *adap, u16 addr,
                unsigned short flags, char read_write,
                u8 command, int size, union i2c_smbus_data *data);

      /* To determine what the adapter supports */
      u32 (*functionality) (struct i2c_adapter *);//返回说支持的通讯协议
    };

i2c_driver 对应一套驱动方法

    struct i2c_driver {
     unsigned int class;

     int (*probe)(struct i2c_client *, const struct i2c_device_id *);
     int (*remove)(struct i2c_client *);
     void (*shutdown)(struct i2c_client *);
     int (*suspend)(struct i2c_client *, pm_message_t mesg);
     int (*resume)(struct i2c_client *);
     void (*alert)(struct i2c_client *, unsigned int data);
     int (*command)(struct i2c_client *client, unsigned int cmd, void *arg);
     struct device_driver driver;
     const struct i2c_device_id *id_table;//该驱动所支持的i2c设备的ID表
     int (*detect)(struct i2c_client *, struct i2c_board_info *);
     const unsigned short *address_list;
     struct list_head clients;
    };

i2c_client 对应真实的物理设备，每个 i2c 设备都需要一个 i2c_client 来描述

    struct i2c_client {
     unsigned short flags;       /* div., see below      */
     unsigned short addr;        /* chip address - NOTE: 7bit    */
                     /* addresses are stored in the  */
                     /* _LOWER_ 7 bits       */
     char name[I2C_NAME_SIZE];
     struct i2c_adapter *adapter;    /* the adapter we sit on    */
     struct i2c_driver *driver;  /* and our access routines  */
     struct device dev;      /* the device structure     */
     int irq;            /* irq issued by device     */
     struct list_head detected;
    };

（1）i2c_adapter 与 i2c_algorithm
一个 I2C 适配器需要 i2c_algorithm 中提供的通信函数来控制适配器上产生特定的访问周期。i2c_algorithm 中的关键函数 master_xfer()用于产生 I2C 访问周期需要的信号，以 i2c_msg 为单位。

    struct i2c_msg {
     __u16 addr; /* slave address            */
     __u16 flags;
     __u16 len;      /* msg length               */
     __u8 *buf;      /* pointer to msg data          */
    };

（2）i2c_adapter 与 i2c_client
i2c_driver 与 i2c_client 是一对多的关系，一个 i2c_driver 上可以支持多个同等类型的 i2c_client。

（3）i2c_adapter 与 i2c_client
i2c_adapter 与 i2c_client 的关系与 I2C 硬件体系中适配器和从设备的关系一致，i2c_client 依附在 i2c_adapter 上。

<h2 id="6">六、LinuxI2C驱动--I2C设备驱动</h2>
本节主要分析eeprom的所属的i2c设备驱动，此驱动主要实现了能够通过sysfs文件系统访问eeprom。

<h3 id="6.1">6.1 eeprom板级设备资源</h3>
因为原开发板的eeprom驱动还没调试好，板级资源还没写好，所以需要自己加进去。
修改arch/arm/mach-s5pv210/mach-smdkc110.c文件。

    static struct at24_platform_data at24c01 = {
     .byte_len = SZ_8K / 8,/*eeprom大小*/
     .page_size = 8,/*页大小*/
    };

    /* I2C0 */
    static struct i2c_board_info i2c_devs0[] __initdata = {
     {
         I2C_BOARD_INFO("24c01",0x50),//0x50是eeprom的设备地址
         .platform_data=&at24c01,
     },
    }

这样以后后面调用 smdkc110_machine_init 就会把资源注册进去。

     static void __init smdkc110_machine_init(void)
     {
     ….
     i2c_register_board_info(0, i2c_devs0, ARRAY_SIZE(i2c_devs0));
     ….
     }

<h3 id="6.2">6.2 AT24C01A EEPROM 的I2C设备驱动</h3>
<h4 id="6.2.1">6.2.1 at24_driver</h4>

前面讲 i2c 驱动架构的时候，说到 I2C 设备驱动主要由 i2c_driver 来描述。

在 drivers/misc/eeprom/at24.c 中可以看到 eeprom 驱动对 i2c_driver 结构的实例化。

     static struct i2c_driver at24_driver = {
     .driver = {
         .name = "at24",
         .owner = THIS_MODULE,
     },
     .probe = at24_probe,
     .remove = __devexit_p(at24_remove),
     .id_table = at24_ids,
     };

其中 probe 和 remove 会在模块初始化和卸载的时候被调用。

     static int __init at24_init(void)//模块初始化
     {
        io_limit = rounddown_pow_of_two(io_limit);//io_limit是写eeprom时允许一次写入的最大字节，默认128Byte，是驱动模块参数。
        return i2c_add_driver(&at24_driver);//添加i2c_driver,在i2c核心中实现，会调用at24_probe.
     }
     module_init(at24_init);

     static void __exit at24_exit(void)//模块卸载
     {
        i2c_del_driver(&at24_driver);//删除i2c_driver,会调用at24_remove
     }
     module_exit(at24_exit);

<h4 id="6.2.2">6.2.2 at24_probe() / at24_remove()</h4>
    static int at24_probe(struct i2c_client *client, const struct i2c_device_id *id)
    {
    struct at24_platform_data chip;
    bool writable;
    int use_smbus = 0;
    struct at24_data *at24;
    int err;
    unsigned i, num_addresses;
    kernel_ulong_t magic;

    //获取板级设备信息
    if (client->dev.platform_data) {
        chip = *(struct at24_platform_data *)client->dev.platform_data;
    } else {
        if (!id->driver_data) {
            err = -ENODEV;
            goto err_out;
        }
        magic = id->driver_data;
        chip.byte_len = BIT(magic & AT24_BITMASK(AT24_SIZE_BYTELEN));
        magic >>= AT24_SIZE_BYTELEN;
        chip.flags = magic & AT24_BITMASK(AT24_SIZE_FLAGS);
        /*
         * This is slow, but we can't know all eeproms, so we better
         * play safe. Specifying custom eeprom-types via platform_data
         * is recommended anyhow.
         */
        chip.page_size = 1;

        chip.setup = NULL;
        chip.context = NULL;
    }

    //检查参数，必须为2的幂
    if (!is_power_of_2(chip.byte_len))
        dev_warn(&client->dev,
            "byte_len looks suspicious (no power of 2)!\n");
    if (!is_power_of_2(chip.page_size))
        dev_warn(&client->dev,
            "page_size looks suspicious (no power of 2)!\n");

    /* Use I2C operations unless we're stuck with SMBus extensions. */
    //检查是否支持I2C协议，如果不支持则检查是否支持SMBUS
    if (!i2c_check_functionality(client->adapter, I2C_FUNC_I2C)) {
        if (chip.flags & AT24_FLAG_ADDR16) {
            err = -EPFNOSUPPORT;
            goto err_out;
        }
        if (i2c_check_functionality(client->adapter,
                I2C_FUNC_SMBUS_READ_I2C_BLOCK)) {
            use_smbus = I2C_SMBUS_I2C_BLOCK_DATA;
        } else if (i2c_check_functionality(client->adapter,
                I2C_FUNC_SMBUS_READ_WORD_DATA)) {
            use_smbus = I2C_SMBUS_WORD_DATA;
        } else if (i2c_check_functionality(client->adapter,
                I2C_FUNC_SMBUS_READ_BYTE_DATA)) {
            use_smbus = I2C_SMBUS_BYTE_DATA;
        } else {
            err = -EPFNOSUPPORT;
            goto err_out;
        }
    }

    if (chip.flags & AT24_FLAG_TAKE8ADDR)//检查时候使用8个地址
        num_addresses = 8;
    else
        num_addresses = DIV_ROUND_UP(chip.byte_len,//AT24C01使用一个地址
            (chip.flags & AT24_FLAG_ADDR16) ? 65536 : 256);

    at24 = kzalloc(sizeof(struct at24_data) +
        num_addresses * sizeof(struct i2c_client *), GFP_KERNEL);//为at24_data分配内存，同时根据地址个数分配i2c_client
    if (!at24) {
        err = -ENOMEM;
        goto err_out;
    }

    mutex_init(&at24->lock);
    //初始化at24_data，也就是填充此结构体
    at24->use_smbus = use_smbus;
    at24->chip = chip;
    at24->num_addresses = num_addresses;

    /*
     * Export the EEPROM bytes through sysfs, since that's convenient.
     * By default, only root should see the data (maybe passwords etc)
     */
    //以二进制结点的形式呈现eeprom的数据
    sysfs_bin_attr_init(&at24->bin);
    at24->bin.attr.name = "eeprom";//结点名字
    at24->bin.attr.mode = chip.flags & AT24_FLAG_IRUGO ? S_IRUGO : S_IRUSR;
    at24->bin.read = at24_bin_read;//绑定读函数
    at24->bin.size = chip.byte_len;

    at24->macc.read = at24_macc_read;

    //判断是否可写
    writable = !(chip.flags & AT24_FLAG_READONLY);
    if (writable) {//如果可写
        if (!use_smbus || i2c_check_functionality(client->adapter,
                I2C_FUNC_SMBUS_WRITE_I2C_BLOCK)) {

            unsigned write_max = chip.page_size;

            at24->macc.write = at24_macc_write;

            at24->bin.write = at24_bin_write;//绑定写函数
            at24->bin.attr.mode |= S_IWUSR;//文件拥有者可写

            if (write_max > io_limit)//一次最多写io_limit个字节
                write_max = io_limit;
            if (use_smbus && write_max > I2C_SMBUS_BLOCK_MAX)
                write_max = I2C_SMBUS_BLOCK_MAX;
            at24->write_max = write_max;

            /* buffer (data + address at the beginning) */
            at24->writebuf = kmalloc(write_max + 2, GFP_KERNEL);//分配缓冲区，多余两个字节用于保存寄存器地址
            if (!at24->writebuf) {
                err = -ENOMEM;
                goto err_struct;
            }
        } else {
            dev_warn(&client->dev,
                "cannot write due to controller restrictions.");
        }
    }

    at24->client[0] = client;

    /* use dummy devices for multiple-address chips */
    for (i = 1; i < num_addresses; i++) {
        at24->client[i] = i2c_new_dummy(client->adapter,
                    client->addr + i);
        if (!at24->client[i]) {
            dev_err(&client->dev, "address 0x%02x unavailable\n",
                    client->addr + i);
            err = -EADDRINUSE;
            goto err_clients;
        }
    }

    //向sysfs文件系统注册二进制结点
    err = sysfs_create_bin_file(&client->dev.kobj, &at24->bin);
    if (err)
        goto err_clients;

    //保存驱动数据
    i2c_set_clientdata(client, at24);

    dev_info(&client->dev, "%zu byte %s EEPROM %s\n",
        at24->bin.size, client->name,
        writable ? "(writable)" : "(read-only)");
    if (use_smbus == I2C_SMBUS_WORD_DATA ||
        use_smbus == I2C_SMBUS_BYTE_DATA) {
        dev_notice(&client->dev, "Falling back to %s reads, "
               "performance will suffer\n", use_smbus ==
               I2C_SMBUS_WORD_DATA ? "word" : "byte");
    }
    dev_dbg(&client->dev,
        "page_size %d, num_addresses %d, write_max %d, use_smbus %d\n",
        chip.page_size, num_addresses,
        at24->write_max, use_smbus);

    /* export data to kernel code */
    if (chip.setup)
        chip.setup(&at24->macc, chip.context);

    return 0;
    err_clients:
    for (i = 1; i < num_addresses; i++)
        if (at24->client[i])
            i2c_unregister_device(at24->client[i]);

    kfree(at24->writebuf);
    err_struct:
    kfree(at24);
    err_out:
    dev_dbg(&client->dev, "probe error %d\n", err);
    return err;
    }

at24_probe()函数主要的工作是在 sys 目录在创建 bin 结点文件，也就是前面通过 sysfs 文件系统访问 i2c 设备中提到的/sys/bus/i2c/devices/0-0050/eeprom 文件，用户可以用此文件来操作 eeprom，提供读/写操作方法，在 probe 里面读写操作已经与二进制结点绑定，读操作函数是 at24_bin_read()，写操作函数是 at24_bin_write()。

其中有个重要的结构体：

    struct at24_data {
     struct at24_platform_data chip;
     struct memory_accessor macc;
     int use_smbus;

     /*
      * Lock protects against activities from other Linux tasks,
      * but not from changes by other I2C masters.
      */
     struct mutex lock;
     struct bin_attribute bin;//二进制结点

     u8 *writebuf;//写缓冲区
     unsigned write_max;
     unsigned num_addresses;

     /*
      * Some chips tie up multiple I2C addresses; dummy devices reserve
      * them for us, and we'll use them with SMBus calls.
      */
     struct i2c_client *client[];
    };

at24_data 是此驱动的一些私有数据的封装，包括二进制结点，以及写缓冲区。

    static int __devexit at24_remove(struct i2c_client *client)
    {
    struct at24_data *at24;
    int i;

    at24 = i2c_get_clientdata(client);
    sysfs_remove_bin_file(&client->dev.kobj, &at24->bin);

    for (i = 1; i < at24->num_addresses; i++)
        i2c_unregister_device(at24->client[i]);

    kfree(at24->writebuf);
    kfree(at24);
    return 0;
    }

at24_remove()基本就是 at24_probe()的反操作。

<h4 id="6.2.3">6.2.3 at24_bin_read()</h4>
    static ssize_t at24_bin_read(struct file *filp, struct kobject *kobj,
         struct bin_attribute *attr,
         char *buf, loff_t off, size_t count)
    {
     struct at24_data *at24;

     //通过kobj获得device，再获取driver_data
     at24 = dev_get_drvdata(container_of(kobj, struct device, kobj));
     return at24_read(at24, buf, off, count);//调用at24_read()
    }

at24_bin_read()通过 dev_get_drvdata()获取 at24_data 结构体数据。然后调用 at24_read()。

    static ssize_t at24_read(struct at24_data *at24,
         char *buf, loff_t off, size_t count)
    {
     ssize_t retval = 0;

     if (unlikely(!count))
         return count;

     /*
      * Read data from chip, protecting against concurrent updates
      * from this host, but not from other I2C masters.
      */
     mutex_lock(&at24->lock);//访问设备前加锁

     while (count) {
         ssize_t status;

         status = at24_eeprom_read(at24, buf, off, count);
         if (status <= 0) {
             if (retval == 0)
                 retval = status;
             break;
         }
         buf += status;
         off += status;
         count -= status;
         retval += status;
     }

     mutex_unlock(&at24->lock);//访问结束后解锁

     return retval;
    }

at24_read()传入的参数，at24 是驱动私有数据结构体 at24_data，buf 是读 eeprom 后读到的数据存储的缓冲区，off 是数据的偏移地址，count 是要读数据的大小。at24_read()主要调用 at24_eeprom_read()去读，但是此函数读 eeprom 能读到的数据个数有限制，不一定一次就把 count 个数据都读到，所以用 while 来读，并且读到 status 个数据后更新 count，表示还剩多少个数据没读到，同时也要更新数据偏移 off，和读入缓冲 buf。

    static ssize_t at24_eeprom_read(struct at24_data *at24, char *buf,
        unsigned offset, size_t count)
    {
    struct i2c_msg msg[2];
    u8 msgbuf[2];
    struct i2c_client *client;
    unsigned long timeout, read_time;
    int status, i;

    memset(msg, 0, sizeof(msg));

    /*
     * REVISIT some multi-address chips don't rollover page reads to
     * the next slave address, so we may need to truncate the count.
     * Those chips might need another quirk flag.
     *
     * If the real hardware used four adjacent 24c02 chips and that
     * were misconfigured as one 24c08, that would be a similar effect:
     * one "eeprom" file not four, but larger reads would fail when
     * they crossed certain pages.
     */

    /*
     * Slave address and byte offset derive from the offset. Always
     * set the byte address; on a multi-master board, another master
     * may have changed the chip's "current" address pointer.
     */
    client = at24_translate_offset(at24, &offset);//获得client

    if (count > io_limit)
        count = io_limit;

    switch (at24->use_smbus) {//如果使用SMBUS
    case I2C_SMBUS_I2C_BLOCK_DATA:
        /* Smaller eeproms can work given some SMBus extension calls */
        if (count > I2C_SMBUS_BLOCK_MAX)
            count = I2C_SMBUS_BLOCK_MAX;
        break;
    case I2C_SMBUS_WORD_DATA:
        count = 2;
        break;
    case I2C_SMBUS_BYTE_DATA:
        count = 1;
        break;
    default://使用I2C协议
        /*
         * When we have a better choice than SMBus calls, use a
         * combined I2C message. Write address; then read up to
         * io_limit data bytes. Note that read page rollover helps us
         * here (unlike writes). msgbuf is u8 and will cast to our
         * needs.
         */
        i = 0;
        if (at24->chip.flags & AT24_FLAG_ADDR16)
            msgbuf[i++] = offset >> 8;
        msgbuf[i++] = offset;

        //由前小节读eeprom的时序可知，需要2条消息，第一条消息是写eeprom
        msg[0].addr = client->addr;//设备地址，即0x50
        msg[0].buf = msgbuf;
        msg[0].len = i;

        //第二条消息才是读eeprom，读到的数据存储在buf中。
        msg[1].addr = client->addr;//设备地址
        msg[1].flags = I2C_M_RD;//读
        msg[1].buf = buf;//读缓冲区
        msg[1].len = count;//要读数据的长度
    }

    /*
     * Reads fail if the previous write didn't complete yet. We may
     * loop a few times until this one succeeds, waiting at least
     * long enough for one entire page write to work.
     */
    timeout = jiffies + msecs_to_jiffies(write_timeout);
    do {
        read_time = jiffies;
        switch (at24->use_smbus) {
        case I2C_SMBUS_I2C_BLOCK_DATA:
            status = i2c_smbus_read_i2c_block_data(client, offset,
                    count, buf);
            break;
        case I2C_SMBUS_WORD_DATA:
            status = i2c_smbus_read_word_data(client, offset);
            if (status >= 0) {
                buf[0] = status & 0xff;
                buf[1] = status >> 8;
                status = count;
            }
            break;
        case I2C_SMBUS_BYTE_DATA:
            status = i2c_smbus_read_byte_data(client, offset);
            if (status >= 0) {
                buf[0] = status;
                status = count;
            }
            break;
        default://使用I2C协议去读
            status = i2c_transfer(client->adapter, msg, 2);//实际的数据传输，
            if (status == 2)
                status = count;
        }
        dev_dbg(&client->dev, "read %zu@%d --> %d (%ld)\n",
                count, offset, status, jiffies);

        if (status == count)//已经全部读取，则返回
            return count;

        /* REVISIT: at HZ=100, this is sloooow */
        msleep(1);
    } while (time_before(read_time, timeout));

    return -ETIMEDOUT;
    }

at24_eeprom_read()根据读 eeprom 所需要的时序，填充两个 i2c 消息结构体，第一个 i2c 消息结构体是写 eeprom，告诉 eeprom 要读的数据是哪个，第二个 i2c 消息才是真正的读 eeprom。最后把这两个 i2c 消息结构体传给 i2c_transfer()进行实际的消息传输。i2c_transfer()是 i2c 核心的函数，用于 i2c 设备与 i2c 适配器直接的消息传递，后面会分析。这里我们看到了 i2c 设备驱动通过 i2c 核心向 i2c 总线驱动传递消息的主要途径，i2c 总线驱动接收到 i2c 消息后就会控制 i2c 适配器根据传入的 i2c 消息，通过 SDA 和 SCL 与 eeprom 进行交互。

<h4 id="6.2.5">6.2.4 at24_bin_write()</h4>
     static ssize_t at24_bin_write(struct file *filp, struct kobject *kobj,
         struct bin_attribute *attr,
         char *buf, loff_t off, size_t count)
     {
     struct at24_data *at24;

     at24 = dev_get_drvdata(container_of(kobj, struct device, kobj));
     return at24_write(at24, buf, off, count);
     }

at24_bin_write()与 at24_bin_read()一样操作，获得 at24_data 后调用 at24_write().

    static ssize_t at24_write(struct at24_data *at24, const char *buf, loff_t off,
               size_t count)
    {
     ssize_t retval = 0;

     if (unlikely(!count))
         return count;

     /*
      * Write data to chip, protecting against concurrent updates
      * from this host, but not from other I2C masters.
      */
     mutex_lock(&at24->lock);

     while (count) {
         ssize_t status;

         status = at24_eeprom_write(at24, buf, off, count);
         if (status <= 0) {
             if (retval == 0)
                 retval = status;
             break;
         }
         buf += status;
         off += status;
         count -= status;
         retval += status;
     }

     mutex_unlock(&at24->lock);

     return retval;
    }

at24_write()的操作也是类似的，通过调用 at24_eeprom_write()来实现。

    static ssize_t at24_eeprom_write(struct at24_data *at24, const char *buf,
        unsigned offset, size_t count)
    {
    struct i2c_client *client;
    struct i2c_msg msg;
    ssize_t status;
    unsigned long timeout, write_time;
    unsigned next_page;

    /* Get corresponding I2C address and adjust offset */
    client = at24_translate_offset(at24, &offset);//获得对应的client

    /* write_max is at most a page */
    //检查写入字数
    if (count > at24->write_max)
        count = at24->write_max;

    /* Never roll over backwards, to the start of this page */
    //写入不会越过页边界（下一页）
    next_page = roundup(offset + 1, at24->chip.page_size);
    if (offset + count > next_page)
        count = next_page - offset;

    /* If we'll use I2C calls for I/O, set up the message */
    if (!at24->use_smbus) {//使用i2c协议，则填充i2c消息结构体
        int i = 0;

        //由前小节分析，写eeprom只需一条i2c消息
        msg.addr = client->addr;//设备地址
        msg.flags = 0;//写eeprom

        /* msg.buf is u8 and casts will mask the values */
        msg.buf = at24->writebuf;//写缓冲区
        if (at24->chip.flags & AT24_FLAG_ADDR16)
            msg.buf[i++] = offset >> 8;

        msg.buf[i++] = offset;
        memcpy(&msg.buf[i], buf, count);//复制需要发送的数据
        msg.len = i + count;//发送传读为要发送的数据长度，加上地址长度
    }

    /*
     * Writes fail if the previous one didn't complete yet. We may
     * loop a few times until this one succeeds, waiting at least
     * long enough for one entire page write to work.
     */
    timeout = jiffies + msecs_to_jiffies(write_timeout);//超时时间，为驱动模块参数，默认25ms
    do {
        write_time = jiffies;
        if (at24->use_smbus) {
            status = i2c_smbus_write_i2c_block_data(client,
                    offset, count, buf);
            if (status == 0)
                status = count;
        } else {//i2c传输
            status = i2c_transfer(client->adapter, &msg, 1);//实际传输
            if (status == 1)
                status = count;
        }
        dev_dbg(&client->dev, "write %zu@%d --> %zd (%ld)\n",
                count, offset, status, jiffies);

        if (status == count)//已经全部写入，返回
            return count;

        /* REVISIT: at HZ=100, this is sloooow */
        msleep(1);
    } while (time_before(write_time, timeout));

    return -ETIMEDOUT;
    }

与 at24_eeprom_read()类似，at24_eeprom_write()因为写 eeprom 需要 1 条 i2c 消息，最后实际的传输也是通过 i2c_transfer()实现。

<h3 id="6.3">6.3 总结</h3>

由上面简单的分析可知，通过 sysfs 文件系统访问 eeprom，对/sys/bus/i2c/devices/0-0050/eeprom 的读写是通过 at24_bin_read()/at24_bin_write() ==> at24_eeprom_read()/at24_eeprom_write() ==>i2c_transfer()来实现的。

<h2 id="7">七、LinuxI2C驱动--I2C总线驱动</h2>
前面分析了i2c设备驱动如何实现通过sysfs文件系统访问eeprom，对于读写eeprom，最后都是调用了i2c_transfer()，此函数的实现在i2c核心中。

    int i2c_transfer(struct i2c_adapter *adap, struct i2c_msg *msgs, int num)
    {
     unsigned long orig_jiffies;
     int ret, try;

     /* REVISIT the fault reporting model here is weak:
      *
      *  - When we get an error after receiving N bytes from a slave,
      *    there is no way to report "N".
      *
      *  - When we get a NAK after transmitting N bytes to a slave,
      *    there is no way to report "N" ... or to let the master
      *    continue executing the rest of this combined message, if
      *    that's the appropriate response.
      *
      *  - When for example "num" is two and we successfully complete
      *    the first message but get an error part way through the
      *    second, it's unclear whether that should be reported as
      *    one (discarding status on the second message) or errno
      *    (discarding status on the first one).
      */

     if (adap->algo->master_xfer) {
     #ifdef DEBUG
         for (ret = 0; ret < num; ret++) {
             dev_dbg(&adap->dev, "master_xfer[%d] %c, addr=0x%02x, "
                 "len=%d%s\n", ret, (msgs[ret].flags & I2C_M_RD)
                 ? 'R' : 'W', msgs[ret].addr, msgs[ret].len,
                 (msgs[ret].flags & I2C_M_RECV_LEN) ? "+" : "");
         }
     #endif

         if (in_atomic() || irqs_disabled()) {
             ret = rt_mutex_trylock(&adap->bus_lock);
             if (!ret)
                 /* I2C activity is ongoing. */
                 return -EAGAIN;
         } else {
             rt_mutex_lock(&adap->bus_lock);
         }

         /* Retry automatically on arbitration loss */
         orig_jiffies = jiffies;
         for (ret = 0, try = 0; try <= adap->retries; try++) {
             ret = adap->algo->master_xfer(adap, msgs, num);//i2c总线驱动的入口
             if (ret != -EAGAIN)
                 break;
             if (time_after(jiffies, orig_jiffies + adap->timeout))
                 break;
         }
         rt_mutex_unlock(&adap->bus_lock);

         return ret;
     } else {
         dev_dbg(&adap->dev, "I2C level transfers not supported\n");
         return -EOPNOTSUPP;
     }
    }

可以看到，语句 ret = adap->algo->master_xfer(adap, msgs, num)就是 i2c 总线驱动的入口，此语句是寻找 i2c_adapter 对应的 i2c_algorithm 后，使用 master_xfer()驱动硬件流程来进行实际的传输。

那么 i2c_adapter 是在哪里绑定了 i2c_algorithm 呢？master_xfer()又是如何来启动 i2c 传输的呢？在 i2c 总线驱动中我们就可以找到答案。

<h3 id="7.1">7.1 三星S5PV210 i2c适配器的硬件描述</h3>

s5pv210 处理器内部集成了一个 i2c 控制器，通过 4 个主要的寄存器就可以对其进行控制。
在 arch/arm/plat-samsung/include/plat/regs-iic.h 中列出了这几个寄存器。

    #define S3C2410_IICREG(x) (x)

    #define S3C2410_IICCON    S3C2410_IICREG(0x00)//i2c控制寄存器
    #define S3C2410_IICSTAT   S3C2410_IICREG(0x04)//i2c状态寄存器
    #define S3C2410_IICADD    S3C2410_IICREG(0x08)//i2c地址寄存器
    #define S3C2410_IICDS     S3C2410_IICREG(0x0C)//i2c收发数据移位寄存器

i2c 寄存器支持收发两种模式，我们主要使用主模式，通过对 IICCON、IICDS 和 IICADD 寄存器的操作，可以在 i2c 总线上产生开始位，停止位，数据和地址，而传输的状态则是通过 IICSTAT 寄存器获取。

在三星的 i2c 总线说明文档中给出了 i2c 总线进行传输的整个流程。

![linux-i2c-7.1.png](https://raw.githubusercontent.com/hello2mao/hello2mao.github.io/f68f7e805c54be0eeee638d9c194c6a650ae3a3c/public/img/technology/linux-i2c-7.1.png)

以通过 i2c 总线写 eeprom 为例，具体的流程如下：

1. 设置 GPIO 的相关引脚为 IIC 输出；
2. 设置 IIC（打开 ACK，打开 IIC 中断，设置 CLK 等）；
3. 设备地址赋给 IICDS ，并设置 IICSTAT，启动 IIC 发送设备地址出去；从而找到相应的设备即 IIC 总线上的设备。
4. 第一个 Byte 的设备地址发送后，从 EEPROM 得到 ACK 信号，此信号触发中断；
5. 在中断处理函数中把第二个 Byte（设备内地址）发送出去；发送之后，接收到 ACK 又触发中断；
6. 中断处理函数把第三个 Byte（真正的数据）发送到设备中。
7. 发送之后同样接收到 ACK 并触发中断，中断处理函数判断，发现数据传送完毕。
8. IIC Stop 信号，关 IIC 中断，置位各寄存器。

在下面的小节中将结合代码来分析 i2c 总线对上面流程的具体实现。

<h3 id="7.2">7.2 i2c总线驱动的加载/卸载</h3>

i2c 总线驱动被作为一个单独的模块加载，下面首先分析它的加载/卸载函数。

    static int __init i2c_adap_s3c_init(void)
    {
     return platform_driver_register(&s3c24xx_i2c_driver);//注册为平台驱动
    }
    subsys_initcall(i2c_adap_s3c_init);

    static void __exit i2c_adap_s3c_exit(void)
    {
     platform_driver_unregister(&s3c24xx_i2c_driver);
    }
    module_exit(i2c_adap_s3c_exit);

三星 s5pv210 的 i2c 总线驱动是作为平台驱动来实现的，其中传入的结构体 s3c24xx_i2c_driver 就是 platform_driver。

    static struct platform_driver s3c24xx_i2c_driver = {
     .probe      = s3c24xx_i2c_probe,
     .remove     = s3c24xx_i2c_remove,
     .id_table   = s3c24xx_driver_ids,
     .driver     = {
         .owner  = THIS_MODULE,
         .name   = "s3c-i2c",
         .pm = S3C24XX_DEV_PM_OPS,
     },
    };

<h3 id="7.3">7.3 i2c总线驱动的probe</h3>

i2c 总线驱动的 probe 函数会在一个合适的设备被发现的时候由总线驱动调用。

    /* s3c24xx_i2c_probe
    *
    * called by the bus driver when a suitable device is found
    */
    static int s3c24xx_i2c_probe(struct platform_device *pdev)
    {
    struct s3c24xx_i2c *i2c;//封装i2c适配器的信息
    struct s3c2410_platform_i2c *pdata;//i2c平台数据
    struct resource *res;//平台资源
    int ret;

    pdata = pdev->dev.platform_data;//找到平台数据
    if (!pdata) {
        dev_err(&pdev->dev, "no platform data\n");
        return -EINVAL;
    }

    i2c = kzalloc(sizeof(struct s3c24xx_i2c), GFP_KERNEL);//为i2c适配器私有数据结构体分配内存空间，并且初始化为0
    if (!i2c) {
        dev_err(&pdev->dev, "no memory for state\n");
        return -ENOMEM;
    }

    //填充i2c适配器私有数据结构体
    strlcpy(i2c->adap.name, "s3c2410-i2c", sizeof(i2c->adap.name));//名字
    i2c->adap.owner   = THIS_MODULE;//模块拥有者
    i2c->adap.algo    = &s3c24xx_i2c_algorithm;//总线通讯方法
    i2c->adap.retries = 2;//重试次数
    i2c->adap.class   = I2C_CLASS_HWMON | I2C_CLASS_SPD;
    i2c->tx_setup     = 50;

    spin_lock_init(&i2c->lock);//i2c适配器私有数据的锁进行初始化
    init_waitqueue_head(&i2c->wait);//初始化等待队列

    /* find the clock and enable it */
    //找到时钟，并且使能
    i2c->dev = &pdev->dev;
    i2c->clk = clk_get(&pdev->dev, "i2c");//找到时钟

    if (IS_ERR(i2c->clk)) {
        dev_err(&pdev->dev, "cannot get clock\n");
        ret = -ENOENT;
        goto err_noclk;
    }

    dev_dbg(&pdev->dev, "clock source %p\n", i2c->clk);

    clk_enable(i2c->clk);//使能

    /* map the registers */
    //映射寄存器

    //获取平台设备资源，对于IORESOURSE_MEM类型的资源，start,end表示platform_device占据的内存的开始地址和结束地址
    res = platform_get_resource(pdev, IORESOURCE_MEM, 0);
    if (res == NULL) {
        dev_err(&pdev->dev, "cannot find IO resource\n");
        ret = -ENOENT;
        goto err_clk;
    }

    //申请io内存资源
    i2c->ioarea = request_mem_region(res->start, resource_size(res),
                     pdev->name);

    if (i2c->ioarea == NULL) {
        dev_err(&pdev->dev, "cannot request IO\n");
        ret = -ENXIO;
        goto err_clk;
    }

    //映射io
    //I/O端口空间映射到内存的虚拟地址
    i2c->regs = ioremap(res->start, resource_size(res));

    if (i2c->regs == NULL) {
        dev_err(&pdev->dev, "cannot map IO\n");
        ret = -ENXIO;
        goto err_ioarea;
    }

    dev_dbg(&pdev->dev, "registers %p (%p, %p)\n",
        i2c->regs, i2c->ioarea, res);

    /* setup info block for the i2c core */
    //设置i2c核心所需数据

    i2c->adap.algo_data = i2c;
    i2c->adap.dev.parent = &pdev->dev;

    /* initialise the i2c controller */
    //i2c适配器私有数据结构提填充完了，就初始化i2c控制器

    ret = s3c24xx_i2c_init(i2c);
    if (ret != 0)
        goto err_iomap;

    /* find the IRQ for this unit (note, this relies on the init call to
     * ensure no current IRQs pending
     */

    //找到要申请的中断号
    i2c->irq = ret = platform_get_irq(pdev, 0);
    if (ret <= 0) {
        dev_err(&pdev->dev, "cannot find IRQ\n");
        goto err_iomap;
    }

    //申请中断，指定了中断处理函数s3c24xx_i2c_irq
    ret = request_irq(i2c->irq, s3c24xx_i2c_irq, IRQF_DISABLED,
              dev_name(&pdev->dev), i2c);

    if (ret != 0) {
        dev_err(&pdev->dev, "cannot claim IRQ %d\n", i2c->irq);
        goto err_iomap;
    }

    //动态变频，忽略
    ret = s3c24xx_i2c_register_cpufreq(i2c);
    if (ret < 0) {
        dev_err(&pdev->dev, "failed to register cpufreq notifier\n");
        goto err_irq;
    }

    /* Note, previous versions of the driver used i2c_add_adapter()
     * to add the bus at any number. We now pass the bus number via
     * the platform data, so if unset it will now default to always
     * being bus 0.
     */

    i2c->adap.nr = pdata->bus_num;

    //添加i2c适配器（cpu内部集成）
    ret = i2c_add_numbered_adapter(&i2c->adap);
    if (ret < 0) {
        dev_err(&pdev->dev, "failed to add bus to i2c core\n");
        goto err_cpufreq;
    }

    platform_set_drvdata(pdev, i2c);

    clk_disable(i2c->clk);

    dev_info(&pdev->dev, "%s: S3C I2C adapter\n", dev_name(&i2c->adap.dev));
    return 0;
    err_cpufreq:
        s3c24xx_i2c_deregister_cpufreq(i2c);
    err_irq:
        free_irq(i2c->irq, i2c);
    err_iomap:
        iounmap(i2c->regs);
    err_ioarea:
        release_resource(i2c->ioarea);
        kfree(i2c->ioarea);
    err_clk:
        clk_disable(i2c->clk);
        clk_put(i2c->clk);
    err_noclk:
        kfree(i2c);
    return ret;
    }

可以看到，i2c24xx_i2c_probe()的主要工作有：使能硬件，申请 i2c 适配器使用的 io 地址、中断号，然后向 i2c 核心添加了这个适配器。

s3c24xx_i2c 是 i2c 适配器的私有数据结构体，封装了适配器的所有信息。

    struct s3c24xx_i2c {
    spinlock_t      lock;//用于防止并发访问的锁
    wait_queue_head_t   wait;//等待队列
    unsigned int        suspended:1;

    struct i2c_msg      *msg;//i2c消息
    unsigned int        msg_num;//i2c消息的数量
    unsigned int        msg_idx;//当前消息中的一个指针
    unsigned int        msg_ptr;//消息索引

    unsigned int        tx_setup;//等待数据发送到总线上的一个建立时间
    unsigned int        irq;//中断

    enum s3c24xx_i2c_state  state;//i2c状态
    unsigned long       clkrate;

    void __iomem        *regs;
    struct clk      *clk;
    struct device       *dev;
    struct resource     *ioarea;
    struct i2c_adapter  adap;//i2c_adapter

    #ifdef CONFIG_CPU_FREQ
    struct notifier_block   freq_transition;
    #endif
    };

初始化 i2c 控制器函数 s3c24xx_i2c_init()如下

     static int s3c24xx_i2c_init(struct s3c24xx_i2c *i2c)
     {
     unsigned long iicon = S3C2410_IICCON_IRQEN | S3C2410_IICCON_ACKEN;//中断使能，ACK使能
     struct s3c2410_platform_i2c *pdata;
     unsigned int freq;

     /* get the plafrom data */

     pdata = i2c->dev->platform_data;//获取平台数据

     /* inititalise the gpio */

     if (pdata->cfg_gpio)//初始化gpio ，流程（1）
         pdata->cfg_gpio(to_platform_device(i2c->dev));

     /* write slave address */

     writeb(pdata->slave_addr, i2c->regs + S3C2410_IICADD);//写从设备地址

     dev_dbg(i2c->dev, "slave address 0x%02x\n", pdata->slave_addr);

     writel(iicon, i2c->regs + S3C2410_IICCON);//写控制寄存器，也就是使能中断和使能ACK，流程（2）

     /* we need to work out the divisors for the clock... */

     if (s3c24xx_i2c_clockrate(i2c, &freq) != 0) {//计算时钟分频
         writel(0, i2c->regs + S3C2410_IICCON);
         dev_err(i2c->dev, "cannot meet bus frequency required\n");
         return -EINVAL;
     }

     /* todo - check that the i2c lines aren't being dragged anywhere */

     dev_dbg(i2c->dev, "bus frequency set to %d KHz\n", freq);
     dev_dbg(i2c->dev, "S3C2410_IICCON=0x%02lx\n", iicon);

     dev_dbg(i2c->dev, "S3C2440_IICLC=%08x\n", pdata->sda_delay);
     writel(pdata->sda_delay, i2c->regs + S3C2440_IICLC);

     return 0;
     }

s3c24xx_i2c_init()中完成了前面所说的通过 i2c 总线写 eeprom 流程的（1）（2）两步。

---

在浅谈 LinuxI2C 驱动架构这一小节中提到了，i2c 总线驱动是对 I2C 硬件体系结构中适配器端的实现，主要是实现了两个结构 i2c_adapter 和 i2c_algorithm，从而控制 i2c 适配器产生通讯信号。

在 i2c24xx_i2c_probe()中就填充了 i2c_adapter，并且通过 i2c->adap.algo = &s3c24xx_i2c_algorithm 给 i2c_adapter 绑定了 i2c_algorithm。

其中 s3c24xx_i2c_algorithm 为

    static const struct i2c_algorithm s3c24xx_i2c_algorithm = {
        .master_xfer = s3c24xx_i2c_xfer,
        .functionality = s3c24xx_i2c_func,
    };

其中 s3c24xx_i2c_xfer()用来启动 i2c 传输，s3c24xx_i2c_func()返回所支持的通讯协议。

所以说，i2c 设备通过 i2c_transfer()进行实际传输，在 i2c 核心中我们已经看到，i2c_transfer 实际是调用了 i2c_adapter 对应的 master_xfer()，此处，在 i2c 总线驱动中，把 master_xfer()指定为了 s3c24xx_i2c_xfer()，所以说此时，传输任务交给了 s3c24xx_i2c_xfer()。

通过后面分析我们会看到，s3c24xx_i2c_xfer()只是启动了 i2c 传输，把 i2c 传输这个任务进行推进并且完成还需要靠我们在 probe 中注册的中断来完成，对应的中断处理函数是 s3c24xx_i2c_irq()，后面都会详细分析。

<h3 id="7.4">7.4 启动i2c传输</h3>
接下来就是分析负责启动i2c传输任务的s3c24xx_i2c_xfer()。

     static int s3c24xx_i2c_xfer(struct i2c_adapter *adap,
             struct i2c_msg *msgs, int num)
     {
     struct s3c24xx_i2c *i2c = (struct s3c24xx_i2c *)adap->algo_data;//获得i2c适配器私有数据结构
     int retry;
     int ret;

     clk_enable(i2c->clk);//使能时钟

     for (retry = 0; retry < adap->retries; retry++) {//传输不成功，则重试，retries为重试次数。

         ret = s3c24xx_i2c_doxfer(i2c, msgs, num);//启动一次i2c传输

         if (ret != -EAGAIN)
             goto out;

         dev_dbg(i2c->dev, "Retrying transmission (%d)\n", retry);

         udelay(100);
     }
     ret = -EREMOTEIO;
     out:
        clk_disable(i2c->clk);

     return ret;
     }

可以看到 s3c24xx_i2c_xfer()是调用了 s3c24xx_i2c_doxfer()来启动传输的。

    static int s3c24xx_i2c_doxfer(struct s3c24xx_i2c *i2c,
                   struct i2c_msg *msgs, int num)
    {
     unsigned long timeout;
     int ret;

     if (i2c->suspended)
         return -EIO;

     ret = s3c24xx_i2c_set_master(i2c);//检查i2c总线状态，总线不忙返回0
     if (ret != 0) {
         dev_err(i2c->dev, "cannot get bus (error %d)\n", ret);
         ret = -EAGAIN;
         goto out;
     }

     spin_lock_irq(&i2c->lock);

     //把消息写入i2c适配器的私有数据结构体中
     i2c->msg     = msgs;//i2c消息
     i2c->msg_num = num;//消息数量
     i2c->msg_ptr = 0;//消息指针，指向当前消息未发送部分的开始
     i2c->msg_idx = 0;//消息索引
     i2c->state   = STATE_START;//将状态改为STATE_START

     s3c24xx_i2c_enable_irq(i2c);//使能中断
     s3c24xx_i2c_message_start(i2c, msgs);//发送第一个byte，获得ACK后触发中断。
     spin_unlock_irq(&i2c->lock);

     timeout = wait_event_timeout(i2c->wait, i2c->msg_num == 0, HZ * 5);//等待消息传输完成，否则超时

s3c24xx_i2c_doxfer()首先调用 s3c24xx_i2c_set_master()来检查总线状态，s3c24xx_i2c_set_master()的实现如下

     static int s3c24xx_i2c_set_master(struct s3c24xx_i2c *i2c)
     {
     unsigned long iicstat;
     int timeout = 400;

     while (timeout-- > 0) {
         iicstat = readl(i2c->regs + S3C2410_IICSTAT);//读i2c状态寄存器

         if (!(iicstat & S3C2410_IICSTAT_BUSBUSY))//总线不忙，则返回0；否则直到超时
             return 0;

         msleep(1);
     }

     writel(iicstat & ~S3C2410_IICSTAT_TXRXEN, i2c->regs + S3C2410_IICSTAT);
     if (!(readl(i2c->regs + S3C2410_IICSTAT) & S3C2410_IICSTAT_BUSBUSY))
         return 0;

     return -ETIMEDOUT;
     }

在获知总线不忙后，把要消息写入 i2c 适配器私有数据结构，并且把状态改为 STATE_START。
然后使能中断，通过 s3c24xx_i2c_message_start()发送第一个 byte，这样在获取 ACK 后就会触发中断来推进 i2c 的传输。

    static void s3c24xx_i2c_message_start(struct s3c24xx_i2c *i2c,
                       struct i2c_msg *msg)
    {
     unsigned int addr = (msg->addr & 0x7f) << 1;//从设备地址，7位地址，最低位用来表示读或者写，1为读，0为写。
     unsigned long stat;
     unsigned long iiccon;

     stat = 0;
     stat |=  S3C2410_IICSTAT_TXRXEN;//使能RxTx

     if (msg->flags & I2C_M_RD) {//从i2c消息判断，如果是读
         stat |= S3C2410_IICSTAT_MASTER_RX;//把状态设为主模式读
         addr |= 1;//别且设置第一byte最低位为1，表示读
     } else//否则是写
         stat |= S3C2410_IICSTAT_MASTER_TX;//把状态设为主模式写

     if (msg->flags & I2C_M_REV_DIR_ADDR)//如果是读写反转
         addr ^= 1;//读写交换

     /* todo - check for wether ack wanted or not */
     s3c24xx_i2c_enable_ack(i2c);//使能ACK

     iiccon = readl(i2c->regs + S3C2410_IICCON);
     writel(stat, i2c->regs + S3C2410_IICSTAT);//根据前面的设置来配置控制寄存器，流程（3）

     dev_dbg(i2c->dev, "START: %08lx to IICSTAT, %02x to DS\n", stat, addr);
     writeb(addr, i2c->regs + S3C2410_IICDS);//把第一个byte写入i2c收发数据移位寄存器，流程（3）
     /* delay here to ensure the data byte has gotten onto the bus
      * before the transaction is started */

     ndelay(i2c->tx_setup);

     dev_dbg(i2c->dev, "iiccon, %08lx\n", iiccon);
     writel(iiccon, i2c->regs + S3C2410_IICCON);

     stat |= S3C2410_IICSTAT_START;
     writel(stat, i2c->regs + S3C2410_IICSTAT);//修改状态，流程（3）
    }

s3c24xx_i2c_message_start()在 i2c 总线上发送了一个开始信号，即完成了通过 i2c 总线写 eeprom 中的流程（3）的工作，设备地址赋给 IICDS ，并设置 IICSTAT，启动 IIC 发送设备地址出去，当从设备收到此数据并且回复 ACK 后，i2c 适配器收到 ACK 后就会触发中断来推进 i2c 的传输。

<h3 id="7.5">7.5 通过中断来推进i2c的传输</h3>

发送完第一个 byte，收到 ACK 信号后就会进入中断，并且以后只要收到 ACK 信号就都会进入中断。中断在 probe 中已经注册，它的实现
如下

    static irqreturn_t s3c24xx_i2c_irq(int irqno, void *dev_id)
    {
     struct s3c24xx_i2c *i2c = dev_id;
     unsigned long status;
     unsigned long tmp;

     status = readl(i2c->regs + S3C2410_IICSTAT);//获得i2c状态寄存器的值

     if (status & S3C2410_IICSTAT_ARBITR) {//需要仲裁
         /* deal with arbitration loss */
         dev_err(i2c->dev, "deal with arbitration loss\n");
     }

     if (i2c->state == STATE_IDLE) {//空闲状态
         dev_dbg(i2c->dev, "IRQ: error i2c->state == IDLE\n");

         tmp = readl(i2c->regs + S3C2410_IICCON);
         tmp &= ~S3C2410_IICCON_IRQPEND;
         writel(tmp, i2c->regs +  S3C2410_IICCON);
         goto out;
     }

     /* pretty much this leaves us with the fact that we've
      * transmitted or received whatever byte we last sent */

     i2c_s3c_irq_nextbyte(i2c, status);//推进传输，传输下一个byte
     out:
     return IRQ_HANDLED;
    }

i2c 总线驱动的中断处理函数 s3c24xx_i2c_irq()是调用 i2c_s3c_irq_nextbyte()来推进 i2c 的传输的。

    static int i2c_s3c_irq_nextbyte(struct s3c24xx_i2c *i2c, unsigned long iicstat
    {
    unsigned long tmp;
    unsigned char byte;
    int ret = 0;

    switch (i2c->state) {//根据i2c的状态选择

    case STATE_IDLE://空闲
        dev_err(i2c->dev, "%s: called in STATE_IDLE\n", __func__);
        goto out;
        break;

    case STATE_STOP://停止
        dev_err(i2c->dev, "%s: called in STATE_STOP\n", __func__);
        s3c24xx_i2c_disable_irq(i2c);//禁止中断
        goto out_ack;

    case STATE_START://开始
        /* last thing we did was send a start condition on the
         * bus, or started a new i2c message
         */
        //切换为开始状态之前，刚发送了第一个byte，也就是设备地址

        //首先检查下state时候与硬件寄存器的状态一致
        if (iicstat & S3C2410_IICSTAT_LASTBIT &&
            !(i2c->msg->flags & I2C_M_IGNORE_NAK)) {
            /* ack was not received... */

            dev_dbg(i2c->dev, "ack was not received\n");
            s3c24xx_i2c_stop(i2c, -ENXIO);//停止i2c传输
            goto out_ack;
        }

        if (i2c->msg->flags & I2C_M_RD)//如果当前i2c消息的标志为i2c读
            i2c->state = STATE_READ;//则修改状态为i2c读
        else
            i2c->state = STATE_WRITE;//否则修改为i2c写

        /* terminate the transfer if there is nothing to do
         * as this is used by the i2c probe to find devices. */

        if (is_lastmsg(i2c) && i2c->msg->len == 0) {//如果是最后一条消息则停止i2c传输。
            s3c24xx_i2c_stop(i2c, 0);
            goto out_ack;
        }

        if (i2c->state == STATE_READ)//如果i2c状态为读，就跳到读，否则，，就会跳到写，，，因为没有break
            goto prepare_read;

        /* fall through to the write state, as we will need to
         * send a byte as well */

    case STATE_WRITE://第一次开始写i2c
        /* we are writing data to the device... check for the
         * end of the message, and if so, work out what to do
         */

        if (!(i2c->msg->flags & I2C_M_IGNORE_NAK)) {
            if (iicstat & S3C2410_IICSTAT_LASTBIT) {
                dev_dbg(i2c->dev, "WRITE: No Ack\n");

                s3c24xx_i2c_stop(i2c, -ECONNREFUSED);
                goto out_ack;
            }
        }
    retry_write://继续写

        if (!is_msgend(i2c)) {//不是一条消息的最后1Byte
            byte = i2c->msg->buf[i2c->msg_ptr++];//取出此消息的下一个byte
            writeb(byte, i2c->regs + S3C2410_IICDS);//写入收发数据移位寄存器。

            /* delay after writing the byte to allow the
             * data setup time on the bus, as writing the
             * data to the register causes the first bit
             * to appear on SDA, and SCL will change as
             * soon as the interrupt is acknowledged */

            ndelay(i2c->tx_setup);//延迟，等待数据发送

        } else if (!is_lastmsg(i2c)) {//是一条消息的最后一个byte，不是最后一条消息
            /* we need to go to the next i2c message */

            dev_dbg(i2c->dev, "WRITE: Next Message\n");

            i2c->msg_ptr = 0;//当前消息未发数据开始指针复位
            i2c->msg_idx++;//消息索引++
            i2c->msg++;//下一条消息

            /* check to see if we need to do another message */
            if (i2c->msg->flags & I2C_M_NOSTART) {//在发送下个消息之前，检查是否需要一个新的开始信号，如果不需要

                if (i2c->msg->flags & I2C_M_RD) {//如果是读
                    /* cannot do this, the controller
                     * forces us to send a new START
                     * when we change direction */

                    s3c24xx_i2c_stop(i2c, -EINVAL);//错误，返回
                }

                goto retry_write;//继续写
            } else {//如果需要一个新的开始信号
                /* send the new start */
                s3c24xx_i2c_message_start(i2c, i2c->msg);//发送一个新的开始信号
                i2c->state = STATE_START;//并且修改状态
            }

        } else {//是一条消息的最后
            /* send stop */

            s3c24xx_i2c_stop(i2c, 0);//停止发送
        }
        break;

    case STATE_READ://开始读
        /* we have a byte of data in the data register, do
         * something with it, and then work out wether we are
         * going to do any more read/write
         */

        byte = readb(i2c->regs + S3C2410_IICDS);//先获取读到的消息，后面再决定时候有用
        i2c->msg->buf[i2c->msg_ptr++] = byte;//把消息存入读缓冲

    prepare_read://如果第一个byte是读，则跳到此处。
        if (is_msglast(i2c)) {//是当前消息的最后一byte，也就是当前消息只剩1Byte的空余
            /* last byte of buffer */

            if (is_lastmsg(i2c))//如果也是最后一条消息
                s3c24xx_i2c_disable_ack(i2c);//那么就禁止ACK

        } else if (is_msgend(i2c)) {//否则如果是当前消息已经用完读缓冲
            /* ok, we've read the entire buffer, see if there
             * is anything else we need to do */

            if (is_lastmsg(i2c)) {//如果是最后一条消息了
                /* last message, send stop and complete */
                dev_dbg(i2c->dev, "READ: Send Stop\n");

                s3c24xx_i2c_stop(i2c, 0);//停止i2c传输
            } else {//否则进入下一条i2c传输
                /* go to the next transfer */
                dev_dbg(i2c->dev, "READ: Next Transfer\n");

                i2c->msg_ptr = 0;
                i2c->msg_idx++;
                i2c->msg++;//下一条i2c消息
            }
        }

        break;
    }

    /* acknowlegde the IRQ and get back on with the work */
    out_ack:
        tmp = readl(i2c->regs + S3C2410_IICCON);
        tmp &= ~S3C2410_IICCON_IRQPEND;//清中断标志位
        writel(tmp, i2c->regs + S3C2410_IICCON);
    out:
    return ret;
    }

i2c_s3c_irq_nextbyte()推进了 i2c 的传输，以写 eeprom 为例，第一个 Byte 的设备地址发送后，从 EEPROM 得到 ACK 信号，此信号触发中断，在中断处理函数中把第二个 Byte（设备内地址）发送出去；发送之后，接收到 ACK 又触发中断，中断处理函数把第三个 Byte（真正的数据）发送到设备中，发送之后同样接收到 ACK 并触发中断，中断处理函数判断，发现数据传送完毕，就发送 IIC Stop 信号，关 IIC 中断，置位各寄存器。这样就把通过 i2c 总线写 eeprom 的整个流程都实现了。

<h3 id="7.6">7.6 总结</h3>

i2c 总线驱动控制 i2c 适配器产生通信信号，通过 master_xfer()启动一个 i2c 传输，然后通过中断推进 i2c 传输。
