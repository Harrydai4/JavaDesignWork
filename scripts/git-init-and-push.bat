@echo off
chcp 65001 >nul
cd /d "%~dp0.."
echo 当前目录: %CD%
where git >nul 2>&1
if errorlevel 1 (
  echo [错误] 未找到 git。请先安装 Git for Windows 并加入 PATH，见 GIT和GitHub上传指南.md
  exit /b 1
)
git --version
echo.
echo 请先在 GitHub 创建空仓库，然后将下面一行的 YOUR_REPO_URL 换成你的仓库地址（HTTPS 或 SSH）
echo 例如: https://github.com/username/JavaDesignWork.git
echo.
set /p REPO_URL="粘贴仓库 URL 后回车: "
if "%REPO_URL%"=="" (
  echo 已取消。
  exit /b 1
)
if not exist .git git init
git add .
git status
echo.
set /p CONFIRM="确认提交? (Y/N): "
if /i not "%CONFIRM%"=="Y" exit /b 0
git commit -m "初始提交：通讯录管理系统（Swing + Maven + 文档）"
git branch -M main
git remote remove origin 2>nul
git remote add origin "%REPO_URL%"
echo.
echo 接下来执行: git push -u origin main
echo HTTPS 时密码处粘贴 Personal Access Token
git push -u origin main
pause
