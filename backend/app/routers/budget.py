from fastapi import APIRouter, Depends, HTTPException, Query
from typing import List
from ..schemas.budget import BudgetCreate, BudgetResponse
from ..services.budget import BudgetService, get_budget_service
from ..core.security import get_current_user
from ..crud.user import UserRepo

router = APIRouter(prefix="/budgets", tags=["budgets"])

def get_user_id(current_username: str = Depends(get_current_user), repo: UserRepo = Depends(UserRepo)) -> int:
    user = repo.find_user_by_username(current_username)
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return user.id

@router.post("", response_model=BudgetResponse)
def set_budget(
    budget: BudgetCreate,
    user_id: int = Depends(get_user_id),
    service: BudgetService = Depends(get_budget_service)
):
    return service.set_budget(user_id, budget)

@router.get("", response_model=List[BudgetResponse])
def get_budgets(
    month: str = Query(..., description="Month in YYYY-MM format"),
    user_id: int = Depends(get_user_id),
    service: BudgetService = Depends(get_budget_service)
):
    return service.get_budgets(user_id, month)
