from fastapi import Depends

from crud.user import UserRepo
from models.user import Users
from core.hash import hash_text


class UserService:
    def __init__(self, repo: UserRepo):
        self.repo = repo

    def register_user(self, username: str, password: str):
        user = Users(username=username, password=hash_text(password))
        self.repo.create_user(user)


def get_user_service(repo: UserRepo = Depends(UserRepo)) -> UserService:
    return UserService(repo)
