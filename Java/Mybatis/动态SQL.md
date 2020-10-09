# 动态 SQL

Mybatis 动态 sql 可以在 Xml 映射文件内，以标签的形式编写动态 sql，完成逻辑判断和动态拼接 sql 的功能。

Mybatis 提供了 9 种动态 sql 标签：`trim|where|set|foreach|if|choose|when|otherwise|bind`

## if

```xml
<select id ='selectPats' resultType='com.xxx.domain.PatientInfo'>
  select * from patient_info 
  where status=1
  <if test="iptNum!=null">
      and ipt_num=#{iptNum}
  </if>
  <if test="bedNum!=null">
      and bed_num=#{bedNum}
  </if>
</select>
```

`<if>` 标签中的属性 `test` 用来指定判断条件，如果有两个或者多个条件，则和SQL的语法类似， `and` 连接即可，如下：

```xml
<if test="bedNum!=null and bedNum!='' ">
    and bed_num=#{bedNum}
</if>
```

## choose、when、otherwise

```xml
<select id="selectPats"
     resultType="com.xxx.domain.PatientInfo">
  select * from patient_info where 1=1
  <choose>
    <when test="iptNum != null">
      AND ipt_num=#{iptNum}
    </when>
    <when test="bedNum != null">
      AND bed_num = #{bedNum}
    </when>
    <otherwise>
      AND status=1
    </otherwise>
  </choose>
</select>
```

MyBatis 提供了 `choose` 元素，按顺序判断 `when` 中的条件出否成立，如果有一个成立，则 `choose` 结束。当 `choose` 中所有 `when` 的条件都不满则时，则执行 `otherwise` 中的 sql。类似于 Java 的 `switch` 语句， `choose` 为 `switch` ， `when` 为 `case` ， `otherwise` 则为 `default` 。

## where

举个栗子：对于 `choose` 标签的例子中的查询，如果去掉 `where` 后的 `1=1` 此时的SQL语句会变成什么样子，有三种可能的SQL，如下：

```sql
select * from patient_info where AND ipt_num=#{iptNum};

select * from patient_info where AND bed_num = #{bedNum};

select * from patient_info where AND status=1;
```

发生了什么，以上三条SQL语句对吗？很显然是不对的，显然 `where` 后面多了个 `AND` 。如何解决呢？此时就要用到 `where` 这个标签了。

`where` 元素只会在子元素返回任何内容的情况下才插入 `WHERE` 子句。而且，若子句的开头为 `AND` 或 `OR` ， `where` 元素也会将它们去除。

此时的查询改造如下：

```xml
<select id="selectPats"
     resultType="com.xxx.domain.PatientInfo">
  select * from patient_info
    <where>
        <choose>
          <!--住院号不为null时，根据住院号查找-->
          <when test="iptNum != null">
            AND ipt_num=#{iptNum}
          </when>
          <!--床位号不是NUll-->
          <when test="bedNum != null">
            AND bed_num = #{bedNum}
          </when>
          <otherwise>
            AND status=1
          </otherwise>
        </choose>
   </where>
</select>
```

## foreach

`foreach` 是用来对集合的遍历，这个和Java中的功能很类似。通常处理SQL中的 `in` 语句。

`foreach` 元素的功能非常强大，它允许你指定一个集合，声明可以在元素体内使用的集合项（ `item` ）和索引（ `index` ）变量。它也允许你指定开头与结尾的字符串以及集合项迭代之间的分隔符。这个元素也不会错误地添加多余的分隔符

你可以将任何可迭代对象（如 `List` 、 `Set` 等）、 `Map` 对象或者数组对象作为集合参数传递给 foreach。当使用可迭代对象或者数组时， `index` 是当前迭代的序号， `item` 的值是本次迭代获取到的元素。当使用 `Map` 对象（或者 `Map.Entry` 对象的集合）时， `index` 是键， `item` 是值。

例子如下：

```xml
<select id="selectPats" resultType="com.xxx.domain.PatientInfo">
  SELECT *
  FROM patient_info 
  WHERE ID in
  <foreach item="item" index="index" collection="list"
      open="(" separator="," close=")">
        #{item}
  </foreach>
</select>
```

改标签中的各个属性的含义如下：

| 属性      | 含义                                     |
| :-------- | :--------------------------------------- |
| item      | 表示在迭代过程中每一个元素的别名         |
| index     | 表示在迭代过程中每次迭代到的位置（下标） |
| open      | 前缀                                     |
| close     | 后缀                                     |
| separator | 分隔符，表示迭代时每个元素之间以什么分隔 |

## set

```xml
<update id="updateStudent" parameterType="Object">
    UPDATE STUDENT
    SET NAME = #{name},
    MAJOR = #{major},
    HOBBY = #{hobby}
    WHERE ID = #{id};
</update>

<update id="updateStudent" parameterType="Object">
    UPDATE STUDENT SET
    <if test="name!=null and name!='' ">
        NAME = #{name},
    </if>
    <if test="hobby!=null and hobby!='' ">
        MAJOR = #{major},
    </if>
    <if test="hobby!=null and hobby!='' ">
        HOBBY = #{hobby}
    </if>
    WHERE ID = #{id};
</update>
```

