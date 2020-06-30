### 说明

HBase 在建表的时候，一个列族可以指定一个 versions，用以表示所存数据的版本数，~~默认该值为 3，即保存最近的 3 个版本的数据~~，**在0.96的版本之前默认每个列是 3 个版本，在 HBase 0.96 之后每个列是 1 个 version**。在每一个 cell 中有同一数据的多个版本，按时间倒序排序。用户可以在建表的时候指定 versions，在放数据的时候以一个时间戳（long 类型）来表示该数据的版本信息。取数据时可以取最新的数据，也可以取特定时间戳的数据，某段时间戳的数据以及全部数据。

### 示例

#### 1、创建表

```sh
# 对于已经存在的，做个删除操作
disable ‘superz_version’
drop 'superz_version'

# 创建表
create 'superz_version','default'
```

#### 2、查看表的描述信息

```sh
describe 'superz_version'
```

![1561513282187](../images/1561513282187.png)

从上面的表结构中，可以看到 VERSIONS 为 1，也就是说，默认情况下只会存取一个版本的列数据，当再次插入的时候，后面的值会覆盖掉前面的值。

#### 3、修改表的结构

为了让 HBase 表支持多个版本的列数据，要进行表结构的修改，如支持 3 个 versions 的版本列数据

```sh
alter 'superz_version',{NAME=>'default',VERSIONS=>3}
```

![1561601872778](../images/1561601872778.png)

再次查看表结构

![1561601901806](../images/1561601901806.png)

### HBase Shell 操作版本数

#### 查看某列的多版本数据

```sh
get 'superz_version','RowKey',{COLUMN=>'列簇:列名',VERSION=>3}
```

### Java API 操作版本数

#### 设置列簇的版本数

```java
/**
	 * 对表添加带版本数的列簇
	 * @param tableName
	 * @param columnFamilys
	 * @throws IOException
	 */
public void createVersionsColumnFamilys(String tableName,String... columnFamilys) throws IOException {
    Admin admin = getConnection().getAdmin();
    TableName table = TableName.valueOf(tableName);
    HTableDescriptor tableDescriptor = admin.getTableDescriptor(table);
    for (String columnFamily : columnFamilys) {
        HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnFamily);
        columnDescriptor.setMaxVersions(Integer.MAX_VALUE);// 默认设置为最大值，不做其他处理了
        tableDescriptor.addFamily(columnDescriptor);
    }
    admin.modifyTable(table, tableDescriptor);
}

/**
	 * 创建表且带有版本数的列簇
	 * 
	 * @param tableName
	 * @param columnFamilys
	 * @throws IOException
	 */
public void createVersionsTable(String tableName, String... columnFamilys) throws IOException {
    Admin admin = getConnection().getAdmin();
    TableName table = TableName.valueOf(tableName);

    HTableDescriptor tableDescriptor = new HTableDescriptor(table);
    for (String columnFamily : columnFamilys) {
        HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnFamily);
        columnDescriptor.setMaxVersions(Integer.MAX_VALUE);// 默认设置为最大值，不做其他处理了
        tableDescriptor.addFamily(columnDescriptor);
    }
    admin.createTable(tableDescriptor);
}

/**
	 * 修改表的列簇的版本数
	 * 
	 * @param tableName
	 * @param columnFamily
	 * @param versions
	 * @throws IOException
	 */
public void alterVersionsColumnFamily(String tableName, String columnFamily, int versions) throws IOException {
    Admin admin = getConnection().getAdmin();
    TableName table = TableName.valueOf(tableName);

    final HColumnDescriptor columnDescriptor = admin.getTableDescriptor(table)
			.getFamily(Bytes.toBytes(columnFamily));
    columnDescriptor.setMaxVersions(versions);

    admin.modifyColumn(table, columnDescriptor);
}

/**
	 * 批量修改表的多个列簇的版本数
	 * 
	 * @param tableName
	 * @param versions
	 * @param columnFamilys
	 * @throws IOException
	 */
public void alterVerionsTable(String tableName, int versions, String... columnFamilys) throws IOException {
    Admin admin = getConnection().getAdmin();
    TableName table = TableName.valueOf(tableName);
    final HTableDescriptor tableDescriptor = admin.getTableDescriptor(table);
    for (String columnFamily : columnFamilys) {
        HColumnDescriptor columnDescriptor = tableDescriptor.getFamily(Bytes.toBytes(columnFamily));
        columnDescriptor.setMaxVersions(versions);
    }
    admin.modifyTable(table, tableDescriptor);
}
```

