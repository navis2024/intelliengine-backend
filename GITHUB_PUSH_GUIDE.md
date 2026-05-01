# GitHub上传指南

## 方法一：使用IDEA（推荐）

1. **IDEA中打开项目**
2. **创建远程仓库**
   - VCS → Share Project on GitHub
   - 输入仓库名: `intelliengine-backend`
   - 选择: Private (私有)
3. **推送代码**
   - VCS → Git → Push
   - 或点击工具栏推送按钮

## 方法二：命令行

```bash
cd /mnt/e/pj/ZQ_plat

# 1. 创建GitHub仓库（需要gh CLI或curl）
gh repo create intelliengine-backend --private --source=. --push

# 或者使用curl（需要Token）
curl -H "Authorization: token YOUR_TOKEN" \
  -d '{"name":"intelliengine-backend","private":true}' \
  https://api.github.com/user/repos

# 2. 添加远程仓库并推送
git remote add origin https://github.com/你的用户名/intelliengine-backend.git
git branch -M main
git push -u origin main
```

## 仓库信息

- **仓库名**: intelliengine-backend
- **项目路径**: E:\pj\ZQ_plat
- **当前分支**: master
- **已提交**: Initial commit (50个文件)

## 注意事项

1. 不要提交 target/ 目录（已在.gitignore中排除）
2. 不要提交 .idea/ 目录
3. 推送前确保代码可编译通过

---
*创建时间: 2024-05-01*
