from sqlmodel import Session, select
from ..models.budget import Budget
from ..schemas.budget import BudgetCreate, BudgetUpdate
from datetime import datetime

class BudgetRepo:
    def __init__(self, session: Session):
        self.session = session

    def create_budget(self, user_id: int, budget_in: BudgetCreate) -> Budget:
        db_budget = Budget(
            user_id=user_id,
            amount=budget_in.amount,
            category=budget_in.category,
            month=budget_in.month
        )
        self.session.add(db_budget)
        self.session.commit()
        self.session.refresh(db_budget)
        return db_budget

    def get_budget(self, user_id: int, month: str, category: str | None) -> Budget | None:
        statement = select(Budget).where(
            Budget.user_id == user_id,
            Budget.month == month,
            Budget.category == category
        )
        return self.session.exec(statement).first()

    def get_budgets_by_month(self, user_id: int, month: str) -> list[Budget]:
        statement = select(Budget).where(
            Budget.user_id == user_id,
            Budget.month == month
        )
        return self.session.exec(statement).all()

    def update_budget(self, db_budget: Budget, budget_in: BudgetUpdate) -> Budget:
        db_budget.amount = budget_in.amount
        db_budget.updated_at = datetime.now()
        self.session.add(db_budget)
        self.session.commit()
        self.session.refresh(db_budget)
        return db_budget
