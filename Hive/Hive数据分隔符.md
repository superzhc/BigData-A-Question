Hive中默认的记录和字段分隔符

![Hive中默认的记录和字段分隔符](https://gitee.com/superzchao/GraphBed/raw/master/1579082374_20200115175741448_25981.png)

示例：

```sql
create table superz_employees(
    name         string  comment '姓名',
    salary       float   comment '薪水',
    subordinates array<string>   comment '下属员工',
    deductions   map<string,float>   comment '扣税信息',
    address      struct<street:string,city:string,state:string,zip:int> comment '地址'
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\001'
COLLECTION ITEMS TERMINATED BY '\002'
MAP KEYS TERMINATED BY '\003'
LINES TERMINATED BY '\n'
STORED AS TEXTFILE
;
```

- `ROW FORMAT DELIMITED`这组关键字必须要写在其他子句（除了`STORED AS ...`）之前
- 列分隔符：`ROW FORMAT DELIMITED FIELDS TERMINATED BY '\001'`
- 集合元素间的分隔符：`ROW FORMAT DELIMITED COLLECTION ITEMS TERMINATED BY '\002'`
- map的键值之间的分隔符：`ROW FORMAT DELIMITED MAP KEYS TERMINATED BY '\003'`

子句`LINES TERMINATED BY '...'`和`STORED AS ...`不需要`ROW FORMAT DELIMITED`关键字

> 事实上，Hive到目前为止对于`LINES TERMINATED BY '...'`仅支持字符`'\n'`，也就是说行与行之间的分隔符只能为`'\n'`，因此这个子句现在使用起来还是有限制的
> 【Hive编程指南 p98】