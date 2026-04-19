# 通讯录管理系统（课程设计）

Java 25 + Maven + Swing，单机通讯录：分组、联系人 CRUD、搜索（含拼音）、CSV/vCard 导入导出，JSON 持久化。

## 环境

- JDK 25（`JAVA_HOME`）
- Maven 3.9+（`MAVEN_HOME` / `Path`）

详见仓库内 `环境配置留痕.md`。

## 构建与运行

```bat
mvn -q test
mvn -q compile exec:java
```

或使用 Cursor / IDEA 运行 `com.javadesign.addressbook.AddressBookApp`。

## 文档

| 文件 | 说明 |
|------|------|
| `程序框架说明.md` | 分层与类职责 |
| `开发指导文档-通讯录管理系统.md` | 开发与验收 |
| `环境配置留痕.md` | 本机环境记录 |

数据文件默认位于用户目录：`.javadesign-addressbook\`（不纳入版本库）。

## 许可证

课程作业用途，请遵循学校学术诚信要求。
