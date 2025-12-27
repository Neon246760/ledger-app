from fastapi import APIRouter, HTTPException, Depends

from ..schemas.ledger import (
    LedgerCreate,
    LedgerUpdate,
    LedgerResponse,
    LedgerListResponse
)
from ..services.ledger import LedgerService, get_ledger_service
from ..core.security import get_current_user
from ..crud.user import UserRepo

router = APIRouter(prefix="/ledgers", tags=["ledgers"])


def get_user_id(current_username: str = Depends(get_current_user), repo: UserRepo = Depends(UserRepo)) -> int:
    """从token中获取用户名，然后获取用户ID"""
    user = repo.find_user_by_username(current_username)
    if user is None:
        raise HTTPException(status_code=404, detail="User not found")
    return user.id


@router.post("", response_model=LedgerResponse, status_code=201)
def create_ledger(
    ledger: LedgerCreate,
    user_id: int = Depends(get_user_id),
    service: LedgerService = Depends(get_ledger_service)
):
    """创建新的账本"""
    return service.create_ledger(
        user_id=user_id,
        name=ledger.name,
        description=ledger.description
    )


@router.get("", response_model=LedgerListResponse)
def get_ledgers(
    skip: int = 0,
    limit: int = 100,
    user_id: int = Depends(get_user_id),
    service: LedgerService = Depends(get_ledger_service)
):
    """获取用户的账本列表"""
    ledgers = service.get_ledgers(
        user_id=user_id,
        skip=skip,
        limit=limit
    )
    total = service.repo.count_user_ledgers(user_id)
    return LedgerListResponse(
        total=total,
        items=[LedgerResponse.model_validate(l) for l in ledgers]
    )


@router.get("/{ledger_id}", response_model=LedgerResponse)
def get_ledger(
    ledger_id: int,
    user_id: int = Depends(get_user_id),
    service: LedgerService = Depends(get_ledger_service)
):
    """获取单个账本"""
    return service.get_ledger(ledger_id, user_id)


@router.put("/{ledger_id}", response_model=LedgerResponse)
def update_ledger(
    ledger_id: int,
    ledger_update: LedgerUpdate,
    user_id: int = Depends(get_user_id),
    service: LedgerService = Depends(get_ledger_service)
):
    """更新账本"""
    return service.update_ledger(
        ledger_id=ledger_id,
        user_id=user_id,
        name=ledger_update.name,
        description=ledger_update.description
    )


@router.delete("/{ledger_id}", status_code=204)
def delete_ledger(
    ledger_id: int,
    user_id: int = Depends(get_user_id),
    service: LedgerService = Depends(get_ledger_service)
):
    """删除账本"""
    service.delete_ledger(ledger_id, user_id)
    return None

