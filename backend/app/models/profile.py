from sqlmodel import Field, SQLModel


class UserProfile(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    user_id: int = Field(unique=True)
    avatar_url: str | None = None
