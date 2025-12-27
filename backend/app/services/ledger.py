from fastapi import Depends, HTTPException

from ..crud.ledger import LedgerRepo
from ..models.ledger import Ledger
from ..core.db import SessionDep


class LedgerService:
    def __init__(self, repo: LedgerRepo):
        self.repo = repo

    def create_ledger(
        self,
        user_id: int,
        name: str,
        description: str | None = None
    ):
        ledger = Ledger(
            user_id=user_id,
            name=name,
            description=description
        )
        return self.repo.create_ledger(ledger)

    def get_ledger(self, ledger_id: int, user_id: int):
        ledger = self.repo.get_ledger_by_id(ledger_id, user_id)
        if ledger is None:
            raise HTTPException(status_code=404, detail="Ledger not found")
        return ledger

    def get_ledgers(
        self,
        user_id: int,
        skip: int = 0,
        limit: int = 100
    ):
        return self.repo.get_user_ledgers(
            user_id=user_id,
            skip=skip,
            limit=limit
        )

    def update_ledger(
        self,
        ledger_id: int,
        user_id: int,
        name: str | None = None,
        description: str | None = None
    ):
        ledger = self.repo.get_ledger_by_id(ledger_id, user_id)
        if ledger is None:
            raise HTTPException(status_code=404, detail="Ledger not found")
        
        if name is not None:
            ledger.name = name
        if description is not None:
            ledger.description = description
        
        return self.repo.update_ledger(ledger)

    def delete_ledger(self, ledger_id: int, user_id: int):
        return self.repo.delete_ledger(ledger_id, user_id)


def get_ledger_service(repo: LedgerRepo = Depends(LedgerRepo)) -> LedgerService:
    return LedgerService(repo)

