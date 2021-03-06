# DataLog2SQL

## DatalogFileReader

从input/args[0]路径读取.dl文件，分析得到下表变量。

| 变量类型                                | 变量名          | 描述                                                  |
| --------------------------------------- | --------------- | ----------------------------------------------------- |
| List\<Stirng\>                          | queryToConvert  | 是包含所有单条查询的List                              |
| LinkedHashMap\<String, List\<String\>\> | tNameAndCName   | {表1=\[属性1，属性2，...\], 表2=\[属性1，属性2，...]} |
| LinkedHashMap<String, Integer>          | tNameAndAttrNum | {表1=属性数1, 表2=属性数2}                            |



## DatalogProblemTypeParser

| 变量类型                        | 变量名       | 描述                                    |
| ------------------------------- | ------------ | --------------------------------------- |
| LinkedHashMap\<String, String\> | problemTypes | {查询1=查询类型1, 查询2=查询类型2, ...} |

目前支持的查询类型：

| 查询类型            | 对应符号   |
| ------------------- | ---------- |
| 插入                | fact       |
| 投影                | projection |
| 选择                | select     |
| 内连接              | (ij) join  |
| 全连接              | (fj) join  |
| 左连接              | (lj) join  |
| 右连接              | (rj) join  |
| 并集                | union      |
| 差集                | difference |
| 排序                | orderby    |
| 分组（future work） | groupby    |



## DatalogTranslator

实现对不同problemType的翻译。

## DatalogFileWriter

向output/args[1]写入文件。

## SQLResultTracer

执行翻译后的SQL并将结果保存至args[2]（如果该参数不存在则不执行SQL）

## DatalogToSql

main函数，负责工作流。


