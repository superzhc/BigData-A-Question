# Elasticsearch 聚合分析

## 简介

在检索范围确定后，Elasticsearch 还支持对结果集做聚合查询，返回更直接的聚合统计结果。

- 指标聚合（Metric Aggregation）：对一个数据集求最大值、最小值、求和、平均值等指标的聚合
- 桶聚合（Bucket Aggregation）：在组上进行指标聚合
- 管道聚合（Pipeline Aggregation）：
- 矩阵聚合（Matrix Aggregation）：

### 聚合分析查询的写法

```json
"aggregations" : {
    "<aggregation_name>" : { <!--聚合的名字 -->
        "<aggregation_type>" : { <!--聚合的类型 -->
            <aggregation_body> <!--聚合体：对哪些字段进行聚合 -->
        }
        [,"meta" : {  [<meta_data_body>] } ]? <!--元 -->
        [,"aggregations" : { [<sub_aggregation>]+ } ]? <!--在聚合里面在定义子聚合 -->
    }
    [,"<aggregation_name_2>" : { ... } ]*<!--聚合的名字 -->
}
```

> 说明：aggregations 也可简写成 aggs

### 聚合分析的值来源

聚合计算的值可以取**字段的值**，也可以是**脚本计算的结果**。

## 指标聚合

**max**

示例：查询所有客户中余额的最大值

```http
POST /bank/_search
{
  "size": 0, 
  "aggs": {
    "masssbalance": {
      "max": {
        "field": "balance"
      }
    }
  }
}
```

**min**

示例：查询年龄为 24 岁的客户中的余额最大值

```http
POST /bank/_search
{
  "size": 2, 
  "query": {
    "match": {
      "age": 24
    }
  },
  "sort": [
    {
      "balance": {
        "order": "desc"
      }
    }
  ],
  "aggs": {
    "max_balance": {
      "max": {
        "field": "balance"
      }
    }
  }
}
```

**avg**

示例：值来源于脚本，查询所有客户的平均年龄是多少，并对平均年龄加10

```http
POST /bank/_search?size=0
{
  "aggs": {
    "avg_age": {
      "avg": {
        "script": {
          "source": "doc.age.value"
        }
      }
    },
    "avg_age10": {
      "avg": {
        "script": {
          "source": "doc.age.value + 10"
        }
      }
    }
  }
}
```

**sum**

示例：指定field，在脚本中用_value 取字段的值

```http
POST /bank/_search?size=0
{
  "aggs": {
    "sum_balance": {
      "sum": {
        "field": "balance",
        "script": {
            "source": "_value * 1.03"
        }
      }
    }
  }
}
```



## 桶聚合



## 管道聚合



## 矩阵聚合