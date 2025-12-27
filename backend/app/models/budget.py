from datetime import datetime
from sqlmodel import Field, SQLModel
from typing import Optional

class Budget(SQLModel, table=True):
    __tablename__ = "budgets"

    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="users.id")
    amount: float = Field(gt=0)
    category: str | None = None  # If null, it's the total budget for the month
    month: str  # Format: "YYYY-MM"
    created_at: datetime = Field(default_factory=datetime.now)
    updated_at: datetime = Field(default_factory=datetime.now)

