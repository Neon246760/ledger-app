from pydantic import BaseModel
from typing import Optional


class RegisterRequest(BaseModel):
    username: str
    password: str
    repeat_password: str

class UserUpdate(BaseModel):
    avatar_path: Optional[str] = None

class UserResponse(BaseModel):
    id: int
    username: str
    avatar_path: Optional[str] = None


class ChangePasswordRequest(BaseModel):
    password: str
    repeat_password: str


class DeleteAccountRequest(BaseModel):
    password: str
    avatar_url: str | None = None

