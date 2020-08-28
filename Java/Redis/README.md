# Redis

Redis是一个开源的内存数据结构服务器，通常可以当作数据库、缓存、消息队列代理。

- **Redis 的特性**

1. 速度快：
   - 根据官方给出的数据是每秒 10W QPS，如此高的速度主要是因为数据都是存储在内存中的，所以速度非常快。其次就是 Redis 是使用 C 语言编写的，C 相对于一些其他高级语言例如 Java、Python等效率肯定高。再就是 Redis 采用 了单线程模型，避免了多线程上下文切换所消耗的时间。
2. 提供持久化功能：
   - Redis支持基于二进制的 RDB 文件的数据持久化，支持基于日志的 AOF 的存储。从而保证数据不被丢失
3. 支持多种数据结构
   - Redis 支持多种数据类型，String、Hash、List、Set、SortSort，还有衍生的数据类型，如 BitMap、HyperLog。。。
4. 支持多种编程语言
   - Redis 提供了多种语言的客户端，常见的有 Java、Python等
5. 功能丰富
   - Redis 的功能非常丰富，支持 Lua 脚本，支持事务，支持发布订阅，Pipeline 等功能
6. 简单
   - 简单主要体现在 Redis 不依赖第三方库，如其他缓存以来 libevent。Redis 采用单线程模型，避免了多线程问题
7. 支持主从复制、高可用和分布式
   - Redis 从 2.8 开始使用哨兵机制支持高可用，从 3.0 版本开始支持集群和分布式

- **Redis 使用场景**

Redis 最常使用的场景就是缓存，但是 Redis 不仅仅可用作缓存，还可以使用 Redis 单线程特性用作计数器；使用 Redis发布订阅的特性用作消息队列系统；使用 Redis 有序集合的数据结构完成排行榜功能，使用 Redis 分布式锁功能等等。

Redis 可以存储键与5种不同数据结构类型之间的映射，这5种数据结构类型分别为：String（字符串）、List（列表）、Set（集合）、Hash（散列、哈希表）和 Zset（有序集合）。    

| 结构类型 | 结构存储的值                                                                              | 结构的读写能力                                                                                            |
|:--------:|:------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------|
|  String  | 字符串、整数或者浮点数                                                                     | 对整个字符串或者字符串的其中一部分执行操作；对象和浮点数执行自增(increment)或者自减(decrement)             |
|   List   | 一个链表，链表上的每个节点都包含了一个字符串                                               | 从链表的两端推入或者弹出元素；根据偏移量对链表进行修剪(trim)；读取单个或者多个元素；根据值来查找或者移除元素 |
|   Set    | 包含字符串的无序收集器(unorderedcollection)，并且被包含的每个字符串都是独一无二的、各不相同 | 添加、获取、移除单个元素；检查一个元素是否存在于某个集合中；计算交集、并集、差集；从集合里卖弄随机获取元素       |
|   Hash   | 包含键值对的无序散列表                                                                    | 添加、获取、移除单个键值对；获取所有键值对                                                                   |
|   ZSet   | 字符串成员(member)与浮点数分值(score)之间的有序映射，元素的排列顺序由分值的大小决定        | 添加、获取、删除单个元素；根据分值范围(range)或者成员来获取元素                                              |

## 命令

[Redis命令](Java/Redis/Redis命令.md)

## RedisTemplate和StringRedisTemplate

二者主要区别是它们使用的序列化类不一样，RedisTemplate使用的是`JdkSerializationRedisSerializer`，StringRedisTemplate使用的是`StringRedisSerializer`，两者的数据是不共通的。

- RedisTemplate：
    RedisTemplate使用的是JDK的序列化策略，向Redis存入数据会将数据先序列化成字节数组然后在存入Redis数据库，这个时候打开Redis查看的时候，你会看到你的数据不是以可读的形式展现的，而是以字节数组显示，类似下面：`\xAC\xED\x00\x05t\x05sr\x00`。所以使用RedisTemplate可以直接把一个java对象直接存储在redis里面，但是存进去的数据是不易直观的读的，不通用的，建议最好不要直接存一个Object对象，可以变成Hash来存储，也可以转成json格式的数据来存储，在实际应用中也是很多都采用json格式来存储的。
- StringRedisTemplate:
    StringRedisTemplate默认采用的是String的序列化策略，保存的key和value都是采用此策略序列化保存的。StringRedisTemplate是继承RedisTemplate的，这种对redis的操方式更优雅，因为RedisTemplate以字节数组的形式存储不利于管理，也不通用。  

## Redis与Spring的集成

集成配置:

```xml
<bean id="poolConfig" class="redis.clients.jedis.JedisPoolConfig">
	<property name="maxIdle" value="300" />
	<property name="maxTotal" value="600" />
	<property name="maxWaitMillis" value="1000" />
	<property name="testOnBorrow" value="true" />
</bean>

<bean id="jedisConnectionFactory"
	class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
	<property name="hostName" value="127.0.0.1" />
	<property name="password" value="WangFan01!" />
	<property name="port" value="6379" />
	<property name="poolConfig" ref="poolConfig" />
</bean>

<bean id="redisTemplate" class="org.springframework.data.redis.core.StringRedisTemplate">
	<property name="connectionFactory" ref="jedisConnectionFactory" />
</bean>

<!-- 这里可以配置多个redis -->
<bean id="redisUtil" class="com.wf.ew.core.utils.RedisUtil">
	<property name="redisTemplate" ref="redisTemplate" />
</bean>
```