# 1. 环境配置
```bash
# 创建虚拟环境
python -m venv .venv
# 激活虚拟环境
source ./.venv/Scripts/activate
# 安装依赖
pip install -r requirements.txt
```

# 2. 运行项目
## 2.1 初始化数据库
```bash
# 初始化数据库
python app/init_db.py
```

## 2.1 启动项目
```bash
# 运行项目
uvicorn app.main:app --reload
```

## 2.2 访问项目
- 查看 API 文档，访问 `http://localhost:8000/docs`
