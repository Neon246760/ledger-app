from fastapi import Depends

from app.crud.user import UserRepo
from app.models.user import Users
from app.core.security import hash_text


class UserService:
    def __init__(self, repo: UserRepo):
        self.repo = repo

    def register_user(self, username: str, password: str):
        
        print(f"Registering user: {username}, password: {password}")
        hashed = hash_text(password)
        print(f"Hashed password: {hashed}")
        user = Users(username=username, password=hashed)
        self.repo.create_user(user)


def get_user_service(repo: UserRepo = Depends(UserRepo)) -> UserService:
    return UserService(repo)
