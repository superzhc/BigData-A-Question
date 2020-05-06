# URL

在日常使用过程中，一般只是想在**一个或多个特殊的索引**并且**在一个或多个特殊的类型**中进行搜索，对于这种情况，可以通过在 URL 中指定特殊的索引和类型达到这种效果，如下所示：

| 语法                                 | 范围                                               |
| ------------------------------------ | -------------------------------------------------- |
| `/_search`                           | 在所有的索引中搜索所有的类型                       |
| `/index1/_search`                    | 在 index1 索引中搜索所有类型                       |
| `/index1,index2/_search`             | 在 index1 和 index2 索引中搜索所有的文档           |
| `/index*/_search`                    | 在任何以 index 开头的索引中搜索所有的类型          |
| `/index1/type1/_search`              | 在 index1 索引中搜索 type1 类型                    |
| `/index1,index2/type1,type2/_search` | 在 index1 和 index2 索引中搜索 type1 和 type2 类型 |
| `/_all/type1,type2/_search`          | 在所有的索引中搜索 type1 和 type2 类型             |