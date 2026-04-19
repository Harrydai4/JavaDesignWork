# 通讯录项目 — 环境验证与后续工作流

## 一、请你本机完成的验证（约 5 分钟）

> 说明：若 Cursor 内置终端里 `java` 不可用，多半是**未重启终端/IDE** 或 **PATH 未生效**。请在 **Windows 终端（PowerShell）** 或 **CMD** 中新开窗口执行。

### 1. 验证 JDK

```bat
java -version
javac -version
```

- 应显示 **25**（或你安装的主版本），且**不是**「无法将 java 识别为…」。

### 2. 验证 `JAVA_HOME`（建议）

```powershell
echo $env:JAVA_HOME
```

- 应指向含 `bin`、`lib` 的 JDK 根目录，例如：`D:\soft\OracleJdk\jdk25\jdk-25`。

### 3. 验证 Maven（下一步必用）

若尚未安装 Maven，任选其一：

- **官网**：下载 zip，解压到如 `C:\tools\apache-maven`，配置 `MAVEN_HOME` 与 `Path` 增加 `%MAVEN_HOME%\bin`。
- **winget**（若可用）：`winget install Apache.Maven`（以实际包名为准，可先 `winget search maven`）。

然后新开终端：

```bat
mvn -v
```

应显示 Maven 版本，且其中的 Java 指向你的 JDK。

### 4. 在本项目根目录编译并运行骨架

在 `D:\JavaDesignWork` 下执行：

```bat
mvn -q compile exec:java
```

预期控制台输出一行：`通讯录管理系统 — 工程骨架已就绪（JDK: …）`。

**尚未安装 Maven 时**：可先双击或在终端执行根目录下的 `run-without-maven.bat`（同样需要本机 `javac` / `java` 可用），效果等价于先编译再运行 `AddressBookApp`。

---

## 二、前期工作闭环检查表

| 序号 | 内容 | 状态 |
|------|------|------|
| 1 | JDK 已装，`java` / `javac` 可用 | 你自测打勾 |
| 2 | `JAVA_HOME` 正确（可选但强烈建议） | 你自测打勾 |
| 3 | Maven 已装，`mvn -v` 正常 | 你自测打勾 |
| 4 | 本仓库 `mvn compile exec:java` 能跑通 | 你自测打勾 |
| 5 | IntelliJ IDEA 已装，能 **Open** 本目录为 Maven 项目（推荐） | 你自测打勾 |

---

## 三、推荐后续开发顺序（进入功能实现阶段）

1. 在 `com.javadesign.addressbook` 下增加 `model`（`Contact`、`Group`）、`storage`、`service`、`ui` 包。
2. 选定 **Swing 或 JavaFX**，先搭主界面：左侧分组树 + 右侧联系人表。
3. 实现文件持久化，再导入导出 CSV / vCard，最后做搜索与拼音。

详细需求仍以课程附件与 `文件/开发前置文档-通讯录管理系统.md` 为准。

---

## 四、根目录 `hellojava.java` 说明

该文件为 JDK 25 **紧凑源文件**写法，可作个人实验保留。正式课设代码请以 **`src/main/java` 下标准 `public class` + `public static void main`** 为准（见 `AddressBookApp.java`）。