#### 新增/更新指定版本数据

```java
/**
	 * 插入指定版本数据
	 *
	 * @param tableName
	 * @param rowKey
	 * @param columnFamily
	 * @param qualifier
	 * @param value
	 * @param version
	 * @throws IOException
	 */
public void putWithVersion(String tableName, String rowKey, String columnFamily, String qualifier, String value,
                           long version) throws IOException {
    Table table = getConnection().getTable(TableName.valueOf(tableName));

    //		Put put = new Put(Bytes.toBytes(rowKey));
    //		put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(qualifier), version, Bytes.toBytes(value));
    //		table.put(put);

    // 构建Put就指定版本，后续添加的列都是put设置的版本
    Put put=new Put(Bytes.toBytes(rowKey), version);
    put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(qualifier), Bytes.toBytes(value));
    table.put(put);
}
```

#### 删除操作

##### 1、删除所有列簇的所有列的所有版本

```java
/**
	 * 删除指定行的所有列簇的所有列的所有版本
	 * 
	 * @param tableName
	 * @param rowKey
	 * @throws IOException
	 */
public void deleteRow(String tableName, String rowKey) throws IOException {
    Table table = getConnection().getTable(TableName.valueOf(tableName));
    Delete delete = new Delete(Bytes.toBytes(rowKey));
    table.delete(delete);
}
```

##### 2、删除指定行的所有列簇的所有列的时间戳<b>小于等于</b>指定时间戳(version)的数据版本

```java
/**
	 * 删除指定行的所有列簇的所有列的时间戳<b>小于等于</b>指定时间戳(version)的数据版本
	 * 
	 * @param tableName
	 * @param rowKey
	 * @param version
	 * @throws IOException
	 */
public void deleteOldVersionsRow(String tableName, String rowKey, long version) throws IOException {
    Table table = getConnection().getTable(TableName.valueOf(tableName));
    Delete delete = new Delete(Bytes.toBytes(rowKey), version);
    //==
    //		Delete delete=new Delete(Bytes.toBytes(rowKey));
    //		delete.setTimestamp(version);
    table.delete(delete);
}
```

##### 3、删除指定行的所有列簇的所有列的等于指定时间戳（version）的版本数据

```java
/**
	 * 删除指定行的所有列簇的所有列的等于指定时间戳（version）的版本数据
	 *
	 * @param tableName
	 * @param rowKey
	 * @param version
	 * @throws IOException
	 */
public void deleteVersionRow(String tableName, String rowKey, long version) throws IOException {
    Table table = getConnection().getTable(TableName.valueOf(tableName));
    HTableDescriptor tableDescriptors = table.getTableDescriptor();
    Set<byte[]> columnFamilys = tableDescriptors.getFamiliesKeys();

    Delete delete = new Delete(Bytes.toBytes(rowKey));
    for (byte[] columnFamily : columnFamilys) {
        delete.addFamilyVersion(columnFamily, version);
    }
    table.delete(delete);

}
```

#### 获取操作

##### 1、获取指定行，指定时间戳数据

```java
/**
	 * 获取指定行，指定时间戳数据
	 *
	 * @param tableName
	 * @param rowKey
	 * @param version
	 * @return
	 * @throws IOException
	 */
public Result getVersionRow(String tableName, String rowKey, long version) throws IOException {
    Table table = getConnection().getTable(TableName.valueOf(tableName));
    Get get = new Get(Bytes.toBytes(rowKey));
    get.setTimeStamp(version);
    Result result = table.get(get);
    return result;
}
```

##### 2、获取指定行，某个区间的所有数据

```java
/**
	 * 获取指定行，某个区间的所有数据
	 *
	 * @param tableName
	 * @param rowKey
	 * @param startVersion
	 * @param endVersion
	 * @return
	 * @throws IOException
	 */
public Result getVersionsRow(String tableName, String rowKey, long startVersion, long endVersion)
    throws IOException {
    Table table = getConnection().getTable(TableName.valueOf(tableName));
    Get get = new Get(Bytes.toBytes(rowKey));
    get.setTimeRange(startVersion, endVersion);
    get.setMaxVersions();
    Result result = table.get(get);
    return result;
}
```

