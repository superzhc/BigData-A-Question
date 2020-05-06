# range

range 查询找出那些落在指定区间内的数字或时间

操作符如下：

| 操作符 | 含义     |
| ------ | -------- |
| `gt`   | 大于     |
| `gte`  | 大于等于 |
| `lt`   | 小于     |
| `lte`  | 小于等于 |

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "range":{
            "age":{
                "gte":20,
                "lt":30
            }
        }
    }
}
```