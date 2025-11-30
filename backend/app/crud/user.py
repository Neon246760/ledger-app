from fastapi import HTTPException
from sqlmodel import select

from models.user import Users
from core.db import SessionDep


class UserRepo:
    def __init__(self, session: SessionDep):
        self.session = session

    def find_user_by_username(self, username: str):
        user = self.session.exec(select(Users).where(Users.username == username)).first()
        return user

    def create_user(self, user: Users):
        existing_user = self.find_user_by_username(user.username)
        if existing_user is not None:
            raise HTTPException(status_code=409, detail="Username already exists")
        self.session.add(user)
        self.session.commit()
        