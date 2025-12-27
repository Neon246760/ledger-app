from datetime import datetime
from sqlmodel import Field, SQLModel, Relationship
from typing import Optional


class Transaction(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="users.id")
    type: str  # "income" 或 "expense"
    amount: float = Field(gt=0)  # 金额必须大于0
    category: str  # 分类：如 "餐饮", "交通", "工资" 等
    description: str | None = None  # 描述
    image_path: str | None = None  # 图片路径
    date: datetime = Field(default_factory=datetime.now)  # 交易日期
    created_at: datetime = Field(default_factory=datetime.now)  # 创建时间
    updated_at: datetime | None = None  # 更新时间

