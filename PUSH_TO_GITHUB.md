# 推送到GitHub指南

## 当前状态
- 本地提交已完成: `638ebe0`
- 消息: "feat: add project, asset, review modules with COLA architecture"
- 87个文件变更，2071行新增

## 手动推送步骤

### 方式1: 使用IDEA推送（推荐）
1. 打开IDEA，加载项目 `E:\pj\ZQ_plat`
2. 点击右上角 Git 图标
3. 点击 "Push"
4. 如果提示登录，输入GitHub账号密码或Token

### 方式2: 命令行（需要GitHub Token）
```bash
# 进入项目目录
cd E:\pj\ZQ_plat

# 使用HTTPS方式（需要Token）
git remote set-url origin https://<YOUR_TOKEN>@github.com/navis2024/intelliengine-backend.git

# 推送
git push origin master
```

### 方式3: 配置SSH密钥
```bash
# 生成SSH密钥
ssh-keygen -t ed25519 -C "your_email@example.com"

# 复制公钥到GitHub Settings -> SSH and GPG keys
cat ~/.ssh/id_ed25519.pub

# 测试连接
ssh -T git@github.com

# 推送
git push origin master
```

## 推送后验证
访问: https://github.com/navis2024/intelliengine-backend
确认能看到最新提交 `638ebe0`

## 本次提交内容
- ✅ User模块（已完善）
- ✅ Project模块（新增）
- ✅ Asset模块（新增）  
- ✅ Review模块（新增）
- ✅ Market模块基础结构
- ✅ 编译通过，可运行
