from fastapi import Depends, HTTPException
from datetime import datetime
from typing import Optional

from ..crud.transaction import TransactionRepo
from ..models.transaction import Transaction
from ..core.db import SessionDep


class TransactionService:
    def __init__(self, repo: TransactionRepo):
        self.repo = repo

    def create_transaction(
        self,
        user_id: int,
        type: str,
        amount: float,
        category: str,
        description: Optional[str] = None,
        image_path: Optional[str] = None,
        date: Optional[datetime] = None
    ):
        if type not in ["income", "expense"]:
            raise HTTPException(status_code=400, detail="Type must be 'income' or 'expense'")
        
        if date is None:
            date = datetime.now()
        
        transaction = Transaction(
            user_id=user_id,
            type=type,
            amount=amount,
            category=category,
            description=description,
            image_path=image_path,
            date=date
        )
        return self.repo.create_transaction(transaction)

    def get_transaction(self, transaction_id: int, user_id: int):
        transaction = self.repo.get_transaction_by_id(transaction_id, user_id)
        if transaction is None:
            raise HTTPException(status_code=404, detail="Transaction not found")
        return transaction

    def get_transactions(
        self,
        user_id: int,
        skip: int = 0,
        limit: int = 100,
        type: Optional[str] = None,
        category: Optional[str] = None,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None
    ):
        if type and type not in ["income", "expense"]:
            raise HTTPException(status_code=400, detail="Type must be 'income' or 'expense'")
        
        return self.repo.get_user_transactions(
            user_id=user_id,
            skip=skip,
            limit=limit,
            type=type,
            category=category,
            start_date=start_date,
            end_date=end_date
        )

    def update_transaction(
        self,
        transaction_id: int,
        user_id: int,
        type: Optional[str] = None,
        amount: Optional[float] = None,
        category: Optional[str] = None,
        description: Optional[str] = None,
        date: Optional[datetime] = None
    ):
        transaction = self.repo.get_transaction_by_id(transaction_id, user_id)
        if transaction is None:
            raise HTTPException(status_code=404, detail="Transaction not found")
        
        if type and type not in ["income", "expense"]:
            raise HTTPException(status_code=400, detail="Type must be 'income' or 'expense'")
        
        if type:
            transaction.type = type
        if amount is not None:
            transaction.amount = amount
        if category:
            transaction.category = category
        if description is not None:
            transaction.description = description
        if date:
            transaction.date = date
        
        return self.repo.update_transaction(transaction)

    def delete_transaction(self, transaction_id: int, user_id: int):
        return self.repo.delete_transaction(transaction_id, user_id)

    def get_summary(
        self,
        user_id: int,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None
    ):
        return self.repo.get_user_summary(user_id, start_date, end_date)


def get_transaction_service(repo: TransactionRepo = Depends(TransactionRepo)) -> TransactionService:
    return TransactionService(repo)

