from fastapi import HTTPException
from sqlmodel import select, func
from datetime import datetime
from typing import Optional

from ..models.transaction import Transaction
from ..core.db import SessionDep


class TransactionRepo:
    def __init__(self, session: SessionDep):
        self.session = session

    def create_transaction(self, transaction: Transaction):
        self.session.add(transaction)
        self.session.commit()
        self.session.refresh(transaction)
        return transaction

    def get_transaction_by_id(self, transaction_id: int, user_id: int) -> Transaction | None:
        statement = select(Transaction).where(
            Transaction.id == transaction_id,
            Transaction.user_id == user_id
        )
        return self.session.exec(statement).first()

    def get_user_transactions(
        self,
        user_id: int,
        skip: int = 0,
        limit: int = 100,
        type: Optional[str] = None,
        category: Optional[str] = None,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None
    ) -> list[Transaction]:
        statement = select(Transaction).where(Transaction.user_id == user_id)
        
        if type:
            statement = statement.where(Transaction.type == type)
        if category:
            statement = statement.where(Transaction.category == category)
        if start_date:
            statement = statement.where(Transaction.date >= start_date)
        if end_date:
            statement = statement.where(Transaction.date <= end_date)
        
        statement = statement.order_by(Transaction.date.desc()).offset(skip).limit(limit)
        return list(self.session.exec(statement).all())

    def count_user_transactions(
        self,
        user_id: int,
        type: Optional[str] = None,
        category: Optional[str] = None,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None
    ) -> int:
        statement = select(func.count(Transaction.id)).where(Transaction.user_id == user_id)
        
        if type:
            statement = statement.where(Transaction.type == type)
        if category:
            statement = statement.where(Transaction.category == category)
        if start_date:
            statement = statement.where(Transaction.date >= start_date)
        if end_date:
            statement = statement.where(Transaction.date <= end_date)
        
        return self.session.exec(statement).one()

    def update_transaction(self, transaction: Transaction):
        transaction.updated_at = datetime.now()
        self.session.add(transaction)
        self.session.commit()
        self.session.refresh(transaction)
        return transaction

    def delete_transaction(self, transaction_id: int, user_id: int):
        transaction = self.get_transaction_by_id(transaction_id, user_id)
        if transaction is None:
            raise HTTPException(status_code=404, detail="Transaction not found")
        self.session.delete(transaction)
        self.session.commit()
        return True

    def get_user_summary(
        self,
        user_id: int,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None
    ) -> dict:
        income_statement = select(func.sum(Transaction.amount)).where(
            Transaction.user_id == user_id,
            Transaction.type == "income"
        )
        expense_statement = select(func.sum(Transaction.amount)).where(
            Transaction.user_id == user_id,
            Transaction.type == "expense"
        )
        
        if start_date:
            income_statement = income_statement.where(Transaction.date >= start_date)
            expense_statement = expense_statement.where(Transaction.date >= start_date)
        if end_date:
            income_statement = income_statement.where(Transaction.date <= end_date)
            expense_statement = expense_statement.where(Transaction.date <= end_date)
        
        total_income = self.session.exec(income_statement).one() or 0.0
        total_expense = self.session.exec(expense_statement).one() or 0.0
        
        return {
            "total_income": float(total_income),
            "total_expense": float(total_expense),
            "balance": float(total_income - total_expense)
        }

