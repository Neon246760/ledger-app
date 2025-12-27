from datetime import datetime
from sqlmodel import Field, SQLModel


class Ledger(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="users.id")
    name: str = Field(max_length=100, description="账本名称")
    description: str | None = Field(default=None, max_length=500, description="账本描述")
    created_at: datetime = Field(default_factory=datetime.now, description="创建时间")
    updated_at: datetime | None = Field(default=None, description="更新时间")

