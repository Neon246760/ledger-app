from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class BudgetBase(BaseModel):
    amount: float
    category: Optional[str] = None
    month: str

class BudgetCreate(BudgetBase):
    pass

class BudgetUpdate(BaseModel):
    amount: float

class BudgetResponse(BudgetBase):
    id: int
    user_id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
