from fastapi import FastAPI, APIRouter
from fastapi.staticfiles import StaticFiles
from sqlmodel import SQLModel
import os

from .routers import user, transaction, ledger, upload, budget
from .core.db import engine
from .models import user as user_models
from .models import transaction as transaction_models
from .models import ledger as ledger_models
from .models import budget as budget_models

app = FastAPI()

# Mount uploads directory to serve static files
if not os.path.exists("uploads"):
    os.makedirs("uploads")
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")

# 在应用启动时创建所有表
@app.on_event("startup")
def on_startup():
    SQLModel.metadata.create_all(engine)

# 创建API路由组，添加 /api 前缀
api_router = APIRouter(prefix="/api")

# 将所有路由添加到API路由组
api_router.include_router(user.router)
api_router.include_router(transaction.router)
api_router.include_router(ledger.router)
api_router.include_router(upload.router)
api_router.include_router(budget.router)

app.include_router(api_router)
api_router.include_router(upload.router)

# 将API路由组添加到应用
app.include_router(api_router)
