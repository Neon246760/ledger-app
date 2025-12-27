from datetime import datetime
from pydantic import BaseModel, Field
from typing import Optional


class LedgerCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100, description="账本名称")
    description: Optional[str] = Field(None, max_length=500, description="账本描述")


class LedgerUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=100, description="账本名称")
    description: Optional[str] = Field(None, max_length=500, description="账本描述")


class LedgerResponse(BaseModel):
    id: int
    user_id: int
    name: str
    description: Optional[str]
    created_at: datetime
    updated_at: Optional[datetime]

    class Config:
        from_attributes = True


class LedgerListResponse(BaseModel):
    total: int
    items: list[LedgerResponse]

