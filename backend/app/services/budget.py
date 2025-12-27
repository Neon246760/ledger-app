from fastapi import Depends
from sqlmodel import Session
from ..core.db import get_session
from ..crud.budget import BudgetRepo
from ..schemas.budget import BudgetCreate, BudgetUpdate
from ..models.budget import Budget

class BudgetService:
    def __init__(self, repo: BudgetRepo):
        self.repo = repo

    def set_budget(self, user_id: int, budget_in: BudgetCreate) -> Budget:
        # Check if budget already exists
        existing_budget = self.repo.get_budget(user_id, budget_in.month, budget_in.category)
        if existing_budget:
            return self.repo.update_budget(existing_budget, BudgetUpdate(amount=budget_in.amount))
        else:
            return self.repo.create_budget(user_id, budget_in)

    def get_budgets(self, user_id: int, month: str) -> list[Budget]:
        return self.repo.get_budgets_by_month(user_id, month)

def get_budget_service(session: Session = Depends(get_session)) -> BudgetService:
    return BudgetService(BudgetRepo(session))
