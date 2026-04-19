# Git 初始化与推送到 GitHub（详细步骤）

> 若你本机已安装 Git 且熟悉流程，可直接跳到 **「四、一次性命令」**。

---

## 一、必须先具备的条件（缺一不可）

### 1. 安装 Git for Windows

若终端输入 `git --version` 提示「不是内部或外部命令」：

1. 打开：https://git-scm.com/download/win  
2. 下载安装包，安装时勾选 **「Git from the command line and also from 3rd-party software」**（把 Git 加入 PATH）。  
3. **关闭并重新打开** CMD / PowerShell / Cursor 终端。  
4. 验证：

```bat
git --version
```

应显示 `git version 2.x.x`。

**替代**：在 PowerShell（管理员可选）执行：

```powershell
winget install --id Git.Git -e --source winget
```

装完后**新开终端**再验证。

### 2. 注册 GitHub 账号

浏览器打开：https://github.com/signup 完成注册并登录。

### 3. 在 GitHub 上新建空仓库（必须由你在网页上操作）

1. 登录后右上角 **+** → **New repository**。  
2. **Repository name**：例如 `JavaDesignWork` 或 `addressbook-course`。  
3. 选择 **Public**（或 Private）。  
4. **不要**勾选「Add a README / .gitignore / license」（本地已有文件，避免冲突）。  
5. 点 **Create repository**。  
6. 记下页面上的仓库地址，二选一：

- HTTPS：`https://github.com/你的用户名/仓库名.git`  
- SSH：`git@github.com:你的用户名/仓库名.git`（需先配置 SSH 密钥，见下文）

---

## 二、身份配置（每台电脑做一次）

在 **CMD 或 PowerShell** 中（把名字和邮箱换成你的；邮箱建议用 GitHub 账号邮箱）：

```bat
git config --global user.name "你的姓名或昵称"
git config --global user.email "你的邮箱@example.com"
```

---

## 三、认证方式（推送时必须）

GitHub 已不再支持「仅用密码」推送，需任选其一：

### 方式 A：HTTPS + Personal Access Token（PAT）（新手常用）

1. GitHub → 右上角头像 → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)** → **Generate new token**。  
2. 勾选至少 **`repo`**，生成后**复制令牌**（只显示一次）。  
3. 首次 `git push` 时：用户名填 GitHub 用户名，**密码处粘贴 PAT**（不是登录密码）。

### 方式 B：SSH 密钥

1. 生成密钥（一路回车即可）：

```bat
ssh-keygen -t ed25519 -C "你的邮箱@example.com"
```

2. 显示公钥并**全选复制**：

```bat
type %USERPROFILE%\.ssh\id_ed25519.pub
```

3. GitHub → **Settings** → **SSH and GPG keys** → **New SSH key**，粘贴保存。  
4. 远程地址使用 `git@github.com:用户名/仓库名.git`。

---

## 四、在项目目录执行（复制粘贴）

在 **`D:\JavaDesignWork`** 下打开终端，**按顺序**执行（将 `你的仓库URL` 换成上一步的真实地址）：

```bat
cd /d D:\JavaDesignWork

git init

git add .

git status

git commit -m "初始提交：通讯录管理系统（Swing + Maven + 文档）"

git branch -M main

git remote add origin 你的仓库URL

git push -u origin main
```

**若 `git push` 报错**：

- `rejected` / `fetch first`：若你在 GitHub 上建库时勾选了 README，需先：`git pull origin main --rebase` 再 `git push`。  
- `Permission denied`：检查 PAT 或 SSH 是否配置正确。  
- `remote origin already exists`：先 `git remote remove origin`，再重新 `git remote add origin ...`。

---

## 五、以后日常保存与上传

```bat
cd /d D:\JavaDesignWork
git add .
git status
git commit -m "说明本次改了什么"
git push
```

---

## 六、本仓库中已为你准备的文件

| 文件 | 作用 |
|------|------|
| `.gitignore` | 忽略 `target/`、IDE 杂项等，避免把编译产物推上去 |
| `README.md` | GitHub 仓库首页说明 |
| `GIT和GitHub上传指南.md` | 本文档 |

---

## 七、必须由你完成、助手无法代劳的部分

| 事项 | 原因 |
|------|------|
| 安装 Git、配置 PATH | 需在你本机执行安装向导 |
| 注册 GitHub、点「New repository」 | 需你的账号与仓库名 |
| 生成 PAT 或 SSH 公钥 | 涉及你的账号安全，不能由他人代操作 |
| 在浏览器里完成首次登录/授权 | 同上 |

完成 **第一节安装 Git** 与 **第三节在 GitHub 建库** 后，把 **`git push` 的完整报错**（若有）发给我，我可以根据报错逐行帮你改命令。
