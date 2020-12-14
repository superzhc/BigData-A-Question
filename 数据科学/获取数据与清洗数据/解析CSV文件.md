<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-11-27 14:41:05
 * @LastEditTime : 2020-11-27 15:43:10
 * @Copyright 2020 SUPERZHC
-->
# 解析 CSV 文件

## 使用 Univocity 工具库

**引入依赖**

```xml
<dependency>
    <groupId>com.univocity</groupId>
    <artifactId>univocity-parsers</artifactId>
    <version>2.9.0</version>
    <type>jar</type>
</dependency>
```

**代码**

```java
import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 * 2020年11月27日 superz add
 */
public class ParseCSVDemo
{
    public static void parseCsv(File file) {
        // 配置对象，该对象用来提供多种配置选项
        CsvParserSettings settings = new CsvParserSettings();
        settings.setLineSeparatorDetectionEnabled(true);
        // 创建一个RowListProcessor对象，用来把每个解析的行存储在列表中
        RowListProcessor rowListProcessor = new RowListProcessor();
        settings.setRowProcessor(rowListProcessor);
        // 如果待解析的CSV文件包含标题头，可以把第一个解析行看作文件中每个列的标题
        settings.setHeaderExtractionEnabled(true);

        // 创建一个parse实例
        CsvParser parser = new CsvParser(settings);
        parser.parse(file);

        String[] headers = rowListProcessor.getHeaders();
        List<String[]> rows = rowListProcessor.getRows();
        for (int i = 0; i < rows.size(); i++) {
            System.out.println(Arrays.asList(rows.get(i)));
        }
    }
}
```