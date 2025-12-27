from datetime import datetime
from pydantic import BaseModel, Field
from typing import Optional


class TransactionCreate(BaseModel):
    type: str = Field(..., description="交易类型: 'income' 或 'expense'")
    amount: float = Field(..., gt=0, description="金额，必须大于0")
    category: str = Field(..., description="分类")
    description: Optional[str] = Field(None, description="描述")
    image_path: Optional[str] = Field(None, description="图片路径")
    date: Optional[datetime] = Field(None, description="交易日期，默认为当前时间")


class TransactionUpdate(BaseModel):
    type: Optional[str] = Field(None, description="交易类型: 'income' 或 'expense'")
    amount: Optional[float] = Field(None, gt=0, description="金额，必须大于0")
    category: Optional[str] = Field(None, description="分类")
    description: Optional[str] = Field(None, description="描述")
    image_path: Optional[str] = Field(None, description="图片路径")
    date: Optional[datetime] = Field(None, description="交易日期")


class TransactionResponse(BaseModel):
    id: int
    user_id: int
    type: str
    amount: float
    category: str
    description: Optional[str]
    image_path: Optional[str]
    date: datetime
    created_at: datetime
    updated_at: Optional[datetime]

    class Config:
        from_attributes = True


class TransactionListResponse(BaseModel):
    total: int
    items: list[TransactionResponse]


class TransactionSummaryResponse(BaseModel):
    total_income: float
    total_expense: float
    balance: float

