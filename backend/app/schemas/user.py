from pydantic import BaseModel
from typing import Optional


class RegisterRequest(BaseModel):
    username: str
    password: str
    repeat_password: str


class ChangePasswordRequest(BaseModel):
    password: str
    repeat_password: str


class DeleteAccountRequest(BaseModel):
    password: str
    avatar_url: str | None = None


class UserResponse(BaseModel):
    id: int
    username: str
    avatar_url: str | None = None


class UserUpdate(BaseModel):
    avatar_url: str | None = None

