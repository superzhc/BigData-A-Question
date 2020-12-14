<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-11-27 15:44:05
 * @LastEditTime : 2020-11-27 16:01:45
 * @Copyright 2020 SUPERZHC
-->
# 解析 TSV 文件

不同于 CSV 文件，TSV（制表符分隔）文件中所包含的数据通过 TAB 制表符进行分隔。

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

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

/**
 * 2020年11月27日 superz add
 */
public class ParseTSVDemo
{
    public static void parseTsv(File file){
        TsvParserSettings settings=new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        TsvParser parse=new TsvParser(settings);
        List<String[]> allRows= parse.parseAll(file);
        for (int i = 0; i <allRows.size(); i++) {
            System.out.println(Arrays.asList(allRows.get(i)));
        }
    }
}
```