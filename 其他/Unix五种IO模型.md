#### IO涉及到的概念

1. 用户空间与内核空间

> 操作系统的核心是内核，独立于普通的应用程序，可以访问受保护的内存空间，也有访问底层硬件设备的所有权限。为了保证用户进程不能直接操作内核（kernel），保证内核的安全，操心系统将虚拟空间划分为两部分，一部分为内核空间，一部分为用户空间。
>
> **使用内核空间是为了解决IO设备与CPU速度不匹配的问题**

2. 进程切换

> 为了控制进程的执行，内核必须有能力挂起正在CPU上运行的进程，并恢复以前挂起的某个进程的执行。这种行为被称为进程切换(**很耗资源**)。因此可以说，任何进程都是在操作系统内核的支持下运行的，是与内核紧密相关的。
>
> 从一个进程的运行转到另一个进程上运行，这个过程中经过下面这些变化：
>
> 保存处理机上下文，包括程序计数器和其他寄存器
>
> 更新PCB信息
>
> 把进程的PCB移入相应的队列，如就绪、在某事件阻塞等队列
>
> 选择另一个进程执行，并更新其PCB
>
> 更新内存管理的数据结构
>
> 恢复处理机上下文

3. 进程的阻塞

> 正在执行的进程，由于期待的某些事件未发生，如请求系统资源失败、等待某种操作的完成、新数据尚未到达或无新工作做等，则由系统自动执行阻塞原语(Block)，使自己由运行状态变为阻塞状态。可见，进程的阻塞是进程自身的一种主动行为，也因此只有处于运行态的进程（获得CPU），才可能将其转为阻塞状态。当进程进入阻塞状态，是不占用CPU资源的。

4. 文件描述符FD

> 文件描述符（File descriptor）是计算机科学中的一个术语，是一个用于表述指向文件的引用的抽象化概念。文件描述符在形式上是一个非负整数。实际上，它是一个索引值，指向内核为每一个进程所维护的该进程打开文件的记录表。当程序打开一个现有文件或者创建一个新文件时，内核向进程返回一个文件描述符。

5. 缓存 I/O

> 缓存 I/O 又被称作标准 I/O，大多数文件系统的默认 I/O 操作都是缓存 I/O。在 Linux 的缓存 I/O 机制中，操作系统会将 I/O 的数据缓存在文件系统的页缓存（ page cache ）中，也就是说，数据会先被拷贝到操作系统内核的缓冲区中，然后才会从操作系统内核的缓冲区拷贝到应用程序的地址空间。

------

#### IO模式

1. 阻塞 I/O（Blocking IO）



![img](https:////upload-images.jianshu.io/upload_images/9529993-3aa8c7b650951ec2?imageMogr2/auto-orient/strip|imageView2/2/w/552/format/webp)

> 当用户进程调用了recvfrom这个系统调用，kernel就开始了IO的第一个阶段：准备数据（对于网络IO来说，很多时候数据在一开始还没有到达。比如，还没有收到一个完整的UDP包。这个时候kernel就要等待足够的数据到来）。这个过程需要等待，也就是说数据被拷贝到操作系统内核的缓冲区中是需要一个过程的。而在用户进程这边，整个进程会被阻塞（当然，是进程自己选择的阻塞）。当kernel一直等到数据准备好了，它就会将数据从kernel中拷贝到用户内存，然后kernel返回结果，用户进程才解除block的状态，重新运行起来。
>
> **Blocking IO的特点就是在IO执行的两个阶段都被block了**

2. 非阻塞 I/O（NonBlocking IO）



![img](https:////upload-images.jianshu.io/upload_images/9529993-af292701a56d2565?imageMogr2/auto-orient/strip|imageView2/2/w/603/format/webp)

> 当用户进程发出read操作时，如果kernel中的数据还没有准备好，那么它并不会block用户进程，而是立刻返回一个error。从用户进程角度讲 ，它发起一个read操作后，并不需要等待，而是马上就得到了一个结果。用户进程判断结果是一个error时，它就知道数据还没有准备好，于是它可以再次发送read操作。一旦kernel中的数据准备好了，并且又再次收到了用户进程的system call，那么它马上就将数据拷贝到了用户内存，然后返回。
>
> **NonBlocking IO的特点是用户进程需要不断的主动询问kernel数据好了没有**

3. I/O 多路复用



![img](https:////upload-images.jianshu.io/upload_images/9529993-5bd232347b7fa5e8?imageMogr2/auto-orient/strip|imageView2/2/w/609/format/webp)

> 当用户进程调用了select，那么整个进程会被block，而同时，kernel会“监视”所有select负责的socket，当任何一个socket中的数据准备好了，select就会返回。这个时候用户进程再调用read操作，将数据从kernel拷贝到用户进程。I/O 多路复用的特点是通过一种机制一个进程能同时等待多个文件描述符，而这些文件描述符（套接字描述符）其中的任意一个进入读就绪状态，select()函数就可以返回。
>
> **select/epoll的优势并不是对于单个连接能处理得更快，而是在于能处理更多的连接**

4. 异步 I/O（Asynchronous IO）



![img](https:////upload-images.jianshu.io/upload_images/9529993-5159f11ce6e868b2?imageMogr2/auto-orient/strip|imageView2/2/w/572/format/webp)

> 用户进程发起read操作之后，立刻就可以开始去做其它的事。而另一方面，从kernel的角度，当它受到一个asynchronous read之后，首先它会立刻返回，所以不会对用户进程产生任何block。然后，kernel会等待数据准备完成，然后将数据拷贝到用户内存，当这一切都完成之后，kernel会给用户进程发送一个signal，告诉它read操作完成了。

------

#### select、poll、epoll详解

> select，poll，epoll都是IO多路复用的机制。I/O多路复用就是通过一种机制，一个进程可以监视多个描述符，一旦某个描述符就绪（一般是读就绪或者写就绪），能够通知程序进行相应的读写操作。但select，poll，epoll本质上都是同步I/O，因为他们都需要在读写事件就绪后自己负责进行读写，也就是说这个读写过程是阻塞的，而异步I/O则无需自己负责进行读写，异步I/O的实现会负责把数据从内核拷贝到用户空间。

1. select

> int select (int n, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);
>
> select 函数监视的文件描述符分3类，分别是writefds、readfds、和exceptfds。调用后select函数会阻塞，直到有描述副就绪（有数据 可读、可写、或者有except），或者超时（timeout指定等待时间，如果立即返回设为null即可），函数返回。当select函数返回后，可以通过遍历fdset，来找到就绪的描述符。select目前几乎在所有的平台上支持，其良好跨平台支持也是它的一个优点。select的一个缺点在于**单个进程能够监视的文件描述符的数量存在最大限制**，在Linux上一般为1024，可以通过修改宏定义甚至重新编译内核的方式提升这一限制，但是这样也会造成效率的降低。

2. poll

> int poll (struct pollfd *fds, unsigned int nfds, int timeout);
>
> select和poll都需要在返回后，通过遍历文件描述符来获取已经就绪的socket。事实上，同时连接的大量客户端在一时刻可能只有很少的处于就绪状态，因此随着监视的描述符数量的增长，其效率也会线性下降。

3. epoll

> int epoll_create(int size);
>
> int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event)；
>
> int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout);
>
> 在 select/poll中，进程只有在调用一定的方法后，内核才对所有监视的文件描述符进行扫描，而epoll事先通过epoll_ctl()来注册一个文件描述符，一旦基于某个文件描述符就绪时，内核会采用类似callback的回调机制，迅速激活这个文件描述符，当进程调用epoll_wait()时便得到通知。