上面的例子中没有使用 `if` 标签时，如果有一个参数为 `null` ，都会导致错误。当在 `update` 语句中使用 `if` 标签时，如果最后的 `if` 没有执行，则或导致逗号多余错误。使用 `set` 标签可以将动态的配置 `set` 关键字，和剔除追加到条件末尾的任何不相关的逗号。

使用 set+if 标签修改后，如果某项为 null 则不进行更新，而是保持数据库原值。此时的查询如下：

```xml
<update id="updateStudent" parameterType="Object">
    UPDATE STUDENT
    <set>
        <if test="name!=null and name!='' ">
            NAME = #{name},
        </if>
        <if test="hobby!=null and hobby!='' ">
            MAJOR = #{major},
        </if>
        <if test="hobby!=null and hobby!='' ">
            HOBBY = #{hobby}
        </if>
    </set>
    WHERE ID = #{id};
</update>
```

## sql

```xml
<sql>
<select>
<!-- 查询字段 -->
<sql id="Base_Column_List">
    ID,MAJOR,BIRTHDAY,AGE,NAME,HOBBY
</sql>

<!-- 查询条件 -->
<sql id="Example_Where_Clause">
    where 1=1
    <trim suffixOverrides=",">
        <if test="id != null and id !=''">
            and id = #{id}
        </if>
        <if test="major != null and major != ''">
            and MAJOR = #{major}
        </if>
        <if test="birthday != null ">
            and BIRTHDAY = #{birthday}
        </if>
        <if test="age != null ">
            and AGE = #{age}
        </if>
        <if test="name != null and name != ''">
            and NAME = #{name}
        </if>
        <if test="hobby != null and hobby != ''">
            and HOBBY = #{hobby}
        </if>
    </trim>
</sql>
```

## include

这个标签和 `<sql>` 是天仙配，是共生的， `include` 用于引用 `sql` 标签定义的常量。比如引用上面sql标签定义的常量，如下：

```xml
<select id="selectAll" resultMap="BaseResultMap">
    SELECT
    <include refid="Base_Column_List" />
    FROM student
    <include refid="Example_Where_Clause" />
</select>
```

`refid` 这个属性就是指定 `<sql>` 标签中的 `id` 值（唯一标识）。

## 拓展一下

### Mybatis中如何避免魔数

开过阿里巴巴开发手册的大概都知道代码中是不允许出现 `魔数` 的，何为 `魔数` ？简单的说就是一个数字，一个只有你知道，别人不知道这个代表什么意思的数字。通常我们在Java代码中都会定义一个常量类专门定义这些数字。

比如获取医生和护士的权限，但是医生和护士的权限都不相同，在这条SQL中肯定需要根据登录的类型 `type` 来区分，比如 `type=1` 是医生， `type=2` 是护士，估计一般人会这样写：

```xml
<if test="type!=null and type==1">
    -- ....获取医生的权限
</if>

<if test="type!=null and type==2">
    -- ....获取护士的权限
</if>
```

```java
package com.xxx.core.Constants;

public class CommonConstants{
  //医生
  public final static int DOC_TYPE=1;
  
  //护士
  public final static int NUR_TYPE=2;
  
}
```

那么此时的SQL应该如何写呢？如下：

```xml
<if test="type!=null and type==@com.xxx.core.Constants.CommonConstants@DOC_TYPE">
    -- ....获取医生的权限
</if>

<if test="type!=null and type==@com.xxx.core.Constants.CommonConstants@NUR_TYPE">
    -- ....获取护士的权限
</if>
```

就是这么简单，就是 `@` + `全类名` + `@` + `常量` 。

除了调用常量类中的常量，还可以类中的方法，很少用到，不再介绍了，感兴趣的可以问下度娘。

### 如何引用其他XML中的SQL片段

实际开发中你可能遇到一个问题，比如这个 `resultMap` 或者这个 `<sql>` 片段已经在另外一个 `xxxMapper.xml` 中已经定义过了，此时当前的xml还需要用到，难不成我复制一份？小白什么也不问上来就复制了，好吧，后期修改来了，每个地方都需要修改了。难受不？

其实Mybatis中也是支持引用其他Mapper文件中的SQL片段的。其实很简单，比如你在 `com.xxx.dao.xxMapper` 这个Mapper的XML中定义了一个SQL片段如下：

```xml
<sql id="Base_Column_List">
    ID,MAJOR,BIRTHDAY,AGE,NAME,HOBBY
</sql>
```

此时我在 `com.xxx.dao.PatinetMapper` 中的XML文件中需要引用，如下：

```xml
<include refid="com.xxx.dao.xxMapper.Base_Column_List"></include>
```

如此简单，类似于Java中的全类名。

`<select>` 标签中的 `resultMap` 同样可以这么引用，和上面引用的方式一样，不再赘述了。