from typing import Annotated
import os

from fastapi import Depends
from sqlmodel import Session, create_engine

DB_PATH = os.getenv("DB_PATH", "app.db")
engine = create_engine(url=f"sqlite:///{DB_PATH}", connect_args={"check_same_thread": False})


def get_session():
    with Session(engine) as session:
        yield session


SessionDep = Annotated[Session, Depends(get_session)]
