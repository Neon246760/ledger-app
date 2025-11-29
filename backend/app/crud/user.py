from fastapi import HTTPException
from sqlmodel import select

from models.user import Users
from core.db import SessionDep


class UserRepo:
    def __init__(self, session: SessionDep):
        self.session = session

    def create_user(self, user: Users):
        existing = self.session.exec(select(Users).where(Users.username == user.username)).first()
        if existing:
            raise HTTPException(status_code=409, detail="Username already exists")
        self.session.add(user)
        self.session.commit()
        