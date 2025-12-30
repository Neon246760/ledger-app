from pydantic import BaseModel


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

