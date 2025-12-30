from fastapi import HTTPException
from sqlmodel import select
from datetime import datetime
from typing import Optional

from ..models.ledger import Ledger
from ..core.db import SessionDep


class LedgerRepo:
    def __init__(self, session: SessionDep):
        self.session = session

    def create_ledger(self, ledger: Ledger):
        self.session.add(ledger)
        self.session.commit()
        self.session.refresh(ledger)
        return ledger

    def get_ledger_by_id(self, ledger_id: int, user_id: int) -> Ledger | None:
        statement = select(Ledger).where(
            Ledger.id == ledger_id,
            Ledger.user_id == user_id
        )
        return self.session.exec(statement).first()

    def get_user_ledgers(
        self,
        user_id: int,
        skip: int = 0,
        limit: int = 100
    ) -> list[Ledger]:
        statement = select(Ledger).where(
            Ledger.user_id == user_id
        ).order_by(Ledger.created_at.desc()).offset(skip).limit(limit)
        return list(self.session.exec(statement).all())

    def count_user_ledgers(self, user_id: int) -> int:
        statement = select(Ledger).where(Ledger.user_id == user_id)
        return len(list(self.session.exec(statement).all()))

    def update_ledger(self, ledger: Ledger):
        ledger.updated_at = datetime.now()
        self.session.add(ledger)
        self.session.commit()
        self.session.refresh(ledger)
        return ledger

    def delete_ledger(self, ledger_id: int, user_id: int):
        ledger = self.get_ledger_by_id(ledger_id, user_id)
        if ledger is None:
            raise HTTPException(status_code=404, detail="Ledger not found")
        self.session.delete(ledger)
        self.session.commit()
        return True

    def delete_all_for_user(self, user_id: int):
        items = self.get_user_ledgers(user_id=user_id, skip=0, limit=10_000)
        for l in items:
            self.session.delete(l)
        self.session.commit()

