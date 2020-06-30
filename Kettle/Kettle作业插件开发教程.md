一个 Kettle 的 作业（Job）插件主要包括两个类，和转换（Trans）步骤插件一样，一个是**用于客户端定义参数配置的 UI Dialog 类**，一个是 **Job Entry 类，主要是跟踪保存客户的配置信息和执行 job 具体的功能**（在 `execute()` 方法中执行）。

#### UI Dialog 类

UI Dialog 类和转换步骤中的 Dialog 一样，在 open 方法中进行配置界面的 UI 绘制，然后通过构造参数中的 JobEntryInterface 类加载历史配置信息到各种 UI 控件上。和转换步骤一样，注意配置信息变更状态的更新。Job Dialog 和 Step Dialog 的不同之处包括以下几点：

- JobDialog 的 open 方法返回的是一个更改后的 job entry，而 StepDialog 返回的是 Step 的名字
- JobDialog 必须保证用户输入一个正确的 job entry 名字，不能接受一个空的名字，如果名字为空，不能让用户保存配置信息

#### 国际化

Job 插件的国际化于转换步骤的国际化相同

#### Job Entry 类

这个类有以下三个主要的职责：

- 跟踪处理用户的配置信息（包括序列化到 XML 或资源库）

  job entry 类通过一些私有变量来跟踪用户的配置信息，提供一些 get/set 方法使其能够方便的访问。同时也实现一些方法来将配置信息保存（读取）到 XML 或资源库，也提供一个 clone 方法来处理用户复制 job 的一些初始化工作

- 向 Kettle 引擎报告各种处理状态（下一步的跳转方式）

  Kettle 客户端 Spoon 为 job entry 预设了三种不同的处理状态：成功success（绿色），失败failure（红色），无条件的 unconditional（黑色）。

  每一个 job entry 都要提供关于它所支持的处理状态信息。Kettle 会调用 evaluates() 方法来确定 job entry 是否支持成功和失败，调用 isUnconditional() 来确定是否支持无条件跳转。除非不想支持这个 job 之后执行别的后续任务，否则必须有一个能返回 true。如果想给客户更多的选择，那么这两个方法都返回 true 即可。

  ```java
  public boolean evaluates() {
      return true;
  }
  
  public boolean isUnconditional() {
      return true;
  }
  ```

- 执行这个任务需要执行的相关操作

  当一个控制流程到达 job entry 时，Kettle 将会执行它的 execute() 方法，这里才执行真正的一个任务的具体操作。这个方法有两个参数，第一个是一个结果对象（result object），第二个是当前任务是第几个任务的数值（这个参数很少会被用到）。结果对象里有上一个任务的 job entry。很少情况下当前任务会关心上一个任务的执行状态，所以基本上都是在这个结果对象里标识当前任务是否执行成功，如果你想跳转到无条件跳转路径上，那么你直接返回未更改的结果对象就可以

