### JUnit

[此处默认JUnit4的情况下]

JUnit依赖的包： **junit.jar** 、 **hamcrest-core.jar**

#### JUnit测试类

命名规则【一般都是约定，并不是强制的】:

- 类名：包名一致，类名在要测试的类名后加上Test
- 方法名：在要测试的方法名前加上test

测试方法结构：

- 所有测试方法返回类型必须为void且无参数
- 测试方法能被JUnit运行需要添加上@Test注解

> Idea自动生成测试类的快捷方式：Navigate->Test

JUnit4的注解：

- @Test：把一个方法标记为测试方法
- @Before：每一个测试方法执行前自动调用一次
- @After：每一个测试方法执行完自动调用一次
- @BeforeClass：所有测试方法执行前执行一次，在测试类还没有实例化就已经被加载，所以用static修饰
- @AfterClass：所有测试方法执行完执行一次，在测试类还没有实例化就已经被加载，所以用static修饰
- @Ignore：暂不执行该测试方法